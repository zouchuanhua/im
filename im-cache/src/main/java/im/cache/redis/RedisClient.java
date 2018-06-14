package im.cache.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.JedisShardInfo;

/**
 * redis client
 * Date: 2018-06-14
 *
 * @author zouchuanhua
 */
public final class RedisClient {

    private static final RedisClient INSTANCE = new RedisClient();

    private RedisClient(){}

    public static RedisClient getINSTANCE() {
        return INSTANCE;
    }

    private static JedisPool jedisPool;

    static {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setTestOnBorrow(true);
        poolConfig.setMaxTotal(20);
        poolConfig.setMaxWaitMillis(2000);
        jedisPool = new JedisPool(poolConfig, "127.0.0.1", 6379);
    }

    public void setex(String key, int seconds, String value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.setex(key, seconds, value);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public String get(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.get(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

}
