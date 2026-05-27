package routes

import data.dto.resume.ResumeRequest
import data.dto.resume.ResumeStatusRequest
import data.dto.resume.WorkExperienceRequest
import domain.repository.ResumeRepository
import domain.usecase.resume.AddWorkExperienceUseCase
import domain.usecase.resume.DeleteResumeUseCase
import domain.usecase.resume.DeleteWorkExperienceUseCase
import domain.usecase.resume.UpdateResumeUseCase
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.resumeRoutes(
    resumeRepository: ResumeRepository,
    updateResumeUseCase: UpdateResumeUseCase,
    deleteResumeUseCase: DeleteResumeUseCase,
    addWorkExperienceUseCase: AddWorkExperienceUseCase,
    deleteWorkExperienceUseCase: DeleteWorkExperienceUseCase
) {
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
                val seekerId = UUID.fromString(
                    call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                )
                val request = call.receive<ResumeRequest>()
                val id = resumeRepository.create(seekerId, request)
                call.respond(HttpStatusCode.Created, mapOf("id" to id.toString()))
            }

            put("/{id}") {
                val resumeId = runCatching {
                    UUID.fromString(call.parameters["id"])
                }.getOrElse {
                    call.respond(HttpStatusCode.BadRequest, "Неверный ID")
                    return@put
                }

                val callerId = UUID.fromString(
                    call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                )

                val request = call.receive<ResumeRequest>()
                when (updateResumeUseCase(callerId, resumeId, request)) {
                    is UpdateResumeUseCase.Result.Success -> call.respond(HttpStatusCode.OK)
                    is UpdateResumeUseCase.Result.Forbidden -> call.respond(HttpStatusCode.Forbidden, "Нет доступа")
                    is UpdateResumeUseCase.Result.NotFound -> call.respond(HttpStatusCode.NotFound, "Резюме не найдено")
                }
            }

            patch("/{id}/status") {
                val resumeId = runCatching {
                    UUID.fromString(call.parameters["id"])
                }.getOrElse {
                    call.respond(HttpStatusCode.BadRequest, "Неверный ID")
                    return@patch
                }

                val callerId = UUID.fromString(
                    call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                )

                val ownerId = resumeRepository.getOwnerId(resumeId)
                    ?: return@patch call.respond(HttpStatusCode.NotFound, "Резюме не найдено")
                if (ownerId != callerId) return@patch call.respond(HttpStatusCode.Forbidden, "Нет доступа")

                val request = call.receive<ResumeStatusRequest>()
                resumeRepository.setActive(resumeId, request.isActive)
                call.respond(HttpStatusCode.OK)
            }

            delete("/{id}") {
                val resumeId = runCatching {
                    UUID.fromString(call.parameters["id"])
                }.getOrElse {
                    call.respond(HttpStatusCode.BadRequest, "Неверный ID")
                    return@delete
                }

                val callerId = UUID.fromString(
                    call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                )

                when (deleteResumeUseCase(callerId, resumeId)) {
                    is DeleteResumeUseCase.Result.Success -> call.respond(HttpStatusCode.OK)
                    is DeleteResumeUseCase.Result.Forbidden -> call.respond(HttpStatusCode.Forbidden, "Нет доступа")
                    is DeleteResumeUseCase.Result.NotFound -> call.respond(HttpStatusCode.NotFound, "Резюме не найдено")
                }
            }

            post("/{id}/experience") {
                val resumeId = runCatching {
                    UUID.fromString(call.parameters["id"])
                }.getOrElse {
                    call.respond(HttpStatusCode.BadRequest, "Неверный ID")
                    return@post
                }

                val callerId = UUID.fromString(
                    call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                )

                val request = call.receive<WorkExperienceRequest>()
                when (val result = addWorkExperienceUseCase(callerId, resumeId, request)) {
                    is AddWorkExperienceUseCase.Result.Success -> call.respond(HttpStatusCode.Created, mapOf("id" to result.id.toString()))
                    is AddWorkExperienceUseCase.Result.Forbidden -> call.respond(HttpStatusCode.Forbidden, "Нет доступа")
                    is AddWorkExperienceUseCase.Result.NotFound -> call.respond(HttpStatusCode.NotFound, "Резюме не найдено")
                }
            }

            delete("/{id}/experience/{experienceId}") {
                val resumeId = runCatching {
                    UUID.fromString(call.parameters["id"])
                }.getOrElse {
                    call.respond(HttpStatusCode.BadRequest, "Неверный ID")
                    return@delete
                }

                val experienceId = runCatching {
                    UUID.fromString(call.parameters["experienceId"])
                }.getOrElse {
                    call.respond(HttpStatusCode.BadRequest, "Неверный ID опыта")
                    return@delete
                }

                val callerId = UUID.fromString(
                    call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                )

                when (deleteWorkExperienceUseCase(callerId, resumeId, experienceId)) {
                    is DeleteWorkExperienceUseCase.Result.Success -> call.respond(HttpStatusCode.OK)
                    is DeleteWorkExperienceUseCase.Result.Forbidden -> call.respond(HttpStatusCode.Forbidden, "Нет доступа")
                    is DeleteWorkExperienceUseCase.Result.NotFound -> call.respond(HttpStatusCode.NotFound, "Резюме не найдено")
                }
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
    }
}
