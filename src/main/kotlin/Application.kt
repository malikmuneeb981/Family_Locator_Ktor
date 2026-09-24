package com.example

import com.example.data.LocationSharingService
import com.example.data.UserService
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.server.application.*
import kotlinx.coroutines.runBlocking
import org.bson.BsonDocument
import org.bson.BsonInt32
import org.koin.ktor.ext.getKoin
import org.koin.ktor.ext.inject

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}


fun Application.module() {
    configureKoin()
    val userService by inject<UserService>()
    val locationService by inject<LocationSharingService>()
    configureMonitoring()
    configureSecurity()
    configureWebsockets()
    configureSerialization()
    configurePostgres()
    configureRouting(userService = userService, locationService = locationService)

    // MongoDB connection check
    try {
        val db = getKoin().get<MongoDatabase>()
        log.info("Connecting to MongoDB...")
        runBlocking {
            db.runCommand(BsonDocument("ping", BsonInt32(1)))
        }
        log.info(" Connected successfully to MongoDB database: '${db.name}'")
    } catch (e: Exception) {
        log.error("❌ Failed to connect to MongoDB: ${e.message}", e)
    }
}