package com.ledahl.apps.recieppyapi.repository

import com.ledahl.apps.recieppyapi.model.RecipeList
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
class RecipeListRepository(@Autowired private val jdbcTemplate: JdbcTemplate) {

    private val logger = LoggerFactory.getLogger(RecipeListRepository::class.java)

    fun isRecipeListAvailableToUser(userId: Long, recipeListId: Long): Boolean {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("recipe_list_id", recipeListId)
        parameterSource.addValue("user_id", userId)

        val query = """
            SELECT
                COUNT(*)
            FROM (
                SELECT
                    rl.id
                FROM
                    recipe_list rl
                        INNER JOIN location_recipe_list lrl ON rl.id = lrl.recipe_list_id
                        INNER JOIN location l ON lrl.location_id = l.id
                        INNER JOIN location_user_account lua ON l.id = lua.location_id
                WHERE
                    lua.user_account_id = :user_id
                AND
                    rl.id = :recipe_list_id
                UNION
                SELECT
                    rl.id
                FROM
                    recipe_list rl
                        INNER JOIN shared_recipe_list srl ON srl.recipe_list_id = rl.id
                        INNER JOIN user_account ua ON ua.id = srl.recipient_id
                WHERE
                    ua.id = :user_id
                AND
                    rl.id = :recipe_list_id
                AND
                    srl.accepted
            ) AS recipe_lists
        """.trimIndent()

        return try {
            namedTemplate.queryForObject(query, parameterSource) { rs, _ ->
                rs.getInt("count") > 0
            } ?: false
        } catch (ex: DataAccessException) {
            logger.info("isRecipeListAvailableToUser (userId: $userId, recipeListId: $recipeListId) failed")
            false
        }
    }

    fun isRecipeListEditableForUser(userId: Long, recipeListId: Long): Boolean {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("user_id", userId)
        parameterSource.addValue("recipe_list_id", recipeListId)

        val query = """
            SELECT
                COUNT(*)
            FROM
                recipe_list rl
                INNER JOIN location_recipe_list lrl ON rl.id = lrl.recipe_list_id
                INNER JOIN location l ON lrl.location_id = l.id
                INNER JOIN location_user_account lua ON l.id = lua.location_id
            WHERE
                lua.user_account_id = :user_id
            AND
                rl.id = :recipe_list_id
        """.trimIndent()

        return try {
            namedTemplate.queryForObject(query, parameterSource) { rs, _ ->
                rs.getInt("count") > 0
            } ?: false
        } catch (ex: DataAccessException) {
            logger.info("isRecipeListEditableForUser (userId: $userId, recipeListId: $recipeListId) failed", ex)
            false
        }
    }

    fun getRecipeList(userId: Long, recipeListId: Long): RecipeList? {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("recipe_list_id", recipeListId)
        parameterSource.addValue("user_id", userId)

        val query = """
            SELECT
                rl.id, rl.name, rl.created, false AS shared, concat(owner.first_name, ' ', owner.last_name) AS created_by
            FROM
                recipe_list rl
                INNER JOIN location_recipe_list lrl ON rl.id = lrl.recipe_list_id
                INNER JOIN location_user_account lua ON lrl.location_id = lua.location_id
                LEFT JOIN user_account owner ON rl.owner_id = owner.id
            WHERE
                rl.id = :recipe_list_id
            AND
                lua.user_account_id = :user_id
            UNION
            SELECT
                rl.id, rl.name, rl.created, true AS shared, concat(owner.first_name, ' ', owner.last_name) AS created_by
            FROM
                recipe_list rl
                INNER JOIN shared_recipe_list srl ON rl.id = srl.recipe_list_id
                LEFT JOIN user_account owner ON srl.sharer_id = owner.id
            WHERE
                rl.id = :recipe_list_id
            AND
                srl.recipient_id = :user_id
            AND
                accepted
        """.trimIndent()

        return try {
            namedTemplate.queryForObject(query, parameterSource) { rs, _ ->
                mapToRecipeList(rs)
            }
        } catch (ex: DataAccessException) {
            logger.info("getRecipeList (userId: $userId, recipeListId: $recipeListId) failed", ex)
            null
        }
    }

