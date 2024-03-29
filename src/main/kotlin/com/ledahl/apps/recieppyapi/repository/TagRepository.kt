package com.ledahl.apps.recieppyapi.repository

import com.ledahl.apps.recieppyapi.model.Tag
import com.ledahl.apps.recieppyapi.model.mappers.Mapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.set
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class TagRepository(@Autowired private val jdbcTemplate: JdbcTemplate,
                    @Autowired private val mapper: Mapper<ResultSet, Tag>){

    private val logger = LoggerFactory.getLogger(TagRepository::class.java)

    fun getTags(): List<Tag> {
        return try {
            jdbcTemplate.query("SELECT * FROM tag") { rs, _ ->
                mapper.map(rs)
            }
        } catch (ex: DataAccessException) {
            logger.info("getTags failed", ex)
            emptyList()
        }
    }

    fun getTagsForLocation(locationId: Long): List<Tag> {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("location_id", locationId)

        val query = """
            SELECT DISTINCT
                t.id, t.text
            FROM tag t
            WHERE
                t.location_id = :location_id
        """.trimIndent()

        return try {
            namedTemplate.query(query, parameterSource) { rs, _ ->
                mapper.map(rs)
            }
        } catch (ex: DataAccessException) {
            logger.info("getTagsForLocation (locationId: $locationId) failed", ex)
            emptyList()
        }
    }

    fun getTagsForRecipe(recipeId: Long): List<Tag> {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("recipe_id", recipeId)

        val query = """
            SELECT
                tag_id AS id, text
            FROM
                recipe_tag AS rt
                INNER JOIN tag ON rt.tag_id = tag.id
            WHERE
                rt.recipe_id = :recipe_id
        """.trimIndent()

        return try {
            namedTemplate.query(query, parameterSource) { rs, _ ->
                mapper.map(rs)
            }
        } catch (ex: DataAccessException) {
            logger.info("getTagsForRecipe (recipeId: $recipeId) failed", ex)
            emptyList()
        }
    }

    fun createTag(tag: Tag, locationId: Long): Number {
        val simpleJdbcInsert = SimpleJdbcInsert(jdbcTemplate)
                .withTableName("tag")
                .usingGeneratedKeyColumns("id")
        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("text", tag.text)
        parameterSource.addValue("location_id", locationId)

        return simpleJdbcInsert.executeAndReturnKey(parameterSource)
    }

    fun saveTagsForRecipe(recipeId: Long, tags: List<Long>): Int {
        val simpleJdbcInsert = SimpleJdbcInsert(jdbcTemplate)
                .withTableName("recipe_tag")

        val parameters = tags.map {
            val parameterSource = MapSqlParameterSource()
            parameterSource["recipe_id"] = recipeId
            parameterSource["tag_id"] = it
            parameterSource
        }.toTypedArray()

        return simpleJdbcInsert.executeBatch(*parameters).size
    }

    fun deleteTagsForRecipe(recipeId: Long): Int {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameters = MapSqlParameterSource()
        parameters["recipe_id"] = recipeId

        val query = """
            DELETE FROM
                recipe_tag
            WHERE
                recipe_id = :recipe_id
        """.trimIndent()

        return try {
            namedTemplate.update(query, parameters)
        } catch (ex: DataAccessException) {
            logger.info("deleteTagsForRecipe (recipeId: $recipeId) failed", ex)
            0
        }
    }
}