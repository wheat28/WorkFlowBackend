package routes

import data.dto.employer.EmployerRegisterRequest
import data.dto.employer.EmployerUpdateRequest
import domain.repository.EmployerRepository
import domain.repository.VacancyRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.employerRoutes(
    employerRepository: EmployerRepository,
    vacancyRepository: VacancyRepository
) {
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

        authenticate("auth-jwt") {
            get("/{id}") {
                val id = runCatching {
                    UUID.fromString(call.parameters["id"])
                }.getOrElse {
                    call.respond(HttpStatusCode.BadRequest, "Неверный ID")
                    return@get
                }

                val employer = employerRepository.findById(id)
                    ?: return@get call.respond(HttpStatusCode.NotFound, "Работодатель не найден")
                call.respond(employer)
            }

            get("/{id}/vacancies") {
                val id = runCatching {
                    UUID.fromString(call.parameters["id"])
                }.getOrElse {
                    call.respond(HttpStatusCode.BadRequest, "Неверный ID")
                    return@get
                }

                call.respond(vacancyRepository.getByEmployerId(id))
            }

            put("/{id}") {
                val id = runCatching {
                    UUID.fromString(call.parameters["id"])
                }.getOrElse {
                    call.respond(HttpStatusCode.BadRequest, "Неверный ID")
                    return@put
                }
                val callerId = UUID.fromString(
                    call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                )
                if (callerId != id) return@put call.respond(HttpStatusCode.Forbidden, "Нет доступа")
                val request = call.receive<EmployerUpdateRequest>()
                if (employerRepository.update(id, request)) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Работодатель не найден")
                }
            }

            get("/{id}/stats") {
                val id = runCatching {
                    UUID.fromString(call.parameters["id"])
                }.getOrElse {
                    call.respond(HttpStatusCode.BadRequest, "Неверный ID")
                    return@get
                }

                call.respond(vacancyRepository.getEmployerStats(id))
            }
        }
    }
}
