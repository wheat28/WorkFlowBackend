package data.database

import org.jetbrains.exposed.sql.Table

object VacancySkillTable : Table("vacancy_skills") {
    val vacancyId = uuid("vacancy_id").references(VacancyTable.id)
    val skillId = integer("skill_id").references(SkillTable.id)

    override val primaryKey = PrimaryKey(vacancyId, skillId)
}
