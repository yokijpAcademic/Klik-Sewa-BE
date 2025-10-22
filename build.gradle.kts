plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.0"
}

group = "com.gity"
version = "0.0.1"

application {
    mainClass = "com.gity.ApplicationKt"
}

ktor {
    development = true
}

dependencies {
    // Ktor Features
    implementation("io.ktor:ktor-server-content-negotiation-jvm:3.0.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:3.0.1")
    implementation("io.ktor:ktor-server-auth-jvm:3.0.1")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:3.0.1")
    implementation("io.ktor:ktor-server-cors-jvm:3.0.1")
    implementation("io.ktor:ktor-server-call-logging-jvm:3.0.1")
    implementation("io.ktor:ktor-server-status-pages-jvm:3.0.1")
    implementation("io.ktor:ktor-server-rate-limit:3.0.1")

    // OpenAPI/Swagger Documentation
    implementation("io.ktor:ktor-server-swagger-jvm:3.0.1")
    implementation("io.ktor:ktor-server-openapi:3.0.1")

    // MongoDB Official Kotlin Driver (Mengganti KMongo)
    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:5.2.1")
    implementation("org.mongodb:bson-kotlinx:5.2.1")

    // Redis (Jedis)
    implementation("redis.clients:jedis:5.2.0")

    // Security
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("commons-codec:commons-codec:1.17.1")

    // Email
    implementation("org.simplejavamail:simple-java-mail:8.12.3")

    // Cloudinary
    implementation("com.cloudinary:kotlin-url-gen:1.7.0")

    // Validation
    implementation("io.konform:konform-jvm:0.6.1")

    // Dependency Injection
    implementation("io.insert-koin:koin-ktor:4.1.1")
    implementation("io.insert-koin:koin-logger-slf4j:4.1.1")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.12")

    // Testing
    testImplementation("io.ktor:ktor-server-test-host-jvm:3.0.1")
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.0.21")

    // Dot Env
    implementation("io.github.cdimascio:java-dotenv:5.2.2")

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.config.yaml)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
