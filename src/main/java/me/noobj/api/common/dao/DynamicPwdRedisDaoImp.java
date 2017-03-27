package me.noobj.api.common.dao;

import me.noobj.api.common.data.DynamicPwdRetCodeSetting;
import me.noobj.api.common.data.config.DynamicPwdServiceSetting;
import me.noobj.api.common.data.result.DynamicPwd;
import me.noobj.api.common.service.AbstractDynamicPwdService;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class DynamicPwdRedisDaoImp implements IDynamicPwdDao {
	private final DynamicPwdKey _jedisKey ;
	private final JedisPool _jedisPool ;
	private final static String Delim = ",";
	
	public DynamicPwdRedisDaoImp (JedisPool jedisPool, String appId){
		_jedisPool = jedisPool;
		_jedisKey = new DynamicPwdKey(appId);
	}
	
	@Override
	public DynamicPwd createDynamicPwd(AbstractDynamicPwdService service, String id) throws Exception {
		Jedis jedis = null;
		DynamicPwd data = new DynamicPwd();
		try {
			jedis = _jedisPool.getResource();
			String value;
			if((value = jedis.get(_jedisKey.keyOfPwd(id))) != null){
				String[] pwds = value.split(",");
				data.setRet_code(DynamicPwdRetCodeSetting.ErrorDynamicPwdLocked);
				data.setToken(pwds[0]);
				data.setPwd(pwds[1]);
				return data;
			}
			String token = service.generateTokenForDynamicPwd();
			String pwd = service.generateDynamicPwd();
			jedis.setex(
					_jedisKey.keyOfPwd(id), ((DynamicPwdServiceSetting)service.getSetting()).getExpiringSeconds(), 
					token.concat(Delim).concat(pwd));
			
			data.setRet_code(DynamicPwdRetCodeSetting.Success);
			data.setToken(token);
			data.setPwd(pwd);
			return data;
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}

	@Override
	public boolean verifyDynamicPwd(AbstractDynamicPwdService service, String token, String id, String pwd)throws Exception {
		Jedis jedis = null;
		try {
			jedis = _jedisPool.getResource();
			String tokenPwd1 = token.concat(Delim).concat(pwd);

			String key = _jedisKey.keyOfPwd(id);
			String tokenPwd2 = jedis.get(key);
			
			boolean verifyOK = tokenPwd1.equals(tokenPwd2);
			if(verifyOK) {
				jedis.del(key);
			}
			
			return verifyOK;
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}

	@Override
	public boolean tryLockVerifyDynamicPwd(AbstractDynamicPwdService service, String id) throws Exception{
		Jedis jedis = null;
		try {
			jedis = _jedisPool.getResource();
			String key = _jedisKey.keyOfPwdLock(id);
			long n = jedis.incr(key);
			jedis.expire(key, service.getSetting().getMinLockSeconds());
			
			if(n == 1) {
				return true;
			} else {
				return false;
			}
		} finally {
			if (jedis != null)
				jedis.close();
		}
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


}
