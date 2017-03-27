package me.noobj.api.common.junittest;

import org.junit.Test;

import me.noobj.api.common.dynamic.data.result.DynamicPwd;
import me.noobj.api.common.dynamic.service.DynamicPwdService;

public class TestService {
	
	private DynamicPwdService service = new DynamicPwdService(null, null, null);
	@Test
	public void testCreatePwd(){
		String ids[] = new String[]{"j","j","j","j","j"};
		for (int i = 0; i < ids.length; i++) {
			DynamicPwd pwd = service.createDynamicPwd(ids[i]);
			System.out.println(pwd);
			System.out.println( "result= " +service.verifyDynamicPwd(pwd.getToken(), ids[i], "1").getRet_code());
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
		}
	}
}
