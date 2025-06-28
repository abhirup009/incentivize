package com.example.redis

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.HashOperations
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * Thin wrapper around Spring's [StringRedisTemplate] to expose simple convenience methods
 * used by Incentivize components (counters, sets, JSON strings, etc.).
 */
@Component
class RedisClient(private val template: StringRedisTemplate) {

    private val hashOps: HashOperations<String, String, String> = template.opsForHash()

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

    fun scard(key: String): Long = template.opsForSet().size(key) ?: 0L

    fun keys(pattern: String): Set<String> = template.keys(pattern) ?: emptySet()

    // ---- Hash helpers ----
    fun hincrBy(key: String, field: String, delta: Long): Long {
        val value = hashOps.increment(key, field, delta) ?: 0L
        return value
    }

    fun hget(key: String, field: String): String? = hashOps.get(key, field)

    // ---- Set membership ----
    fun sismember(key: String, member: String): Boolean = template.opsForSet().isMember(key, member) ?: false
}
