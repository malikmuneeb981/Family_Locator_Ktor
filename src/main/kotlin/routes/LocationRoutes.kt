package com.example.routes

import com.example.data.LocationSharingService
import com.example.data.UserService
import data.MemberAlreadyExistsException
import domain.models.DisconnectRequest
import domain.models.MyLatLng
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receiveNullable
import io.ktor.server.response.respond

import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json
import java.util.UUID

fun Route.locationWebSocketRoute(
    locationService: LocationSharingService,
) {
    webSocket("/location-share") {
        // Sessions cannot be set during a WebSocket upgrade, so we read
        // identity directly from query parameters instead.
        val senderUsername = call.request.queryParameters["sender"]
        val receiverUsername = call.request.queryParameters["receiver"]

        if (senderUsername == null || receiverUsername == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Missing sender or receiver query parameters."))
            return@webSocket
        }

        val sessionId = call.request.queryParameters["sessionId"] ?: UUID.randomUUID().toString()

        try {
            locationService.onJoin(
                userName = senderUsername,
                sessionId = sessionId,
                socketSession = this
            )
           // val receiverFcmToken = userService.getUserFcmToken(userSession.reciverusername)
            // println("receiver Fcm Token $receiverFcmToken")
            incoming.consumeEach {
                if (it is Frame.Text){
                    val decodedLocation = Json.decodeFromString<MyLatLng>(it.readText())
                    println("Incoming , $decodedLocation.toString()")
                    locationService.broadcastLocation(location = decodedLocation)
                }

            }
        } catch (e: MemberAlreadyExistsException) {

            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Member already exists."))

        } catch (e: Exception) {

            close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, "Something went wrong: ${e.localizedMessage}"))

        } finally {


           // locationService.onDisconnect(senderUsername)

        }

    }

}
fun Route.disconnectUser( locationService: LocationSharingService,){
    post("/disconnectUser") {
        val request = call.receiveNullable<DisconnectRequest>() ?: run {
            call.respond(HttpStatusCode.BadRequest, "Invalid request payload")
            return@post
        }
        if (request.userName.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Both userA and userB are required")
            return@post
        }
        locationService.onDisconnect(userName = request.userName)
        call.respond(HttpStatusCode.OK)
    }
}
