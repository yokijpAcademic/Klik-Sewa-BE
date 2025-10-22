# 🚀 Development Phase - Klik Sewa Backend

## 📋 Table of Contents
- [Phase 0: Prerequisites](#phase-0-prerequisites)
- [Phase 1: Project Initialization](#phase-1-project-initialization)
- [Phase 2: Infrastructure Setup](#phase-2-infrastructure-setup)
- [Phase 3: Configuration Layer](#phase-3-configuration-layer)
- [Phase 4: Dependency Injection](#phase-4-dependency-injection)
- [Phase 5: Shared Utilities](#phase-5-shared-utilities)
- [Phase 6: Ktor Plugins](#phase-6-ktor-plugins)
- [Phase 7: Application Entry Point](#phase-7-application-entry-point)
- [Phase 8: Testing & Verification](#phase-8-testing--verification)
- [Next Steps](#next-steps)

---

## Phase 0: Prerequisites

### Tools yang Dibutuhkan
1. **JDK 21** - [Download](https://adoptium.net/)
2. **Docker Desktop** - [Download](https://www.docker.com/products/docker-desktop)
3. **IntelliJ IDEA** (Recommended)
4. **Postman** atau **Insomnia** untuk testing API
5. **Git** untuk version control

### Verifikasi Instalasi
```bash
# Check Java version
java -version  # Harus 21 or higher

# Check Docker
docker --version
docker-compose --version

# Check Git
git --version
```

---

## Phase 1: Project Initialization

### 1.1 Create Project via Ktor Generator

**Option A: Via Website**
1. Buka https://start.ktor.io
2. Pilih:
    - **Project**: Server
    - **Engine**: Netty
    - **Build System**: Gradle (Kotlin)
    - **Kotlin Version**: 2.0.21
3. Add plugins:
    - Routing
    - Content Negotiation
    - Kotlinx Serialization
    - Authentication JWT
    - CORS
    - Status Pages
4. Download & extract

**Option B: Clone from Repository**
```bash
git clone <your-repo-url>
cd klik-sewa-BE
```

### 1.2 Setup Dependencies

Edit `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.0"
}

group = "com.gity"
version = "0.0.1"

application {
    mainClass = "com.gity.Application"
}

dependencies {
    // Ktor Core
    implementation("io.ktor:ktor-server-core-jvm:3.0.1")
    implementation("io.ktor:ktor-server-netty-jvm:3.0.1")
    
    // Ktor Features
    implementation("io.ktor:ktor-server-content-negotiation-jvm:3.0.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:3.0.1")
    implementation("io.ktor:ktor-server-auth-jvm:3.0.1")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:3.0.1")
    implementation("io.ktor:ktor-server-cors-jvm:3.0.1")
    implementation("io.ktor:ktor-server-call-logging-jvm:3.0.1")
    implementation("io.ktor:ktor-server-status-pages-jvm:3.0.1")
    implementation("io.ktor:ktor-server-rate-limit:3.0.1")

    // Database
    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:5.2.1")
    implementation("org.mongodb:bson-kotlinx:5.2.1")

    // Redis
    implementation("redis.clients:jedis:5.2.0")

    // Security
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("commons-codec:commons-codec:1.17.1")

    // Email
    implementation("org.simplejavamail:simple-java-mail:8.12.3")

    // Dependency Injection
    implementation("io.insert-koin:koin-ktor:4.0.0")
    implementation("io.insert-koin:koin-logger-slf4j:4.0.0")

    // Utilities
    implementation("io.github.cdimascio:java-dotenv:5.2.2")
    implementation("ch.qos.logback:logback-classic:1.5.12")

    // Testing
    testImplementation("io.ktor:ktor-server-test-host-jvm:3.0.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.0.21")
}
```

### 1.3 Sync Gradle

```bash
# Via terminal
./gradlew build --refresh-dependencies

# Atau di IntelliJ: Click "Load Gradle Changes"
```

**Expected Output:**
```
BUILD SUCCESSFUL in 30s
```

---

## Phase 2: Infrastructure Setup

### 2.1 Create Docker Compose File

Create `docker-compose.yaml` di root project:

```yaml
services:
  mongodb:
    image: mongo:7.0
    container_name: kliksewa-mongodb
    restart: always
    ports:
      - "27017:27017"
    environment:
      MONGO_INITDB_DATABASE: kliksewa_db
    volumes:
      - mongodb_data:/data/db
      - mongodb_config:/data/configdb
    networks:
      - kliksewa-network

  redis:
    image: redis:7-alpine
    container_name: kliksewa-redis
    restart: always
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
    volumes:
      - redis_data:/data
    networks:
      - kliksewa-network

  mongo-express:
    image: mongo-express:latest
    container_name: kliksewa-mongo-express
    restart: always
    ports:
      - "8081:8081"
    environment:
      ME_CONFIG_MONGODB_SERVER: mongodb
      ME_CONFIG_MONGODB_PORT: 27017
      ME_CONFIG_BASICAUTH_USERNAME: admin
      ME_CONFIG_BASICAUTH_PASSWORD: admin123
    depends_on:
      - mongodb
    networks:
      - kliksewa-network

  redis-commander:
    image: rediscommander/redis-commander:latest
    container_name: kliksewa-redis-commander
    restart: always
    ports:
      - "8082:8081"
    environment:
      - REDIS_HOSTS=local:redis:6379
    depends_on:
      - redis
    networks:
      - kliksewa-network

volumes:
  mongodb_data:
  mongodb_config:
  redis_data:

networks:
  kliksewa-network:
    driver: bridge
```

### 2.2 Start Docker Services

```bash
# Start all services
docker-compose up -d

# Expected output:
# Creating network "kliksewa-network" ...
# Creating volume "kliksewa_mongodb_data" ...
# Creating volume "kliksewa_redis_data" ...
# Creating kliksewa-mongodb ...
# Creating kliksewa-redis ...
# Creating kliksewa-mongo-express ...
# Creating kliksewa-redis-commander ...

# Verify services are running
docker-compose ps

# Expected output:
# NAME                      STATUS
# kliksewa-mongodb          Up
# kliksewa-redis            Up
# kliksewa-mongo-express    Up
# kliksewa-redis-commander  Up
```

### 2.3 Verify Services

**MongoDB Express:**
- URL: http://localhost:8081
- Username: `admin`
- Password: `admin123`
- You should see `kliksewa_db` database (empty for now)

**Redis Commander:**
- URL: http://localhost:8082
- You should see Redis interface

**Test MongoDB Connection:**
```bash
docker exec -it kliksewa-mongodb mongosh
# Should open MongoDB shell
```

**Test Redis Connection:**
```bash
docker exec -it kliksewa-redis redis-cli
# Type: PING
# Expected: PONG
```

---

## Phase 3: Configuration Layer

### 3.1 Create .env File

Create `.env` in root directory:

```env
# Database Configuration
MONGODB_URI=mongodb://localhost:27017/kliksewa_db

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT Configuration
JWT_SECRET=change-this-to-a-very-long-and-secure-random-string-in-production
JWT_EXPIRATION_IN_MINUTES=10080

# Email Configuration (Brevo)
BREVO_API_KEY=your-brevo-api-key-here
SENDER_EMAIL=noreply@kliksewa.com
SENDER_NAME=Klik Sewa

# Application Configuration
APP_ENV=development
FRONTEND_URL=http://localhost:3000
```

**Important:** Add `.env` to `.gitignore`!

### 3.2 Create Configuration Classes

Create `src/main/kotlin/config/AppConfig.kt`:

```kotlin
package com.gity.config

import io.github.cdimascio.dotenv.dotenv
import org.slf4j.LoggerFactory

data class AppConfig(
    val database: DatabaseConfig,
    val redis: RedisConfig,
    val email: EmailConfig,
    val jwt: JwtConfig,
    val app: ApplicationConfig
)

data class DatabaseConfig(val uri: String)
data class RedisConfig(val host: String, val port: Int, val password: String?)
data class EmailConfig(val apiKey: String, val senderEmail: String, val senderName: String)
data class JwtConfig(val secret: String, val expirationInMinutes: Long)
data class ApplicationConfig(val environment: String, val frontendUrl: String)

class ConfigLoader {
    private val logger = LoggerFactory.getLogger(ConfigLoader::class.java)
    private val dotenv = dotenv {
        ignoreIfMissing = true
        systemProperties = true
    }

    fun load(): AppConfig {
        logger.info("Loading application configuration...")

        return AppConfig(
            database = DatabaseConfig(
                uri = dotenv["MONGODB_URI"] ?: "mongodb://localhost:27017/kliksewa_db"
            ),
            redis = RedisConfig(
                host = dotenv["REDIS_HOST"] ?: "localhost",
                port = dotenv["REDIS_PORT"]?.toIntOrNull() ?: 6379,
                password = dotenv["REDIS_PASSWORD"]?.takeIf { it.isNotBlank() }
            ),
            email = EmailConfig(
                apiKey = dotenv["BREVO_API_KEY"] ?: "",
                senderEmail = dotenv["SENDER_EMAIL"] ?: "noreply@kliksewa.com",
                senderName = dotenv["SENDER_NAME"] ?: "Klik Sewa"
            ),
            jwt = JwtConfig(
                secret = dotenv["JWT_SECRET"] ?: throw IllegalStateException("JWT_SECRET not set!"),
                expirationInMinutes = dotenv["JWT_EXPIRATION_IN_MINUTES"]?.toLongOrNull() ?: 10080L
            ),
            app = ApplicationConfig(
                environment = dotenv["APP_ENV"] ?: "development",
                frontendUrl = dotenv["FRONTEND_URL"] ?: "http://localhost:3000"
            )
        ).also {
            logger.info("Configuration loaded successfully")
            logger.info("Environment: ${it.app.environment}")
        }
    }
}
```

### 3.3 Create Database Configuration

Create `src/main/kotlin/config/DatabaseConfig.kt`:

```kotlin
package com.gity.config

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import org.bson.codecs.configuration.CodecRegistries.*
import org.bson.codecs.pojo.PojoCodecProvider
import org.slf4j.LoggerFactory

class DatabaseClient(private val appConfig: AppConfig) {
    private val logger = LoggerFactory.getLogger(DatabaseClient::class.java)
    
    private val connectionString = ConnectionString(appConfig.database.uri)
    private val databaseName = connectionString.database ?: "kliksewa_db"
    
    private val codecRegistry = fromRegistries(
        MongoClientSettings.getDefaultCodecRegistry(),
        fromProviders(PojoCodecProvider.builder().automatic(true).build())
    )
    
    private val settings = MongoClientSettings.builder()
        .applyConnectionString(connectionString)
        .codecRegistry(codecRegistry)
        .build()
    
    val client: MongoClient = MongoClient.create(settings)
    val database: MongoDatabase = client.getDatabase(databaseName)
    
    init {
        logger.info("DatabaseClient initialized")
        logger.info("Connected to database: $databaseName")
    }
    
    inline fun <reified T : Any> getCollection(name: String) =
        database.getCollection(name, T::class.java)
    
    fun close() {
        client.close()
        logger.info("Database connection closed")
    }
}
```

### 3.4 Create Redis Configuration

Create `src/main/kotlin/config/RedisConfig.kt`:

```kotlin
package com.gity.config

import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import org.slf4j.LoggerFactory

class RedisClient(private val appConfig: AppConfig) {
    private val logger = LoggerFactory.getLogger(RedisClient::class.java)
    
    private val poolConfig = JedisPoolConfig().apply {
        maxTotal = 128
        maxIdle = 16
        minIdle = 8
        testOnBorrow = true
    }
    
    val jedisPool: JedisPool = createJedisPool()
    
    private fun createJedisPool(): JedisPool {
        return try {
            val pool = if (appConfig.redis.password != null) {
                JedisPool(poolConfig, appConfig.redis.host, appConfig.redis.port, 
                         2000, appConfig.redis.password)
            } else {
                JedisPool(poolConfig, appConfig.redis.host, appConfig.redis.port)
            }
            
            pool.resource.use { jedis ->
                jedis.ping()
                logger.info("Redis connected: ${appConfig.redis.host}:${appConfig.redis.port}")
            }
            pool
        } catch (e: Exception) {
            logger.error("Failed to connect to Redis", e)
            throw e
        }
    }
    
    // Helper functions
    fun set(key: String, value: String, expirationSeconds: Int? = null) {
        jedisPool.resource.use { jedis ->
            if (expirationSeconds != null) {
                jedis.setex(key, expirationSeconds.toLong(), value)
            } else {
                jedis.set(key, value)
            }
        }
    }
    
    fun get(key: String): String? = jedisPool.resource.use { it.get(key) }
    fun delete(key: String): Long = jedisPool.resource.use { it.del(key) }
    fun exists(key: String): Boolean = jedisPool.resource.use { it.exists(key) }
}
```

---

## Phase 4: Dependency Injection

### 4.1 Create Koin Modules

Create `src/main/kotlin/di/KoinModules.kt`:

```kotlin
package com.gity.di

import com.gity.config.*
import com.gity.shared.utils.*
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
```

**Why Koin?**
- Lightweight dependency injection
- Easy to setup and use
- Perfect for Ktor
- Type-safe injection

---

## Phase 5: Shared Utilities

### 5.1 HashingUtil

Create `src/main/kotlin/shared/utils/HashingUtil.kt`:

```kotlin
package com.gity.shared.utils

import org.mindrot.jbcrypt.BCrypt
import org.apache.commons.codec.digest.DigestUtils
import java.security.SecureRandom
import java.util.*

class HashingUtil {
    fun hashPassword(password: String): String =
        BCrypt.hashpw(password, BCrypt.gensalt(12))
    
    fun verifyPassword(password: String, hashedPassword: String): Boolean =
        try { BCrypt.checkpw(password, hashedPassword) } 
        catch (e: Exception) { false }
    
    fun generateToken(length: Int = 32): String {
        val random = SecureRandom()
        val bytes = ByteArray(length)
        random.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
    
    fun hashToken(token: String): String = DigestUtils.sha256Hex(token)
}
```

### 5.2 JwtUtil

Create `src/main/kotlin/shared/utils/JwtUtil.kt`:

```kotlin
package com.gity.shared.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.gity.config.AppConfig
import java.util.Date

class JwtUtil(private val appConfig: AppConfig) {
    private val algorithm = Algorithm.HMAC256(appConfig.jwt.secret)
    
    fun generateToken(userId: String, role: String): String {
        val expiresAt = Date(System.currentTimeMillis() + 
                            (appConfig.jwt.expirationInMinutes * 60000L))
        return JWT.create()
            .withSubject(userId)
            .withClaim("role", role)
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }
    
    fun verifyToken(token: String): String? =
        try {
            JWT.require(algorithm).build().verify(token).subject
        } catch (e: Exception) { null }
}
```

### 5.3 EmailUtil

Already implemented in your project! ✅

---

## Phase 6: Ktor Plugins

### 6.1 Configure Serialization

Create `src/main/kotlin/plugins/ConfigureSerialization.kt`:

```kotlin
package com.gity.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}
```

### 6.2 Configure Authentication

Already implemented! ✅ Check your `ConfigureAuthentication.kt`

### 6.3 Configure CORS

Already implemented! ✅ Check your `ConfigureCORS.kt`

### 6.4 Configure Other Plugins

All other plugins already implemented! ✅

---

## Phase 7: Application Entry Point

### 7.1 Main Application File

Update `src/main/kotlin/Application.kt`:

```kotlin
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
    // 1. Dependency Injection (MUST BE FIRST!)
    koinModules(this)
    
    // 2. Configure Plugins
    configureSerialization()
    configureMonitoring()
    configureCORS()
    configureAuthentication()
    configureStatusPages()
    configureHTTP()
    
    // 3. Feature Modules (will be added later)
    // configureAuthModule()
    // configureListingModule()
    // configureCategoryModule()
    // configureAdminModule()
}
```

### 7.2 Application Configuration

Update `src/main/resources/application.yaml`:

```yaml
ktor:
    application:
        modules:
            - com.gity.ApplicationKt.module
    deployment:
        port: 8080
    development: true
```

### 7.3 Logging Configuration

Update `src/main/resources/logback.xml`:

```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{YYYY-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    <root level="INFO">
        <appender-ref ref="STDOUT"/>
    </root>
    <logger name="org.eclipse.jetty" level="INFO"/>
    <logger name="io.netty" level="INFO"/>
</configuration>
```

---

## Phase 8: Testing & Verification

### 8.1 Build the Project

```bash
./gradlew clean build

# Expected output:
# BUILD SUCCESSFUL in 15s
```

**Possible Issues:**

**Issue 1: Dependencies not found**
```bash
./gradlew build --refresh-dependencies
```

**Issue 2: Kotlin version mismatch**
- Check `build.gradle.kts` uses Kotlin 2.0.21+

### 8.2 Run the Application

```bash
./gradlew run

# Or in IntelliJ: Right-click Application.kt > Run
```

**Expected Console Output:**

```
2025-10-22 10:30:45.123 [main] INFO  ConfigLoader - Loading application configuration...
2025-10-22 10:30:45.234 [main] INFO  ConfigLoader - Configuration loaded successfully
2025-10-22 10:30:45.235 [main] INFO  ConfigLoader - Environment: development
2025-10-22 10:30:45.456 [main] INFO  DatabaseClient - DatabaseClient initialized
2025-10-22 10:30:45.457 [main] INFO  DatabaseClient - Connected to database: kliksewa_db
2025-10-22 10:30:45.678 [main] INFO  RedisClient - Redis connected: localhost:6379
2025-10-22 10:30:46.123 [main] INFO  Application - Application started in 1.234 seconds.
2025-10-22 10:30:46.234 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

### 8.3 Test Basic Endpoint

**Test 1: Server Health Check**

```bash
curl http://localhost:8080

# Expected: 404 (normal, karena belum ada routes)
```

**Test 2: Check Server Logs**

You should see in console:
```
[DefaultDispatcher-worker-1] INFO  Application - 404 Not Found: GET - /
```

### 8.4 Verify Database Connection

**MongoDB Test:**

```bash
# Open MongoDB shell
docker exec -it kliksewa-mongodb mongosh

# Inside mongosh:
> show dbs
> use kliksewa_db
> show collections  # Should be empty for now
```

**Redis Test:**

```bash
# Open Redis CLI
docker exec -it kliksewa-redis redis-cli

# Inside redis-cli:
> PING  # Should return PONG
> KEYS * # Should return empty array
```

### 8.5 Verify Web Interfaces

**MongoDB Express:**
- Open http://localhost:8081
- Login: admin / admin123
- Navigate to `kliksewa_db`
- Should see database (no collections yet)

**Redis Commander:**
- Open http://localhost:8082
- Should see Redis interface
- No keys yet (normal)

### 8.6 Test Koin Injection

Add temporary test endpoint to verify Koin works:

```kotlin
// In Application.kt, add this temporarily:
import io.ktor.server.routing.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject
import com.gity.config.AppConfig

fun Application.module() {
    koinModules(this)
    
    configureSerialization()
    // ... other configs
    
    // TEMPORARY TEST ENDPOINT
    routing {
        get("/health") {
            val appConfig by inject<AppConfig>()
            call.respondText("OK - Environment: ${appConfig.app.environment}")
        }
    }
}
```

Test:
```bash
curl http://localhost:8080/health

# Expected output:
# OK - Environment: development
```

**If works, remove this test endpoint!**

---

## Phase 9: Create Shared Models & DTOs

### 9.1 Create Enums

**UserRole.kt:**

```kotlin
package com.gity.shared.models.enums

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    USER,
    ADMIN
}
```

**ListingStatus.kt:**

```kotlin
package com.gity.shared.models.enums

enum class ListingStatus {
    PENDING,
    APPROVED,
    REJECTED,
    ARCHIVED
}
```

**Availability.kt:**

```kotlin
package com.gity.shared.models.enums

enum class Availability {
    AVAILABLE,
    RENTED,
    UNAVAILABLE
}
```

### 9.2 Create Common Response DTOs

Already implemented! ✅ Check your `CommonResponse.kt`

---

## Phase 10: Verify Complete Setup

### Final Checklist

Run through this checklist:

```bash
# 1. Environment file exists
[ -f .env ] && echo "✅ .env exists" || echo "❌ .env missing"

# 2. Docker containers running
docker-compose ps | grep "Up" && echo "✅ Docker running" || echo "❌ Docker not running"

# 3. Project builds
./gradlew build > /dev/null 2>&1 && echo "✅ Build successful" || echo "❌ Build failed"

# 4. MongoDB accessible
curl -s http://localhost:8081 > /dev/null && echo "✅ MongoDB Express OK" || echo "❌ MongoDB Express down"

# 5. Redis accessible
curl -s http://localhost:8082 > /dev/null && echo "✅ Redis Commander OK" || echo "❌ Redis Commander down"
```

### Expected Checklist Output:

```
✅ .env exists
✅ Docker running
✅ Build successful
✅ MongoDB Express OK
✅ Redis Commander OK
```

---

## Common Issues & Solutions

### Issue 1: Port Already in Use

**Error:** `Port 8080 is already in use`

**Solution:**

**Windows:**
```powershell
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

**Mac/Linux:**
```bash
lsof -i :8080
kill -9 <PID>
```

Or change port in `application.yaml`:
```yaml
ktor:
    deployment:
        port: 8081  # Use different port
```

### Issue 2: MongoDB Connection Refused

**Error:** `Connection refused: mongodb://localhost:27017`

**Solution:**

```bash
# Check if MongoDB container is running
docker-compose ps

# Restart MongoDB
docker-compose restart mongodb

# Check logs
docker-compose logs mongodb
```

### Issue 3: Redis Connection Failed

**Error:** `Could not connect to Redis`

**Solution:**

```bash
# Restart Redis
docker-compose restart redis

# Test connection
docker exec -it kliksewa-redis redis-cli PING
```

### Issue 4: JWT_SECRET Not Set

**Error:** `JWT_SECRET environment variable is not set!`

**Solution:**

Make sure `.env` file exists and contains:
```env
JWT_SECRET=your-secret-key-here
```

Then restart application.

### Issue 5: Email Configuration Warning

**Warning:** `Email functionality will be disabled`

**Solution:**

This is OK for development! Email will be skipped if `BREVO_API_KEY` is empty.

To enable email:
1. Sign up at https://www.brevo.com
2. Get API key from Settings > SMTP & API
3. Add to `.env`:
```env
BREVO_API_KEY=xkeysib-your-api-key-here
```

### Issue 6: Gradle Build Failed

**Error:** Various build errors

**Solution:**

```bash
# Clean and rebuild
./gradlew clean
./gradlew build --refresh-dependencies

# If still fails, delete Gradle cache
rm -rf ~/.gradle/caches
./gradlew build
```

---

## Next Steps

### 🎯 Ready for Feature Development!

Your setup is complete! Now you can implement:

### **Priority 1: Domain Models** (MUST DO FIRST!)

Create these models in `src/main/kotlin/shared/models/`:

1. **User.kt**
    - Properties: id, email, passwordHash, fullName, phoneNumber, role, etc.
    - Used by: Auth, Admin features

2. **Category.kt**
    - Properties: id, name, description, icon, etc.
    - Used by: Category, Listing features

3. **Listing.kt**
    - Properties: id, title, description, price, categoryId, ownerId, etc.
    - Used by: Listing, Admin features

**Why Models First?**
- All repositories need models
- All services need models
- All DTOs reference models
- Cannot implement features without models!

### **Priority 2: Auth Feature**

After models are done:

1. Create DTOs (requests & responses)
2. Implement UserRepository
3. Implement AuthService
4. Create AuthModule (routes)
5. Register in Koin & Application

### **Priority 3: Category Feature**

Simple CRUD for practice:

1. Create DTOs
2. Implement CategoryRepository
3. Implement CategoryService
4. Create CategoryModule

### **Priority 4: Listing Feature**

Complex CRUD with images:

1. Create DTOs
2. Implement ListingRepository
3. Implement ListingService
4. Create ListingModule
5. Add image upload (later)

### **Priority 5: Admin Feature**

Dashboard & management:

1. Dashboard stats
2. Listing approval/rejection
3. User management

---

## Project Structure Overview

Your current structure:

```
klik-sewa-BE/
├── src/main/kotlin/
│   ├── Application.kt              ✅ Entry point
│   ├── config/
│   │   ├── AppConfig.kt           ✅ Configuration
│   │   ├── DatabaseConfig.kt      ✅ MongoDB setup
│   │   └── RedisConfig.kt         ✅ Redis setup
│   ├── di/
│   │   └── KoinModules.kt         ✅ Dependency injection
│   ├── features/
│   │   ├── auth/                  ⏳ To implement
│   │   ├── listing/               ⏳ To implement
│   │   ├── category/              ⏳ To implement
│   │   └── admin/                 ⏳ To implement
│   ├── plugins/                   ✅ All configured
│   │   ├── ConfigureAuthentication.kt
│   │   ├── ConfigureCORS.kt
│   │   ├── ConfigureHTTP.kt
│   │   ├── ConfigureMonitoring.kt
│   │   ├── ConfigureSerialization.kt
│   │   └── ConfigureStatusPages.kt
│   └── shared/
│       ├── constants/             ⏳ To add
│       ├── models/                ⚠️  NEXT: Create models!
│       │   ├── User.kt           ❌ Empty
│       │   ├── Listing.kt        ❌ Empty
│       │   ├── Category.kt       ❌ Empty
│       │   └── enums/            ✅ Done
│       ├── dtos/
│       │   └── response/
│       │       └── CommonResponse.kt  ✅ Done
│       └── utils/                 ✅ All implemented
│           ├── HashingUtil.kt
│           ├── JwtUtil.kt
│           ├── EmailUtil.kt
│           └── ValidationUtil.kt
├── .env                           ✅ Configured
├── docker-compose.yaml            ✅ Running
├── build.gradle.kts               ✅ Complete
└── README.md                      ✅ Updated
```

---

## Development Workflow

### Daily Development Flow:

```bash
# 1. Morning: Start infrastructure
docker-compose start

# 2. Start application
./gradlew run

# 3. Develop features
# - Edit code
# - Save (auto-reload if using development mode)

# 4. Test with Postman
# - Test endpoints
# - Verify responses

# 5. Check MongoDB data
# - Open http://localhost:8081
# - Inspect collections

# 6. Check Redis cache
# - Open http://localhost:8082
# - Verify cached data

# 7. Evening: Stop infrastructure
docker-compose stop
```

### Git Workflow:

```bash
# Create feature branch
git checkout -b feature/auth-implementation

# Make changes, then:
git add .
git commit -m "Implement user registration endpoint"

# Push to remote
git push origin feature/auth-implementation

# Create Pull Request on GitHub/GitLab
```

---

## Performance Tips

### Development Mode:

```yaml
# application.yaml
ktor:
    development: true  # Enable auto-reload
```

### IntelliJ IDEA Tips:

1. **Enable Auto-Build:**
    - Settings > Build > Compiler
    - Check "Build project automatically"

2. **Hot Reload:**
    - Run with "Debug" instead of "Run"
    - Code changes reload automatically

3. **Shortcuts:**
    - `Ctrl + Space` - Auto-complete
    - `Alt + Enter` - Quick fix
    - `Ctrl + B` - Go to definition
    - `Shift + Shift` - Search everywhere

---

## Security Checklist

Before going to production:

- [ ] Change `JWT_SECRET` to strong random string
- [ ] Set `APP_ENV=production` in production
- [ ] Configure proper CORS (not `anyHost()`)
- [ ] Use strong Redis password
- [ ] Enable MongoDB authentication
- [ ] Review rate limiting settings
- [ ] Enable HTTPS/SSL
- [ ] Secure `.env` file (never commit!)
- [ ] Set up proper logging
- [ ] Configure error monitoring (Sentry, etc.)

---

## Monitoring & Debugging

### View Application Logs:

```bash
# Real-time logs
./gradlew run

# Or with IntelliJ console
```

### View Docker Logs:

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f mongodb
docker-compose logs -f redis
```

### Check System Resources:

```bash
# Docker stats
docker stats

# Expected output:
# CONTAINER           CPU %   MEM USAGE / LIMIT
# kliksewa-mongodb    0.5%    100MB / 2GB
# kliksewa-redis      0.1%    50MB / 2GB
```

---

## Additional Resources

### Documentation:

- **Ktor:** https://ktor.io/docs
- **Koin:** https://insert-koin.io
- **MongoDB Kotlin:** https://mongodb.github.io/mongo-java-driver/
- **Redis:** https://redis.io/docs

### Tutorials:

- Ktor Authentication: https://ktor.io/docs/authentication.html
- MongoDB CRUD: https://mongodb.github.io/mongo-java-driver/
- JWT Best Practices: https://jwt.io/introduction

### Community:

- Ktor Slack: https://kotlinlang.slack.com
- Stack Overflow: Tag with `ktor`, `kotlin`
- GitHub Issues: For bugs/questions

---

## Summary

### What We've Built:

✅ **Infrastructure**
- Docker containerization
- MongoDB database
- Redis cache

✅ **Configuration**
- Environment variables
- Database connection
- Redis connection
- Email setup

✅ **Architecture**
- Dependency injection (Koin)
- Layered architecture
- Repository pattern ready

✅ **Security**
- JWT authentication
- Password hashing
- Rate limiting
- CORS

✅ **Utilities**
- Email templates
- JWT generation
- Password hashing

### What's Next:

⏳ **Feature Implementation**
1. Create domain models
2. Implement Auth feature
3. Implement Category feature
4. Implement Listing feature
5. Implement Admin feature

---

## Conclusion

🎉 **Congratulations!**

Your Klik Sewa backend is now fully set up and ready for feature development!

The foundation is solid:
- Clean architecture
- Dependency injection
- Database & cache ready
- Security configured
- Error handling in place

**Next Action:** Start implementing domain models (User, Category, Listing)!

**Need help?** Check the example implementations in `IMPLEMENTATION_EXAMPLE.md`

---

## Quick Reference Commands

```bash
# Docker
docker-compose up -d        # Start all services
docker-compose stop         # Stop services
docker-compose logs -f      # View logs
docker-compose restart      # Restart services

# Gradle
./gradlew build            # Build project
./gradlew run              # Run application
./gradlew clean            # Clean build
./gradlew test             # Run tests

# MongoDB
docker exec -it kliksewa-mongodb mongosh  # MongoDB shell

# Redis
docker exec -it kliksewa-redis redis-cli  # Redis CLI

# Git
git status                 # Check status
git add .                  # Stage changes
git commit -m "message"    # Commit
git push                   # Push to remote
```

Happy Coding! 🚀