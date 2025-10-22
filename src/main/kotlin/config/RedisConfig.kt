package com.gity.config

import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import org.slf4j.LoggerFactory

class RedisClient(
    private val appConfig: AppConfig
) {
    private val logger = LoggerFactory.getLogger(RedisClient::class.java)

    private val poolConfig = JedisPoolConfig().apply {
        maxTotal = 128
        maxIdle = 16
        minIdle = 8
        testOnBorrow = true
        testOnReturn = true
        testWhileIdle = true
        minEvictableIdleTimeMillis = 60000
        timeBetweenEvictionRunsMillis = 30000
        numTestsPerEvictionRun = 3
        blockWhenExhausted = true
    }

    val jedisPool: JedisPool = createJedisPool()

    private fun createJedisPool(): JedisPool {
        return try {
            val pool = if (appConfig.redis.password != null && appConfig.redis.password.isNotBlank()) {
                JedisPool(
                    poolConfig,
                    appConfig.redis.host,
                    appConfig.redis.port,
                    2000,
                    appConfig.redis.password
                )
            } else {
                JedisPool(
                    poolConfig,
                    appConfig.redis.host,
                    appConfig.redis.port
                )
            }

            // Test connection
            pool.resource.use { jedis ->
                jedis.ping()
                logger.info("Redis connection successful: ${appConfig.redis.host}:${appConfig.redis.port}")
            }

            pool
        } catch (e: Exception) {
            logger.error("Failed to connect to Redis: ${e.message}", e)
            throw e
        }
    }

    fun closePool() {
        try {
            if (!jedisPool.isClosed) {
                jedisPool.close()
                logger.info("Redis connection pool closed")
            }
        } catch (e: Exception) {
            logger.error("Error closing Redis pool: ${e.message}", e)
        }
    }

    // Helper functions untuk operasi Redis
    fun set(key: String, value: String, expirationSeconds: Int? = null) {
        jedisPool.resource.use { jedis ->
            if (expirationSeconds != null) {
                jedis.setex(key, expirationSeconds.toLong(), value)
            } else {
                jedis.set(key, value)
            }
        }
    }

    fun get(key: String): String? {
        return jedisPool.resource.use { jedis ->
            jedis.get(key)
        }
    }

    fun delete(key: String): Long {
        return jedisPool.resource.use { jedis ->
            jedis.del(key)
        }
    }

    fun exists(key: String): Boolean {
        return jedisPool.resource.use { jedis ->
            jedis.exists(key)
        }
    }

    fun expire(key: String, seconds: Int): Boolean {
        return jedisPool.resource.use { jedis ->
            jedis.expire(key, seconds.toLong()) == 1L
        }
    }
}