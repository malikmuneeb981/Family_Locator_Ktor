package com.example.di

import com.example.data.LocationServiceImpl
import com.example.data.LocationSharingService
import com.example.data.UserService
import com.example.data.UserServiceImpl
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.ServerApi
import com.mongodb.ServerApiVersion
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import org.koin.dsl.module

val koinModule = module {
    single<MongoClient> {
        val rawUri = System.getenv("MONGO_DB_CONNECT_STRING")
            ?: "mongodb://localhost:27017"
        val settings = MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(rawUri))
            .serverApi(ServerApi.builder().version(ServerApiVersion.V1).build())
            .build()

        MongoClient.create(settings)
    }

    single<MongoDatabase> {
        val client = get<MongoClient>()
        client.getDatabase("Family_Locator")
    }

    single<UserService> {
        UserServiceImpl(get())
    }

    single<LocationSharingService> {
        LocationServiceImpl()
    }
}