package com.ledahl.apps.recieppyapi.repository

import com.ledahl.apps.recieppyapi.model.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
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

    fun getTagsForRecipe(id: Long): List<Tag>? {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("recipeId", id)

        return namedTemplate.query(
                "SELECT tag_id AS id, text " +
                        "FROM recipe_tag AS rt " +
                        "INNER JOIN tag ON rt.tag_id = tag.id " +
                        "WHERE rt.recipe_id = :recipeId",
                parameterSource
        ) { rs, _ ->
            mapToTag(rs)
        }
    }

    fun save(tag: Tag): Number {
        val simpleJdbcInsert = SimpleJdbcInsert(jdbcTemplate)
                .withTableName("tag")
                .usingGeneratedKeyColumns("id")
        val parameters = HashMap<String, Any>()
        parameters["text"] = tag.text

        return simpleJdbcInsert.executeAndReturnKey(MapSqlParameterSource(parameters))
    }

    private fun mapToTag(rs: ResultSet): Tag {
        return Tag(
                id = rs.getLong("id"),
                text = rs.getString("text")
        )
    }
}