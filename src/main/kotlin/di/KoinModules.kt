package com.gity.di

import com.gity.config.AppConfig
import com.gity.config.ConfigLoader
import com.gity.config.DatabaseClient
import com.gity.config.RedisClient
import com.gity.shared.utils.HashingUtil
import com.gity.shared.utils.JwtUtil
import com.gity.shared.utils.EmailUtil

// Import dari masing-masing fitur (akan diimplementasi nanti)
// import com.gity.features.auth.repositories.UserRepository
// import com.gity.features.auth.services.AuthService
// import com.gity.features.listing.repositories.ListingRepository
// import com.gity.features.listing.services.ListingService
// import com.gity.features.category.repositories.CategoryRepository
// import com.gity.features.category.services.CategoryService
// import com.gity.features.admin.services.AdminService

import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun koinModules(app: Application) {
    app.install(Koin) {
        slf4jLogger()
        modules(
            configModule,
            databaseModule,
            redisModule,
            sharedUtilModule
            // Uncomment setelah implementasi
            // userRepositoryModule,
            // listingRepositoryModule,
            // categoryRepositoryModule,
            // authServiceModule,
            // listingServiceModule,
            // categoryServiceModule,
            // adminServiceModule
        )
    }
}

val configModule = module {
    single { ConfigLoader().load() }
}

val databaseModule = module {
    single { DatabaseClient(get()) }
}

val redisModule = module {
    single { RedisClient(get()) }
}

val sharedUtilModule = module {
    single { HashingUtil() }
    single { JwtUtil(get()) }
    single { EmailUtil(get()) }
}

// Uncomment dan sesuaikan setelah implementasi repository & service
/*
val userRepositoryModule = module {
    single { UserRepository(get()) }
}

val listingRepositoryModule = module {
    single { ListingRepository(get()) }
}

val categoryRepositoryModule = module {
    single { CategoryRepository(get()) }
}

val authServiceModule = module {
    single { AuthService(get(), get(), get(), get()) }
}

val listingServiceModule = module {
    single { ListingService(get(), get(), get()) }
}

val categoryServiceModule = module {
    single { CategoryService(get()) }
}

val adminServiceModule = module {
    single { AdminService(get(), get(), get()) }
}
*/