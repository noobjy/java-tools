package me.noobj.api.common.control;

import java.util.concurrent.ConcurrentHashMap;

import me.noobj.api.common.dao.IDynamicPwdDao;

public class AccessControlHashMapImp implements IDynamicPwdDao.IAccessLockControl{
	public final static String REDIS_KEY_SUFFIX_LOCK = ".accesslock.";
	class LockElement {
		private volatile int _invalidCount = 0;
		private long _lockUntil = 0l;
		
		public int incInvalidCount(){
			return ++_invalidCount;
		}
		public int getInvalidCount() {
			return _invalidCount;
		}
		public long getLockUntil() {
			return _lockUntil;
		}
		
		public void setLockUntil(long lockUntil) {
			_lockUntil = lockUntil;
		}
	}

	private final String _hashKeyPrefix;
	private final IDynamicPwdDao.IAccessLockPolicy _accessLockPolicy;
	private final int _maxLockSeconds;
	private final ConcurrentHashMap<String, LockElement> _hashPool;

	public AccessControlHashMapImp(
			String hashKeyPrefix,
			IDynamicPwdDao.IAccessLockPolicy accessLockPolicy,
			int maxLockSeconds
			) {
		_hashKeyPrefix = hashKeyPrefix;
		_accessLockPolicy = accessLockPolicy;
		_maxLockSeconds = maxLockSeconds;
		_hashPool = new ConcurrentHashMap<String, LockElement>();
	}
	
	@Override
	public int getLockKeepTimeSeconds(String id) {
		
			String key = keyOfLockInvalidCount(id);

			LockElement element = _hashPool.get(key);
			if(element == null) {
				return 0;
			}
			
			long lockKeepSeconds = (long) Math.ceil(
					(element.getLockUntil() - System.currentTimeMillis()) / 1000
					);
			if(lockKeepSeconds < 0) {
				return 0;
			} else {
				return (int) lockKeepSeconds;
			}
		
	}

	@Override
	public int reportInvalidAccess(String id) {
		return increInvalidCount(id);
	}

	@Override
	public void reportValidAccess(String id) {
			String key = keyOfLockInvalidCount(id);
			_hashPool.remove(key);
	}

	protected int increInvalidCount(String id) {
			String key = keyOfLockInvalidCount(id);
			
			LockElement lockElement = _hashPool.get(key);
			if(lockElement == null) {
				lockElement = new LockElement();
				_hashPool.put(key,lockElement);
			}
			int invalidAccessCount = lockElement.incInvalidCount();
			
			int lockSeconds = _accessLockPolicy.getLockTimeSecondsForInvalidAccess(
					invalidAccessCount);
			if(lockSeconds <= 0) {
				lockSeconds = 0;
			} 
			if (lockSeconds > _maxLockSeconds){
				lockSeconds = _maxLockSeconds;
			}
			
			long lockUntil = System.currentTimeMillis() + lockSeconds * 1000L; 
			lockElement.setLockUntil(lockUntil);
			
			return invalidAccessCount;
	} 
	
	protected String keyOfLockInvalidCount(String id) {
		return _hashKeyPrefix.concat(REDIS_KEY_SUFFIX_LOCK).concat(id);
	}
}
