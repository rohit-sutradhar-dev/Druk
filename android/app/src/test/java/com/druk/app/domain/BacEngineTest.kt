package com.druk.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BacEngineTest {
    private val profile = UserProfile(
        name = "Test",
        sex = Sex.Male,
        weightKg = 75.0,
        heightCm = 175.0,
        r = BacEngine.seidlR(Sex.Male, 75.0, 175.0),
        drinkName = "Rum",
        drinkAbv = 40.0,
        caloriesPer30Ml = 65.0
    )

    @Test
    fun alcoholGramsUsesEthanolDensity() {
        assertEquals(9.468, BacEngine.alcoholGrams(30.0, 40.0), 0.001)
    }

    @Test
    fun seidlRIsClampedToExpectedRange() {
        assertTrue(BacEngine.seidlR(Sex.Male, 75.0, 175.0) in 0.60..0.87)
        assertTrue(BacEngine.seidlR(Sex.Female, 60.0, 165.0) in 0.44..0.80)
    }

    @Test
    fun foodDelayMeansDrinkDoesNotImmediatelyRaiseBac() {
        val drink = DrinkLog(
            sessionId = 1,
            timestampMillis = 0,
            startedAtMillis = 0,
            endedAtMillis = 0,
            volumeMl = 30.0,
            abv = 40.0,
            gramsAlcohol = BacEngine.alcoholGrams(30.0, 40.0),
            calories = 65.0,
            foodLevel = FoodLevel.Meal
        )

        assertEquals(0.0, BacEngine.bacAt(listOf(drink), profile, 30 * 60_000L), 0.0001)
        assertTrue(BacEngine.bacAt(listOf(drink), profile, 80 * 60_000L) > 0.0)
    }

    @Test
    fun bacEliminatesOverTime() {
        val drink = DrinkLog(
            sessionId = 1,
            timestampMillis = 0,
            startedAtMillis = 0,
            endedAtMillis = 0,
            volumeMl = 90.0,
            abv = 40.0,
            gramsAlcohol = BacEngine.alcoholGrams(90.0, 40.0),
            calories = 195.0,
            foodLevel = FoodLevel.Empty
        )

        val peak = BacEngine.bacAt(listOf(drink), profile, 35 * 60_000L)
        val later = BacEngine.bacAt(listOf(drink), profile, 3 * 3_600_000L)

        assertTrue(peak > later)
    }

    @Test
    fun schemePlansCoverDrukAndDeepEnd() {
        val druk = BacEngine.schemePlan(TargetScheme.Druk, profile, FoodLevel.Snack)
        val deepEnd = BacEngine.schemePlan(TargetScheme.DeepEnd, profile, FoodLevel.Snack)

        assertTrue(druk.pegsToReach > 0.0)
        assertTrue(deepEnd.pegsToReach > druk.pegsToReach)
        assertTrue(deepEnd.maintenanceMinutes == druk.maintenanceMinutes)
    }

    @Test
    fun pacerAsksToPickSchemeWhenNoneSelected() {
        val state = BacEngine.pacerState(
            profile = profile,
            session = SessionLog(id = 1, startedAtMillis = 0),
            drinks = emptyList(),
            scheme = null,
            foodLevel = FoodLevel.Snack,
            nowMillis = 0
        )

        assertEquals(PacerKind.PickScheme, state.kind)
    }

    @Test
    fun pacerSaysDrinkNowAtSessionStart() {
        val state = BacEngine.pacerState(
            profile = profile,
            session = SessionLog(id = 1, startedAtMillis = 0),
            drinks = emptyList(),
            scheme = TargetScheme.Druk,
            foodLevel = FoodLevel.Snack,
            nowMillis = 0
        )

        assertEquals(PacerKind.DrinkNow, state.kind)
    }
}
