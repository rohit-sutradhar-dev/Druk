package com.druk.app.domain

enum class Sex {
    Male,
    Female
}

enum class FoodLevel(
    val label: String,
    val deficit: Double,
    val lagMinutes: Int
) {
    Empty("Empty", 0.10, 30),
    Snack("Snack", 0.15, 45),
    Meal("Meal", 0.20, 75),
    Feast("Feast", 0.30, 120)
}

enum class TargetScheme(
    val label: String,
    val targetBac: Double,
    val feel: String
) {
    Whisper("Monk's Whisper", 0.02, "Soft warmth. Still subtle."),
    Druk("Druk", 0.05, "Relaxed, sharp, present."),
    LooseCannon("Loose Cannon", 0.08, "Animated. Coordination slipping."),
    DeepEnd("The Deep End", 0.10, "Clearly drunk. Slow decisions down.")
}

enum class PacerKind {
    PickScheme,
    DrinkNow,
    Wait,
    Stop,
    Paused
}

data class UserProfile(
    val name: String,
    val sex: Sex,
    val weightKg: Double,
    val heightCm: Double,
    val r: Double,
    val drinkName: String,
    val drinkAbv: Double,
    val caloriesPer30Ml: Double
)

data class DrinkLog(
    val id: Long = 0,
    val sessionId: Long,
    val timestampMillis: Long,
    val volumeMl: Double,
    val abv: Double,
    val gramsAlcohol: Double,
    val calories: Double,
    val foodLevel: FoodLevel
)

data class MealLog(
    val id: Long = 0,
    val sessionId: Long,
    val timestampMillis: Long,
    val foodLevel: FoodLevel
)

data class SessionLog(
    val id: Long = 0,
    val startedAtMillis: Long,
    val endedAtMillis: Long? = null
)

data class AppSettings(
    val selectedScheme: TargetScheme? = TargetScheme.Druk,
    val foodLevel: FoodLevel = FoodLevel.Snack,
    val notificationsEnabled: Boolean = true
)

data class SchemePlan(
    val pegsToReach: Double,
    val totalMlToReach: Int,
    val rampMinutes: Int,
    val rampIntervalMinutes: Int,
    val maintenanceMinutes: Int,
    val soberHoursAfterStopping: Double,
    val caloriesToReach: Int,
    val effectiveGramsPerPeg: Double,
    val distributionMassKg: Double,
    val clearGramsPer10Minutes: Double,
    val emptyStomachPegs: Double
)

data class PacerState(
    val kind: PacerKind,
    val title: String,
    val detail: String,
    val remainingMillis: Long? = null,
    val progressPercent: Int = 0
)

data class BacPoint(
    val hours: Double,
    val bac: Double
)

