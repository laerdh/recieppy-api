package com.ledahl.apps.recieppyapi.model.mappers

import com.ledahl.apps.recieppyapi.model.*
import com.ledahl.apps.recieppyapi.model.enums.UserRole
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.sql.ResultSet

@Component
class RepositoryMapper {

    @Bean
    fun userRepositoryMapper() : Mapper<ResultSet, User> {
        return object : Mapper<ResultSet, User> {
            override fun map(item: ResultSet): User {
                return User(
                        id = item.getLong("id"),
                        subject = item.getString("subject") ?: "",
                        firstName = item.getString("first_name"),
                        lastName = item.getString("last_name"),
                        email = item.getString("email"),
                        phoneNumber = item.getString("phone_number") ?: "",
                        role = UserRole.valueOf(item.getString("user_role"))
                )
            }
        }
    }

    @Bean
    fun locationRepositoryMapper(): Mapper<ResultSet, Location> {
        return object : Mapper<ResultSet, Location> {
            override fun map(item: ResultSet): Location {
                return Location(
                        id = item.getLong("id"),
                        name = item.getString("name"),
                        address = item.getString("address"),
                        owner = item.getLong("created_by"),
                        inviteCode = item.getString("invite_code"),
                        imageUrl = item.getString("image_url")
                )
            }
        }
    }

    @Bean
    fun recipeListRepositoryMapper(): Mapper<ResultSet, RecipeList> {
        return object : Mapper<ResultSet, RecipeList> {
            override fun map(item: ResultSet): RecipeList {
                return RecipeList(
                        id = item.getLong("id"),
                        name = item.getString("name"),
                        shared = item.getBoolean("shared"),
                        created = item.getTimestamp("created").toLocalDateTime(),
                        createdBy = item.getString("created_by")
                )
            }
        }
    }

    @Bean
    fun recipeRepositoryMapper(): Mapper<ResultSet, Recipe> {
        return object : Mapper<ResultSet, Recipe> {
            override fun map(item: ResultSet): Recipe {
                return Recipe(
                        id = item.getLong("id"),
                        recipeListId = item.getLong("recipe_list_id"),
                        title = item.getString("title"),
                        url = item.getString("url"),
                        imageUrl = item.getString("image_url"),
                        site = item.getString("site"),
                        comment = item.getString("comment"),
                        shared = item.getBoolean("shared"),
                        created = item.getTimestamp("created").toLocalDateTime(),
                        createdBy = item.getString("created_by")
                )
            }
        }
    }

    @Bean
    fun recipePlanRepositoryMapper(): Mapper<ResultSet, RecipePlanEvent> {
        return object : Mapper<ResultSet, RecipePlanEvent> {
            override fun map(item: ResultSet): RecipePlanEvent {
                return RecipePlanEvent(
                        date = item.getDate("date").toLocalDate(),
                        recipeId = item.getLong("recipe_id")
                )
            }
        }
    }

    @Bean
    fun tagRepositoryMapper(): Mapper<ResultSet, Tag> {
        return object : Mapper<ResultSet, Tag> {
            override fun map(item: ResultSet): Tag {
                return Tag(
                        id = item.getLong("id"),
                        text = item.getString("text")
                )
            }
        }
    }

    @Bean
    fun locationInviteMapper(): Mapper<ResultSet, LocationInvite> {
        return object : Mapper<ResultSet, LocationInvite> {
            override fun map(item: ResultSet): LocationInvite {
                val acceptedUserId = item.getLong("accepted_user_id")

                return LocationInvite(
                        id = item.getLong("id"),
                        invitedBy = item.getLong("invited_by"),
                        timeSent = item.getTimestamp("time_sent")?.toLocalDateTime(),
                        locationId = item.getLong("location_id"),
                        email = item.getString("email"),
                        inviteCode = item.getString("invite_code"),
                        acceptedByUser = if (acceptedUserId == 0L) null else acceptedUserId
                )
            }
        }
    }
}