    fun getRecipeLists(userId: Long, locationId: Long): List<RecipeList> {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("user_id", userId)
        parameterSource.addValue("location_id", locationId)

        val query = """
            SELECT
                rl.id, rl.name, rl.created, false AS shared, concat(owner.first_name, ' ', owner.last_name) AS created_by
            FROM
                recipe_list rl
                    INNER JOIN location_recipe_list lrl ON rl.id = lrl.recipe_list_id
                    INNER JOIN location l ON lrl.location_id = l.id
                    INNER JOIN location_user_account lua ON l.id = lua.location_id
                    LEFT JOIN user_account owner ON rl.owner_id = owner.id
            WHERE
                    l.id = :location_id
            AND
                  lua.user_account_id = :user_id
            UNION
            SELECT
                rl.id, rl.name, rl.created, true AS shared, concat(owner.first_name, ' ', owner.last_name) AS created_by
            FROM
                recipe_list rl
                    INNER JOIN shared_recipe_list srl ON srl.recipe_list_id = rl.id
                    LEFT JOIN user_account owner ON owner.id = rl.owner_id
            WHERE
                  srl.recipient_id = :user_id
            AND
                  srl.accepted
        """.trimIndent()

        return try {
            namedTemplate.query(query, parameterSource) { rs, _ ->
                mapToRecipeList(rs)
            }
        } catch (ex: DataAccessException) {
            logger.info("getRecipeLists (userId: $userId, locationId: $locationId) failed", ex)
            emptyList()
        }
    }

    fun createRecipeList(userId: Long, recipeList: RecipeList): Number {
        val simpleJdbcInsert = SimpleJdbcInsert(jdbcTemplate)
                .withTableName("recipe_list")
                .usingGeneratedKeyColumns("id")
                .usingColumns("name", "owner_id")

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("name", recipeList.name)
        parameterSource.addValue("owner_id", userId)

        return simpleJdbcInsert.executeAndReturnKey(parameterSource)
    }

    fun connectRecipeListAndLocation(recipeListId: Long, locationId: Long) {
        val simpleJdbcInsert = SimpleJdbcInsert(jdbcTemplate)
                .withTableName("location_recipe_list")
                .usingGeneratedKeyColumns("id")

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("location_id", locationId)
        parameterSource.addValue("recipe_list_id", recipeListId)

        simpleJdbcInsert.execute(parameterSource)
    }

    fun deleteRecipeList(recipeListId: Long): Int {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("recipe_list_id", recipeListId)

        return try {
            namedTemplate.update("""
                DELETE FROM 
                    recipe_list rl
                WHERE 
                    rl.id = :recipe_list_id
            """.trimIndent(), parameterSource)
        } catch (ex: DataAccessException) {
            logger.info("deleteRecipeList (recipeListId: $recipeListId) failed", ex)
            0
        }
    }

    fun deleteLocationRecipeList(recipeListId: Long, locationId: Long): Int {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("recipe_list_id", recipeListId)
        parameterSource.addValue("location_id", locationId)

        return try {
            namedTemplate.update("""
                DELETE FROM 
                    location_recipe_list lrl
                WHERE 
                    lrl.recipe_list_id = :recipe_list_id
                    AND lrl.location_id = :location_id
            """.trimIndent(), parameterSource)
        } catch (ex: DataAccessException) {
            logger.info("deleteLocationRecipeList (recipeListId: $recipeListId, locationId: $locationId) failed", ex)
            0
        }
    }

    fun renameRecipeList(recipeListId: Long, newName: String): Int {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("recipe_list_id", recipeListId)
        parameterSource.addValue("new_name", newName)

        return try {
            namedTemplate.update("""
                UPDATE 
                    recipe_list
                SET 
                    name = :new_name
                WHERE 
                    id = :recipe_list_id
            """.trimIndent(), parameterSource)
        } catch (ex: DataAccessException) {
            logger.info("renameRecipeList (recipeListId: $recipeListId, newName: $newName) failed", ex)
            0
        }
    }

    private fun mapToRecipeList(rs: ResultSet): RecipeList {
        return RecipeList(
                id = rs.getLong("id"),
                name = rs.getString("name"),
                shared = rs.getBoolean("shared"),
                created = rs.getTimestamp("created").toLocalDateTime(),
                createdBy = rs.getString("created_by")
        )
    }
}