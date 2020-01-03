package com.ledahl.apps.recieppyapi.repository

import com.ledahl.apps.recieppyapi.model.RecipeEvent
import com.ledahl.apps.recieppyapi.model.input.RecipeEventInput
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.jdbc.core.namedparam.set
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Repository
import java.sql.Date
import java.time.LocalDate

@Repository
class RecipePlanRepository(@Autowired private val jdbcTemplate: JdbcTemplate) {
    fun getRecipeEventsForWeek(locationId: Long, weekNumber: Int): List<RecipeEvent> {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameters = MapSqlParameterSource()
        parameters["location_id"] = locationId
        parameters["week_number"] = weekNumber

        return try {
            namedTemplate.query("""
                SELECT lrp.date, lrp.recipe_id
                FROM location_recipe_plan lrp
                INNER JOIN location l ON l.id = lrp.location_id
                WHERE EXTRACT(WEEK FROM lrp.date) = :week_number
                ORDER BY lrp.date
            """.trimIndent(), parameters) { rs, _ ->
                RecipeEvent(
                        date = rs.getDate("date").toLocalDate(),
                        recipeId = rs.getLong("recipe_id")
                )
            }
        } catch (exception: DataAccessException) {
            emptyList()
        }
    }

    fun createRecipeEvent(locationId: Long, recipeEvent: RecipeEventInput): Boolean {
        val simpleJdbcInsert = SimpleJdbcInsert(jdbcTemplate)
                .withTableName("location_recipe_plan")

        val parameters = MapSqlParameterSource()
        parameters["location_id"] = locationId
        parameters["date"] = recipeEvent.date
        parameters["recipe_id"] = recipeEvent.recipeId

        return simpleJdbcInsert.execute(parameters) > 0
    }

    fun updateRecipeEvent(locationId: Long,
                          recipeEvents: Map<Long, List<LocalDate>>): Boolean {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameterList = Array<SqlParameterSource>(recipeEvents.size) {
            val parameters = MapSqlParameterSource()
            val recipeId = recipeEvents.keys.elementAt(it)
            parameters["location_id"] = locationId
            parameters["recipe_id"] = recipeId
            parameters["old_date"] = recipeEvents[recipeId]!!.first()
            parameters["new_date"] = recipeEvents[recipeId]!!.last()
            parameters
        }

        return try {
            val updatedRows = namedTemplate.batchUpdate("""
                UPDATE location_recipe_plan
                SET date = :new_date
                WHERE recipe_id = :recipe_id AND location_id = :location_id AND date = :old_date
            """.trimIndent(), parameterList)

            return updatedRows.size == recipeEvents.size
        } catch (exception: DataAccessException) {
            false
        }
    }

    fun deleteRecipeEvent(locationId: Long, recipeEvent: RecipeEventInput): Boolean {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameters = MapSqlParameterSource()
        parameters["recipe_id"] = recipeEvent.recipeId
        parameters["location_id"] = locationId
        parameters["date"] = Date.valueOf(recipeEvent.date)

        return try {
            val deleted = namedTemplate.update("""
                DELETE FROM location_recipe_plan
                WHERE recipe_id = :recipe_id AND location_id = :location_id AND date = :date
            """.trimIndent(), parameters)
            return deleted > 0
        } catch (exception: DataAccessException) {
            false
        }
    }
}