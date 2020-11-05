package com.ledahl.apps.recieppyapi.repository

import com.ledahl.apps.recieppyapi.model.Location
import com.ledahl.apps.recieppyapi.model.mappers.Mapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.jdbc.core.namedparam.set
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.*
import kotlin.collections.HashMap

@Repository
class LocationRepository(@Autowired private val jdbcTemplate: JdbcTemplate,
                         @Autowired private val mapper: Mapper<ResultSet, Location>) {

    private val logger = LoggerFactory.getLogger(LocationRepository::class.java)

    fun createNewLocation(name: String, address: String?, userId: Long, inviteCode: String, imageUrl: String?): Number? {
        val simpleJdbcInsert = SimpleJdbcInsert(jdbcTemplate)
                .withTableName("location")
                .usingGeneratedKeyColumns("id")

        val parameters = HashMap<String, Any?>()
        parameters["name"] = name
        parameters["address"] = address ?: ""
        parameters["created_by"] = userId
        parameters["invite_code"] = inviteCode
        parameters["created"] = Date()
        parameters["image_url"] = imageUrl

        return try {
            simpleJdbcInsert.executeAndReturnKey(MapSqlParameterSource(parameters))
        } catch (ex: Exception) {
            logger.info("createNewLocation (name: $name, address: $address, userId: $userId, inviteCode: $inviteCode), imageUrl: $imageUrl failed", ex)
            return null
        }
    }

    fun updateLocation(locationId: Long, name: String, address: String?): Location? {
        val namedJdbcTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameters = HashMap<String, Any?>()
        parameters["location_id"] = locationId
        parameters["name"] = name
        parameters["address"] = address

        val query = """
            UPDATE
                location
            SET
                name = :name, address = :address
            WHERE
                id = :location_id
            RETURNING
                location.*
        """.trimIndent()

        return try {
            namedJdbcTemplate.queryForObject(query, parameters) { rs, _ ->
                mapper.map(rs)
            }
        } catch (ex: DataAccessException) {
            logger.info("updateLocation (location: $locationId) failed", ex)
            null
        }
    }

    fun addUserToLocation(userId: Long, locationId: Long): Number {
        val simpleJdbcInsert = SimpleJdbcInsert(jdbcTemplate)
                .withTableName("location_user_account")

        val parameters = HashMap<String, Any>()
        parameters["location_id"] = locationId
        parameters["user_account_id"] = userId

        return try {
            simpleJdbcInsert.execute(MapSqlParameterSource(parameters))
        } catch (ex: DataAccessException) {
            logger.info("addUserToLocation failed (userId: $userId, locationId: $locationId)", ex)
            0
        }
    }

    fun removeUsersFromLocation(userIds: List<Long>, locationId: Long): Int {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameterList = Array<SqlParameterSource>(userIds.size) {
            val parameters = MapSqlParameterSource()
            parameters["user_id"] = userIds[it]
            parameters["location_id"] = locationId
            parameters
        }

        val query = """
            DELETE FROM
                location_user_account
            WHERE
                user_account_id = :user_id AND location_id = :location_id
        """.trimIndent()

        return try {
            val updatedRows = namedTemplate.batchUpdate(query, parameterList)
            return updatedRows.reduce { acc, row -> acc + row }
        } catch (ex: DataAccessException) {
            logger.info("removeUserFromLocation (userId: $userIds, locationId: $locationId) failed", ex)
            0
        }
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
        } catch (ex: DataAccessException) {
            logger.info("findLocationWithId (locationId: $locationId) failed", ex)
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

        return try {
            namedTemplate.query(query, parameterSource) { rs, _ ->
                mapper.map(rs)
            }
        } catch (ex: DataAccessException) {
            logger.info("getLocationsForUser (userId: $userId) failed", ex)
            emptyList()
        }
    }

    fun getLocation(userId: Long, locationId: Long): Location? {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameters = HashMap<String, Any>()
        parameters["user_id"] = userId
        parameters["location_id"] = locationId

        val query = """
            SELECT
                l.id, l.name, l.address, l.created_by, l.invite_code, l.created, l.image_url
            FROM
                location l
                INNER JOIN location_user_account lua ON lua.location_id = l.id
            WHERE
                lua.location_id = :location_id AND lua.user_account_id = :user_id
        """.trimIndent()

        return try {
            namedTemplate.queryForObject(query, parameters) { rs, _ ->
                mapper.map(rs)
            }
        } catch (ex: DataAccessException) {
            logger.info("getLocation (userId: $userId, locationId: $locationId) failed", ex)
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
        } catch (ex: DataAccessException) {
            logger.info("isUserMemberOfLocation (userId: $userId, locationId: $locationId) failed", ex)
            false
        }
    }

    fun isUserOwnerOfLocation(userId: Long, locationId: Long): Boolean {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameters = HashMap<String, Any>()
        parameters["user_id"] = userId
        parameters["location_id"] = locationId

        val query = """
            SELECT
                COUNT(*)
            FROM
                location
            WHERE
                id = :location_id AND created_by = :user_id
        """.trimIndent()

        return try {
            namedTemplate.queryForObject(query, parameters) { rs, _ ->
                rs.getInt("count") > 0
            } ?: false
        } catch (ex: DataAccessException) {
            logger.info("isUserOwnerOfLocation (userId: $userId, locationId: $locationId) failed", ex)
            false
        }
    }

    fun getLocationFromInviteCode(inviteCode: String): Location? {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("invite_code", inviteCode)

        val query = """
            SELECT
                *
            FROM
                LOCATION
            WHERE
                invite_code = :invite_code
        """.trimIndent()

        return try {
            namedTemplate.queryForObject(query, parameterSource) { rs, _ ->
                mapper.map(rs)
            }
        } catch (ex: DataAccessException) {
            logger.info("getLocationFromInviteCode (inviteCode: $inviteCode) failed", ex)
            null
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
        } catch (ex: DataAccessException) {
            logger.info("getLocationIdFromInviteCode (inviteCode: $inviteCode) failed", ex)
            return null
        }
    }

    fun getLocationId(userId: Long, recipeListId: Long): Long? {
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
            }
        } catch (ex: DataAccessException) {
            logger.info("getLocationId (userId: $userId, recipeListId: $recipeListId) failed", ex)
            return null
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
        } catch (ex: DataAccessException) {
            logger.info("getLocationNameFromInviteCode (inviteCode: $inviteCode) failed", ex)
            return null
        }
    }
}
