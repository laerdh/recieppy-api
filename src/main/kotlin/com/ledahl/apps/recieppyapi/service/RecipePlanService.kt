package com.ledahl.apps.recieppyapi.service

import com.ledahl.apps.recieppyapi.exception.NotAuthorizedException
import com.ledahl.apps.recieppyapi.model.RecipePlanEvent
import com.ledahl.apps.recieppyapi.model.RecipePlan
import com.ledahl.apps.recieppyapi.model.User
import com.ledahl.apps.recieppyapi.model.input.RecipePlanEventInput
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

    fun getRecipePlanEventsByWeek(user: User?, locationId: Long, weekNumber: Int): List<RecipePlanEvent> {
        preConditionCheck(user = user, locationId = locationId)
        return recipePlanRepository.getRecipePlanEventsForWeek(locationId = locationId, weekNumber = weekNumber)
    }

    fun createRecipePlanEvent(user: User?, locationId: Long, recipePlanEvent: RecipePlanEventInput): RecipePlan {
        preConditionCheck(user = user, locationId = locationId)

        recipePlanRepository.createRecipePlanEvent(locationId = locationId, recipePlanEvent = recipePlanEvent)

        return getRecipePlan(locationId = locationId, date = recipePlanEvent.date)
    }

    fun updateRecipePlanEvent(user: User?, locationId: Long, recipePlanEvent: RecipePlanEventInput): RecipePlan {
        preConditionCheck(user = user, locationId = locationId)

        val recipePlan = getRecipePlan(locationId = locationId, date = recipePlanEvent.date)

        val existingRecipePlanEventsForWeek = recipePlanRepository.getRecipePlanEventsForWeek(
                locationId = locationId,
                weekNumber = recipePlan.weekNumber
        )
        val existingRecipePlanEvent = existingRecipePlanEventsForWeek.find { it.recipeId == recipePlanEvent.recipeId }
                ?: throw IllegalArgumentException("Recipe does not exist")

        val recipePlanEvents = HashMap<Long, List<LocalDate>>()

        existingRecipePlanEventsForWeek.find { it.date == recipePlanEvent.date }?.let {
            recipePlanEvents[it.recipeId] = listOf(it.date, existingRecipePlanEvent.date)
        }
        recipePlanEvents[recipePlanEvent.recipeId] = listOf(existingRecipePlanEvent.date, recipePlanEvent.date)

        recipePlanRepository.updateRecipePlanEvent(locationId = locationId, recipePlanEvents = recipePlanEvents)

        return recipePlan
    }

    fun deleteRecipePlanEvent(user: User?, locationId: Long, recipePlanEvent: RecipePlanEventInput): RecipePlan {
        preConditionCheck(user = user, locationId = locationId)

        recipePlanRepository.deleteRecipePlanEvent(locationId = locationId, recipePlanEvent = recipePlanEvent)

        return getRecipePlan(locationId = locationId, date = recipePlanEvent.date)
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