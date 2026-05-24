package routes

import data.dto.vacancy.VacancyRequest
import data.repository.VacancyRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.vacancyRoutes(vacancyRepository: VacancyRepository) {
    route("/vacancies") {

        get {
            call.respond(vacancyRepository.getAll())
        }

        get("/{id}") {
            val id = runCatching {
                UUID.fromString(call.parameters["id"])
            }.getOrElse {

                call.respond(HttpStatusCode.BadRequest, "Неверный ID")
                return@get
            }

            val vacancy = vacancyRepository.getById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, "Вакансия не найдена")
            call.respond(vacancy)
        }

        post {
            val employerId = runCatching {
                UUID.fromString(call.request.headers["EmployerId"])
            }.getOrElse {

                call.respond(HttpStatusCode.BadRequest, "Не указан EmployerId")
                return@post
            }

            val request = call.receive<VacancyRequest>()
            val id = vacancyRepository.create(employerId, request)
            call.respond(HttpStatusCode.Created, mapOf("id" to id.toString()))
        }

        put("/{id}") {
            val id = runCatching {
                UUID.fromString(call.parameters["id"])
            }.getOrElse {

                call.respond(HttpStatusCode.BadRequest, "Неверный ID")
                return@put
            }

            val request = call.receive<VacancyRequest>()
            val updated = vacancyRepository.update(id, request)
            if (updated) call.respond(HttpStatusCode.OK)
            else call.respond(HttpStatusCode.NotFound, "Вакансия не найдена")
        }

        delete("/{id}") {
            val id = runCatching {
                UUID.fromString(call.parameters["id"])
            }.getOrElse {

                call.respond(HttpStatusCode.BadRequest, "Неверный ID")
                return@delete
            }

            val deleted = vacancyRepository.delete(id)
            if (deleted) call.respond(HttpStatusCode.OK)
            else call.respond(HttpStatusCode.NotFound, "Вакансия не найдена")
        }

        get("/{id}/applications") {

            val id = runCatching {
                UUID.fromString(call.parameters["id"])
            }.getOrElse {

                call.respond(HttpStatusCode.BadRequest, "Неверный ID")
                return@get
            }
            call.respond(vacancyRepository.getByEmployerId(id))
        }
    }
}
