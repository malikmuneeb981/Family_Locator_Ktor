package com.example.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class ConnectRequest(
    val myName: String,
    val friendsName: String
)
