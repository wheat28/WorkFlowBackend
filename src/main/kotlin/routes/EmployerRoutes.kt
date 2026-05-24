package routes

import data.dto.auth.LoginRequest
import data.dto.employer.EmployerRegisterRequest
import data.repository.EmployerRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.employerRoutes(employerRepository: EmployerRepository) {
    route("/employers") {

        post("/register") {
            val request = call.receive<EmployerRegisterRequest>()
            if (employerRepository.emailExists(request.email)) {
                call.respond(HttpStatusCode.Conflict, "Email уже занят")
                return@post
            }
            val id = employerRepository.create(request)
            call.respond(HttpStatusCode.Created, mapOf("id" to id.toString()))
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val hash = employerRepository.getPasswordHash(request.email)
            if (hash == null || hash != request.password) {
                call.respond(HttpStatusCode.Unauthorized, "Неверный email или пароль")
                return@post
            }
            val employer = employerRepository.findByEmail(request.email)!!
            call.respond(employer)
        }

        get("/{id}") {
            val id = runCatching { UUID.fromString(call.parameters["id"]) }.getOrElse {
                call.respond(HttpStatusCode.BadRequest, "Неверный ID")
                return@get
            }
            val employer = employerRepository.findById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, "Работодатель не найден")
            call.respond(employer)
        }
    }
}
