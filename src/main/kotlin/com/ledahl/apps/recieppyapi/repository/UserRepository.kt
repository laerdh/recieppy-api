package com.ledahl.apps.recieppyapi.repository

import com.ledahl.apps.recieppyapi.model.User
import com.ledahl.apps.recieppyapi.model.enums.UserRole
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
class UserRepository(@Autowired private val jdbcTemplate: JdbcTemplate) {

    private val logger = LoggerFactory.getLogger(UserRepository::class.java)

    fun getUsers(): List<User> {
        return try {
            jdbcTemplate.query("""
                SELECT u.id, phone_number, first_name, last_name, email, external_id
                FROM user_account u
                LEFT JOIN user_role ur ON u.id = ur.user_id
                LEFT JOIN role r ON ur.role_id = r.id
        """.trimIndent()) { rs, _ ->
                mapToUser(rs)
            }
        } catch (ex: DataAccessException) {
            logger.info("getUsers failed", ex)
            emptyList()
        }
    }

    fun getUserBySubject(subject: String): User? {
        val namedJdbcTemplate = NamedParameterJdbcTemplate(jdbcTemplate)

        val parameters = MapSqlParameterSource()
        parameters.addValue("subject", subject)

        return try {
            namedJdbcTemplate.queryForObject("""
                SELECT u.id, u.phone_number, u.first_name, u.last_name, u.email, u.subject, r.name AS user_role
                FROM user_account u
                INNER JOIN user_role ur ON u.id = ur.user_id
                INNER JOIN role r ON ur.role_id = r.id
                WHERE u.subject = :subject
            """.trimIndent(), parameters) { rs, _ ->
                mapToUser(rs)
            }
        } catch (ex: DataAccessException) {
            logger.info("getUserBySubject (subject: $subject) failed", ex)
            null
        }
    }

    fun save(user: User): Number {
        val simpleJdbcInsert = SimpleJdbcInsert(jdbcTemplate)
                .withTableName("user_account")
                .usingGeneratedKeyColumns("id")
                .usingColumns("subject, first_name", "last_name", "email")

        val parameters = HashMap<String, Any?>()
        parameters["subject"] = user.subject
        parameters["first_name"] = user.firstName
        parameters["last_name"] = user.lastName
        parameters["email"] = user.email

        return simpleJdbcInsert.executeAndReturnKey(MapSqlParameterSource(parameters))
    }

    fun saveRoleForUser(userId: Long) {
        val simpleJdbcInsert = SimpleJdbcInsert(jdbcTemplate)
                .withTableName("user_role")

        val parameters = HashMap<String, Any?>()
        parameters["user_id"] = userId
        parameters["role_id"] = 1 // ROLE: USER

        simpleJdbcInsert.execute(MapSqlParameterSource(parameters))
    }

    fun savePushToken(pushToken: String?, id: Long): Int? {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("push_token", pushToken)
        parameterSource.addValue("id", id)

        return try {
            namedTemplate.update("""
                UPDATE user_account
                SET push_token = :push_token
                WHERE id = :id
            """.trimIndent(), parameterSource)
        } catch (ex: DataAccessException) {
            logger.info("savePushToken (pushToken: $pushToken) failed", ex)
            null
        }
    }

    private fun mapToUser(rs: ResultSet): User? {
        return User(
                id = rs.getLong("id"),
                subject = rs.getString("subject") ?: "",
                firstName = rs.getString("first_name"),
                lastName = rs.getString("last_name"),
                email = rs.getString("email"),
                phoneNumber = rs.getString("phone_number") ?: "",
                role = UserRole.valueOf(rs.getString("user_role"))
        )
    }
}