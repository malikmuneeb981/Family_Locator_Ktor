package com.example.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val userName: String,
    val phoneNumber: String,
    val userImage: String = "",
    val groupId: String = ""
)

