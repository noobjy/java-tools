package me.noobj.api.common.dynamic.dao;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import me.noobj.api.common.dynamic.data.DynamicPwdRetCodeSetting;
import me.noobj.api.common.dynamic.data.result.DynamicPwd;
import me.noobj.api.common.dynamic.service.AbstractDynamicPwdService;

public class DynamicPwdHashMapDaoImp implements IDynamicPwdDao {
	private final static String Delim = ",";
	
	private final DynamicPwdKey _hashKey;
	private final ConcurrentHashMap<String, DynamicElement> _hashPool;

	public DynamicPwdHashMapDaoImp(String appId) {
		_hashKey = new DynamicPwdKey(appId);
		_hashPool = new ConcurrentHashMap<String, DynamicElement>();
	}

	@Override
	public DynamicPwd createDynamicPwd(AbstractDynamicPwdService service, String id) throws Exception {
		DynamicPwd data = new DynamicPwd();
		DynamicElement element;
		long curTime = System.currentTimeMillis();
		if ((element = _hashPool.get(_hashKey.keyOfPwd(id))) != null && element.getExpireTime() >= curTime) {
			String[] pwds = element.getKey().split(",");
			data.setRet_code(DynamicPwdRetCodeSetting.ErrorDynamicPwdLocked);
			data.setToken(pwds[0]);
			data.setPwd(pwds[1]);
			return data;
		}
		String token = service.generateTokenForDynamicPwd();
		String pwd = service.generateDynamicPwd();
		long expireTime = 1000l * service.getSetting().getExpiringSeconds() + curTime;
		data.setRet_code(DynamicPwdRetCodeSetting.Success);
		data.setToken(token);
		data.setPwd(pwd);
		
		_hashPool.put(_hashKey.keyOfPwd(id), new DynamicElement(token.concat(Delim).concat(pwd), expireTime));
		
		return data;

	}

	@Override
	public boolean verifyDynamicPwd(AbstractDynamicPwdService service, String token, String id, String pwd)
			throws Exception {
		String tokenPwd1 = token.concat(Delim).concat(pwd);

		String key = _hashKey.keyOfPwd(id);
		DynamicElement element = _hashPool.get(key);

		boolean verifyOK = false;
		if (element != null && tokenPwd1.equals(element.getKey())) {
			verifyOK = true;
			_hashPool.remove(key);
			_hashPool.remove(_hashKey.keyOfPwdLock(id));
		}
		return verifyOK;

	}

	@Override
	public boolean tryLockVerifyDynamicPwd(AbstractDynamicPwdService service, String id) throws Exception {
		String key = _hashKey.keyOfPwdLock(id);
		boolean hasLock = false;
		DynamicElement element;
		long curTime = System.currentTimeMillis();
		if ((element = _hashPool.get(key)) != null && element.getExpireTime() > curTime) {
			hasLock = true;
		} else{
			long expireTime = curTime + 1000l * service.getSetting().getMinLockSeconds();
			_hashPool.put(key, new DynamicElement("", expireTime));
		}

		return !hasLock;
	}

	class DynamicPwdKey {
		private final String _appId;

		public DynamicPwdKey(String appId) {
			_appId = appId;
		}

		public final String keyOfPwd(String id) {
			return _appId.concat(".dynapwd.").concat(id);
		}

		public final String keyOfPwdLock(String id) {
			return _appId.concat(".dynapwdlock.").concat(id);
		}
	}

	class DynamicElement implements Delayed {
		private volatile long _expireTime = 0l;
		private volatile String _key = "";

		public DynamicElement(String key, long expireTime) {
			_key = key;
			_expireTime = expireTime;
		}
		
		public void updateElement(String key, long expireTime) {
			_key = key;
			_expireTime = expireTime;
		}
		
		public long getExpireTime() {
			return _expireTime;
		}

		public String getKey() {
			return _key;
		}

		@Override
		public int compareTo(Delayed o) {
			return (int) (this.getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
		}

		@Override
		public long getDelay(TimeUnit unit) {
			return unit.convert(this._expireTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		}

	}

}
