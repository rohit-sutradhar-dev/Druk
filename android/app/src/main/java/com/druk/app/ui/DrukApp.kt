package com.druk.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.druk.app.domain.BacEngine
import com.druk.app.domain.BacPoint
import com.druk.app.domain.FoodLevel
import com.druk.app.domain.PacerKind
import com.druk.app.domain.Sex
import com.druk.app.domain.TargetScheme
import com.druk.app.ui.theme.MonkAmber
import com.druk.app.ui.theme.MonkAmberMuted
import com.druk.app.ui.theme.MonkBg
import com.druk.app.ui.theme.MonkBorder
import com.druk.app.ui.theme.MonkCream
import com.druk.app.ui.theme.MonkGreen
import com.druk.app.ui.theme.MonkMuted
import com.druk.app.ui.theme.MonkOrange
import com.druk.app.ui.theme.MonkRed
import com.druk.app.ui.theme.MonkSurface1
import com.druk.app.ui.theme.MonkSurface2
import com.druk.app.ui.theme.MonkSurface3
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

private val BacFormat = DecimalFormat("0.000")
private val OneDecimal = DecimalFormat("0.0")
private val ClockFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
private val Mono = FontFamily.Monospace
private val Serif = FontFamily.Serif

private enum class DesignVariant(val label: String) {
    ClassicClean("Classic Clean"),
    Focus("Focus"),
    Compact("Compact")
}

private data class DesignSpec(
    val pagePadding: Int,
    val cardPadding: Int,
    val cardRadius: Int,
    val bacSize: Int,
    val bacLineHeight: Int,
    val paceTitleSize: Int,
    val paceRingSize: Int,
    val schemeWidth: Int,
    val showFoodDetail: Boolean,
    val showFullGuide: Boolean
)

private fun DesignVariant.spec(): DesignSpec {
    return when (this) {
        DesignVariant.ClassicClean -> DesignSpec(
            pagePadding = 16,
            cardPadding = 16,
            cardRadius = 16,
            bacSize = 48,
            bacLineHeight = 50,
            paceTitleSize = 20,
            paceRingSize = 64,
            schemeWidth = 205,
            showFoodDetail = true,
            showFullGuide = true
        )
        DesignVariant.Focus -> DesignSpec(
            pagePadding = 16,
            cardPadding = 16,
            cardRadius = 18,
            bacSize = 56,
            bacLineHeight = 58,
            paceTitleSize = 22,
            paceRingSize = 72,
            schemeWidth = 190,
            showFoodDetail = false,
            showFullGuide = false
        )
        DesignVariant.Compact -> DesignSpec(
            pagePadding = 14,
            cardPadding = 14,
            cardRadius = 12,
            bacSize = 40,
            bacLineHeight = 42,
            paceTitleSize = 18,
            paceRingSize = 56,
            schemeWidth = 178,
            showFoodDetail = false,
            showFullGuide = false
        )
    }
}

@Composable
fun DrukApp(
    state: DrukUiState,
    actions: DrukViewModel,
    requestNotificationPermission: () -> Unit
) {
    var variant by remember { mutableStateOf(DesignVariant.ClassicClean) }
    val spec = variant.spec()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MonkBg)
    ) {
        if (state.profile == null) {
            SetupScreen(state, actions)
        } else {
            MainScreen(state, actions, requestNotificationPermission, variant, spec) { variant = it }
        }
    }
}

@Composable
private fun SetupScreen(state: DrukUiState, actions: DrukViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF3D2010), MonkBg),
                    radius = 1100f
                )
            )
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Text("🥃", fontSize = 42.sp)
            Text(
                "Monk Mode",
                color = MonkAmber,
                fontFamily = Serif,
                fontSize = 38.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 3.sp
            )
            Text(
                "Widmark · Seidl 2000 · Personal BAC planner",
                color = MonkMuted,
                fontFamily = Mono,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )
        }
        item {
            SetupLabel("About you")
            MonkField("Name", state.setup.name) { value -> actions.updateSetup { it.copy(name = value) } }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectPill("Male", state.setup.sex == Sex.Male, Modifier.weight(1f)) {
                    actions.updateSetup { it.copy(sex = Sex.Male) }
                }
                SelectPill("Female", state.setup.sex == Sex.Female, Modifier.weight(1f)) {
                    actions.updateSetup { it.copy(sex = Sex.Female) }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("Weight kg", state.setup.weightKg, Modifier.weight(1f)) { value ->
                    actions.updateSetup { it.copy(weightKg = value) }
                }
                NumberField("Height cm", state.setup.heightCm, Modifier.weight(1f)) { value ->
                    actions.updateSetup { it.copy(heightCm = value) }
                }
            }
            SetupLabel("Your drink", Modifier.padding(top = 18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MonkField("Drink name", state.setup.drinkName, Modifier.weight(1f)) { value ->
                    actions.updateSetup { it.copy(drinkName = value) }
                }
                NumberField("ABV %", state.setup.drinkAbv, Modifier.weight(1f)) { value ->
                    actions.updateSetup { it.copy(drinkAbv = value) }
                }
            }
            NumberField("Calories per 30 ml", state.setup.caloriesPer30Ml) { value ->
                actions.updateSetup { it.copy(caloriesPer30Ml = value) }
            }
            AmberButton("Set up →", Modifier.padding(top = 12.dp), actions::saveProfile)
        }
    }
}

