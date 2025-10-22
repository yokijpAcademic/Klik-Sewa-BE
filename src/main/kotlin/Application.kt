package com.gity

import com.gity.config.AppConfig
import com.gity.di.koinModules
import com.gity.features.auth.modules.configureAuthModule
import com.gity.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.routing
import org.koin.ktor.plugin.koin

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

    routing {
        get("/health") {
            val appConfig = org.koin.java.KoinJavaComponent.get<AppConfig>(AppConfig::class.java)
            call.respondText("OK - Environment: ${appConfig.app.environment}")
        }
    }

    // 3. Install Modules (Routes)
    configureAuthModule()

    // Uncomment setelah implementasi
    // configureListingModule()
    // configureCategoryModule()
    // configureAdminModule()
}