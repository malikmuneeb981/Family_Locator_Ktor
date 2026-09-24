package domain.models

import kotlinx.serialization.Serializable

@Serializable
data class DisconnectRequest(
    val userName: String
)