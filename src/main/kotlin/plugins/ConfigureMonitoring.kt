package com.gity.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import org.slf4j.event.Level

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO

        // Filter untuk tidak log request ke path tertentu (opsional)
        filter { call ->
            call.request.path().startsWith("/api")
        }

        // Format log
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val uri = call.request.uri
            val userAgent = call.request.headers["User-Agent"]

            "Status: $status, HTTP method: $httpMethod, URI: $uri, User agent: $userAgent"
        }
    }
}