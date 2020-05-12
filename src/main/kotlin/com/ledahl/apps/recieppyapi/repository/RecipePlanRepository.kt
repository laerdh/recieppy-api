package com.ledahl.apps.recieppyapi.repository

import com.ledahl.apps.recieppyapi.model.RecipePlanEvent
import com.ledahl.apps.recieppyapi.model.input.RecipePlanEventInput
import org.slf4j.LoggerFactory
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

    private val logger = LoggerFactory.getLogger(RecipePlanRepository::class.java)

    fun getRecipePlanEventsForWeek(locationId: Long, weekNumber: Int): List<RecipePlanEvent> {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameters = MapSqlParameterSource()
        parameters["location_id"] = locationId
        parameters["week_number"] = weekNumber

        val query = """
            SELECT
                lrp.date, lrp.recipe_id
            FROM
                location_recipe_plan lrp
                INNER JOIN location l ON l.id = lrp.location_id
            WHERE
                EXTRACT(WEEK FROM lrp.date) = :week_number
                AND lrp.location_id = :location_id
            ORDER BY
                lrp.date
        """.trimIndent()

        return try {
            namedTemplate.query(query, parameters) { rs, _ ->
                RecipePlanEvent(
                        date = rs.getDate("date").toLocalDate(),
                        recipeId = rs.getLong("recipe_id")
                )
            }
        } catch (ex: DataAccessException) {
            logger.info("getRecipePlanEventsForWeek (locationId: $locationId, weekNumber: $weekNumber) failed", ex)
            emptyList()
        }
    }

    fun createRecipePlanEvent(locationId: Long, recipePlanEvent: RecipePlanEventInput): Boolean {
        val simpleJdbcInsert = SimpleJdbcInsert(jdbcTemplate)
                .withTableName("location_recipe_plan")

        val parameters = MapSqlParameterSource()
        parameters["location_id"] = locationId
        parameters["date"] = recipePlanEvent.currentDate
        parameters["recipe_id"] = recipePlanEvent.recipeId

        return simpleJdbcInsert.execute(parameters) > 0
    }

    fun updateRecipePlanEvent(locationId: Long,
                              recipePlanEvents: Map<Long, List<LocalDate>>): Boolean {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameterList = Array<SqlParameterSource>(recipePlanEvents.size) {
            val parameters = MapSqlParameterSource()
            val recipeId = recipePlanEvents.keys.elementAt(it)
            parameters["location_id"] = locationId
            parameters["recipe_id"] = recipeId
            parameters["old_date"] = recipePlanEvents[recipeId]?.first() ?: LocalDate.now()
            parameters["new_date"] = recipePlanEvents[recipeId]?.last() ?: LocalDate.now()
            parameters
        }

        val query = """
            UPDATE
                location_recipe_plan
            SET
                date = :new_date
            WHERE
                recipe_id = :recipe_id
                AND location_id = :location_id
                AND date = :old_date
        """.trimIndent()

        return try {
            val updatedRows = namedTemplate.batchUpdate(query, parameterList)
            return updatedRows.size == recipePlanEvents.size
        } catch (ex: DataAccessException) {
            logger.info("updateRecipePlanEvent (locationId: $locationId, recipePlanEvents: $recipePlanEvents) failed", ex)
            false
        }
    }

    fun deleteRecipePlanEvent(locationId: Long, recipePlanEvent: RecipePlanEventInput): Boolean {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameters = MapSqlParameterSource()
        parameters["recipe_id"] = recipePlanEvent.recipeId
        parameters["location_id"] = locationId
        parameters["date"] = Date.valueOf(recipePlanEvent.currentDate)

        val query = """
            DELETE FROM
                location_recipe_plan
            WHERE
                recipe_id = :recipe_id
                AND location_id = :location_id
                AND date = :date
        """.trimIndent()

        return try {
            val deleted = namedTemplate.update(query, parameters)
            return deleted > 0
        } catch (ex: DataAccessException) {
            logger.info("deleteRecipePlanEvent (locationId: $locationId, recipeId: ${recipePlanEvent.recipeId}, date: ${recipePlanEvent.currentDate}) failed", ex)
            false
        }
    }

    fun deleteRecipeFromRecipePlanEvents(locationId: Long, recipeId: Long): Boolean {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("recipe_id", recipeId)
        parameterSource.addValue("location_id", locationId)

        val query = """
            DELETE FROM
                location_recipe_plan lrp
            WHERE 
                lrp.recipe_id = :recipe_id
                AND lrp.location_id = :location_id
                
        """.trimIndent()
        return try {
            val deleted = namedTemplate.update(query, parameterSource)
            return deleted > 0
        } catch (ex: DataAccessException) {
            logger.info("deleteRecipeFromRecipePlanEvents (locationId: $locationId) failed", ex)
            false
        }
    }
}
