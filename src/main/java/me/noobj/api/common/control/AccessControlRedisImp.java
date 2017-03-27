package me.noobj.api.common.control;

import me.noobj.api.common.dao.IDynamicPwdDao;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class AccessControlRedisImp implements IDynamicPwdDao.IAccessLockControl{
	public final static String REDIS_KEY_SUFFIX_LOCK = ".accesslock.";
	private final static class RedisFields {
		public final static String InvalidCount = "invalid_cnt";
		public final static String LockUntil = "lockuntil";
	}

	private final JedisPool _jedisPool;
	private final String _redisKeyPrefix;
	private final IDynamicPwdDao.IAccessLockPolicy _accessLockPolicy;
	private final int _maxLockSeconds;

	public AccessControlRedisImp(
			JedisPool jedisPool,
			String redisKeyPrefix,
			IDynamicPwdDao.IAccessLockPolicy accessLockPolicy,
			int maxLockSeconds
			) {
		_jedisPool = jedisPool;
		_redisKeyPrefix = redisKeyPrefix;
		_accessLockPolicy = accessLockPolicy;
		_maxLockSeconds = maxLockSeconds;
	}
	@Override
	public int getLockKeepTimeSeconds(String id) {
		Jedis jedis = null;
		
		try {
			jedis = _jedisPool.getResource();
			String key = keyOfLockInvalidCount(id);

			String lockUntil = jedis.hget(key, RedisFields.LockUntil);
			if(lockUntil == null || lockUntil.length() == 0) {
				return 0;
			}
			
			long lockKeepSeconds = (long) Math.ceil(
					(Long.parseLong(lockUntil) - System.currentTimeMillis()) / 1000
					);
			if(lockKeepSeconds < 0) {
				return 0;
			} else {
				return (int) lockKeepSeconds;
			}
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}

	@Override
	public int reportInvalidAccess(String id) {
		return increInvalidCount(id);
	}

	@Override
	public void reportValidAccess(String id) {
		Jedis jedis = null;
		
		try {
			jedis = _jedisPool.getResource();
			String key = keyOfLockInvalidCount(id);

			jedis.del(key);
		} finally {
			if(jedis != null)
				jedis.close();
		}
		
	}

	protected int increInvalidCount(String id) {
		Jedis jedis = null;
		
		try {
			jedis = _jedisPool.getResource();
			String key = keyOfLockInvalidCount(id);
			
			int invalidAccessCount = jedis.hincrBy(
					key, RedisFields.InvalidCount, 1).intValue();
			if(invalidAccessCount == 1) {
				jedis.expire(key, _maxLockSeconds);
			}
			
			int lockSeconds = _accessLockPolicy.getLockTimeSecondsForInvalidAccess(
					invalidAccessCount);
			if(lockSeconds <= 0) {
				lockSeconds = 0;
			} else {
				jedis.expire(key, lockSeconds);
			}
			
			long lockUntil = System.currentTimeMillis() + lockSeconds * 1000L; 
			jedis.hset(key, RedisFields.LockUntil, Long.toString(lockUntil));
			
			return invalidAccessCount;
		} finally {
			if (jedis != null)
				jedis.close();
		}
	} 
	
	protected String keyOfLockInvalidCount(String id) {
		return _redisKeyPrefix.concat(REDIS_KEY_SUFFIX_LOCK).concat(id);
	}
}
