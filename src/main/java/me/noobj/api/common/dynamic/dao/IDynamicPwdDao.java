package me.noobj.api.common.dynamic.dao;

import me.noobj.api.common.dynamic.data.result.DynamicPwd;
import me.noobj.api.common.dynamic.service.AbstractDynamicPwdService;

public interface IDynamicPwdDao {
	public DynamicPwd createDynamicPwd(AbstractDynamicPwdService service, String id) throws Exception;
	
	public boolean verifyDynamicPwd(AbstractDynamicPwdService service, String token, String id, String pwd) throws Exception;
	
	public boolean tryLockVerifyDynamicPwd(AbstractDynamicPwdService service, String id) throws Exception;

	public static interface IAccessLockControl {

		public int getLockKeepTimeSeconds(String id) ;

		public int reportInvalidAccess(String id);

		public void reportValidAccess(String id);

	}
	
	public static interface IAccessLockPolicy {
		/**
		 * 
		 * @param invalidAccessCount (1 ~ )
		 * @return
		 */
		public int getLockTimeSecondsForInvalidAccess(int invalidAccessCount);
		
	}
}