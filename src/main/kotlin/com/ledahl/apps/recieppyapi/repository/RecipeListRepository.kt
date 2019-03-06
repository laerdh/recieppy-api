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
            namedTemplate.queryForObject("SELECT * "+
                    "FROM recipe_list " +
                    "INNER JOIN user_recipe_list url on recipe_list.id = url.recipe_list " +
                    "WHERE id = :id AND url.user_id = :user_id ", parameterSource) { rs, _ ->
                mapToRecipeList(rs)
            }
        } catch (exception: DataAccessException) {
            null
        }
    }

    fun getRecipeLists(userId: Long): List<RecipeList> {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("id", userId)

        return namedTemplate.query("SELECT * " +
                "FROM recipe_list " +
                "INNER JOIN user_recipe_list url on recipe_list.id = url.recipe_list " +
                "WHERE url.user_id = :id", parameterSource) { rs, _ ->
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

    fun saveRecipeList(recipeListId: Long, userId: Long) {
        val simpleJdbcInsert = SimpleJdbcInsert(jdbcTemplate)
                .withTableName("user_recipe_list")
                .usingGeneratedKeyColumns("id")

        val parameters = HashMap<String, Any>()
        parameters["user_id"] = userId
        parameters["recipe_list"] = recipeListId

        simpleJdbcInsert.execute(MapSqlParameterSource(parameters))
    }

    fun delete(recipeListId: Long): Int {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("id", recipeListId)

        return namedTemplate.update(
                "DELETE FROM recipe_list " +
                        "WHERE id = :id",
                parameterSource
        )
    }

    fun deleteUserRecipeList(recipeListId: Long, userId: Long): Int {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("recipe_list", recipeListId)
        parameterSource.addValue("user_id", userId)

        return namedTemplate.update(
                "DELETE FROM user_recipe_list " +
                        "WHERE user_id = :user_id " +
                        "AND recipe_list = :recipe_list",
                parameterSource
        )
    }

    fun mapToRecipeList(rs: ResultSet): RecipeList {
        return RecipeList(
                id = rs.getLong("id"),
                name = rs.getString("name"),
                created = rs.getDate("created").toLocalDate()
        )
    }
}