package routes

import data.dto.user.UserRegisterRequest
import data.dto.user.UserUpdateRequest
import domain.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.userRoutes(userRepository: UserRepository) {
    route("/seekers") {

        post("/register") {
            val request = call.receive<UserRegisterRequest>()
            if (userRepository.emailExists(request.email)) {
                call.respond(HttpStatusCode.Conflict, "Email уже занят")
                return@post
            }

            val id = userRepository.create(request)
            call.respond(HttpStatusCode.Created, mapOf("id" to id.toString()))
        }

        authenticate("auth-jwt") {
            get("/{id}") {
                val id = runCatching {
                    UUID.fromString(call.parameters["id"])
                }.getOrElse {
                    call.respond(HttpStatusCode.BadRequest, "Неверный ID")
                    return@get
                }

                val user = userRepository.findById(id)
                    ?: return@get call.respond(HttpStatusCode.NotFound, "Пользователь не найден")
                call.respond(user)
            }

            put("/{id}") {
                val id = runCatching {
                    UUID.fromString(call.parameters["id"])
                }.getOrElse {
                    call.respond(HttpStatusCode.BadRequest, "Неверный ID")
                    return@put
                }

                val callerId = call.principal<JWTPrincipal>()
                    ?.payload?.getClaim("userId")?.asString()
                if (callerId != id.toString()) {
                    call.respond(HttpStatusCode.Forbidden, "Нет доступа")
                    return@put
                }

                val request = call.receive<UserUpdateRequest>()
                val updated = userRepository.update(id, request)
                if (updated) call.respond(HttpStatusCode.OK)
                else call.respond(HttpStatusCode.NotFound, "Пользователь не найден")
            }
        }
    }
}
