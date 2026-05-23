package com.druk.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.druk.app.data.DrukRepository
import com.druk.app.domain.AppSettings
import com.druk.app.domain.BacEngine
import com.druk.app.domain.BacPoint
import com.druk.app.domain.DrinkLog
import com.druk.app.domain.FoodLevel
import com.druk.app.domain.MealLog
import com.druk.app.domain.PacerKind
import com.druk.app.domain.PacerState
import com.druk.app.domain.SchemePlan
import com.druk.app.domain.SessionLog
import com.druk.app.domain.Sex
import com.druk.app.domain.TargetScheme
import com.druk.app.domain.UserProfile
import com.druk.app.notifications.PacingNotifier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DrukUiState(
    val profile: UserProfile? = null,
    val settings: AppSettings = AppSettings(),
    val activeSession: SessionLog? = null,
    val completedSessions: List<SessionLog> = emptyList(),
    val drinks: List<DrinkLog> = emptyList(),
    val meals: List<MealLog> = emptyList(),
    val currentBac: Double = 0.0,
    val soberHours: Double = 0.0,
    val elapsedMillis: Long = 0L,
    val pacerState: PacerState = PacerState(PacerKind.Paused, "Ready when you are", "Start a session when the first drink begins."),
    val selectedPlan: SchemePlan? = null,
    val actualCurve: List<BacPoint> = emptyList(),
    val targetCurve: List<BacPoint> = emptyList(),
    val nowMillis: Long = System.currentTimeMillis(),
    val setup: SetupDraft = SetupDraft(),
    val customDrink: DrinkDraft = DrinkDraft(),
    val mealDraft: MealDraft = MealDraft(),
    val permissionRequested: Boolean = false
)

data class SetupDraft(
    val name: String = "",
    val sex: Sex = Sex.Male,
    val weightKg: String = "",
    val heightCm: String = "",
    val drinkName: String = "Old Monk",
    val drinkAbv: String = "42.8",
    val caloriesPer30Ml: String = "71"
)

data class DrinkDraft(
    val volumeMl: String = "30",
    val abv: String = ""
)

data class MealDraft(
    val foodLevel: FoodLevel = FoodLevel.Meal,
    val offsetMinutes: Int = 0
)

sealed interface TimelineItem {
    val id: Long
    val timestampMillis: Long

    data class Drink(val value: DrinkLog) : TimelineItem {
        override val id: Long = value.id
        override val timestampMillis: Long = value.timestampMillis
    }

    data class Meal(val value: MealLog) : TimelineItem {
        override val id: Long = value.id
        override val timestampMillis: Long = value.timestampMillis
    }
}

class DrukViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DrukRepository(application)
    private val now = MutableStateFlow(System.currentTimeMillis())
    private val _state = MutableStateFlow(DrukUiState())
    val state: StateFlow<DrukUiState> = _state.asStateFlow()
    private var lastScheduled: Pair<PacerKind, Long?>? = null

    init {
        observeState()
        startTicker()
        PacingNotifier.ensureChannel(application)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeState() {
        viewModelScope.launch {
            combine(
                repository.profile,
                repository.settings,
                repository.activeSession,
                repository.completedSessions,
                now
            ) { profile, settings, activeSession, completed, nowMillis ->
                BaseState(profile, settings, activeSession, completed, nowMillis)
            }.flatMapLatest { base ->
                val sessionId = base.activeSession?.id
                if (sessionId == null) {
                    flowOf(FullState(base, emptyList(), emptyList()))
                } else {
                    combine(repository.drinks(sessionId), repository.meals(sessionId)) { drinks, meals ->
                        FullState(base, drinks, meals)
                    }
                }
            }.collect { full ->
                publish(full)
            }
        }
    }

    private fun startTicker() {
        viewModelScope.launch {
            while (true) {
                delay(30_000L)
                now.value = System.currentTimeMillis()
            }
        }
    }

    fun updateSetup(transform: (SetupDraft) -> SetupDraft) {
        _state.update { it.copy(setup = transform(it.setup)) }
    }

    fun updateDrinkDraft(transform: (DrinkDraft) -> DrinkDraft) {
        _state.update { it.copy(customDrink = transform(it.customDrink)) }
    }

    fun updateMealDraft(transform: (MealDraft) -> MealDraft) {
        _state.update { it.copy(mealDraft = transform(it.mealDraft)) }
    }

    fun saveProfile() {
        val draft = state.value.setup
        val weight = draft.weightKg.toDoubleOrNull()?.coerceIn(30.0, 250.0) ?: return
        val height = draft.heightCm.toDoubleOrNull()?.coerceIn(100.0, 250.0) ?: return
        val abv = draft.drinkAbv.toDoubleOrNull()?.coerceIn(1.0, 99.0) ?: return
        val calories = draft.caloriesPer30Ml.toDoubleOrNull()?.coerceAtLeast(0.0) ?: return
        val profile = UserProfile(
            name = draft.name.trim().ifBlank { "You" },
            sex = draft.sex,
            weightKg = weight,
            heightCm = height,
            r = BacEngine.seidlR(draft.sex, weight, height),
            drinkName = draft.drinkName.trim().ifBlank { "House drink" },
            drinkAbv = abv,
            caloriesPer30Ml = calories
        )
        viewModelScope.launch {
            repository.saveProfile(profile)
        }
    }

    fun startSession() {
        viewModelScope.launch {
            val started = System.currentTimeMillis()
            repository.startSession(started)
            now.value = started
            scheduleFrom(state.value)
        }
    }

    fun endSession() {
        viewModelScope.launch {
            repository.endActiveSession(System.currentTimeMillis())
            PacingNotifier.cancel(getApplication<Application>())
            lastScheduled = null
            now.value = System.currentTimeMillis()
        }
    }

    fun selectScheme(scheme: TargetScheme?) {
        viewModelScope.launch {
            repository.saveSettings(state.value.settings.copy(selectedScheme = scheme))
            now.value = System.currentTimeMillis()
        }
    }

    fun selectFood(foodLevel: FoodLevel) {
        viewModelScope.launch {
            repository.saveSettings(state.value.settings.copy(foodLevel = foodLevel))
            now.value = System.currentTimeMillis()
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.saveSettings(state.value.settings.copy(notificationsEnabled = enabled))
            if (!enabled) {
                PacingNotifier.cancel(getApplication<Application>())
                lastScheduled = null
            }
        }
    }

    fun markPermissionRequested() {
        _state.update { it.copy(permissionRequested = true) }
    }

    fun logDrink(volumeMl: Double? = null) {
        val profile = state.value.profile ?: return
        val settings = state.value.settings
        val draft = state.value.customDrink
        val volume = volumeMl ?: draft.volumeMl.toDoubleOrNull()?.coerceIn(1.0, 1000.0) ?: 30.0
        val abv = draft.abv.toDoubleOrNull()?.coerceIn(1.0, 99.0) ?: profile.drinkAbv
        viewModelScope.launch {
            val at = System.currentTimeMillis()
            repository.logDrink(profile, settings, volume, abv, at)
            now.value = at
        }
    }

    fun logMeal() {
        val draft = state.value.mealDraft
        viewModelScope.launch {
            val at = System.currentTimeMillis()
            repository.logMeal(draft.foodLevel, draft.offsetMinutes, at)
            now.value = at
        }
    }

    fun undoLatest() {
        val session = state.value.activeSession ?: return
        viewModelScope.launch {
            repository.undoLatest(session.id)
            now.value = System.currentTimeMillis()
        }
    }

    fun deleteTimelineItem(item: TimelineItem) {
        viewModelScope.launch {
            when (item) {
                is TimelineItem.Drink -> repository.deleteDrink(item.id)
                is TimelineItem.Meal -> repository.deleteMeal(item.id)
            }
            now.value = System.currentTimeMillis()
        }
    }

    private fun publish(full: FullState) {
        val base = full.base
        val profile = base.profile
        val session = base.activeSession
        val nowMillis = base.nowMillis
        val currentBac = BacEngine.bacAt(full.drinks, profile, nowMillis)
        val pacer = BacEngine.pacerState(profile, session, full.drinks, base.settings.selectedScheme, base.settings.foodLevel, nowMillis)
        val plan = profile?.let { p -> base.settings.selectedScheme?.let { BacEngine.schemePlan(it, p, base.settings.foodLevel) } }
        val elapsed = session?.let { nowMillis - it.startedAtMillis } ?: 0L
        _state.update { old ->
            old.copy(
                profile = profile,
                settings = base.settings,
                activeSession = session,
                completedSessions = base.completedSessions,
                drinks = full.drinks,
                meals = full.meals,
                currentBac = currentBac,
                soberHours = BacEngine.soberHours(currentBac),
                elapsedMillis = elapsed.coerceAtLeast(0L),
                pacerState = pacer,
                selectedPlan = plan,
                actualCurve = BacEngine.actualCurve(full.drinks, profile, session, nowMillis),
                targetCurve = BacEngine.targetCurve(base.settings.selectedScheme, base.settings.foodLevel),
                nowMillis = nowMillis
            )
        }
    }

    private fun scheduleFrom(current: DrukUiState) {
        val session = current.activeSession
        if (session == null || !current.settings.notificationsEnabled) {
            PacingNotifier.cancel(getApplication<Application>())
            lastScheduled = null
            return
        }
        val pacer = current.pacerState
        val key = pacer.kind to pacer.remainingMillis
        if (key == lastScheduled) return
        lastScheduled = key
        when (pacer.kind) {
            PacerKind.Wait -> PacingNotifier.schedule(
                getApplication<Application>(),
                pacer.remainingMillis ?: 0L,
                "Druk pacing window",
                "Next pacing window is open."
            )

            PacerKind.Stop -> PacingNotifier.schedule(
                getApplication<Application>(),
                (pacer.remainingMillis ?: 30 * 60_000L).coerceAtMost(30 * 60_000L),
                "Druk pacing update",
                "You are above target. Wait before another drink."
            )

            PacerKind.DrinkNow -> PacingNotifier.schedule(
                getApplication<Application>(),
                90 * 60_000L,
                "Water break",
                "Water break."
            )

            PacerKind.PickScheme,
            PacerKind.Paused -> PacingNotifier.cancel(getApplication<Application>())
        }
    }

    private data class BaseState(
        val profile: UserProfile?,
        val settings: AppSettings,
        val activeSession: SessionLog?,
        val completedSessions: List<SessionLog>,
        val nowMillis: Long
    )

    private data class FullState(
        val base: BaseState,
        val drinks: List<DrinkLog>,
        val meals: List<MealLog>
    )
}
