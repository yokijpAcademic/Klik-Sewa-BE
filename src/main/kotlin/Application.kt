package com.gity


import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // 1. Configure Koin (Dependency Injection)
    installKoinModules()

    // 2. Configure Database Connection (akan dilakukan dalam KoinModules)
    // 3. Configure Redis Connection (akan dilakukan dalam KoinModules)

    // 4. Configure Plugins
    configureSerialization()
    configureMonitoring()
    configureCORS()
    configureAuthentication()
    configureStatusPages()
    configureHTTP()

    // 5. Install Modules (Routes) - Sekarang dipanggil berdasarkan fitur
    configureAuthModule()      // features.auth.modules
    configureListingModule()   // features.listing.modules
    configureCategoryModule()  // features.category.modules
    configureAdminModule()     // features.admin.modules
}

private fun Application.installKoinModules() {
    // Instal Koin dan muat modul-modulnya
    koinModules(this)
}
