package com.druk.app.domain

import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

object BacEngine {
    private const val ETHANOL_DENSITY = 0.789
    private const val BETA_PER_MILLE_PER_HOUR = 0.15
    private const val PEG_ML = 30.0
    private const val TARGET_BAND = 0.004

    fun seidlR(sex: Sex, weightKg: Double, heightCm: Double): Double {
        val raw = when (sex) {
            Sex.Male -> 0.31608 - 0.004821 * weightKg + 0.004632 * heightCm
            Sex.Female -> 0.31223 - 0.006446 * weightKg + 0.004466 * heightCm
        }
        return when (sex) {
            Sex.Male -> raw.coerceIn(0.60, 0.87)
            Sex.Female -> raw.coerceIn(0.44, 0.80)
        }
    }

    fun alcoholGrams(volumeMl: Double, abv: Double): Double {
        return volumeMl * (abv / 100.0) * ETHANOL_DENSITY
    }

    fun bacAt(drinks: List<DrinkLog>, profile: UserProfile?, atMillis: Long): Double {
        if (profile == null || drinks.isEmpty()) return 0.0
        val distributionMass = profile.r * profile.weightKg
        val events = drinks
            .map {
                AbsorptionEvent(
                    timestampMillis = it.timestampMillis + it.foodLevel.lagMinutes * 60_000L,
                    grams = it.gramsAlcohol * (1.0 - it.foodLevel.deficit)
                )
            }
            .sortedBy { it.timestampMillis }

        var perMille = 0.0
        var previousMillis: Long? = null
        events.forEach { event ->
            if (event.timestampMillis > atMillis) return@forEach
            previousMillis?.let {
                perMille = max(0.0, perMille - BETA_PER_MILLE_PER_HOUR * hoursBetween(it, event.timestampMillis))
            }
            perMille += event.grams / distributionMass
            previousMillis = event.timestampMillis
        }
        previousMillis?.let {
            perMille = max(0.0, perMille - BETA_PER_MILLE_PER_HOUR * hoursBetween(it, atMillis))
        }
        return perMille * 0.1
    }

    fun schemePlan(scheme: TargetScheme, profile: UserProfile, foodLevel: FoodLevel): SchemePlan {
        val distributionMass = profile.r * profile.weightKg
        val effectiveGrams = alcoholGrams(PEG_ML, profile.drinkAbv) * (1.0 - foodLevel.deficit)
        val targetPerMille = scheme.targetBac * 10.0
        val pegsToReach = targetPerMille * distributionMass / effectiveGrams
        val maintenanceMinutes = (60.0 / (BETA_PER_MILLE_PER_HOUR * distributionMass / effectiveGrams))
            .toInt()
            .coerceAtLeast(10)
        return SchemePlan(
            pegsToReach = pegsToReach,
            totalMlToReach = (pegsToReach * PEG_ML).toInt(),
            rampMinutes = max(30, (pegsToReach * 18.0 / 5.0).toInt() * 5),
            rampIntervalMinutes = if (pegsToReach > 0.0) 18 else 20,
            maintenanceMinutes = maintenanceMinutes,
            soberHoursAfterStopping = targetPerMille / BETA_PER_MILLE_PER_HOUR,
            caloriesToReach = (pegsToReach * profile.caloriesPer30Ml).toInt(),
            effectiveGramsPerPeg = effectiveGrams,
            distributionMassKg = distributionMass,
            clearGramsPer10Minutes = BETA_PER_MILLE_PER_HOUR * distributionMass / 6.0,
            emptyStomachPegs = targetPerMille * distributionMass / alcoholGrams(PEG_ML, profile.drinkAbv)
        )
    }

