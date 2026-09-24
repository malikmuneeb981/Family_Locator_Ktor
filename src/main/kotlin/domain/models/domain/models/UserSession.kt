package com.example.domain.models.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class UserSession(val senderusername:String,val reciverusername:String,val sessionId:String)