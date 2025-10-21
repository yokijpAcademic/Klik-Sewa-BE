package com.gity.config

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import org.bson.codecs.configuration.CodecRegistries.fromProviders
import org.bson.codecs.kotlinx.KotlinXExtensions
import org.bson.codecs.pojo.PojoCodecProvider
import org.slf4j.LoggerFactory

class DatabaseConfig(
    private val appConfig: AppConfig
) {
    private val logger = LoggerFactory.getLogger(DatabaseConfig::class.java)

    private val connectionString = ConnectionString(appConfig.database.uri)

    private val codecRegistry = fromProviders(
        PojoCodecProvider.builder().automatic(true).build(),
        KotlinXExtensions.kotlinXCodecProvider()
    )

    private val settings = MongoClientSettings.builder()
        .applyConnectionString(connectionString)
        .codecRegistry(codecRegistry)
        .build()

    val client: MongoClient = MongoClient.create(settings)
    val database: MongoDatabase = client.getDatabase("kliksewa_db") // Nama database tetap disini atau bisa juga dari AppConfig

    init {
        logger.info("DatabaseConfig initialized, connecting to: ${connectionString.connectionString}")
    }

    // Fungsi untuk mendapatkan koleksi dengan tipe yang benar
    inline fun <reified T> getCollection(name: String): com.mongodb.kotlin.client.coroutine.MongoCollection<T> {
        return database.getCollection(name, T::class.java)
    }
}