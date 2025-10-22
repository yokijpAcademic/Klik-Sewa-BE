package com.gity.config

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import org.bson.codecs.configuration.CodecRegistries.fromProviders
import org.bson.codecs.configuration.CodecRegistries.fromRegistries
import org.bson.codecs.kotlinx.KotlinSerializerCodec
import org.bson.codecs.pojo.PojoCodecProvider
import org.slf4j.LoggerFactory

class DatabaseClient(
    private val appConfig: AppConfig
) {
    private val logger = LoggerFactory.getLogger(DatabaseClient::class.java)

    private val connectionString = ConnectionString(appConfig.database.uri)

    // Extract database name from URI or use default
    private val databaseName = connectionString.database ?: "kliksewa_db"

    private val codecRegistry = fromRegistries(
        MongoClientSettings.getDefaultCodecRegistry(),
        fromProviders(
            PojoCodecProvider.builder().automatic(true).build()
        )
    )

    private val settings = MongoClientSettings.builder()
        .applyConnectionString(connectionString)
        .codecRegistry(codecRegistry)
        .build()

    val client: MongoClient = MongoClient.create(settings)
    val database: MongoDatabase = client.getDatabase(databaseName)

    init {
        logger.info("DatabaseClient initialized")
        logger.info("Connecting to database: $databaseName")
        logger.info("Connection string: ${connectionString.connectionString}")
    }

    // Fungsi untuk mendapatkan koleksi dengan tipe yang benar
    inline fun <reified T : Any> getCollection(name: String): com.mongodb.kotlin.client.coroutine.MongoCollection<T> {
        return database.getCollection(name, T::class.java)
    }

    // Fungsi untuk close connection (optional, untuk cleanup)
    fun close() {
        try {
            client.close()
            logger.info("Database connection closed")
        } catch (e: Exception) {
            logger.error("Error closing database connection: ${e.message}", e)
        }
    }
}