package com.example.data

import domain.models.MyLatLng
import io.ktor.websocket.WebSocketSession

interface LocationSharingService {

    suspend fun onJoin(
        userName: String,
        sessionId: String,
        socketSession: WebSocketSession,
    )

    suspend fun broadcastLocation(
        location: MyLatLng
    )

    suspend fun onDisconnect(
        userName: String
    )
}