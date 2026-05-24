package data.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object ResumeTable : Table("resumes") {
    val id = uuid("id").autoGenerate()
    val seekerId = uuid("seeker_id").references(UserTable.id)
    val title = varchar("title", 255)
    val position = varchar("position", 255)
    val salaryExpected = integer("salary_expected").nullable()
    val currency = varchar("currency", 10).default("RUB")
    val city = varchar("city", 100).nullable()
    val employmentType = varchar("employment_type", 20)
    val about = text("about").nullable()
    val isActive = bool("is_active").default(true)
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())

    override val primaryKey = PrimaryKey(id)
}
