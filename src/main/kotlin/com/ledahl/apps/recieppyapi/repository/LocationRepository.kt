package com.ledahl.apps.recieppyapi.repository

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class LocationRepository(
        @Autowired private val jdbcTemplate: JdbcTemplate
) {

    fun createNewLocation(name: String, address: String?, userId: Long, inviteCode: String): Number {
        val simpleJdbcInsert = SimpleJdbcInsert(jdbcTemplate)
                .withTableName("location")
                .usingGeneratedKeyColumns("id")

        val parameters = HashMap<String, Any>()
        parameters["name"] = name
        parameters["address"] = address ?: ""
        parameters["created_by"] = userId
        parameters["invite_code"] = inviteCode
        parameters["created"] = Date()

        return simpleJdbcInsert.executeAndReturnKey(MapSqlParameterSource(parameters))
    }

    fun addUserToLocation(userId: Long, locationId: Long): Number {
        val simpleJdbcInsert = SimpleJdbcInsert(jdbcTemplate)
                .withTableName("location_user_account")

        val parameters = HashMap<String, Any>()
        parameters["locationId"] = locationId
        parameters["user_account_id"] = userId

        return simpleJdbcInsert.execute(MapSqlParameterSource(parameters))
    }

    fun findLocationWithId(locationId: Long): Int? {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("id", locationId)

        return try {
            namedTemplate.queryForObject("SELECT id FROM location WHERE id = :id", parameterSource) { rs, _ ->
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

        return try {
            namedTemplate.queryForObject(
                    """
                        SELECT invite_code 
                        FROM location 
                        WHERE id = :location_id
                        """.trimMargin(), parameterSource) { rs, _ ->
                rs.getString("invite_code")
            }
        } catch (exception: DataAccessException) {
            null
        }
    }

    fun getLocationsForUser(userId: Long): List<Long> {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("created_by", userId)

        return namedTemplate.query("SELECT id from location WHERE created_by = :created_by", parameterSource) { rs, _ ->
            rs.getLong("id")
        }
    }

    fun getLocationFromInviteCode(inviteCode: String): Long? {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()

        parameterSource.addValue("invite_code", inviteCode)

        return try {
            namedTemplate.queryForObject("SELECT id from location WHERE invite_code = :invite_code", parameterSource) { rs, _ ->
                rs.getLong("id")
            }
        } catch (dae: DataAccessException) {
            return 0
        }
    }
}
