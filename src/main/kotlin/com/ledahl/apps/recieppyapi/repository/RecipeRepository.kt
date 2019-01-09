package com.ledahl.apps.recieppyapi.repository

import com.ledahl.apps.recieppyapi.model.Recipe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class RecipeRepository(@Autowired private val jdbcTemplate: JdbcTemplate) {
    fun getRecipes(): List<Recipe> {
        return jdbcTemplate.query("SELECT * FROM recipe") { rs, _ ->
            Recipe(
                    id = rs.getLong("id"),
                    title = rs.getString("title"),
                    url = rs.getString("url"),
                    imageUrl = rs.getString("image_url"),
                    site = rs.getString("site")
            )
        }
    }

    fun getRecipe(id: Long): Recipe? {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("id", id)

        try {
            return namedTemplate.queryForObject("SELECT * FROM recipe WHERE id = :id", parameterSource) { rs, _ ->
                Recipe(
                        id = rs.getLong("id"),
                        title = rs.getString("title"),
                        url = rs.getString("url"),
                        imageUrl = rs.getString("image_url"),
                        site = rs.getString("site")
                )
            }
        } catch (exception: DataAccessException) {
            return null
        }
    }
}