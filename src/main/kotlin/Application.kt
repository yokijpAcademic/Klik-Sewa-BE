package com.gity

import com.gity.di.koinModules
import com.gity.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // 1. Configure Koin (Dependency Injection) - HARUS PERTAMA
    koinModules(this)

    // 2. Configure Plugins
    configureSerialization()
    configureMonitoring()
    configureCORS()
    configureAuthentication()
    configureStatusPages()
    configureHTTP()

    // 3. Install Modules (Routes) - Akan diimplementasi nanti
    // Uncomment setelah implementasi
    // configureAuthModule()
    // configureListingModule()
    // configureCategoryModule()
    // configureAdminModule()
}