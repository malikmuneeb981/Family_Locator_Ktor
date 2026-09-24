package com.example.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(val userName: String)
