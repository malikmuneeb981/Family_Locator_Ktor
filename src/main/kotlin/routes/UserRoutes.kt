package com.example.routes

import com.example.data.UserService
import com.example.domain.models.AuthRequest
import com.example.domain.models.ConnectRequest
import com.example.domain.models.User
import domain.models.DisconnectRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receiveNullable
import io.ktor.server.request.receiveOrNull
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.signUpUser(
    userService: UserService
){
    post("/signUpUser") {
        val user = call.receiveNullable<User>() ?: run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        if (user.userName.isBlank() || user.phoneNumber.length < 8){
            call.respond(HttpStatusCode.Conflict)
            return@post
        }
        val userAdded = userService.signUpUser(user)
        userAdded?.let {
            call.respond(HttpStatusCode.OK,userAdded)
        }?:run {
            call.respond(HttpStatusCode.NotAcceptable)
        }

    }
}
fun Route.userExists(
    userService: UserService
){
    post("/userExists") {
        val authRequest = call.receiveNullable<AuthRequest>() ?: run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        if (authRequest.userName.isBlank()){
            call.respond(HttpStatusCode.Conflict)
            return@post
        }
        val user = userService.checkCurrentUserExists(authRequest)
        user?.let {
            call.respond(HttpStatusCode.OK,it)
        }?:run {
            call.respond(HttpStatusCode.Conflict,"User Does Not Exists")
        }
    }
}
fun Route.connectUsers(
    userService: UserService
){
    post("/connectUsers") {
        val request = call.receiveNullable<ConnectRequest>() ?: run {
            call.respond(HttpStatusCode.BadRequest, "Invalid request payload")
            return@post
        }
        if (request.myName.isBlank() || request.myName.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Both userA and userB are required")
            return@post
        }
        val updatedUserB = userService.connectUsers(userA = request.myName, userB = request.friendsName)
        if (updatedUserB != null) {
            call.respond(HttpStatusCode.OK, updatedUserB)
        } else {
            call.respond(HttpStatusCode.NotFound, "One or both users not found or could not be updated")
        }
    }
}