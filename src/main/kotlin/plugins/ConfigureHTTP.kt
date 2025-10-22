package com.gity.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import kotlin.time.Duration.Companion.seconds

fun Application.configureHTTP() {
    install(RateLimit) {
        // Rate limit global untuk semua endpoint
        global {
            rateLimiter(limit = 100, refillPeriod = 60.seconds)
        }

        // Rate limit khusus untuk endpoint auth (login/register)
        register(RateLimitName("auth")) {
            rateLimiter(limit = 10, refillPeriod = 60.seconds)
        }

        // Rate limit khusus untuk endpoint upload/create
        register(RateLimitName("create")) {
            rateLimiter(limit = 20, refillPeriod = 60.seconds)
        }
    }
}