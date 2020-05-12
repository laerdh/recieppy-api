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
        val recipePlan = getRecipePlan(locationId = locationId, date = recipePlanEvent.currentDate)

        val existingRecipePlanEventForDate = recipePlanRepository
                .getRecipePlanEventsForWeek(locationId = locationId, weekNumber = recipePlan.weekNumber)
                .any { it.date == recipePlanEvent.currentDate }

        if (existingRecipePlanEventForDate) {
            throw IllegalArgumentException("Recipe plan event with date (${recipePlanEvent.currentDate}) already exists.")
        }

        recipePlanRepository.createRecipePlanEvent(locationId = locationId, recipePlanEvent = recipePlanEvent)

        return recipePlan
    }

    @PreAuthorize("@authService.isMemberOfLocation(#user, #locationId)")
    fun updateRecipePlanEvent(user: User, locationId: Long, recipePlanEvent: RecipePlanEventInput): RecipePlan {
        val newRecipePlanEventDate = recipePlanEvent.newDate ?: throw IllegalArgumentException("Please provide new date")
        val recipePlan = getRecipePlan(locationId = locationId, date = newRecipePlanEventDate)

        val existingRecipePlanEventsForWeek = recipePlanRepository.getRecipePlanEventsForWeek(
                locationId = locationId,
                weekNumber = recipePlan.weekNumber
        )
        val existingRecipePlanEvent = existingRecipePlanEventsForWeek.find { it.recipeId == recipePlanEvent.recipeId && it.date == recipePlanEvent.currentDate }
                ?: throw IllegalArgumentException("No recipeId (${recipePlanEvent.recipeId}) exists on this date (${recipePlanEvent.currentDate})")

        val recipePlanEvents = HashMap<Long, List<LocalDate>>()

        existingRecipePlanEventsForWeek.find { it.date == newRecipePlanEventDate }?.let {
            recipePlanEvents[it.recipeId] = listOf(it.date, existingRecipePlanEvent.date)
        }
        recipePlanEvents[recipePlanEvent.recipeId] = listOf(existingRecipePlanEvent.date, newRecipePlanEventDate)

        recipePlanRepository.updateRecipePlanEvent(locationId = locationId, recipePlanEvents = recipePlanEvents)

        return recipePlan
    }

    @PreAuthorize("@authService.isMemberOfLocation(#user, #locationId)")
    fun deleteRecipePlanEvent(user: User, locationId: Long, recipePlanEvent: RecipePlanEventInput): RecipePlan {
        recipePlanRepository.deleteRecipePlanEvent(locationId = locationId, recipePlanEvent = recipePlanEvent)
        return getRecipePlan(locationId = locationId, date = recipePlanEvent.currentDate)
    }

    @PreAuthorize("@authService.isMemberOfLocation(#user, #locationId)")
    fun deleteRecipeFromRecipePlanEvents(user: User, locationId: Long, recipeId: Long): Boolean {
        return recipePlanRepository.deleteRecipeFromRecipePlanEvents(locationId = locationId, recipeId = recipeId)
    }

    private fun getRecipePlan(locationId: Long, date: LocalDate): RecipePlan {
        val weekNumber = date.get(WeekFields.ISO.weekOfWeekBasedYear())
        return RecipePlan(locationId = locationId, weekNumber = weekNumber)
    }
}