package me.noobj.api.common.data;

public class DynamicPwdRetCodeSetting {
	public final static int Success = 0;
	public final static int ErrorDynamicPwdLocked = 100;
	public final static int ErrorDynamicPwdInvalid = 101;
	public final static int ErrorDynamicPwdVerifyTooFrequently = 102;
	public final static int ErrorDynamicPwdVerifyLocked = 103;
	public final static int SysError = -1;
}
