package com.ledahl.apps.recieppyapi.repository

import com.ledahl.apps.recieppyapi.model.RecipeList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.*

@Repository
class RecipeListRepository(@Autowired private val jdbcTemplate: JdbcTemplate) {
    fun getRecipeList(id: Long, userId: Long): RecipeList? {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("id", id)
        parameterSource.addValue("user_id", userId)

        return try {
            namedTemplate.queryForObject("""
                SELECT *
                FROM recipe_list
                INNER JOIN user_recipe_list url on recipe_list.id = url.recipe_list
                WHERE id = :id AND url.user_id = :user_id
            """.trimIndent(), parameterSource) { rs, _ ->
                mapToRecipeList(rs)
            }
        } catch (exception: DataAccessException) {
            null
        }
    }

    fun getRecipeLists(userId: Long, locationId: Int): List<RecipeList> {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("user_id", userId)
        parameterSource.addValue("location_id", locationId)

        val query = """
            SELECT
            	rl.id, rl.name, rl.created, lrc.location_id
            FROM
            	recipe_list rl
            	INNER JOIN location_recipe_list lrc ON lrc.recipe_list_id = rl.id
            	INNER JOIN location_user_account lua ON lua.location_id = lrc.location_id
            WHERE
            	lua.user_account_id = :user_id
            AND
            	lrc.location_id = :location_id
        """.trimIndent()

        return namedTemplate.query(query, parameterSource) { rs, _ ->
            mapToRecipeList(rs)
        }
    }

    fun save(recipeList: RecipeList): Number {
        val simpleJdbcInsert = SimpleJdbcInsert(jdbcTemplate)
                .withTableName("recipe_list")
                .usingGeneratedKeyColumns("id")

        val parameters = HashMap<String, Any>()
        parameters["name"] = recipeList.name
        parameters["created"] = Date()

        return simpleJdbcInsert.executeAndReturnKey(MapSqlParameterSource(parameters))
    }

    fun connectRecipeListAndLocation(recipeListId: Long, locationId: Long) {
        val simpleJdbcInsert = SimpleJdbcInsert(jdbcTemplate)
                .withTableName("location_recipe_list")
                .usingGeneratedKeyColumns("id")

        val parameters = HashMap<String, Any>()
        parameters["location_id"] = locationId
        parameters["recipe_list_id"] = recipeListId

        simpleJdbcInsert.execute(MapSqlParameterSource(parameters))
    }

    fun delete(recipeListId: Long): Int {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("id", recipeListId)

        return namedTemplate.update("""
            DELETE FROM recipe_list
            WHERE id = :id
        """.trimIndent(), parameterSource)
    }

    fun deleteUserRecipeList(recipeListId: Long, userId: Long): Int {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("recipe_list", recipeListId)
        parameterSource.addValue("user_id", userId)

        return namedTemplate.update("""
            DELETE FROM user_recipe_list
            WHERE user_id = :user_id
            AND recipe_list = :recipe_list
        """.trimIndent(), parameterSource)
    }

    fun renameRecipeList(recipeListId: Long, newName: String): Int {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("recipe_list_id", recipeListId)
        parameterSource.addValue("new_name", newName)

        return namedTemplate.update("""
                UPDATE recipe_list
                SET name = :new_name
                WHERE id = :recipe_list_id
            """.trimIndent(), parameterSource)
    }

    private fun mapToRecipeList(rs: ResultSet): RecipeList {
        return RecipeList(
                id = rs.getLong("id"),
                name = rs.getString("name"),
                created = rs.getDate("created").toLocalDate()
        )
    }
}