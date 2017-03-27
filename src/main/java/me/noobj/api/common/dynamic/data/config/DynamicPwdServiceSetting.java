package me.noobj.api.common.dynamic.data.config;

public class DynamicPwdServiceSetting {

	private int expiringSeconds = 60;
	
	private int minLockSeconds = 2;
	
	private int maxLockSeconds = 60;
	
	private int pwdLength = 6;

	private String appId = "";
	

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public int getPwdLength() {
		return pwdLength;
	}

	public void setPwdLength(int pwdLength) {
		this.pwdLength = pwdLength;
	}

	public int getExpiringSeconds() {
		return expiringSeconds;
	}

	public void setExpiringSeconds(int expiringSeconds) {
		this.expiringSeconds = expiringSeconds;
	}

	public int getMinLockSeconds() {
		return minLockSeconds;
	}

	public void setMinLockSeconds(int minLockSeconds) {
		this.minLockSeconds = minLockSeconds;
	}

	public int getMaxLockSeconds() {
		return maxLockSeconds;
	}

	public void setMaxLockSeconds(int maxLockSeconds) {
		this.maxLockSeconds = maxLockSeconds;
	}
	
}
