package com.ledahl.apps.recieppyapi.repository

import com.ledahl.apps.recieppyapi.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class UserRepository(@Autowired private val jdbcTemplate: JdbcTemplate) {
    fun getUsers(): List<User> {
        return jdbcTemplate.query("SELECT * FROM \"user\"") { rs,_ ->
            mapToUser(rs)
        }
    }

    fun getUserById(id: Long): User? {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("id", id)

        return namedTemplate.queryForObject("SELECT * FROM \"user\" WHERE id = :id", parameterSource) { rs, _ ->
            mapToUser(rs)
        }
    }

    fun getUserByToken(token: String): User? {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("token", token)

        return namedTemplate.queryForObject("SELECT * FROM \"user\" WHERE token = :token", parameterSource) { rs, _ ->
            mapToUser(rs)
        }
    }

    private fun mapToUser(rs: ResultSet): User? {
        return User(
                id = rs.getLong("id"),
                name = rs.getString("name"),
                firebaseId = rs.getString("firebase_id"),
                phoneNumber = rs.getString("phone_number"),
                token = rs.getString("token")
        )
    }
}