@Composable
private fun MainScreen(
    state: DrukUiState,
    actions: DrukViewModel,
    requestNotificationPermission: () -> Unit,
    variant: DesignVariant,
    spec: DesignSpec,
    onVariantChange: (DesignVariant) -> Unit
) {
    val timeline = (state.drinks.map { TimelineItem.Drink(it) } + state.meals.map { TimelineItem.Meal(it) })
        .sortedByDescending { it.timestampMillis }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(MonkBg),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { HeaderBar(state, spec) }
        item { DesignSwitcher(variant, onVariantChange, spec) }
        item { SessionCard(state, actions, requestNotificationPermission, spec) }
        if (state.activeSession != null) {
            item { LogButtons(state, actions, spec) }
        }
        item { FoodSelector(state, actions, spec) }
        item { SchemeSelector(state, actions, spec) }
        item { PlanGuide(state, spec) }
        item { BacCurve(state.actualCurve, state.targetCurve, state.settings.selectedScheme, spec) }
        item { TimelineCard(timeline, actions, state, spec) }
        item { Footer(state, actions, spec) }
    }
}

@Composable
private fun HeaderBar(state: DrukUiState, spec: DesignSpec) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = spec.pagePadding.dp, end = spec.pagePadding.dp, top = 8.dp, bottom = 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(state.profile?.name?.ifBlank { "Monk Mode" } ?: "Monk Mode", color = MonkCream, fontFamily = Mono, fontSize = 13.sp)
        GhostButton("Edit profile") { }
    }
}

@Composable
private fun DesignSwitcher(
    variant: DesignVariant,
    onVariantChange: (DesignVariant) -> Unit,
    spec: DesignSpec
) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = spec.pagePadding.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DesignVariant.entries.forEach { option ->
            GhostButton(
                text = option.label,
                selected = variant == option,
                onClick = { onVariantChange(option) }
            )
        }
    }
}

@Composable
private fun SessionCard(
    state: DrukUiState,
    actions: DrukViewModel,
    requestNotificationPermission: () -> Unit,
    spec: DesignSpec
) {
    DarkCard(
        modifier = Modifier.padding(horizontal = spec.pagePadding.dp),
        background = Brush.linearGradient(listOf(Color(0xFF2E1A0F), MonkSurface1)),
        radius = spec.cardRadius,
        padding = spec.cardPadding
    ) {
        if (state.activeSession == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Ready when you are.",
                    color = MonkMuted,
                    fontFamily = Serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 15.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Pacing alerts", color = MonkMuted, fontFamily = Mono, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Switch(
                        checked = state.settings.notificationsEnabled,
                        onCheckedChange = {
                            actions.setNotificationsEnabled(it)
                            if (it) requestNotificationPermission()
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = MonkAmber, checkedTrackColor = MonkAmberMuted)
                    )
                }
                AmberButton("⋄ First drink now") {
                    requestNotificationPermission()
                    actions.startSession()
                }
            }
        } else {
            ActiveSessionContent(state, actions, spec)
        }
    }
}

@Composable
private fun ActiveSessionContent(state: DrukUiState, actions: DrukViewModel, spec: DesignSpec) {
    val status = bacStatus(state.currentBac)
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 0.5.dp, color = Color.Transparent)
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "${BacFormat.format(state.currentBac)}%",
                color = status.color,
                fontFamily = Mono,
                fontSize = spec.bacSize.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = spec.bacLineHeight.sp
            )
            Text("${OneDecimal.format(state.currentBac * 10)}‰", color = MonkMuted, fontFamily = Mono, fontSize = 14.sp)
            Text(status.label, color = status.color, fontFamily = Serif, fontStyle = FontStyle.Italic, fontSize = 16.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Meta("Session", formatDuration(state.elapsedMillis), Modifier.weight(1f))
            Meta("Drinks", state.drinks.size.toString(), Modifier.weight(1f))
            Meta("Calories", "${state.drinks.sumOf { it.calories }.toInt()} kcal", Modifier.weight(1f))
            Meta("Sober in", if (state.soberHours <= 0.05) "Now ✓" else "${OneDecimal.format(state.soberHours)}h", Modifier.weight(1f))
        }
        PaceCard(state, spec)
        GhostButton("Undo last log", enabled = state.drinks.isNotEmpty() || state.meals.isNotEmpty(), onClick = actions::undoLatest)
    }
}

