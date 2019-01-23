package com.ledahl.apps.recieppyapi.repository

import com.ledahl.apps.recieppyapi.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class UserRepository(@Autowired private val jdbcTemplate: JdbcTemplate) {
    fun getUsers(): List<User> {
        return jdbcTemplate.query("SELECT * FROM user_account") { rs,_ ->
            mapToUser(rs)
        }
    }

    fun getUserFromToken(token: String): User? {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("token", token)

        return try {
            namedTemplate.queryForObject("SELECT * FROM user_account WHERE token = :token", parameterSource) { rs, _ ->
                mapToUser(rs)
            }
        } catch (exception: DataAccessException) {
            null
        }
    }

    fun getUserFromPhoneNumber(phoneNumber: String): User? {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("phone_number", phoneNumber)

        return try {
            namedTemplate.queryForObject("SELECT * FROM user_account WHERE phone_number = :phone_number", parameterSource) { rs, _ ->
                mapToUser(rs)
            }
        } catch (exception: DataAccessException) {
            null
        }
    }

    fun save(user: User): Number {
        val simpleJdbcInsert = SimpleJdbcInsert(jdbcTemplate)
                .withTableName("user_account")
                .usingGeneratedKeyColumns("id")

        val parameters = HashMap<String, Any?>()
        parameters["first_name"] = user.firstName
        parameters["last_name"] = user.lastName
        parameters["phone_number"] = user.phoneNumber
        parameters["token"] = user.token

        return simpleJdbcInsert.executeAndReturnKey(MapSqlParameterSource(parameters))
    }

    fun update(user: User): Int? {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("id", user.id)
        parameterSource.addValue("first_name", user.firstName)
        parameterSource.addValue("last_name", user.lastName)
        parameterSource.addValue("email", user.email)

        return try {
            namedTemplate.update("UPDATE user_account SET first_name = :first_name, last_name = :last_name, email = :email WHERE id = :id", parameterSource)
        } catch (exception: DataAccessException) {
            null
        }
    }

    fun saveTokenForUser(user: User): Int? {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("id", user.id)
        parameterSource.addValue("token", user.token)

        return try {
            namedTemplate.update("UPDATE user_account SET token = :token WHERE id = :id", parameterSource)
        } catch (exception: DataAccessException) {
            null
        }
    }

    private fun mapToUser(rs: ResultSet): User? {
        return User(
                id = rs.getLong("id"),
                firstName = rs.getString("first_name"),
                lastName = rs.getString("last_name"),
                email = rs.getString("email"),
                firebaseId = rs.getString("firebase_id"),
                phoneNumber = rs.getString("phone_number"),
                token = rs.getString("token")
        )
    }
}