package com.example.redis

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * Thin wrapper around Spring's [StringRedisTemplate] to expose simple convenience methods
 * used by Incentivize components (counters, sets, JSON strings, etc.).
 */
@Component
class RedisClient(private val template: StringRedisTemplate) {

    fun incr(key: String, ttl: Duration? = null): Long {
        val value = template.opsForValue().increment(key)!!
        ttl?.let { template.expire(key, it) }
        return value
    }

    fun get(key: String): String? = template.opsForValue().get(key)

    fun set(key: String, value: String, ttl: Duration? = null) {
        template.opsForValue().set(key, value)
        ttl?.let { template.expire(key, it) }
    }

    fun sadd(key: String, member: String) {
        template.opsForSet().add(key, member)
    }

    fun expire(key: String, ttl: Duration) {
        template.expire(key, ttl)
    }

    fun smembers(key: String): Set<String> = template.opsForSet().members(key) ?: emptySet()
}
