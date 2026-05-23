package com.druk.app.data

import android.content.Context
import com.druk.app.domain.AppSettings
import com.druk.app.domain.BacEngine
import com.druk.app.domain.DrinkLog
import com.druk.app.domain.FoodLevel
import com.druk.app.domain.MealLog
import com.druk.app.domain.SessionLog
import com.druk.app.domain.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DrukRepository(context: Context) {
    private val dao = DrukDatabase.get(context).dao()

    val profile: Flow<UserProfile?> = dao.observeProfile().map { it?.toDomain() }
    val settings: Flow<AppSettings> = dao.observeSettings().map { it?.toDomain() ?: AppSettings() }
    val activeSession: Flow<SessionLog?> = dao.observeActiveSession().map { it?.toDomain() }
    val completedSessions: Flow<List<SessionLog>> = dao.observeCompletedSessions().map { sessions ->
        sessions.map { it.toDomain() }
    }

    fun drinks(sessionId: Long): Flow<List<DrinkLog>> {
        return dao.observeDrinks(sessionId).map { rows -> rows.map { it.toDomain() } }
    }

    fun meals(sessionId: Long): Flow<List<MealLog>> {
        return dao.observeMeals(sessionId).map { rows -> rows.map { it.toDomain() } }
    }

    suspend fun saveProfile(profile: UserProfile) {
        dao.upsertProfile(profile.toEntity())
        ensureSettings()
    }

    suspend fun saveSettings(settings: AppSettings) {
        dao.upsertSettings(settings.toEntity())
    }

    suspend fun startSession(nowMillis: Long): Long {
        val active = dao.getActiveSession()
        return active?.id ?: dao.insertSession(SessionEntity(startedAtMillis = nowMillis))
    }

    suspend fun endActiveSession(nowMillis: Long) {
        dao.getActiveSession()?.let { dao.endSession(it.id, nowMillis) }
    }

    suspend fun logDrink(
        profile: UserProfile,
        foodLevel: FoodLevel,
        volumeMl: Double,
        abv: Double,
        nowMillis: Long
    ) {
        val sessionId = startSession(nowMillis)
        val grams = BacEngine.alcoholGrams(volumeMl, abv)
        val calories = if (abv == profile.drinkAbv) {
            (volumeMl / 30.0) * profile.caloriesPer30Ml
        } else {
            grams * 7.0
        }
        dao.insertDrink(
            DrinkEntity(
                sessionId = sessionId,
                timestampMillis = nowMillis,
                volumeMl = volumeMl,
                abv = abv,
                gramsAlcohol = grams,
                calories = calories,
                foodLevel = foodLevel.name
            )
        )
    }

    suspend fun logMeal(foodLevel: FoodLevel, offsetMinutes: Int, nowMillis: Long) {
        val sessionId = startSession(nowMillis)
        val timestamp = nowMillis - offsetMinutes.coerceAtLeast(0) * 60_000L
        dao.insertMeal(MealEntity(sessionId = sessionId, timestampMillis = timestamp, foodLevel = foodLevel.name))
        val current = dao.getSettings()?.toDomain() ?: AppSettings()
        dao.upsertSettings(current.copy(foodLevel = foodLevel).toEntity())
    }

    suspend fun deleteDrink(drinkId: Long) {
        dao.deleteDrink(drinkId)
    }

    suspend fun deleteMeal(mealId: Long) {
        dao.deleteMeal(mealId)
    }

    suspend fun undoLatest(sessionId: Long) {
        val drink = dao.getLatestDrink(sessionId)
        val meal = dao.getLatestMeal(sessionId)
        when {
            drink == null && meal == null -> Unit
            meal == null -> drink?.let { dao.deleteDrink(it.id) }
            drink == null -> dao.deleteMeal(meal.id)
            drink.timestampMillis >= meal.timestampMillis -> dao.deleteDrink(drink.id)
            else -> dao.deleteMeal(meal.id)
        }
    }

    private suspend fun ensureSettings() {
        if (dao.getSettings() == null) {
            dao.upsertSettings(AppSettings().toEntity())
        }
    }
}