@Composable
private fun PaceCard(state: DrukUiState, spec: DesignSpec) {
    val kind = state.pacerState.kind
    val color = when (kind) {
        PacerKind.DrinkNow -> MonkGreen
        PacerKind.Wait -> MonkAmber
        PacerKind.Stop -> MonkRed
        else -> MonkMuted
    }
    val label = when (kind) {
        PacerKind.DrinkNow -> "due"
        PacerKind.Stop -> "wait"
        else -> "left"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
            .border(1.dp, MonkBorder, RoundedCornerShape(14.dp))
            .padding(if (spec.cardPadding < 16) 12.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                if (state.settings.selectedScheme == null) "Pacer" else "Next peg",
                color = MonkAmberMuted,
                fontFamily = Mono,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Text(state.pacerState.title, color = color, fontFamily = Serif, fontSize = spec.paceTitleSize.sp, fontWeight = FontWeight.Bold, lineHeight = (spec.paceTitleSize + 2).sp)
            Text(state.pacerState.detail, color = MonkMuted, fontFamily = Mono, fontSize = 11.sp, lineHeight = 16.sp)
        }
        PaceRing(
            percent = state.pacerState.progressPercent,
            color = color,
            sizeDp = spec.paceRingSize,
            text = when (kind) {
                PacerKind.DrinkNow -> "now"
                PacerKind.Wait, PacerKind.Stop -> state.pacerState.remainingMillis?.let(BacEngine::formatShort) ?: "—"
                else -> "—"
            },
            label = label
        )
    }
}

