package me.noobj.api.common.dynamic.data.result;
public class DynamicPwd {
	private int _ret_code = 0;
	
	private String _token;
	
	private String _pwd;
	
	public int getRet_code() {
		return _ret_code;
	}

	public void setRet_code(int ret_code) {
		_ret_code = ret_code;
	}

	public String getToken() {
		return _token;
	}

	public void setToken(String token) {
		_token = token;
	}

	public String getPwd() {
		return _pwd;
	}

	public void setPwd(String pwd) {
		_pwd = pwd;
	}
	public String toString(){
		return "_ret_code=" + _ret_code + "\n_token=" + _token + "\n_pwd=" + _pwd;
	}
}