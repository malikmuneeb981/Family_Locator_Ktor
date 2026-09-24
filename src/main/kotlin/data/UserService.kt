package com.example.data

import com.example.domain.models.AuthRequest
import com.example.domain.models.User

interface UserService {
    suspend fun signUpUser(user: User): User?
    suspend fun checkCurrentUserExists(authRequest: AuthRequest): User?
    suspend fun connectUsers(userA: String, userB: String): User?
    suspend fun getUserByUsername(userName: String): User?
}