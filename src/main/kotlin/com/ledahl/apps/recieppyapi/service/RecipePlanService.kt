package com.ledahl.apps.recieppyapi.service

import com.ledahl.apps.recieppyapi.exception.NotAuthorizedException
import com.ledahl.apps.recieppyapi.model.RecipeEvent
import com.ledahl.apps.recieppyapi.model.RecipePlan
import com.ledahl.apps.recieppyapi.model.User
import com.ledahl.apps.recieppyapi.model.input.RecipeEventInput
import com.ledahl.apps.recieppyapi.repository.RecipePlanRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.temporal.WeekFields

@Service
class RecipePlanService(@Autowired private val recipePlanRepository: RecipePlanRepository,
                        @Autowired private val locationService: LocationService) {

    fun getRecipePlanForWeek(user: User?, locationId: Long, weekNumber: Int): RecipePlan {
        preConditionCheck(user = user, locationId = locationId)
        return RecipePlan(locationId = locationId, weekNumber = weekNumber)
    }

    fun getRecipePlanForCurrentWeek(user: User?, locationId: Long): RecipePlan {
        preConditionCheck(user = user, locationId = locationId)
        return getRecipePlan(locationId = locationId, date = LocalDate.now())
    }

    fun getRecipeEventsByWeek(user: User?, locationId: Long, weekNumber: Int): List<RecipeEvent> {
        preConditionCheck(user = user, locationId = locationId)
        return recipePlanRepository.getRecipeEventsForWeek(locationId = locationId, weekNumber = weekNumber)
    }

    fun createRecipeEvent(user: User?, locationId: Long, recipeEvent: RecipeEventInput): RecipePlan {
        preConditionCheck(user = user, locationId = locationId)

        recipePlanRepository.createRecipeEvent(locationId = locationId, recipeEvent = recipeEvent)

        return getRecipePlan(locationId = locationId, date = recipeEvent.date)
    }

    fun updateRecipeEvent(user: User?, locationId: Long, recipeEvent: RecipeEventInput): RecipePlan {
        preConditionCheck(user = user, locationId = locationId)

        val recipePlan = getRecipePlan(locationId = locationId, date = recipeEvent.date)

        val existingRecipeEventsForWeek = recipePlanRepository.getRecipeEventsForWeek(
                locationId = locationId,
                weekNumber = recipePlan.weekNumber
        )
        val existingRecipeEvent = existingRecipeEventsForWeek.find { it.recipeId == recipeEvent.recipeId }
                ?: throw IllegalArgumentException("Recipe does not exist")

        val recipeEvents = HashMap<Long, List<LocalDate>>()

        existingRecipeEventsForWeek.find { it.date == recipeEvent.date }?.let {
            recipeEvents[it.recipeId] = listOf(it.date, existingRecipeEvent.date)
        }
        recipeEvents[recipeEvent.recipeId] = listOf(existingRecipeEvent.date, recipeEvent.date)

        recipePlanRepository.updateRecipeEvent(locationId = locationId, recipeEvents = recipeEvents)

        return recipePlan
    }

    fun deleteRecipeEvent(user: User?, locationId: Long, recipeEvent: RecipeEventInput): RecipePlan {
        preConditionCheck(user = user, locationId = locationId)

        recipePlanRepository.deleteRecipeEvent(locationId = locationId, recipeEvent = recipeEvent)

        return getRecipePlan(locationId = locationId, date = recipeEvent.date)
    }

    private fun getRecipePlan(locationId: Long, date: LocalDate): RecipePlan {
        val weekNumber = date.get(WeekFields.ISO.weekOfWeekBasedYear())
        return RecipePlan(locationId = locationId, weekNumber = weekNumber)
    }

    private fun preConditionCheck(user: User?, locationId: Long) {
        locationService.getLocation(user = user, locationId = locationId)
                ?: throw NotAuthorizedException("User is not part of location")
    }
}