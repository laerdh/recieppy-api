package com.ledahl.apps.recieppyapi.repository

import com.ledahl.apps.recieppyapi.model.Recipe
import com.ledahl.apps.recieppyapi.model.input.RecipeInput
import com.ledahl.apps.recieppyapi.model.mappers.Mapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class RecipeRepository(@Autowired private val jdbcTemplate: JdbcTemplate,
                       @Autowired private val mapper: Mapper<ResultSet, Recipe>) {

    private val logger = LoggerFactory.getLogger(RecipeRepository::class.java)

    fun isRecipeAvailableToUser(userId: Long, recipeId: Long): Boolean {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("user_id", userId)
        parameterSource.addValue("recipe_id", recipeId)

        val query = """
            SELECT
                COUNT(*) > 0 AS accessible
            FROM (
                SELECT
                    r.id
                FROM
                    recipe r
                    INNER JOIN recipe_list_recipe rlr on r.id = rlr.recipe_id
                    INNER JOIN recipe_list rl on rlr.recipe_list_id = rl.id
                    INNER JOIN location_recipe_list lrl ON rl.id = lrl.recipe_list_id
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
                    INNER JOIN recipe_list_recipe rlr ON r.id = rlr.recipe_id
                WHERE
                    (r.id IN (SELECT recipe_id FROM shared_recipe sr WHERE sr.recipient_id = :user_id AND sr.accepted)
                OR
                    rlr.recipe_list_id IN (SELECT recipe_list_id FROM shared_recipe_list srl WHERE srl.recipient_id = :user_id AND srl.accepted))
                AND
                    r.id = :recipe_id
            ) AS recipes
        """.trimIndent()

        return try {
            namedTemplate.queryForObject(query, parameterSource) { rs, _ ->
                rs.getBoolean("accessible")
            } ?: false
        } catch (ex: DataAccessException) {
            logger.info("isRecipeAvailableToUser (userId: $userId, recipeId: $recipeId) failed", ex)
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
            logger.info("isRecipeEditableForUser (userId: $userId, recipeId: $recipeId) failed", ex)
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
                r.id, rlr.recipe_list_id, r.title, r.url, r.image_url, r.site, r.comment, r.created, r.ingredients, concat(owner.first_name, ' ', owner.last_name) AS created_by, rlr.recipe_list_id, false AS shared
            FROM
                recipe r
                INNER JOIN recipe_list_recipe rlr ON r.id = rlr.recipe_id
                INNER JOIN recipe_list rl ON rlr.recipe_list_id = rl.id
                INNER JOIN location_recipe_list lrl ON rl.id = lrl.recipe_list_id
                INNER JOIN location_user_account lua ON lrl.location_id = lua.location_id
                LEFT JOIN user_account owner ON r.owner_id = owner.id
            WHERE
                lua.user_account_id = :user_id
            AND
                lua.location_id = :location_id
            UNION
            SELECT
                r.id, rlr.recipe_list_id, r.title, r.url, r.image_url, r.site, r.comment, r.created, r.ingredients, concat(owner.first_name, ' ', owner.last_name) AS created_by, rlr.recipe_list_id, true AS shared
            FROM
                recipe r
                INNER JOIN recipe_list_recipe rlr ON r.id = rlr.recipe_id
                LEFT JOIN user_account owner ON r.owner_id = owner.id
            WHERE
                (r.id IN (SELECT recipe_id FROM shared_recipe sr WHERE sr.recipient_id = :user_id AND sr.accepted)
            OR
                rlr.recipe_list_id IN (SELECT recipe_list_id FROM shared_recipe_list srl WHERE srl.recipient_id = :user_id AND srl.accepted))
        """.trimIndent()

        return try {
            namedTemplate.query(query, parameterSource) { rs, _ ->
                mapper.map(rs)
            }
        } catch (ex: DataAccessException) {
            logger.info("getRecipesForUser (userId: $userId, locationId: $locationId) failed", ex)
            emptyList()
        }
    }

    fun getRecipesForRecipeList(userId: Long, recipeListId: Long): List<Recipe> {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("user_id", userId)
        parameterSource.addValue("recipe_list_id", recipeListId)

        val query = """
            SELECT
                r.id, rlr.recipe_list_id, r.title, r.url, r.image_url, r.site, r.comment, r.created, r.ingredients, concat(r_owner.first_name, ' ', r_owner.last_name) AS created_by, 
                (SELECT COUNT(*) > 0 FROM shared_recipe_list sr WHERE sr.recipient_id = :user_id AND sr.recipe_list_id = :recipe_list_id AND sr.accepted) AS shared
            FROM
                recipe r
                INNER JOIN recipe_list_recipe rlr on r.id = rlr.recipe_id
                LEFT JOIN user_account r_owner ON r.owner_id = r_owner.id
            WHERE
                rlr.recipe_list_id = :recipe_list_id
        """.trimIndent()

        return try {
            namedTemplate.query(query, parameterSource) { rs, _ ->
                mapper.map(rs)
            }
        } catch (ex: DataAccessException) {
            logger.info("getRecipesForRecipeList (userId: $userId, recipeListId: $recipeListId) failed", ex)
            emptyList()
        }
    }

    fun getRecipe(userId: Long, recipeId: Long): Recipe? {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("user_id", userId)
        parameterSource.addValue("recipe_id", recipeId)

        val query = """
            SELECT
                r.id, rlr.recipe_list_id, r.title, r.url, r.image_url, r.site, r.comment, r.created, r.ingredients, concat(r_owner.first_name, ' ', r_owner.last_name) AS created_by, 
                ( SELECT COUNT(*) > 0
                    FROM (
                        SELECT 1 FROM shared_recipe sr WHERE sr.recipe_id = :recipe_id AND sr.recipient_id = :user_id AND sr.accepted
                        UNION
                        SELECT 1 FROM shared_recipe_list srl WHERE srl.recipe_list_id = rlr.recipe_list_id AND srl.recipient_id = :user_id AND srl.accepted
                    ) AS shared_items
                ) AS shared
            FROM
                recipe r
                INNER JOIN recipe_list_recipe rlr ON r.id = rlr.recipe_id
                LEFT JOIN user_account r_owner ON r.owner_id = r_owner.id
            WHERE
                r.id = :recipe_id
        """.trimIndent()

        return try {
            namedTemplate.queryForObject(query, parameterSource) { rs, _ ->
                mapper.map(rs)
            }
        } catch (ex: DataAccessException) {
            logger.info("getRecipe (userId: $userId, recipeId: $recipeId) failed", ex)
            null
        }
    }

    fun getSharedRecipes(userId: Long): List<Recipe> {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("user_id", userId)

        val query = """
            SELECT
                r.id, rlr.recipe_list_id, r.title, r.url, r.image_url, r.site, r.comment, r.created, r.ingredients, concat(owner.first_name, ' ', owner.last_name) AS created_by, rlr.recipe_list_id, true AS shared
            FROM
                recipe r
                INNER JOIN recipe_list_recipe rlr ON r.id = rlr.recipe_id
                INNER JOIN shared_recipe sr on r.id = sr.recipe_id
                LEFT JOIN user_account owner on sr.sharer_id = owner.id
            WHERE
                sr.recipient_id = :user_id
            AND
                sr.accepted
        """.trimIndent()

        return try {
            namedTemplate.query(query, parameterSource) { rs, _ ->
                mapper.map(rs)
            }
        } catch (ex: DataAccessException) {
            logger.info("getSharedRecipes (userId: $userId) failed", ex)
            emptyList()
        }
    }

    fun createRecipe(userId: Long, recipe: RecipeInput): Number {
        val simpleJdbcInsert = SimpleJdbcInsert(jdbcTemplate)
                .withTableName("recipe")
                .usingGeneratedKeyColumns("id")
                .usingColumns("owner_id", "title", "url", "image_url", "site", "comment", "ingredients")

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("owner_id", userId)
        parameterSource.addValue("title", recipe.title)
        parameterSource.addValue("url", recipe.url)
        parameterSource.addValue("image_url", recipe.imageUrl)
        parameterSource.addValue("site", recipe.site)
        parameterSource.addValue("comment", recipe.comment)
        parameterSource.addValue("ingredients", recipe.ingredients)

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
            logger.info("deleteRecipeFromRecipeList (recipeId: $recipeId, recipeListId: $recipeListId) failed", ex)
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
        parameterSource.addValue("ingredients", recipe.ingredients)
        parameterSource.addValue("comment", recipe.comment)

        val query = """
            UPDATE
                recipe
            SET
                title = :title, url = :url, image_url = :image_url, site = :site, comment = :comment, ingredients = :ingredients
            WHERE
                id = :recipe_id
        """.trimIndent()

        return try {
            namedTemplate.update(query, parameterSource)
        } catch (ex: DataAccessException) {
            logger.info("updateRecipe (recipeId: ${recipe.id}) failed", ex)
            0
        }
    }

    fun deleteRecipe(recipeId: Long): Int {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("recipe_id", recipeId)

        return try {
            namedTemplate.update("""
                DELETE FROM 
                    recipe r
                WHERE 
                    r.id = :recipe_id
            """.trimIndent(), parameterSource)
        } catch (ex: DataAccessException) {
            logger.info("deleteRecipe (recipeId: $recipeId) failed", ex)
            0
        }
    }

    fun deleteRecipesForRecipeList(recipeListId: Long): Int {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("recipe_list_id", recipeListId)

        return try {
            namedTemplate.update("""
                DELETE FROM 
                    recipe r
                WHERE 
                    r.recipe_list_id = :recipe_list_id
            """.trimIndent(), parameterSource)
        } catch (ex: DataAccessException) {
            logger.info("deleteRecipesForRecipeList (recipeListId: $recipeListId) failed", ex)
            0
        }
    }
}