@Composable
private fun PaceRing(percent: Int, color: Color, sizeDp: Int, text: String, label: String) {
    val outer = sizeDp + 8
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(outer.dp)) {
        Canvas(modifier = Modifier.size(sizeDp.dp)) {
            drawCircle(Color.White.copy(alpha = 0.08f))
            drawArc(color, -90f, 360f * percent.coerceIn(0, 100) / 100f, useCenter = true)
            drawCircle(MonkSurface1, radius = size.minDimension / 2f - 7.dp.toPx())
            drawCircle(MonkBorder.copy(alpha = 0.75f), radius = size.minDimension / 2f - 7.dp.toPx(), style = Stroke(1.dp.toPx()))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text, color = MonkCream, fontFamily = Mono, fontSize = 10.sp, lineHeight = 11.sp)
            Text(label, color = MonkMuted, fontFamily = Mono, fontSize = 8.sp, lineHeight = 9.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogButtons(state: DrukUiState, actions: DrukViewModel, spec: DesignSpec) {
    var showDrink by remember { mutableStateOf(false) }
    var showMeal by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.padding(horizontal = spec.pagePadding.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ActionButton("+ Log a drink", MonkSurface3, MonkAmber, Modifier.weight(2f)) { showDrink = true }
        ActionButton("🍛 Log meal", MonkSurface2, MonkGreen, Modifier.weight(1f)) { showMeal = true }
    }

    if (showDrink) {
        ModalBottomSheet(
            onDismissRequest = { showDrink = false },
            containerColor = MonkSurface2,
            contentColor = MonkCream,
            dragHandle = null
        ) {
            var selectedVolume by remember(showDrink) { mutableStateOf(30.0) }
            var selectedFood by remember(showDrink) { mutableStateOf(state.settings.foodLevel) }
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Log a drink", color = MonkAmber, fontFamily = Serif, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(state.profile?.drinkName.orEmpty(), color = MonkCream, fontFamily = Mono, fontSize = 13.sp, modifier = Modifier.background(MonkSurface3, CircleShape).padding(horizontal = 14.dp, vertical = 6.dp))
                Text("Volume", color = MonkMuted, fontFamily = Mono, fontSize = 11.sp, letterSpacing = 1.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(30.0 to "30 ml · chhota", 60.0 to "60 ml · large").forEach { (ml, label) ->
                        GhostButton(label, Modifier.weight(1f), selected = selectedVolume == ml) { selectedVolume = ml }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(90.0 to "90 ml").forEach { (ml, label) ->
                        GhostButton(label, Modifier.weight(1f), selected = selectedVolume == ml) { selectedVolume = ml }
                    }
                    GhostButton("Custom →", Modifier.weight(1f), selected = selectedVolume == 0.0) { selectedVolume = 0.0 }
                }
                Text("Stomach right now", color = MonkMuted, fontFamily = Mono, fontSize = 11.sp, letterSpacing = 1.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FoodLevel.entries.forEach { food ->
                        FoodMini(food, selectedFood == food, Modifier.weight(1f)) {
                            selectedFood = food
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField("Custom ml", state.customDrink.volumeMl, Modifier.weight(1f)) { value ->
                        actions.updateDrinkDraft { it.copy(volumeMl = value) }
                    }
                    NumberField("Different ABV?", state.customDrink.abv, Modifier.weight(1f)) { value ->
                        actions.updateDrinkDraft { it.copy(abv = value) }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 4.dp)) {
                    GhostButton("Cancel", Modifier.weight(1f)) { showDrink = false }
                    AmberButton("Log it", Modifier.weight(2f)) {
                        val volume = if (selectedVolume == 0.0) null else selectedVolume
                        actions.logDrink(volumeMl = volume, foodLevel = selectedFood)
                        showDrink = false
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (showMeal) {
        ModalBottomSheet(
            onDismissRequest = { showMeal = false },
            containerColor = MonkSurface2,
            contentColor = MonkCream,
            dragHandle = null
        ) {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("🍛 Log a meal", color = MonkGreen, fontFamily = Serif, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("What did you eat?", color = MonkMuted, fontFamily = Mono, fontSize = 11.sp, letterSpacing = 1.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FoodLevel.entries.drop(1).forEach { food ->
                        FoodMini(food, state.mealDraft.foodLevel == food, Modifier.weight(1f), green = true) {
                            actions.updateMealDraft { it.copy(foodLevel = food) }
                        }
                    }
                }
                Text("When?", color = MonkMuted, fontFamily = Mono, fontSize = 11.sp, letterSpacing = 1.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(0 to "Just now", 30 to "30 min ago").forEach { (offset, label) ->
                        GhostButton(label, Modifier.weight(1f), selected = state.mealDraft.offsetMinutes == offset) {
                            actions.updateMealDraft { it.copy(offsetMinutes = offset) }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(60 to "1 hour ago", 120 to "2 hours ago").forEach { (offset, label) ->
                        GhostButton(label, Modifier.weight(1f), selected = state.mealDraft.offsetMinutes == offset) {
                            actions.updateMealDraft { it.copy(offsetMinutes = offset) }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 4.dp)) {
                    GhostButton("Cancel", Modifier.weight(1f)) { showMeal = false }
                    Button(
                        onClick = {
                            actions.logMeal()
                            showMeal = false
                        },
                        modifier = Modifier.weight(2f).height(52.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MonkGreen, contentColor = MonkBg)
                    ) {
                        Text("Log meal", fontFamily = Serif, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun FoodSelector(state: DrukUiState, actions: DrukViewModel, spec: DesignSpec) {
    SectionLabel("Stomach level", "(affects absorption — tap to change)", spec)
    Row(
        modifier = Modifier.padding(horizontal = spec.pagePadding.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        FoodLevel.entries.forEach { food ->
            FoodMini(food, state.settings.foodLevel == food, Modifier.weight(1f)) {
                actions.selectFood(food)
            }
        }
    }
    val food = state.settings.foodLevel
    if (spec.showFoodDetail) {
        DarkCard(modifier = Modifier.padding(horizontal = spec.pagePadding.dp), backgroundColor = MonkSurface1, radius = 9, padding = 12) {
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = MonkCream, fontWeight = FontWeight.Bold)) {
                        append("${food.emoji()} ${food.label}")
                    }
                    append(" — ${(food.deficit * 100).toInt()}% of each peg absorbed before reaching blood.\n")
                    append("Peak BAC arrives ~${food.lagMinutes} min after drinking, not immediately.")
                },
                color = MonkMuted,
                fontFamily = Mono,
                fontSize = 11.sp,
                lineHeight = 18.sp
            )
            Text("Elimination rate (0.15‰/hr) is unchanged — food only slows absorption.", color = MonkAmberMuted, fontFamily = Mono, fontSize = 10.sp)
        }
    }
}

@Composable
private fun SchemeSelector(state: DrukUiState, actions: DrukViewModel, spec: DesignSpec) {
    SectionLabel("Drinking schemes", "(tap to show plan)", spec)
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = spec.pagePadding.dp),
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        TargetScheme.entries.forEach { scheme ->
            val selected = state.settings.selectedScheme == scheme
            val plan = state.profile?.let { BacEngine.schemePlan(scheme, it, state.settings.foodLevel) }
            SchemeCard(scheme, plan, selected, spec) {
                actions.selectScheme(if (selected) null else scheme)
            }
        }
    }
}

@Composable
private fun SchemeCard(scheme: TargetScheme, plan: com.druk.app.domain.SchemePlan?, selected: Boolean, spec: DesignSpec, onClick: () -> Unit) {
    val color = scheme.color()
    Column(
        modifier = Modifier
            .width(spec.schemeWidth.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MonkSurface2)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) color else MonkBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(spec.cardPadding.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(scheme.emoji(), fontSize = 18.sp)
            Text(scheme.label, color = if (selected) color else MonkCream, fontFamily = Serif, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("${BacFormat.format(scheme.targetBac)}%", color = color, fontFamily = Mono, fontSize = 11.sp, modifier = Modifier.background(Color.Black.copy(alpha = 0.30f), RoundedCornerShape(4.dp)).padding(horizontal = 5.dp, vertical = 2.dp))
        }
        Text(scheme.feel, color = MonkMuted, fontFamily = Serif, fontStyle = FontStyle.Italic, fontSize = 12.sp, lineHeight = 17.sp)
        if (plan != null) {
            SchemeNumber("${OneDecimal.format(plan.pegsToReach)} × 30ml pegs to reach")
            SchemeNumber("1 peg every ${plan.maintenanceMinutes} min to hold")
            SchemeNumber("~${plan.caloriesToReach} kcal to reach")
            SchemeNumber("Sober ${OneDecimal.format(plan.soberHoursAfterStopping)}h after stopping")
        }
    }
}

@Composable
private fun PlanGuide(state: DrukUiState, spec: DesignSpec) {
    SectionLabel("Your drinking guide", spec = spec)
    DarkCard(modifier = Modifier.padding(horizontal = spec.pagePadding.dp), backgroundColor = MonkSurface1, radius = spec.cardRadius, padding = 0) {
        val plan = state.selectedPlan
        val scheme = state.settings.selectedScheme
        val profile = state.profile
        if (plan == null || scheme == null || profile == null) {
            Text(
                "Select a scheme above for your step-by-step guide.",
                color = MonkMuted,
                fontFamily = Serif,
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp,
                modifier = Modifier.padding(24.dp)
            )
        } else {
            GuideStep("Step 1 — Ramp up", "Drink ${plan.totalMlToReach} ml over the first ${plan.rampMinutes} minutes") {
                append("That's one 30ml peg roughly every ")
                strong("${plan.rampIntervalMinutes} minutes")
                append(". You're drinking faster than your liver clears, so BAC climbs to ${BacFormat.format(scheme.targetBac)}%.")
                if (scheme == TargetScheme.Druk) {
                    append("\nThis is the Another Round (Druk) protocol — Phase 2 discipline is everything.")
                }
            }
            GuideStep("Step 2 — Hold", "Then 30 ml every ${plan.maintenanceMinutes} minutes") {
                append("Start this clock ")
                strong("after Step 1 is done")
                append(", not from the first drink. One peg every ${plan.maintenanceMinutes} min roughly matches what your liver clears.")
            }
            GuideStep("To stop", "Just put the glass down — sober in ${OneDecimal.format(plan.soberHoursAfterStopping)} hours") {
                append("Your liver clears ")
                strong("${OneDecimal.format(plan.clearGramsPer10Minutes)}g every 10 minutes")
                append(", no matter what. Coffee, food, and water don't speed it up.")
            }
            val food = state.settings.foodLevel
            GuideStep("${food.emoji()} Food — ${food.label} (${(food.deficit * 100).toInt()}% deficit)", "You'll feel it around ${food.lagMinutes} min in, not immediately") {
                append("${(food.deficit * 100).toInt()}% of each peg is broken down before reaching blood — which is why you need ")
                strong("${plan.totalMlToReach}ml")
                append(" instead of the empty-stomach ${(plan.emptyStomachPegs * 30).toInt()}ml.")
            }
            if (spec.showFullGuide) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.20f))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    MetaRow("Your Seidl r", OneDecimal.format(profile.r))
                    MetaRow("Distribution mass", "${OneDecimal.format(plan.distributionMassKg)} kg")
                    MetaRow("Effective g per 30ml peg", "${OneDecimal.format(plan.effectiveGramsPerPeg)}g after deficit")
                    MetaRow("Formula", "Seidl 2000 + Widmark 1932")
                }
            }
            Text(
                "Real BAC varies ±30% with hydration, sleep, medications and individual enzyme differences. Never drive.",
                color = Color(0xFFD07060),
                fontFamily = Mono,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MonkRed.copy(alpha = 0.10f))
                    .border(0.5.dp, MonkRed.copy(alpha = 0.25f))
                    .padding(14.dp)
            )
        }
    }
}

@Composable
private fun BacCurve(actual: List<BacPoint>, target: List<BacPoint>, scheme: TargetScheme?, spec: DesignSpec) {
    SectionLabel("BAC curve", spec = spec)
    DarkCard(modifier = Modifier.padding(horizontal = spec.pagePadding.dp), backgroundColor = MonkSurface1, radius = spec.cardRadius, padding = spec.cardPadding) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val maxX = max((actual + target).maxOfOrNull { it.hours } ?: 10.0, 10.0)
            val maxY = max((actual + target).maxOfOrNull { it.bac } ?: 0.12, 0.12)
            val grid = MonkBorder.copy(alpha = 0.40f)
            repeat(5) { i ->
                val y = size.height * i / 4f
                drawLine(grid, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            }
            fun map(point: BacPoint): Offset {
                val x = (point.hours / maxX).toFloat() * size.width
                val y = size.height - (point.bac / maxY).toFloat() * size.height
                return Offset(x, y)
            }
            fun drawPathLine(points: List<BacPoint>, color: Color, width: Float) {
                if (points.size < 2) return
                val path = Path().apply {
                    moveTo(map(points.first()).x, map(points.first()).y)
                    points.drop(1).forEach { lineTo(map(it).x, map(it).y) }
                }
                drawPath(path, color = color, style = Stroke(width = width, cap = StrokeCap.Round))
            }
            TargetScheme.entries.forEach {
                val y = size.height - (it.targetBac / maxY).toFloat() * size.height
                drawLine(it.color().copy(alpha = 0.34f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            }
            drawPathLine(target, (scheme?.color() ?: MonkAmber).copy(alpha = 0.85f), 3f)
            drawPathLine(actual, MonkAmber, 5f)
        }
        Text("Actual BAC · ${scheme?.label ?: "target plan"} · horizontal target bands", color = MonkMuted, fontFamily = Mono, fontSize = 10.sp)
    }
}

@Composable
private fun TimelineCard(items: List<TimelineItem>, actions: DrukViewModel, state: DrukUiState, spec: DesignSpec) {
    SectionLabel("Timeline", spec = spec)
    Column(modifier = Modifier.padding(horizontal = spec.pagePadding.dp)) {
        if (items.isEmpty()) {
            Text("Nothing logged yet.", color = MonkMuted, fontFamily = Serif, fontStyle = FontStyle.Italic, fontSize = 14.sp, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp))
        } else {
            items.forEach { item ->
                TimelineRow(item, state, actions)
            }
        }
    }
}

@Composable
private fun TimelineRow(item: TimelineItem, state: DrukUiState, actions: DrukViewModel) {
    val isMeal = item is TimelineItem.Meal
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isMeal) MonkGreen.copy(alpha = 0.05f) else Color.Transparent)
            .border(0.5.dp, if (isMeal) MonkGreen.copy(alpha = 0.20f) else MonkBorder)
            .padding(vertical = 10.dp, horizontal = if (isMeal) 10.dp else 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "${ClockFormat.format(Date(item.timestampMillis))}  +${state.activeSession?.let { OneDecimal.format((item.timestampMillis - it.startedAtMillis) / 3_600_000.0) } ?: "0.0"}h",
                color = if (isMeal) MonkGreen else MonkAmber,
                fontFamily = Mono,
                fontSize = 12.sp
            )
            when (item) {
                is TimelineItem.Drink -> {
                    Text(
                        "${state.profile?.drinkName.orEmpty()} · ${item.value.volumeMl.toInt()}ml · ${OneDecimal.format(item.value.abv)}%ABV · ${item.value.foodLevel.emoji()}${(item.value.foodLevel.deficit * 100).toInt()}%↓",
                        color = MonkMuted,
                        fontSize = 12.sp
                    )
                    Text("${OneDecimal.format(item.value.gramsAlcohol)}g → ${OneDecimal.format(item.value.gramsAlcohol * (1.0 - item.value.foodLevel.deficit))}g absorbed", color = MonkCream, fontFamily = Mono, fontSize = 11.sp)
                }
                is TimelineItem.Meal -> {
                    Text("${item.value.foodLevel.emoji()} ${item.value.foodLevel.label} logged — ${(item.value.foodLevel.deficit * 100).toInt()}% deficit · peaks +${item.value.foodLevel.lagMinutes}min", color = MonkMuted, fontSize = 12.sp)
                    Text("Food event", color = MonkGreen, fontFamily = Mono, fontSize = 11.sp)
                }
            }
        }
        TextButton(onClick = { actions.deleteTimelineItem(item) }) {
            Text("Delete", color = MonkMuted, fontFamily = Mono, fontSize = 11.sp)
        }
    }
}

@Composable
private fun Footer(state: DrukUiState, actions: DrukViewModel, spec: DesignSpec) {
    Column(
        modifier = Modifier
            .padding(horizontal = spec.pagePadding.dp, vertical = 16.dp)
            .navigationBarsPadding()
            .border(0.5.dp, MonkBorder)
            .padding(top = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Seidl 2000 + Widmark · r = ${state.profile?.let { DecimalFormat("0.000").format(it.r) } ?: "—"} · β = 0.15‰/hr", color = MonkMuted, fontFamily = Mono, fontSize = 10.sp)
        Text("Estimates ±30% variance. Never drive, even at sweet spot.", color = MonkRed, fontFamily = Mono, fontSize = 10.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = actions::endSession, enabled = state.activeSession != null) {
                Text("Clear session", color = MonkMuted, fontFamily = Mono, fontSize = 11.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Alerts", color = MonkMuted, fontFamily = Mono, fontSize = 11.sp)
                Switch(
                    checked = state.settings.notificationsEnabled,
                    onCheckedChange = actions::setNotificationsEnabled,
                    colors = SwitchDefaults.colors(checkedThumbColor = MonkAmber, checkedTrackColor = MonkAmberMuted)
                )
            }
        }
    }
}

@Composable
private fun DarkCard(
    modifier: Modifier = Modifier,
    background: Brush? = null,
    backgroundColor: Color = MonkSurface2,
    radius: Int = 14,
    padding: Int = 16,
    content: @Composable ColumnScope.() -> Unit
) {
    val base = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(radius.dp))
        .then(if (background != null) Modifier.background(background) else Modifier.background(backgroundColor))
        .border(1.dp, MonkBorder, RoundedCornerShape(radius.dp))
        .padding(padding.dp)
    Column(modifier = base, verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
}

@Composable
private fun SectionLabel(title: String, small: String? = null, spec: DesignSpec) {
    Text(
        buildAnnotatedString {
            append(title.uppercase())
            if (small != null) {
                append(" ")
                withStyle(SpanStyle(color = MonkMuted, fontSize = 10.sp, letterSpacing = 0.sp)) { append(small) }
            }
        },
        color = MonkAmberMuted,
        fontFamily = Mono,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(start = spec.pagePadding.dp, end = spec.pagePadding.dp, top = 8.dp)
    )
}

@Composable
private fun SetupLabel(text: String, modifier: Modifier = Modifier) {
    Text(text.uppercase(), color = MonkAmberMuted, fontFamily = Mono, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, modifier = modifier.fillMaxWidth().padding(bottom = 8.dp))
}

@Composable
private fun MonkField(label: String, value: String, modifier: Modifier = Modifier, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, fontFamily = Mono) },
        modifier = modifier.fillMaxWidth().padding(bottom = 10.dp),
        singleLine = true,
        colors = fieldColors()
    )
}

@Composable
private fun NumberField(label: String, value: String, modifier: Modifier = Modifier, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> onChange(input.filter { it.isDigit() || it == '.' }.take(6)) },
        label = { Text(label, fontFamily = Mono) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.fillMaxWidth().padding(bottom = 10.dp),
        singleLine = true,
        colors = fieldColors()
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MonkCream,
    unfocusedTextColor = MonkCream,
    focusedContainerColor = MonkSurface2,
    unfocusedContainerColor = MonkSurface2,
    focusedBorderColor = MonkAmber,
    unfocusedBorderColor = MonkBorder,
    focusedLabelColor = MonkAmber,
    unfocusedLabelColor = MonkMuted,
    cursorColor = MonkAmber
)

@Composable
private fun AmberButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MonkAmber, contentColor = MonkBg)
    ) {
        Text(text, fontFamily = Serif, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ActionButton(text: String, bg: Color, fg: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, if (fg == MonkGreen) Color(0xFF3A6E3C) else MonkBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = fg, fontFamily = Serif, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun GhostButton(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) MonkSurface3 else Color.Transparent)
            .border(1.dp, if (selected) MonkAmber else MonkBorder, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) MonkAmber else MonkMuted, fontFamily = Mono, fontSize = 11.sp)
    }
}

