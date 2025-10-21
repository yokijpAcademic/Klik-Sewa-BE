package com.gity.config

import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import org.slf4j.LoggerFactory

class RedisConfig(
    private val appConfig: AppConfig
) {
    private val logger = LoggerFactory.getLogger(RedisConfig::class.java)

    private val poolConfig = JedisPoolConfig().apply {
        maxTotal = 128 // Sesuaikan kebutuhan
        maxIdle = 16
        minIdle = 8
    }

    val jedisPool: JedisPool = if (appConfig.redis.password != null) {
        JedisPool(poolConfig, appConfig.redis.host, appConfig.redis.port, 2000, appConfig.redis.password)
    } else {
        JedisPool(poolConfig, appConfig.redis.host, appConfig.redis.port)
    }

    init {
        logger.info("RedisConfig initialized, connecting to: ${appConfig.redis.host}:${appConfig.redis.port}")
    }

    fun closePool() {
        jedisPool.close()
    }
}