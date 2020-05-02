package com.ledahl.apps.recieppyapi.repository

import com.ledahl.apps.recieppyapi.model.Tag
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
class TagRepository(@Autowired private val jdbcTemplate: JdbcTemplate) {

    fun getTags(): List<Tag> {
        return jdbcTemplate.query("SELECT * FROM tag") { rs, _ ->
            mapToTag(rs)
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
                mapToTag(rs)
            }
        } catch (ex: DataAccessException) {
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

        return namedTemplate.query(query, parameterSource) { rs, _ ->
            mapToTag(rs)
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

        return namedTemplate.update(query, parameters)
    }

    private fun mapToTag(rs: ResultSet): Tag {
        return Tag(
                id = rs.getLong("id"),
                text = rs.getString("text")
        )
    }
}