package com.gity

import com.cloudinary.config.CloudinaryConfig
import com.gity.config.DatabaseConfig
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
//    Setup Koin Dependency Injection
    install(Koin) {
        slf4jLogger()
        modules()
    }

//    Initialize configurations
    DatabaseConfig.init()
//    RedisConfig.init()
//    CloudinaryConfig.init()
//    EmailConfig.init()

//    Install Ktor Plugins
//    configureSerialization()
//    configureSecurity()
//    configureHTTP()
//    configureMonitoring()
//    configureStatusPages()
//    configureRateLimit()
    configureRouting()
}