    fun pacerState(
        profile: UserProfile?,
        session: SessionLog?,
        drinks: List<DrinkLog>,
        scheme: TargetScheme?,
        foodLevel: FoodLevel,
        nowMillis: Long
    ): PacerState {
        if (session == null || session.endedAtMillis != null) {
            return PacerState(PacerKind.Paused, "Ready when you are", "Start a session when the first drink begins.")
        }
        if (profile == null || scheme == null) {
            return PacerState(PacerKind.PickScheme, "Pick a scheme", "Choose a target zone to get a drink / wait / stop instruction.")
        }

        val plan = schemePlan(scheme, profile, foodLevel)
        val currentBac = bacAt(drinks, profile, nowMillis)
        val latestDrink = drinks.maxByOrNull { it.timestampMillis }
        val target = scheme.targetBac

        if (currentBac > target + TARGET_BAND) {
            val waitMillis = (((currentBac - target) * 10.0 / BETA_PER_MILLE_PER_HOUR) * 3_600_000.0).toLong()
            return PacerState(
                kind = PacerKind.Stop,
                title = "Stop. You are above target.",
                detail = "Wait before another drink if you want to hold ${scheme.label}.",
                remainingMillis = waitMillis
            )
        }

        val intervalMinutes = if (currentBac < target - TARGET_BAND) {
            plan.rampIntervalMinutes.coerceAtLeast(8)
        } else {
            plan.maintenanceMinutes.coerceAtLeast(10)
        }
        val intervalMillis = intervalMinutes * 60_000L
        val dueAtMillis = latestDrink?.timestampMillis?.plus(intervalMillis) ?: session.startedAtMillis
        val leftMillis = dueAtMillis - nowMillis
        val elapsedMillis = latestDrink?.let { nowMillis - it.timestampMillis } ?: intervalMillis
        val progress = ((elapsedMillis.toDouble() / intervalMillis.toDouble()) * 100.0).toInt().coerceIn(0, 100)
        val pegsLeft = max(0, ceil(((target * 10.0 - currentBac * 10.0) * plan.distributionMassKg) / plan.effectiveGramsPerPeg).toInt())

        return if (latestDrink == null || leftMillis <= 0L) {
            PacerState(
                kind = PacerKind.DrinkNow,
                title = "Pacing window open",
                detail = if (currentBac < target - TARGET_BAND) {
                    "Ramp phase for ${scheme.label}. About $pegsLeft more 30 ml peg${if (pegsLeft == 1) "" else "s"} to target."
                } else {
                    "Hold phase for ${scheme.label}. Then wait about ${plan.maintenanceMinutes} min."
                },
                progressPercent = 100
            )
        } else {
            PacerState(
                kind = PacerKind.Wait,
                title = "Wait ${formatShort(leftMillis)}",
                detail = if (currentBac < target - TARGET_BAND) {
                    "Ramp phase: one 30 ml peg every $intervalMinutes min."
                } else {
                    "Hold phase: one 30 ml peg every ${plan.maintenanceMinutes} min."
                },
                remainingMillis = leftMillis,
                progressPercent = progress
            )
        }
    }

    fun actualCurve(
        drinks: List<DrinkLog>,
        profile: UserProfile?,
        session: SessionLog?,
        nowMillis: Long
    ): List<BacPoint> {
        if (profile == null || session == null) return emptyList()
        val endMillis = max(nowMillis, session.startedAtMillis + 9 * 3_600_000L)
        val points = mutableListOf<BacPoint>()
        var at = session.startedAtMillis
        while (at <= endMillis) {
            points += BacPoint(hoursBetween(session.startedAtMillis, at), bacAt(drinks, profile, at))
            at += 15 * 60_000L
        }
        return points
    }

    fun targetCurve(scheme: TargetScheme?, foodLevel: FoodLevel): List<BacPoint> {
        if (scheme == null) return emptyList()
        val points = mutableListOf<BacPoint>()
        val lagHours = foodLevel.lagMinutes / 60.0
        var t = 0.0
        while (t <= 10.0) {
            val effectiveT = max(0.0, t - lagHours)
            val y = when {
                effectiveT <= 0.75 -> scheme.targetBac * sin(PI / 2.0 * effectiveT / 0.75)
                effectiveT <= 4.75 -> scheme.targetBac
                else -> {
                    val soberHours = scheme.targetBac * 10.0 / BETA_PER_MILLE_PER_HOUR
                    max(0.0, scheme.targetBac * (1.0 - (effectiveT - 4.75) / soberHours))
                }
            }
            points += BacPoint(t, y)
            t += 0.25
        }
        return points
    }

    fun soberHours(bac: Double): Double {
        return if (bac <= 0.0) 0.0 else bac * 10.0 / BETA_PER_MILLE_PER_HOUR
    }

    fun formatShort(ms: Long): String {
        val totalMinutes = (ms / 60_000L).coerceAtLeast(0)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }

    private fun hoursBetween(startMillis: Long, endMillis: Long): Double {
        return (endMillis - startMillis).coerceAtLeast(0L) / 3_600_000.0
    }

    private data class AbsorptionEvent(
        val timestampMillis: Long,
        val grams: Double
    )
}

