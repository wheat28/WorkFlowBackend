package routes

import data.dto.application.ApplicationRequest
import domain.repository.ApplicationRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.applicationRoutes(applicationRepository: ApplicationRepository) {
    authenticate("auth-jwt") {

    post("/applications") {
        val seekerId = runCatching {
            UUID.fromString(call.request.headers["SeekerId"])
        }.getOrElse {

            call.respond(HttpStatusCode.BadRequest, "Не указан SeekerId")
            return@post
        }

        val request = call.receive<ApplicationRequest>()
        val id = applicationRepository.create(seekerId, request)
        call.respond(HttpStatusCode.Created, mapOf("id" to id.toString()))
    }

    patch("/applications/{id}/status") {
        val id = runCatching{
            UUID.fromString(call.parameters["id"])
        }.getOrElse {

            call.respond(HttpStatusCode.BadRequest, "Неверный ID")
            return@patch
        }

        val body = call.receive<Map<String, String>>()

        val status = body["status"] ?: run {
            call.respond(HttpStatusCode.BadRequest, "Не указан статус")
            return@patch
        }

        val updated = applicationRepository.updateStatus(id, status)
        if (updated) call.respond(HttpStatusCode.OK)
        else call.respond(HttpStatusCode.NotFound, "Отклик не найден")
    }

    get("/seekers/{id}/applications") {
        val id = runCatching {
            UUID.fromString(call.parameters["id"])
        }.getOrElse {

            call.respond(HttpStatusCode.BadRequest, "Неверный ID")
            return@get
        }
        call.respond(applicationRepository.getBySeekerId(id))
    }

    get("/vacancies/{id}/applications") {
        val id = runCatching {
            UUID.fromString(call.parameters["id"])
        }.getOrElse {

            call.respond(HttpStatusCode.BadRequest, "Неверный ID")
            return@get
        }
        call.respond(applicationRepository.getByVacancyId(id))
    }

    } // authenticate
}
