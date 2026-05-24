package routes

import data.dto.resume.ResumeRequest
import data.dto.resume.WorkExperienceRequest
import data.repository.ResumeRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.resumeRoutes(resumeRepository: ResumeRepository) {
    authenticate("auth-jwt") {
    route("/resumes") {

        get("/{id}") {
            val id = runCatching {
                UUID.fromString(call.parameters["id"])
            }.getOrElse {

                call.respond(HttpStatusCode.BadRequest, "Неверный ID")
                return@get
            }

            val resume = resumeRepository.getById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, "Резюме не найдено")
            call.respond(resume)
        }

        post {
            val seekerId = runCatching {
                UUID.fromString(call.request.headers["SeekerId"])
            }.getOrElse {

                call.respond(HttpStatusCode.BadRequest, "Не указан SeekerId")
                return@post
            }

            val request = call.receive<ResumeRequest>()
            val id = resumeRepository.create(seekerId, request)
            call.respond(HttpStatusCode.Created, mapOf("id" to id.toString()))
        }

        put("/{id}") {
            val id = runCatching {
                UUID.fromString(call.parameters["id"])
            }.getOrElse {

                call.respond(HttpStatusCode.BadRequest, "Неверный ID")
                return@put
            }

            val request = call.receive<ResumeRequest>()
            val updated = resumeRepository.update(id, request)
            if (updated) call.respond(HttpStatusCode.OK)
            else call.respond(HttpStatusCode.NotFound, "Резюме не найдено")
        }

        delete("/{id}") {
            val id = runCatching {
                UUID.fromString(call.parameters["id"])
            }.getOrElse {

                call.respond(HttpStatusCode.BadRequest, "Неверный ID")
                return@delete
            }

            val deleted = resumeRepository.delete(id)
            if (deleted) call.respond(HttpStatusCode.OK)
            else call.respond(HttpStatusCode.NotFound, "Резюме не найдено")
        }

        post("/{id}/experience") {
            val id = runCatching {
                UUID.fromString(call.parameters["id"])
            }.getOrElse {

                call.respond(HttpStatusCode.BadRequest, "Неверный ID")
                return@post
            }

            val request = call.receive<WorkExperienceRequest>()
            val experienceId = resumeRepository.addWorkExperience(id, request)
            call.respond(HttpStatusCode.Created, mapOf("id" to experienceId.toString()))
        }

        delete("/{id}/experience/{experienceId}") {
            val experienceId = runCatching {
                UUID.fromString(call.parameters["experienceId"])
            }.getOrElse {

                call.respond(HttpStatusCode.BadRequest, "Неверный ID")
                return@delete
            }

            val deleted = resumeRepository.deleteWorkExperience(experienceId)
            if (deleted) call.respond(HttpStatusCode.OK)
            else call.respond(HttpStatusCode.NotFound, "Опыт работы не найден")
        }
    }

    get("/seekers/{id}/resumes") {
        val id = runCatching {
            UUID.fromString(call.parameters["id"])
        }.getOrElse {

            call.respond(HttpStatusCode.BadRequest, "Неверный ID")
            return@get
        }
        call.respond(resumeRepository.getBySeekerID(id))
    }
    } // authenticate
}
