package data.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object VacancyTable : Table("vacancies") {
    val id = uuid("id").autoGenerate()
    val employerId = uuid("employer_id").references(EmployerTable.id)
    val categoryId = integer("category_id").references(CategoryTable.id).nullable()
    val title = varchar("title", 255)
    val description = text("description")
    val salaryFrom = integer("salary_from").nullable()
    val salaryTo = integer("salary_to").nullable()
    val currency = varchar("currency", 10).default("RUB")
    val city = varchar("city", 100).nullable()
    val employmentType = varchar("employment_type", 20)
    val experience = varchar("experience", 30)
    val isActive = bool("is_active").default(true)
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())

    override val primaryKey = PrimaryKey(id)
}
