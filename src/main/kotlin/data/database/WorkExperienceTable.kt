package data.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date

object WorkExperienceTable : Table("work_experiences") {
    val id = uuid("id").autoGenerate()
    val resumeId = uuid("resume_id").references(ResumeTable.id)
    val companyName = varchar("company_name", 255)
    val position = varchar("position", 255)
    val startDate = date("start_date")
    val endDate = date("end_date").nullable()
    val description = text("description").nullable()

    override val primaryKey = PrimaryKey(id)
}
