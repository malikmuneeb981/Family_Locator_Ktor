package com.example

import com.example.data.LocationSharingService
import com.example.data.UserService
import com.example.routes.connectUsers
import com.example.routes.disconnectUser
import com.example.routes.locationWebSocketRoute
import com.example.routes.signUpUser
import com.example.routes.userExists
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    userService: UserService,
    locationService: LocationSharingService
) {
    routing {
        signUpUser(userService = userService)
        userExists(userService = userService)
        connectUsers(userService = userService)
        locationWebSocketRoute(locationService = locationService)
        disconnectUser(locationService = locationService)
    }
}