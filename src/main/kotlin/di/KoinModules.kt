package com.gity.di

import com.gity.config.DatabaseConfig
import com.gity.config.RedisConfig
import com.gity.config.EmailConfig
import com.gity.config.ConfigLoader
import com.gity.shared.utils.HashingUtil
import com.gity.shared.utils.JwtUtil
import com.gity.shared.utils.EmailUtil

// Import dari masing-masing fitur
import com.gity.features.auth.repositories.UserRepository
import com.gity.features.auth.services.AuthService
import com.gity.features.listing.repositories.ListingRepository
import com.gity.features.listing.services.ListingService
import com.gity.features.category.repositories.CategoryRepository
import com.gity.features.category.services.CategoryService
import com.gity.features.admin.services.AdminService // Bisa reuse repo lain

import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger


fun koinModules(app: Application) {
    app.install(Koin) {
        slf4jLogger() // Gunakan logger SLF4J
        modules(
            configModule, // Load AppConfig dulu
            databaseModule,
            redisModule,
            sharedUtilModule,
            userRepositoryModule,
            listingRepositoryModule,
            categoryRepositoryModule,
            authServiceModule,
            listingServiceModule,
            categoryServiceModule,
            adminServiceModule
        )
    }
}

val configModule = module {
    single { ConfigLoader().load() } // Load konfigurasi dari .env
}

val databaseModule = module {
    single { DatabaseConfig(get()) } // Inject AppConfig
}

val redisModule = module {
    single { RedisConfig(get()) } // Inject AppConfig
}

val sharedUtilModule = module {
    single { HashingUtil() }
    single { JwtUtil(get()) } // Inject AppConfig (untuk secret dan exp)
    single { EmailUtil(get()) } // Inject AppConfig (untuk apiKey, sender details)
}

val userRepositoryModule = module {
    single { UserRepository(get()) } // get() untuk mendapatkan DatabaseConfig
}

val listingRepositoryModule = module {
    single { ListingRepository(get()) }
}

val categoryRepositoryModule = module {
    single { CategoryRepository(get()) }
}

val authServiceModule = module {
    single { AuthService(get(), get(), get(), get()) } // UserRepo, HashUtil, JWTUtil, EmailUtil
}

val listingServiceModule = module {
    single { ListingService(get(), get(), get()) } // ListingRepo, CategoryRepo, UserRepo
}

val categoryServiceModule = module {
    single { CategoryService(get()) } // CategoryRepo
}

val adminServiceModule = module {
    single { AdminService(get(), get(), get()) } // ListingRepo, UserRepo, EmailUtil (contoh)
}