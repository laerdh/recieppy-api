package com.ledahl.apps.recieppyapi.repository

import com.ledahl.apps.recieppyapi.model.Location
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.*
import kotlin.collections.HashMap

@Repository
class LocationRepository(@Autowired private val jdbcTemplate: JdbcTemplate) {

    fun createNewLocation(name: String, address: String?, userId: Long, inviteCode: String): Number? {
        val simpleJdbcInsert = SimpleJdbcInsert(jdbcTemplate)
                .withTableName("location")
                .usingGeneratedKeyColumns("id")

        val parameters = HashMap<String, Any>()
        parameters["name"] = name
        parameters["address"] = address ?: ""
        parameters["created_by"] = userId
        parameters["invite_code"] = inviteCode
        parameters["created"] = Date()

        return try {
            simpleJdbcInsert.executeAndReturnKey(MapSqlParameterSource(parameters))
        } catch (ex: Exception) {
            return null
        }
    }

    fun addUserToLocation(userId: Long, locationId: Long): Number {
        val simpleJdbcInsert = SimpleJdbcInsert(jdbcTemplate)
                .withTableName("location_user_account")

        val parameters = HashMap<String, Any>()
        parameters["location_id"] = locationId
        parameters["user_account_id"] = userId

        return simpleJdbcInsert.execute(MapSqlParameterSource(parameters))
    }

    fun findLocationWithId(locationId: Long): Int? {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("location_id", locationId)

        val query = """
            SELECT
            	id
            FROM
            	LOCATION
            WHERE
            	id = :location_id
        """.trimIndent()

        return try {
            namedTemplate.queryForObject(query, parameterSource) { rs, _ ->
                rs.getInt("id")
            }
        } catch (exception: DataAccessException) {
            null
        }
    }

    fun getInviteCode(locationId: Long): String? {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("location_id", locationId)

        val query = """
            SELECT
            	invite_code
            FROM
            	LOCATION
            WHERE
            	id = :location_id
        """.trimIndent()

        return try {
            namedTemplate.queryForObject(query, parameterSource) { rs, _ ->
                rs.getString("invite_code")
            }
        } catch (exception: DataAccessException) {
            null
        }
    }

    fun getLocationsForUser(userId: Long): List<Location> {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("user_id", userId)

        val query = """
            SELECT
            	*
            FROM
            	LOCATION l
                INNER JOIN location_user_account lua ON lua.location_id = l.id
            WHERE
            	lua.user_account_id = :user_id
        """.trimIndent()

        return namedTemplate.query(query, parameterSource) { rs, _ ->
            mapToLocation(rs)
        }
    }

    fun getLocation(userId: Long, locationId: Long): Location? {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameters = HashMap<String, Any>()
        parameters["user_id"] = userId
        parameters["location_id"] = locationId

        val query = """
            SELECT
                *
            FROM
                location l
                INNER JOIN location_user_account lua ON lua.user_account_id = :user_id
            WHERE
                l.id = :location_id
        """.trimIndent()

        return try {
            namedTemplate.queryForObject(query, parameters) { rs, _ ->
                mapToLocation(rs)
            }
        } catch (exception: DataAccessException) {
            null
        }
    }

    fun isUserMemberOfLocation(userId: Long, locationId: Long): Boolean {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("user_id", userId)
        parameterSource.addValue("location_id", locationId)

        val query = """
            SELECT
                COUNT(*)
            FROM
                location_user_account
            WHERE
                user_account_id = :user_id
                AND location_id = :location_id
            
        """.trimIndent()

        return try {
            namedTemplate.queryForObject(query, parameterSource) { rs, _ ->
                rs.getInt("count") > 0
            } ?: false
        } catch (exception: DataAccessException) {
            false
        }
    }

    fun isRecipeListInUsersLocation(userId: Long, recipeListId: Long): Boolean {
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
                AND rl.id = :recipe_list_id
        """.trimIndent()

        return try {
            namedTemplate.queryForObject(query, parameterSource) { rs, _ ->
                rs.getInt("count") > 0
            } ?: false
        } catch (exception: DataAccessException) {
            false
        }
    }

    fun isRecipeInUsersLocation(userId: Long, recipeId: Long): Boolean {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameters = MapSqlParameterSource()
        parameters.addValue("user_id", userId)
        parameters.addValue("recipe_id", recipeId)

        val query = """
            SELECT
                COUNT(*)
            FROM
                recipe r
                INNER JOIN recipe_list rl ON r.recipe_list_id = rl.id
                INNER JOIN location_recipe_list lrl ON rl.id = lrl.recipe_list_id
                INNER JOIN location_user_account lua ON lrl.location_id = lua.location_id
            WHERE
                lua.user_account_id = :user_id
                AND r.id = :recipe_id
        """.trimIndent()

        return try {
            namedTemplate.queryForObject(query, parameters) { rs, _ ->
                rs.getInt("count") > 0
            } ?: false
        } catch (exception: DataAccessException) {
            false
        }
    }

    fun getLocationIdFromInviteCode(inviteCode: String): Long? {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()

        parameterSource.addValue("invite_code", inviteCode)

        val query = """
            SELECT
            	id
            FROM
            	LOCATION
            WHERE
            	invite_code = :invite_code
        """.trimIndent()

        return try {
            namedTemplate.queryForObject(query, parameterSource) { rs, _ ->
                rs.getLong("id")
            }
        } catch (dae: DataAccessException) {
            return null
        }
    }

    fun getLocationId(userId: Long, recipeListId: Long): Long {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()

        parameterSource.addValue("user_id", userId)
        parameterSource.addValue("recipe_list_id", recipeListId)

        val query = """
            SELECT 
            	id AS "location_id"
            FROM
            	location l 
            	INNER JOIN location_user_account lua ON lua.location_id = l.id
            	INNER JOIN location_recipe_list lrl ON lrl.location_id = l.id
            WHERE
            	lua.user_account_id = :user_id
            AND 
            	lrl.recipe_list_id = :recipe_list_id
        """.trimIndent()

        return try {
            namedTemplate.queryForObject(query, parameterSource) { rs, _ ->
                rs.getLong("location_id")
            } ?: 0
        } catch (dae: DataAccessException) {
            return 0
        }
    }

    fun getLocationNameFromInviteCode(inviteCode: String): String? {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()

        parameterSource.addValue("invite_code", inviteCode)

        val query = """
            SELECT 
                name
            FROM 
                location l
            WHERE
                l.invite_code = :invite_code
        """.trimIndent()

        return try {
            namedTemplate.queryForObject(query, parameterSource) { rs, _ ->
                rs.getString("name") ?: null
            }
        } catch (dae: DataAccessException) {
            return null
        }
    }

    private fun mapToLocation(rs: ResultSet): Location {
        return Location(
                id = rs.getLong("id"),
                name = rs.getString("name"),
                address = rs.getString("address"),
                owner = rs.getInt("created_by"),
                inviteCode = rs.getString("invite_code")
        )
    }
}
