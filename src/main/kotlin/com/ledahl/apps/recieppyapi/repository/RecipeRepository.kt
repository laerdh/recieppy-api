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

@Repository
class RecipeRepository(@Autowired private val jdbcTemplate: JdbcTemplate) {
    fun getRecipesForUser(userId: Long): List<Recipe> {
        val namedTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
        val parameterSource = MapSqlParameterSource()
        parameterSource.addValue("user_id", userId)

        val query = """
            SELECT
	            DISTINCT ON (r.id) *
            FROM
	            recipe r
	            INNER JOIN location_recipe_list lrl ON lrl.recipe_list_id = r.recipe_list_id
	            INNER JOIN location_user_account lua ON lua.location_id = lrl.location_id
            WHERE
	            lua.user_account_id = 1;
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
                r.id, r.title, r.url, r.image_url, r.site, r.recipe_list_id
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

        return simpleJdbcInsert.executeAndReturnKey(MapSqlParameterSource(parameters))
    }

    fun saveTagsToRecipe(recipeId: Long, tags: List<Long>) {
        tags.forEach { tagId ->
            val simpleJdbcInsert = SimpleJdbcInsert(jdbcTemplate)
                    .withTableName("recipe_tag")

            val parameters = HashMap<String, Any>()
            parameters["recipe_id"] = recipeId
            parameters["tag_id"] = tagId

            simpleJdbcInsert.execute(MapSqlParameterSource(parameters))
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
                recipeListId = rs.getLong("recipe_list_id")
        )
    }
}