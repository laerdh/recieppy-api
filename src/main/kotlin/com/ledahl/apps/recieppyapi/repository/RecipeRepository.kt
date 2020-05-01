package com.ledahl.apps.recieppyapi.repository

import com.ledahl.apps.recieppyapi.model.Recipe
import com.ledahl.apps.recieppyapi.model.input.RecipeInput
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Repository
import java.sql.ResultSet

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
                INNER JOIN recipe_list_recipe rlr ON r.id = rlr.recipe_id
                INNER JOIN recipe_list rl ON rlr.recipe_list_id = rl.id
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

    fun getRecipesForUser(userId: Long, locationId: Long): List<Recipe> {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("user_id", userId)
        parameterSource.addValue("location_id", locationId)

        val query = """
            SELECT
                r.id, rlr.recipe_list_id, r.title, r.url, r.image_url, r.site, r.comment, r.created, concat(u.first_name, ' ', u.last_name) AS created_by, rlr.recipe_list_id
            FROM
                recipe r
                INNER JOIN recipe_list_recipe rlr ON r.id = rlr.recipe_id
                INNER JOIN recipe_list rl ON rlr.recipe_list_id = rl.id
                INNER JOIN location_recipe_list lrl on rl.id = lrl.recipe_list_id
                INNER JOIN location_user_account lua on lrl.location_id = lua.location_id
                INNER JOIN user_account u on lua.user_account_id = u.id
            WHERE
                u.id = :user_id
            AND
                lua.location_id = :location_id
            UNION
            SELECT
                r.id, r.title, r.url, r.image_url, r.site, r.comment, r.created, concat(r_owner.first_name, ' ', r_owner.last_name) AS created_by
            FROM
                recipe r
                INNER JOIN shared_recipe sr on r.id = sr.recipe_id
                INNER JOIN user_account u on sr.recipient_id = u.id
                LEFT JOIN user_account r_owner ON sr.sharer_id = r_owner.id
            WHERE
                u.id = :user_id
            AND
                sr.accepted
        """.trimIndent()

        return try {
            namedTemplate.query(query, parameterSource) { rs, _ ->
                mapToRecipe(rs)
            }
        } catch (ex: DataAccessException) {
            emptyList()
        }
    }

    fun getRecipesForRecipeList(recipeListId: Long): List<Recipe> {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("recipe_list_id", recipeListId)

        val query = """
            SELECT
                r.id, rlr.recipe_list_id, r.title, r.url, r.image_url, r.site, r.comment, r.created, concat(r_owner.first_name, ' ', r_owner.last_name) AS created_by
            FROM
                recipe r
                INNER JOIN recipe_list_recipe rlr on r.id = rlr.recipe_id
                LEFT JOIN user_account r_owner ON r.owner_id = r_owner.id
            WHERE
                rlr.recipe_list_id = :recipe_list_id
        """.trimIndent()

        return try {
            namedTemplate.query(query, parameterSource) { rs, _ ->
                mapToRecipe(rs)
            }
        } catch (ex: DataAccessException) {
            emptyList()
        }
    }

    fun getRecipe(id: Long): Recipe? {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("recipe_id", id)

        return try {
            namedTemplate.queryForObject("""
                SELECT 
                    r.id, rlr.recipe_list_id, r.title, r.url, r.image_url, r.site, r.comment, r.created, concat(r_owner.first_name, ' ', r_owner.last_name) AS created_by
                FROM 
                    recipe r
                    INNER JOIN recipe_list_recipe rlr ON r.id = rlr.recipe_id
                    LEFT JOIN user_account r_owner ON r.owner_id = r_owner.id
                WHERE
                    r.id = :recipe_id
            """.trimIndent(), parameterSource) { rs, _ ->
                mapToRecipe(rs)
            }
        } catch (exception: DataAccessException) {
            null
        }
    }

    fun createRecipe(userId: Long, recipe: RecipeInput): Number {
        val simpleJdbcInsert = SimpleJdbcInsert(jdbcTemplate)
                .withTableName("recipe")
                .usingGeneratedKeyColumns("id")
                .usingColumns("owner_id", "title", "url", "image_url", "site", "comment")

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("owner_id", userId)
        parameterSource.addValue("title", recipe.title)
        parameterSource.addValue("url", recipe.url)
        parameterSource.addValue("image_url", recipe.imageUrl)
        parameterSource.addValue("site", recipe.site)
        parameterSource.addValue("comment", recipe.comment)

        return simpleJdbcInsert.executeAndReturnKey(parameterSource)
    }

    fun addRecipeToRecipeList(recipeId: Long, recipeListId: Long): Number {
        val simpleJdbcInsert = SimpleJdbcInsert(jdbcTemplate)
                .withTableName("recipe_list_recipe")

        val parameters = MapSqlParameterSource()
        parameters.addValue("recipe_id", recipeId)
        parameters.addValue("recipe_list_id", recipeListId)

        return simpleJdbcInsert.execute(parameters)
    }

    fun deleteRecipeFromRecipeList(recipeId: Long, recipeListId: Long): Int {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("recipe_id", recipeId)
        parameterSource.addValue("recipe_list_id", recipeListId)

        val query = """
            DELETE FROM
                recipe_list_recipe rlr
            WHERE
                rlr.recipe_id = :recipe_id
            AND
                rlr.recipe_list_id = :recipe_list_id
        """.trimIndent()

        return try {
            return namedTemplate.update(query, parameterSource)
        } catch (ex: DataAccessException) {
            0
        }
    }

    fun updateRecipe(recipe: Recipe): Int {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("recipe_id", recipe.id)
        parameterSource.addValue("title", recipe.title)
        parameterSource.addValue("url", recipe.url)
        parameterSource.addValue("image_url", recipe.imageUrl)
        parameterSource.addValue("site", recipe.site)
        parameterSource.addValue("comment", recipe.comment)

        val query = """
            UPDATE
                recipe
            SET
                title = :title, url = :url, image_url = :image_url, site = :site, comment = :comment
            WHERE
                id = :recipe_id
        """.trimIndent()

        return try {
            namedTemplate.update(query, parameterSource)
        } catch (exception: DataAccessException) {
            0
        }
    }

    fun deleteRecipe(recipeId: Long): Int {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("recipe_id", recipeId)

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
                recipeListId = rs.getLong("recipe_list_id"),
                title = rs.getString("title"),
                url = rs.getString("url"),
                imageUrl = rs.getString("image_url"),
                site = rs.getString("site"),
                comment = rs.getString("comment"),
                created = rs.getTimestamp("created").toLocalDateTime(),
                createdBy = rs.getString("created_by")
        )
    }
}