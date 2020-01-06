package com.ledahl.apps.recieppyapi.service

import com.ledahl.apps.recieppyapi.model.RecipePlan
import com.ledahl.apps.recieppyapi.model.RecipePlanEvent
import com.ledahl.apps.recieppyapi.model.User
import com.ledahl.apps.recieppyapi.model.input.RecipePlanEventInput
import com.ledahl.apps.recieppyapi.repository.RecipePlanRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.temporal.WeekFields

@Service
class RecipePlanService(@Autowired private val recipePlanRepository: RecipePlanRepository) {

    @PreAuthorize("@authService.isMemberOfLocation(#user, #locationId)")
    fun getRecipePlanForWeek(user: User, locationId: Long, weekNumber: Int): RecipePlan {
        return RecipePlan(locationId = locationId, weekNumber = weekNumber)
    }

    fun getRecipePlanForCurrentWeek(user: User, locationId: Long): RecipePlan {
        return getRecipePlan(locationId = locationId, date = LocalDate.now())
    }

    fun getRecipePlanEventsByWeek(user: User, locationId: Long, weekNumber: Int): List<RecipePlanEvent> {
        return recipePlanRepository.getRecipePlanEventsForWeek(locationId = locationId, weekNumber = weekNumber)
    }

    @PreAuthorize("@authService.isMemberOfLocation(#user, #locationId)")
    fun createRecipePlanEvent(user: User, locationId: Long, recipePlanEvent: RecipePlanEventInput): RecipePlan {
        recipePlanRepository.createRecipePlanEvent(locationId = locationId, recipePlanEvent = recipePlanEvent)
        return getRecipePlan(locationId = locationId, date = recipePlanEvent.date)
    }

    @PreAuthorize("@authService.isMemberOfLocation(#user, #locationId)")
    fun updateRecipePlanEvent(user: User, locationId: Long, recipePlanEvent: RecipePlanEventInput): RecipePlan {
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

    @PreAuthorize("@authService.isMemberOfLocation(#user, #locationId)")
    fun deleteRecipePlanEvent(user: User, locationId: Long, recipePlanEvent: RecipePlanEventInput): RecipePlan {
        recipePlanRepository.deleteRecipePlanEvent(locationId = locationId, recipePlanEvent = recipePlanEvent)
        return getRecipePlan(locationId = locationId, date = recipePlanEvent.date)
    }

    private fun getRecipePlan(locationId: Long, date: LocalDate): RecipePlan {
        val weekNumber = date.get(WeekFields.ISO.weekOfWeekBasedYear())
        return RecipePlan(locationId = locationId, weekNumber = weekNumber)
    }
}