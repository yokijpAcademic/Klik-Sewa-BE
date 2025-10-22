package com.gity.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCORS() {
    install(CORS) {
        // Allow requests from frontend
        anyHost() // Untuk development, ganti dengan allowHost("localhost:3000") di production

        // Allowed methods
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Options)

        // Allowed headers
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)

        // Expose headers
        exposeHeader(HttpHeaders.Authorization)

        // Allow credentials (cookies, authorization headers)
        allowCredentials = true

        // Max age for preflight requests
        maxAgeInSeconds = 3600
    }
}