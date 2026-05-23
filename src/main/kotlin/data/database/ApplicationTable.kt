package data.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object ApplicationTable : Table("applications") {
    val id = uuid("id").autoGenerate()
    val seekerId = uuid("seeker_id").references(UserTable.id)
    val vacancyId = uuid("vacancy_id").references(VacancyTable.id)
    val resumeId = uuid("resume_id").references(ResumeTable.id)
    val status = varchar("status", 20).default("PENDING")
    val coverLetter = text("cover_letter").nullable()
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())

    override val primaryKey = PrimaryKey(id)
}
