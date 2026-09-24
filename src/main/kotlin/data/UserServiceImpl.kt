package com.example.data

import com.example.domain.models.AuthRequest
import com.example.domain.models.User
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class UserServiceImpl(db: MongoDatabase) : UserService {

    private val users = db.getCollection<User>("users")

    override suspend fun signUpUser(user: User): User? {
        val userExists = users.find(eq("userName", user.userName)).firstOrNull()
        if (userExists != null) {
            return null
        }

        // Assign a unique random UUID as groupId if none was provided
        val assignedGroupId = user.groupId.ifBlank {
            UUID.randomUUID().toString()
        }

        val userToSave = user.copy(groupId = assignedGroupId)
        val userAdded = users.insertOne(userToSave).wasAcknowledged()
        return if (userAdded) userToSave else null
    }

    override suspend fun checkCurrentUserExists(authRequest: AuthRequest): User? {
        return users.find(eq("userName", authRequest.userName)).firstOrNull()
    }

    override suspend fun connectUsers(userA: String, userB: String): User? {
        val userAData = users.find(eq("userName", userA)).firstOrNull() ?: return null
        val userBData = users.find(eq("userName", userB)).firstOrNull() ?: return null

        println("myName,$userA")
        println("friendsName,$userB")
        // Ensure userA has a valid groupId (generate one if empty)
        val targetGroupId = userAData.groupId.ifBlank {
            val newGroupId = UUID.randomUUID().toString()
            users.updateOne(eq("userName", userA), Updates.set("groupId", newGroupId))
            newGroupId
        }
        println("myGroupId,$targetGroupId")
        // Update User B's groupId to match User A's groupId
        val updateResult = users.updateOne(
            eq("userName", userB),
            Updates.set("groupId", targetGroupId)
        )

        return if (updateResult.wasAcknowledged()) {
            userBData.copy(groupId = targetGroupId)
        } else {
            null
        }
    }

    override suspend fun getUserByUsername(userName: String): User? {
        return users.find(eq("userName", userName)).firstOrNull()
    }
}