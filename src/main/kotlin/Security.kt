package com.example

import io.ktor.server.application.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.domain.models.domain.models.UserSession
import io.ktor.server.application.ApplicationCallPipeline.ApplicationPhase.Plugins
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import io.ktor.util.generateNonce

fun Application.configureSecurity() {
    val jwtAudience = "jwt-audience"
    val jwtDomain = "https://jwt-provider-domain/"
    val jwtRealm = "ktor sample app"
    val jwtSecret = "secret"
    install(Sessions){
        cookie<UserSession>("user_session")
    }
    intercept(Plugins) {
        // Skip session creation for WebSocket upgrade requests.
        // Setting a session (cookie) inside a WebSocket call causes
        // "Headers can no longer be set because response was already completed"
        // because the HTTP response is already upgraded to a WebSocket connection.
        val isWebSocket = call.request.headers["Upgrade"]?.equals("websocket", ignoreCase = true) == true
        if (!isWebSocket && call.sessions.get<UserSession>() == null) {
            val senderusername = call.parameters["sender"]
            val reciverusername = call.parameters["receiver"]
            if (senderusername != null && reciverusername != null) {
                call.sessions.set(UserSession(senderusername, reciverusername, generateNonce()))
            }
        }
    }
    authentication {
        jwt {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtDomain)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
            }
        }
    }
}