@Composable
private fun SelectPill(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(if (selected) MonkSurface3 else MonkSurface2)
            .border(1.5.dp, if (selected) MonkAmber else MonkBorder, RoundedCornerShape(9.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) MonkAmber else MonkCream, fontFamily = Mono, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FoodMini(food: FoodLevel, selected: Boolean, modifier: Modifier = Modifier, green: Boolean = false, onClick: () -> Unit) {
    val accent = if (green) MonkGreen else MonkAmber
    Column(
        modifier = modifier
            .height(70.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) MonkSurface3 else MonkSurface2)
            .border(1.5.dp, if (selected) accent else MonkBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(food.emoji(), fontSize = 17.sp)
        Text(food.label, color = if (selected) accent else MonkMuted, fontFamily = Mono, fontSize = 9.sp, lineHeight = 11.sp)
        Text("${(food.deficit * 100).toInt()}% off", color = MonkMuted, fontFamily = Mono, fontSize = 8.sp, lineHeight = 10.sp)
    }
}

@Composable
private fun Meta(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label.uppercase(), color = MonkMuted, fontFamily = Mono, fontSize = 9.sp, letterSpacing = 0.8.sp)
        Text(value, color = MonkCream, fontFamily = Mono, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun SchemeNumber(text: String) {
    Text(text, color = MonkMuted, fontFamily = Mono, fontSize = 11.sp, lineHeight = 15.sp)
}

@Composable
private fun GuideStep(label: String, main: String, sub: AnnotatedBuilder.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, MonkBorder)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(label.uppercase(), color = MonkAmberMuted, fontFamily = Mono, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
        Text(main, color = MonkCream, fontFamily = Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, lineHeight = 22.sp)
        val builder = AnnotatedBuilder()
        builder.sub()
        Text(builder.value, color = MonkMuted, fontFamily = Mono, fontSize = 11.sp, lineHeight = 17.sp)
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MonkMuted, fontFamily = Mono, fontSize = 11.sp)
        Text(value, color = MonkCream, fontFamily = Mono, fontSize = 11.sp)
    }
}

private class AnnotatedBuilder {
    private val parts = mutableListOf<Pair<String, Boolean>>()
    fun append(text: String) {
        parts += text to false
    }
    fun strong(text: String) {
        parts += text to true
    }
    val value
        get() = buildAnnotatedString {
            parts.forEach { (text, strong) ->
                if (strong) withStyle(SpanStyle(color = MonkCream, fontWeight = FontWeight.Bold)) { append(text) } else append(text)
            }
        }
}

private data class BacStatus(val label: String, val color: Color)

private fun bacStatus(bac: Double): BacStatus {
    return when {
        bac <= 0.005 -> BacStatus("Clear", MonkMuted)
        bac <= 0.03 -> BacStatus("Monk's Whisper 🌿", MonkGreen)
        bac <= 0.06 -> BacStatus("Druk zone ✦", MonkAmber)
        bac <= 0.09 -> BacStatus("Buzzed 🔥", MonkOrange)
        bac <= 0.15 -> BacStatus("Drunk 💀", MonkRed)
        else -> BacStatus("Danger — stop", MonkRed)
    }
}

private fun FoodLevel.emoji(): String {
    return when (this) {
        FoodLevel.Empty -> "😶"
        FoodLevel.Snack -> "🍌"
        FoodLevel.Meal -> "🍛"
        FoodLevel.Feast -> "🥘"
    }
}

private fun TargetScheme.emoji(): String {
    return when (this) {
        TargetScheme.Whisper -> "🌿"
        TargetScheme.Druk -> "🥃"
        TargetScheme.LooseCannon -> "🔥"
        TargetScheme.DeepEnd -> "💀"
    }
}

private fun TargetScheme.color(): Color {
    return when (this) {
        TargetScheme.Whisper -> MonkGreen
        TargetScheme.Druk -> MonkAmber
        TargetScheme.LooseCannon -> MonkOrange
        TargetScheme.DeepEnd -> MonkRed
    }
}

private fun formatDuration(ms: Long): String {
    val minutes = (ms / 60_000L).coerceAtLeast(0)
    return "${minutes / 60}h ${minutes % 60}m"
}
