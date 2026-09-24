package com.example.data

import domain.models.Member
import domain.models.MyLatLng
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.send
import kotlinx.coroutines.isActive
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

class LocationServiceImpl : LocationSharingService {
    private val usersHashMap = ConcurrentHashMap<String, Member>()
   // private val groupMembers = ConcurrentHashMap<String, CopyOnWriteArraySet<String>>()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    override suspend fun onJoin(
        userName: String,
        sessionId: String,
        socketSession: WebSocketSession,
    ) {
        // If user is already connected on an older session, close/replace it
        usersHashMap[userName]?.let { oldMember ->
            try {
                oldMember.session.close()
            } catch (_: Exception) {
            }
        }

        val member = Member(
            username = userName,
            sessionId = sessionId,
            session = socketSession,
        )
        usersHashMap[userName] = member
        println("Connected $userName")
    }

    override suspend fun broadcastLocation(
        location: MyLatLng
    ) {
       // val sender = usersHashMap[location.senderName]
        val receiver = usersHashMap[location.receiverName]


        val location = MyLatLng(
            senderName = location.senderName,
            receiverName = location.receiverName,
            latitude = location.latitude,
            longitude = location.longitude
        )

      //  chatService.insertMessage(message)
        val serializedMessage = Json.encodeToString(location)

        // Send to receiver via WebSocket
        if (receiver != null && receiver.session.isActive) {
            receiver.session.send(Frame.Text(serializedMessage))
        } else {
            // If receiver is not connected, send FCM notification
            println("Receiver Session Not Active $receiver")
           // sendPushNotification(fcmToken,senderUsername, message.text)
        }

        // Optionally send to sender as well (like echo)
      //  sender?.session?.send(Frame.Text(serializedMessage))
    }

    override suspend fun onDisconnect(userName: String) {
        println("Disconnected $userName")
        usersHashMap[userName]?.session?.close()
        usersHashMap.remove(userName)
    }
}