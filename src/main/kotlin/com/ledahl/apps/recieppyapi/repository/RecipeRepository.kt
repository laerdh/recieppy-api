package com.ledahl.apps.recieppyapi.repository

import com.ledahl.apps.recieppyapi.model.Recipe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import kotlin.collections.set

@Repository
class RecipeRepository(@Autowired private val jdbcTemplate: JdbcTemplate) {

    fun isRecipeAvailableToUser(userId: Long, recipeId: Long): Boolean {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("recipe_id", recipeId)
        parameterSource.addValue("user_id", userId)

        val query = """
            SELECT
                COUNT(*)
            FROM (
                SELECT
                    r.id
                FROM
                    recipe r
                    INNER JOIN recipe_list_recipe rlr on r.id = rlr.recipe_id
                    INNER JOIN recipe_list rl on rlr.recipe_list_id = rl.id
                    INNER JOIN location_recipe_list lrl on rl.id = lrl.recipe_list_id
                    INNER JOIN location_user_account lua on lrl.location_id = lua.location_id
                WHERE
                    lua.user_account_id = :user_id
                AND
                    r.id = :recipe_id
                UNION
                SELECT
                    r.id
                FROM
                    recipe r
                    INNER JOIN shared_recipe sr on r.id = sr.recipe_id
                    INNER JOIN user_account ua on sr.recipient_id = ua.id
                WHERE
                    ua.id = :user_id
                AND
                    r.id = :recipe_id
                AND
                    accepted
            ) AS recipes
        """.trimIndent()

        return try {
            namedTemplate.queryForObject(query, parameterSource) { rs, _ ->
                rs.getInt("count") > 0
            } ?: false
        } catch (ex: DataAccessException) {
            false
        }
    }

    fun isRecipeEditableForUser(userId: Long, recipeId: Long): Boolean {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("user_id", userId)
        parameterSource.addValue("recipe_id", recipeId)

        val query = """
            SELECT
                COUNT(*)
            FROM
                recipe r
                INNER JOIN recipe_list rl on r.recipe_list_id = rl.id
                INNER JOIN location_recipe_list lrl on rl.id = lrl.recipe_list_id
                INNER JOIN location_user_account lua on lrl.location_id = lua.location_id
            WHERE
                lua.user_account_id = :user_id
            AND
                r.id = :recipe_id
        """.trimIndent()

        return try {
            namedTemplate.queryForObject(query, parameterSource) { rs, _ ->
                rs.getInt("count") > 0
            } ?: false
        } catch (ex: DataAccessException) {
            false
        }
    }

    fun getRecipesForLocation(locationId: Long): List<Recipe> {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("location_id", locationId)

        val query = """
            SELECT
                r.id, r.title, r.url, r.image_url, r.site, r.comment
            FROM
                recipe r
                INNER JOIN recipe_list rl ON r.recipe_list_id = rl.id
                INNER JOIN location_recipe_list lrl ON rl.id = lrl.recipe_list_id
            WHERE
                lrl.location_id = :location_id
        """.trimIndent()

        return namedTemplate.query(query, parameterSource) { rs, _ ->
            mapToRecipe(rs)
        }
    }

    fun getRecipesForRecipeList(recipeListId: Long): List<Recipe> {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("recipe_list_id", recipeListId)

        return namedTemplate.query("""
            SELECT 
                r.id, r.title, r.url, r.image_url, r.site, r.recipe_list_id, r.comment
            FROM 
                recipe_list rl
                INNER JOIN recipe r ON r.recipe_list_id = rl.id 
            WHERE 
                rl.id = :recipe_list_id
        """.trimIndent(), parameterSource) { rs, _ ->
            mapToRecipe(rs)
        }
    }

    fun getRecipe(id: Long): Recipe? {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("recipe_id", id)

        return try {
            namedTemplate.queryForObject("""
                SELECT 
                    * 
                FROM 
                    recipe r
                WHERE
                    r.id = :recipe_id
            """.trimIndent(), parameterSource) { rs, _ ->
                mapToRecipe(rs)
            }
        } catch (exception: DataAccessException) {
            null
        }
    }

    fun save(recipe: Recipe): Number {
        val simpleJdbcInsert = SimpleJdbcInsert(jdbcTemplate)
                .withTableName("recipe")
                .usingGeneratedKeyColumns("id")

        val parameters = HashMap<String, Any?>()
        parameters["title"] = recipe.title
        parameters["url"] = recipe.url
        parameters["image_url"] = recipe.imageUrl
        parameters["site"] = recipe.site
        parameters["recipe_list_id"] = recipe.recipeListId
        parameters["comment"] = recipe.comment

        return simpleJdbcInsert.executeAndReturnKey(MapSqlParameterSource(parameters))
    }

    fun updateRecipe(recipe: Recipe): Int {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameters = HashMap<String, Any?>()
        parameters["recipe_id"] = recipe.id
        parameters["title"] = recipe.title
        parameters["url"] = recipe.url
        parameters["image_url"] = recipe.imageUrl
        parameters["site"] = recipe.site
        parameters["recipe_list_id"] = recipe.recipeListId
        parameters["comment"] = recipe.comment

        val query = """
            UPDATE
                recipe
            SET
                title = :title, url = :url, image_url = :image_url, site = :site, recipe_list_id = :recipe_list_id, comment = :comment
            WHERE
                id = :recipe_id
        """.trimIndent()

        return try {
            namedTemplate.update(query, parameters)
        } catch (exception: DataAccessException) {
            0
        }
    }

    fun delete(id: Long): Int {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("recipe_id", id)

        return namedTemplate.update("""
            DELETE FROM 
                recipe r
            WHERE 
                r.id = :recipe_id
        """.trimIndent(), parameterSource)
    }

    fun deleteRecipesForRecipeList(recipeListId: Long): Int {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("recipe_list_id", recipeListId)

        return namedTemplate.update("""
            DELETE FROM 
                recipe r
            WHERE 
                r.recipe_list_id = :recipe_list_id
        """.trimIndent(), parameterSource)
    }

    private fun mapToRecipe(rs: ResultSet): Recipe {
        return Recipe(
                id = rs.getLong("id"),
                title = rs.getString("title"),
                url = rs.getString("url"),
                imageUrl = rs.getString("image_url"),
                site = rs.getString("site"),
                comment = rs.getString("comment"),
                recipeListId = rs.getLong("recipe_list_id")
        )
    }
}