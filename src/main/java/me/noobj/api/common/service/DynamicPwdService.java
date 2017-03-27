package me.noobj.api.common.service;

import org.apache.log4j.Logger;

import me.noobj.api.common.control.AccessControlHashMapImp;
import me.noobj.api.common.control.AccessControlRedisImp;
import me.noobj.api.common.dao.DynamicPwdHashMapDaoImp;
import me.noobj.api.common.dao.DynamicPwdRedisDaoImp;
import me.noobj.api.common.dao.IDynamicPwdDao;
import me.noobj.api.common.data.DynamicPwdRetCodeSetting;
import me.noobj.api.common.data.config.DynamicPwdServiceSetting;
import me.noobj.api.common.data.result.DynamicPwd;
import me.noobj.api.common.data.result.VerifyDynamicPwdResult;
import redis.clients.jedis.JedisPool;

public class DynamicPwdService extends AbstractDynamicPwdService {
	private final Logger logger = Logger.getLogger(DynamicPwdService.class);
	
	private IDynamicPwdDao _dynamicDao;
	
	private IDynamicPwdDao.IAccessLockControl _accessLockController;
	
	private IDynamicPwdDao.IAccessLockPolicy _accessLockPolicy = new IDynamicPwdDao.IAccessLockPolicy() {
		public int getLockTimeSecondsForInvalidAccess(int invalidAccessCount) {
			return 0;
		}
	};
	
	public DynamicPwdService (DynamicPwdServiceSetting setting, IDynamicPwdDao.IAccessLockPolicy accessLockPolicy, JedisPool jedisPool){
		if (setting != null){
			_setting = setting;
		}
		if (accessLockPolicy != null){
			_accessLockPolicy = accessLockPolicy;
		}
		if (jedisPool == null){
			_dynamicDao = new DynamicPwdHashMapDaoImp(_setting.getAppId());
			_accessLockController = new AccessControlHashMapImp(_setting.getAppId(), _accessLockPolicy, _setting.getMaxLockSeconds());

		} else {
			_dynamicDao = new DynamicPwdRedisDaoImp(jedisPool, _setting.getAppId());
			_accessLockController = new AccessControlRedisImp(jedisPool, _setting.getAppId(), _accessLockPolicy, _setting.getMaxLockSeconds());

		}
	}
	/**
	 * @param id
	 * @param length
	 * @param expiringSeconds
	 * @return
	 */
	@Override
	public DynamicPwd createDynamicPwd(String id) {
		try {
			return _dynamicDao.createDynamicPwd(this, id);
		} catch (Exception e) {
			DynamicPwd returnValue = new DynamicPwd();
			returnValue.setRet_code(DynamicPwdRetCodeSetting.SysError);
			return returnValue;
		}
	}
	
	/**
	 * 验证验证码
	 * @param token
	 * @param id
	 * @param dynamicPwd
	 * @return
	 */
	@Override
	public VerifyDynamicPwdResult verifyDynamicPwd(String token, String id, String dynamicPwd){
		VerifyDynamicPwdResult result = new VerifyDynamicPwdResult();
		
		try {
			boolean isLockSuccess = _dynamicDao.tryLockVerifyDynamicPwd(this, id);
			if(!isLockSuccess) {
				result.setRet_code(DynamicPwdRetCodeSetting.ErrorDynamicPwdVerifyTooFrequently);
				return result;
			}
			
			int accessLockKeepSeconds = _accessLockController.getLockKeepTimeSeconds(id);
			if(accessLockKeepSeconds > 0) {
				result.setRet_code(DynamicPwdRetCodeSetting.ErrorDynamicPwdVerifyLocked);
				result.setLock_time_seconds(accessLockKeepSeconds);
				
				logger.debug("dynamic pwd locked(seconds):" + accessLockKeepSeconds);
				return result;
			}
			
			boolean isValid = _dynamicDao.verifyDynamicPwd(this, token, id, dynamicPwd);
			if(isValid) {
				result.setRet_code(DynamicPwdRetCodeSetting.Success);
				
				_accessLockController.reportValidAccess(id);
			} else {
				result.setRet_code(DynamicPwdRetCodeSetting.ErrorDynamicPwdInvalid);
				
				_accessLockController.reportInvalidAccess(id);
			}
			
		} catch (Exception e) {
			result.setRet_code(DynamicPwdRetCodeSetting.SysError);
		}
		
		return result;
	}
	
	
	
}
