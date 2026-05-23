package data.database

import org.jetbrains.exposed.sql.Table

object ResumeSkillTable : Table("resume_skills") {
    val resumeId = uuid("resume_id").references(ResumeTable.id)
    val skillId = integer("skill_id").references(SkillTable.id)

    override val primaryKey = PrimaryKey(resumeId, skillId)
}
