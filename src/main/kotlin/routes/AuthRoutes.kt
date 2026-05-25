package routes

import data.dto.auth.AuthResponse
import data.dto.auth.LoginRequest
import domain.usecase.auth.LoginUseCase
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(loginUseCase: LoginUseCase) {
    post("/auth/login") {
        val request = call.receive<LoginRequest>()
        when (val result = loginUseCase(request.email, request.password)) {
            is LoginUseCase.Result.Success ->
                call.respond(AuthResponse(token = result.token, userType = result.userType, userId = result.userId))
            is LoginUseCase.Result.InvalidCredentials ->
                call.respond(HttpStatusCode.Unauthorized, "Неверный email или пароль")
        }
    }
}