package me.noobj.api.common.dynamic.service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Random;

import org.apache.log4j.Logger;

import me.noobj.api.common.dynamic.data.config.DynamicPwdServiceSetting;
import me.noobj.api.common.dynamic.data.result.DynamicPwd;
import me.noobj.api.common.dynamic.data.result.VerifyDynamicPwdResult;
import me.noobj.api.common.dynamic.util.HexUtil;

public abstract class AbstractDynamicPwdService{
	private final static Logger logger = Logger.getLogger(AbstractDynamicPwdService.class);

	protected static Random _rand ;
	
	static {
		try {
			_rand = SecureRandom.getInstance("SHA1PRNG");
		} catch(Throwable e) {
			logger.error(null, e);
		}
	}
	
	protected DynamicPwdServiceSetting _setting = new DynamicPwdServiceSetting();
	
 
	public DynamicPwdServiceSetting getSetting() {
		return _setting;
	}

	/**
	 * @param id
	 * @param length
	 * @param expiringSeconds
	 * @return
	 */
	public abstract DynamicPwd createDynamicPwd(String id);
	
	/**
	 * 验证验证码
	 * @param token
	 * @param id
	 * @param dynamicPwd
	 * @return
	 */
	public abstract VerifyDynamicPwdResult verifyDynamicPwd(String token, String id, String dynamicPwd) ;
	
	
	public String generateTokenForDynamicPwd() {
		byte[] tokenBytes = new byte[16]; 
		_rand.nextBytes(tokenBytes);
		
		return HexUtil.toHexString(tokenBytes);
	}
	
	/**
	 * limit 8位
	 * @return
	 */
	public String generateDynamicPwd() {
		int l = _setting.getPwdLength();
		int b = BigDecimal.TEN.pow(l-1).intValue();
		int el = BigDecimal.TEN.pow(l).intValue() - b;
		return Integer.toString(b + _rand.nextInt(el));
	}
}
