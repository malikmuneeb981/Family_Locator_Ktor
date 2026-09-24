package domain.models

import io.ktor.websocket.WebSocketSession

data class Member(
    val username: String,
    val sessionId: String,
    val session: WebSocketSession,
    val groupId: String? = null
)

