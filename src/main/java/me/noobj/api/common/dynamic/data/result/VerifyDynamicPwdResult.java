package me.noobj.api.common.dynamic.data.result;

public class VerifyDynamicPwdResult {
	private int _ret_code = 0;
	
	private int _lock_time_seconds = 0;

	public int getRet_code() {
		return _ret_code;
	}

	public void setRet_code(int ret_code) {
		_ret_code = ret_code;
	}

	public int getLock_time_seconds() {
		return _lock_time_seconds;
	}

	public void setLock_time_seconds(int lock_time_seconds) {
		_lock_time_seconds = lock_time_seconds;
	}
	
}
