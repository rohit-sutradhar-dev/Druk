package com.druk.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
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
import kotlin.math.min
import kotlin.math.abs

private val BacFormat = DecimalFormat("0.000")
private val OneDecimal = DecimalFormat("0.0")
private val ClockFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
private val DayFormat = SimpleDateFormat("d MMM, h:mm a", Locale.getDefault())
private val MonthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
private val Mono = FontFamily.Monospace
private val Serif = FontFamily.Serif

private data class Skin(
    val name: String,
    val bg: Color,
    val surface1: Color,
    val surface2: Color,
    val surface3: Color,
    val border: Color,
    val accent: Color,
    val accentMuted: Color,
    val text: Color,
    val muted: Color,
    val green: Color,
    val orange: Color,
    val red: Color,
    val hero: List<Color>
)

private val MonkSkin = Skin(
    name = "Monk",
    bg = MonkBg,
    surface1 = MonkSurface1,
    surface2 = MonkSurface2,
    surface3 = MonkSurface3,
    border = MonkBorder,
    accent = MonkAmber,
    accentMuted = MonkAmberMuted,
    text = MonkCream,
    muted = MonkMuted,
    green = MonkGreen,
    orange = MonkOrange,
    red = MonkRed,
    hero = listOf(Color(0xFF2E1A0F), MonkSurface1)
)

private enum class ThemeChoice(val label: String, val skin: Skin) {
    Monk("Monk", MonkSkin),
    Neon(
        "Neon",
        Skin(
            name = "Neon",
            bg = Color(0xFF05080D),
            surface1 = Color(0xFF08131A),
            surface2 = Color(0xFF0E2026),
            surface3 = Color(0xFF14313A),
            border = Color(0xFF24515C),
            accent = Color(0xFF39D7D4),
            accentMuted = Color(0xFF4B8C97),
            text = Color(0xFFE8FBF8),
            muted = Color(0xFF84A7A6),
            green = Color(0xFF79E27E),
            orange = Color(0xFFFFB24A),
            red = Color(0xFFFF5E5B),
            hero = listOf(Color(0xFF112B31), Color(0xFF071015))
        )
    ),
    Paper(
        "Paper",
        Skin(
            name = "Paper",
            bg = Color(0xFFF4E9D7),
            surface1 = Color(0xFFFFF7EA),
            surface2 = Color(0xFFF8E7CC),
            surface3 = Color(0xFFEED1A3),
            border = Color(0xFFC99C63),
            accent = Color(0xFF9D5A13),
            accentMuted = Color(0xFF8C7355),
            text = Color(0xFF201713),
            muted = Color(0xFF7D6651),
            green = Color(0xFF357B45),
            orange = Color(0xFFC56622),
            red = Color(0xFFB13B2F),
            hero = listOf(Color(0xFFFFF4DF), Color(0xFFF0D4A9))
        )
    )
}

private val LocalSkin = staticCompositionLocalOf { MonkSkin }

private enum class AppPage(val label: String, val icon: String) {
    Pace("Pace", "◇"),
    Plan("Plan", "◎"),
    Curve("Curve", "⌁"),
    Account("Account", "☉")
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

private fun compactSpec() = DesignSpec(
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

@Composable
fun DrukApp(
    state: DrukUiState,
    actions: DrukViewModel,
    requestNotificationPermission: () -> Unit
) {
    var theme by remember { mutableStateOf(ThemeChoice.Monk) }
    var page by remember { mutableStateOf(AppPage.Pace) }
    val spec = compactSpec()
    CompositionLocalProvider(LocalSkin provides theme.skin) {
        val skin = LocalSkin.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(skin.bg)
        ) {
            if (state.profile == null) {
                SetupScreen(state, actions)
            } else {
                MainScreen(
                    state = state,
                    actions = actions,
                    requestNotificationPermission = requestNotificationPermission,
                    theme = theme,
                    page = page,
                    spec = spec,
                    onThemeChange = { theme = it },
                    onPageChange = { page = it }
                )
                BottomNav(page = page, onPageChange = { page = it })
            }
        }
    }
}

@Composable
private fun SetupScreen(state: DrukUiState, actions: DrukViewModel) {
    val skin = LocalSkin.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(
                Brush.radialGradient(
                    colors = listOf(skin.surface3, skin.bg),
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
                color = skin.accent,
                fontFamily = Serif,
                fontSize = 38.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 3.sp
            )
            Text(
                "Widmark · Seidl 2000 · Personal BAC planner",
                color = skin.muted,
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
            AmberButton("Set up →", Modifier.padding(top = 12.dp), actions::saveProfile)
        }
    }
}

@Composable
private fun MainScreen(
    state: DrukUiState,
    actions: DrukViewModel,
    requestNotificationPermission: () -> Unit,
    theme: ThemeChoice,
    page: AppPage,
    spec: DesignSpec,
    onThemeChange: (ThemeChoice) -> Unit,
    onPageChange: (AppPage) -> Unit
) {
    val timeline = (state.drinks.map { TimelineItem.Drink(it) } + state.meals.map { TimelineItem.Meal(it) })
        .sortedByDescending { it.timestampMillis }
    val skin = LocalSkin.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(skin.bg),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (page) {
            AppPage.Pace -> {
                item { SessionCard(state, actions, requestNotificationPermission, spec) }
                if (state.activeSession != null) {
                    item { LogButtons(state, actions, spec) }
                    item { FoodSelector(state, actions, spec) }
                }
            }
            AppPage.Plan -> {
                item { SchemeSelector(state, actions, spec) }
                item { PlanGuide(state, spec) }
                item { TimelineCard(timeline, actions, state, spec) }
            }
            AppPage.Curve -> {
                item { BacCurve(state.actualCurve, state.targetCurve, state.projectionCurve, state.settings.selectedScheme, spec) }
                item { SchemeSelector(state, actions, spec) }
            }
            AppPage.Account -> {
                item { AccountPage(state, actions, theme, onThemeChange, spec) }
            }
        }
    }
}

@Composable
private fun BoxScope.BottomNav(page: AppPage, onPageChange: (AppPage) -> Unit) {
    val skin = LocalSkin.current
    Row(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(skin.surface2.copy(alpha = 0.98f))
            .border(1.dp, skin.border, RoundedCornerShape(18.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppPage.entries.forEach { option ->
            val selected = page == option
            Column(
                modifier = Modifier
                    .width(78.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (selected) skin.surface3 else Color.Transparent)
                    .clickable { onPageChange(option) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(option.icon, color = if (selected) skin.accent else skin.muted, fontSize = 18.sp)
                Text(option.label, color = if (selected) skin.text else skin.muted, fontFamily = Mono, fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun ThemeSwatch(option: ThemeChoice, selected: Boolean, onClick: () -> Unit) {
    val skin = option.skin
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) skin.surface3 else Color.Transparent)
            .border(1.dp, if (selected) skin.accent else skin.border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(Modifier.size(14.dp).clip(CircleShape).background(skin.accent))
        Text(option.label, color = LocalSkin.current.text, fontFamily = Mono, fontSize = 11.sp)
    }
}

@Composable
private fun SessionCard(
    state: DrukUiState,
    actions: DrukViewModel,
    requestNotificationPermission: () -> Unit,
    spec: DesignSpec
) {
    val skin = LocalSkin.current
    DarkCard(
        modifier = Modifier.padding(horizontal = spec.pagePadding.dp),
        background = Brush.linearGradient(skin.hero),
        radius = spec.cardRadius,
        padding = spec.cardPadding
    ) {
        if (state.activeSession == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Set up this session",
                    color = skin.accent,
                    fontFamily = Serif,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("Drink choice belongs to tonight, not your account.", color = skin.muted, fontFamily = Mono, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MonkField("Drink", state.sessionDraft.drinkName, Modifier.weight(1f)) { value ->
                        actions.updateSessionDraft { it.copy(drinkName = value) }
                    }
                    NumberField("ABV %", state.sessionDraft.drinkAbv, Modifier.weight(1f)) { value ->
                        actions.updateSessionDraft { it.copy(drinkAbv = value) }
                    }
                }
                NumberField("Calories per 30 ml", state.sessionDraft.caloriesPer30Ml) { value ->
                    actions.updateSessionDraft { it.copy(caloriesPer30Ml = value) }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Pacing alerts", color = skin.muted, fontFamily = Mono, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Switch(
                        checked = state.settings.notificationsEnabled,
                        onCheckedChange = {
                            actions.setNotificationsEnabled(it)
                            if (it) requestNotificationPermission()
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = skin.accent, checkedTrackColor = skin.accentMuted)
                    )
                }
                AmberButton("Start session") {
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
    val skin = LocalSkin.current
    val completedDrinks = state.drinks.filter { it.endedAtMillis != null }
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
            Text("${OneDecimal.format(state.currentBac * 10)}‰", color = skin.muted, fontFamily = Mono, fontSize = 14.sp)
            Text(status.label, color = status.color, fontFamily = Serif, fontStyle = FontStyle.Italic, fontSize = 16.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Meta("Session", formatDuration(state.elapsedMillis), Modifier.weight(1f))
            Meta("Drinks", completedDrinks.size.toString(), Modifier.weight(1f))
            Meta("Calories", "${completedDrinks.sumOf { it.calories }.toInt()} kcal", Modifier.weight(1f))
            Meta("Sober in", if (state.soberHours <= 0.05) "Now ✓" else "${OneDecimal.format(state.soberHours)}h", Modifier.weight(1f))
        }
        Text(
            "${state.activeSession?.drinkName.orEmpty()} · ${OneDecimal.format(state.activeSession?.drinkAbv ?: 0.0)}% ABV",
            color = skin.muted,
            fontFamily = Mono,
            fontSize = 11.sp
        )
        state.activeDrink?.let {
            Text(
                "Current drink running · ${formatDuration(state.nowMillis - it.startedAtMillis)}",
                color = skin.accent,
                fontFamily = Mono,
                fontSize = 11.sp
            )
        }
        PaceCard(state, spec)
        GhostButton("Undo last log", enabled = state.drinks.isNotEmpty() || state.meals.isNotEmpty(), onClick = actions::undoLatest)
    }
}

@Composable
private fun PaceCard(state: DrukUiState, spec: DesignSpec) {
    val skin = LocalSkin.current
    val kind = state.pacerState.kind
    val color = when (kind) {
        PacerKind.DrinkNow -> skin.green
        PacerKind.Wait -> skin.accent
        PacerKind.Stop -> skin.red
        else -> skin.muted
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
            .border(1.dp, skin.border, RoundedCornerShape(14.dp))
            .padding(if (spec.cardPadding < 16) 12.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                if (state.settings.selectedScheme == null) "Pacer" else "Next peg",
                color = skin.accentMuted,
                fontFamily = Mono,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Text(state.pacerState.title, color = color, fontFamily = Serif, fontSize = spec.paceTitleSize.sp, fontWeight = FontWeight.Bold, lineHeight = (spec.paceTitleSize + 2).sp)
            Text(state.pacerState.detail, color = skin.muted, fontFamily = Mono, fontSize = 11.sp, lineHeight = 16.sp)
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
    val skin = LocalSkin.current
    val outer = sizeDp + 8
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(outer.dp)) {
        Canvas(modifier = Modifier.size(sizeDp.dp)) {
            drawCircle(Color.White.copy(alpha = 0.08f))
            drawArc(color, -90f, 360f * percent.coerceIn(0, 100) / 100f, useCenter = true)
            drawCircle(skin.surface1, radius = size.minDimension / 2f - 7.dp.toPx())
            drawCircle(skin.border.copy(alpha = 0.75f), radius = size.minDimension / 2f - 7.dp.toPx(), style = Stroke(1.dp.toPx()))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text, color = skin.text, fontFamily = Mono, fontSize = 10.sp, lineHeight = 11.sp)
            Text(label, color = skin.muted, fontFamily = Mono, fontSize = 8.sp, lineHeight = 9.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogButtons(state: DrukUiState, actions: DrukViewModel, spec: DesignSpec) {
    var showDrink by remember { mutableStateOf(false) }
    var showMeal by remember { mutableStateOf(false) }
    val skin = LocalSkin.current
    val activeDrink = state.activeDrink

    Row(
        modifier = Modifier.padding(horizontal = spec.pagePadding.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (activeDrink == null) {
            ActionButton("+ Start a drink", skin.surface3, skin.accent, Modifier.weight(2f)) { showDrink = true }
        } else {
            ActionButton("End current drink · ${formatDuration(state.nowMillis - activeDrink.startedAtMillis)}", skin.surface3, skin.red, Modifier.weight(2f)) {
                actions.endDrink()
            }
        }
        ActionButton("🍛 Log meal", skin.surface2, skin.green, Modifier.weight(1f)) { showMeal = true }
    }

    if (showDrink) {
        ModalBottomSheet(
            onDismissRequest = { showDrink = false },
            containerColor = skin.surface2,
            contentColor = skin.text,
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
                Text("Start a drink", color = skin.accent, fontFamily = Serif, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(state.activeSession?.drinkName.orEmpty(), color = skin.text, fontFamily = Mono, fontSize = 13.sp, modifier = Modifier.background(skin.surface3, CircleShape).padding(horizontal = 14.dp, vertical = 6.dp))
                Text("Volume", color = skin.muted, fontFamily = Mono, fontSize = 11.sp, letterSpacing = 1.sp)
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
                Text("Stomach right now", color = skin.muted, fontFamily = Mono, fontSize = 11.sp, letterSpacing = 1.sp)
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
                    AmberButton("Start drink", Modifier.weight(2f)) {
                        val volume = if (selectedVolume == 0.0) null else selectedVolume
                        actions.startDrink(volumeMl = volume, foodLevel = selectedFood)
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
            containerColor = skin.surface2,
            contentColor = skin.text,
            dragHandle = null
        ) {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("🍛 Log a meal", color = skin.green, fontFamily = Serif, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("What did you eat?", color = skin.muted, fontFamily = Mono, fontSize = 11.sp, letterSpacing = 1.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FoodLevel.entries.drop(1).forEach { food ->
                        FoodMini(food, state.mealDraft.foodLevel == food, Modifier.weight(1f), green = true) {
                            actions.updateMealDraft { it.copy(foodLevel = food) }
                        }
                    }
                }
                Text("When?", color = skin.muted, fontFamily = Mono, fontSize = 11.sp, letterSpacing = 1.sp)
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
                        colors = ButtonDefaults.buttonColors(containerColor = skin.green, contentColor = skin.bg)
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
        val skin = LocalSkin.current
        DarkCard(modifier = Modifier.padding(horizontal = spec.pagePadding.dp), backgroundColor = skin.surface1, radius = 9, padding = 12) {
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = skin.text, fontWeight = FontWeight.Bold)) {
                        append("${food.emoji()} ${food.label}")
                    }
                    append(" — ${(food.deficit * 100).toInt()}% of each peg absorbed before reaching blood.\n")
                    append("Peak BAC arrives ~${food.lagMinutes} min after drinking, not immediately.")
                },
                color = skin.muted,
                fontFamily = Mono,
                fontSize = 11.sp,
                lineHeight = 18.sp
            )
            Text("Elimination rate (0.15‰/hr) is unchanged — food only slows absorption.", color = skin.accentMuted, fontFamily = Mono, fontSize = 10.sp)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SchemeSelector(state: DrukUiState, actions: DrukViewModel, spec: DesignSpec) {
    SectionLabel("Drinking schemes", "(tap to show plan)", spec)
    val schemes = TargetScheme.entries
    val selectedIndex = schemes.indexOf(state.settings.selectedScheme).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = selectedIndex) { schemes.size }
    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = spec.pagePadding.dp),
        pageSpacing = 10.dp
    ) { page ->
        val scheme = schemes[page]
            val selected = state.settings.selectedScheme == scheme
            val plan = state.profile?.let { BacEngine.schemePlan(scheme, it, state.settings.foodLevel) }
            SchemeCard(scheme, plan, selected, spec) {
                actions.selectScheme(if (selected) null else scheme)
            }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        schemes.forEachIndexed { index, scheme ->
            Box(
                modifier = Modifier
                    .padding(3.dp)
                    .size(if (index == pagerState.currentPage) 9.dp else 6.dp)
                    .clip(CircleShape)
                    .background(if (index == pagerState.currentPage) scheme.color(LocalSkin.current) else LocalSkin.current.border)
            )
        }
    }
}

@Composable
private fun SchemeCard(scheme: TargetScheme, plan: com.druk.app.domain.SchemePlan?, selected: Boolean, spec: DesignSpec, onClick: () -> Unit) {
    val skin = LocalSkin.current
    val color = scheme.color(skin)
    Column(
        modifier = Modifier
            .width(spec.schemeWidth.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(skin.surface2)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) color else skin.border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(spec.cardPadding.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(scheme.emoji(), fontSize = 18.sp)
            Text(scheme.label, color = if (selected) color else skin.text, fontFamily = Serif, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("${BacFormat.format(scheme.targetBac)}%", color = color, fontFamily = Mono, fontSize = 11.sp, modifier = Modifier.background(Color.Black.copy(alpha = 0.30f), RoundedCornerShape(4.dp)).padding(horizontal = 5.dp, vertical = 2.dp))
        }
        Text(scheme.feel, color = skin.muted, fontFamily = Serif, fontStyle = FontStyle.Italic, fontSize = 12.sp, lineHeight = 17.sp)
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
    val skin = LocalSkin.current
    SectionLabel("Your drinking guide", spec = spec)
    DarkCard(modifier = Modifier.padding(horizontal = spec.pagePadding.dp), backgroundColor = skin.surface1, radius = spec.cardRadius, padding = 0) {
        val plan = state.selectedPlan
        val scheme = state.settings.selectedScheme
        val profile = state.profile
        if (plan == null || scheme == null || profile == null) {
            Text(
                "Select a scheme above for your step-by-step guide.",
                color = skin.muted,
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
                        .background(skin.bg.copy(alpha = 0.20f))
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
                color = skin.red,
                fontFamily = Mono,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(skin.red.copy(alpha = 0.10f))
                    .border(0.5.dp, skin.red.copy(alpha = 0.25f))
                    .padding(14.dp)
            )
        }
    }
}

@Composable
private fun BacCurve(actual: List<BacPoint>, target: List<BacPoint>, projection: List<BacPoint>, scheme: TargetScheme?, spec: DesignSpec) {
    var selectedPoint by remember(actual, projection) { mutableStateOf<BacPoint?>(actual.lastOrNull() ?: projection.firstOrNull()) }
    val skin = LocalSkin.current
    val allPoints = actual + target + projection
    SectionLabel("BAC curve", spec = spec)
    DarkCard(modifier = Modifier.padding(horizontal = spec.pagePadding.dp), backgroundColor = skin.surface1, radius = spec.cardRadius, padding = spec.cardPadding) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CurveChip("▶ Actual", skin.accent)
            CurveChip("⌁ Projection", skin.green)
            CurveChip("${scheme?.emoji() ?: "◇"} ${scheme?.label ?: "Plan"}", scheme?.color(skin) ?: skin.green)
            CurveChip("Zones", skin.muted)
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(skin.surface2.copy(alpha = 0.92f), skin.surface1)
                    )
                )
                .pointerInput(actual, target, projection) {
                    detectTapGestures { offset ->
                        val points = actual.ifEmpty { projection.ifEmpty { target } }
                        if (points.isNotEmpty()) {
                            val maxX = max(allPoints.maxOfOrNull { it.hours } ?: 10.0, 10.0)
                            selectedPoint = points.minByOrNull { point ->
                                val x = (point.hours / maxX).toFloat() * size.width
                                abs(x - offset.x)
                            }
                        }
                    }
                }
        ) {
            val maxX = max(allPoints.maxOfOrNull { it.hours } ?: 10.0, 10.0)
            val maxY = max(allPoints.maxOfOrNull { it.bac } ?: 0.12, 0.12)
            val grid = skin.border.copy(alpha = 0.34f)
            val zoneColors = listOf(skin.green, skin.accent, skin.orange, skin.red)
            listOf(0.03, 0.06, 0.09, 0.12).forEachIndexed { index, bac ->
                val y = size.height - (bac / maxY).toFloat() * size.height
                drawRect(
                    color = zoneColors.getOrElse(index) { skin.red }.copy(alpha = 0.04f),
                    topLeft = Offset(0f, y),
                    size = androidx.compose.ui.geometry.Size(size.width, size.height - y)
                )
            }
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
                drawLine(it.color(skin).copy(alpha = 0.34f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            }
            drawPathLine(target, (scheme?.color(skin) ?: skin.accent).copy(alpha = 0.65f), 2.5f)
            drawPathLine(projection, skin.green.copy(alpha = 0.75f), 3.5f)
            drawPathLine(actual, skin.accent, 5f)
            selectedPoint?.let { point ->
                val p = map(point)
                drawLine(skin.text.copy(alpha = 0.40f), Offset(p.x, 0f), Offset(p.x, size.height), strokeWidth = 1.5f)
                drawCircle(skin.accent, radius = 8.dp.toPx(), center = p)
                drawCircle(skin.bg, radius = 4.dp.toPx(), center = p)
            }
        }
        Text(
            selectedPoint?.let { "Selected · ${OneDecimal.format(it.hours)}h · ${BacFormat.format(it.bac)}%" }
                ?: "Tap the curve to inspect a point.",
            color = skin.muted,
            fontFamily = Mono,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun CurveChip(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, color = LocalSkin.current.muted, fontFamily = Mono, fontSize = 10.sp)
    }
}

@Composable
private fun TimelineCard(items: List<TimelineItem>, actions: DrukViewModel, state: DrukUiState, spec: DesignSpec) {
    val skin = LocalSkin.current
    SectionLabel("Timeline", spec = spec)
    Column(modifier = Modifier.padding(horizontal = spec.pagePadding.dp)) {
        if (items.isEmpty()) {
            Text("Nothing logged yet.", color = skin.muted, fontFamily = Serif, fontStyle = FontStyle.Italic, fontSize = 14.sp, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp))
        } else {
            items.forEach { item ->
                TimelineRow(item, state, actions)
            }
        }
    }
}

@Composable
private fun TimelineRow(item: TimelineItem, state: DrukUiState, actions: DrukViewModel) {
    val skin = LocalSkin.current
    val isMeal = item is TimelineItem.Meal
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isMeal) skin.green.copy(alpha = 0.05f) else Color.Transparent)
            .border(0.5.dp, if (isMeal) skin.green.copy(alpha = 0.20f) else skin.border)
            .padding(vertical = 10.dp, horizontal = if (isMeal) 10.dp else 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "${ClockFormat.format(Date(item.timestampMillis))}  +${state.activeSession?.let { OneDecimal.format((item.timestampMillis - it.startedAtMillis) / 3_600_000.0) } ?: "0.0"}h",
                color = if (isMeal) skin.green else skin.accent,
                fontFamily = Mono,
                fontSize = 12.sp
            )
            when (item) {
                is TimelineItem.Drink -> {
                    Text(
                        "${state.activeSession?.drinkName ?: "Drink"} · ${item.value.volumeMl.toInt()}ml · ${OneDecimal.format(item.value.abv)}%ABV · ${item.value.foodLevel.emoji()}${(item.value.foodLevel.deficit * 100).toInt()}%↓",
                        color = skin.muted,
                        fontSize = 12.sp
                    )
                    val duration = item.value.endedAtMillis?.let { formatDuration(it - item.value.startedAtMillis) }
                        ?: "in progress · ${formatDuration(state.nowMillis - item.value.startedAtMillis)}"
                    Text("${OneDecimal.format(item.value.gramsAlcohol)}g → ${OneDecimal.format(item.value.gramsAlcohol * (1.0 - item.value.foodLevel.deficit))}g absorbed · $duration", color = skin.text, fontFamily = Mono, fontSize = 11.sp)
                }
                is TimelineItem.Meal -> {
                    Text("${item.value.foodLevel.emoji()} ${item.value.foodLevel.label} logged — ${(item.value.foodLevel.deficit * 100).toInt()}% deficit · peaks +${item.value.foodLevel.lagMinutes}min", color = skin.muted, fontSize = 12.sp)
                    Text("Food event", color = skin.green, fontFamily = Mono, fontSize = 11.sp)
                }
            }
        }
        TextButton(onClick = { actions.deleteTimelineItem(item) }) {
            Text("Delete", color = skin.muted, fontFamily = Mono, fontSize = 11.sp)
        }
    }
}

@Composable
private fun Footer(state: DrukUiState, actions: DrukViewModel, spec: DesignSpec) {
    val skin = LocalSkin.current
    Column(
        modifier = Modifier
            .padding(horizontal = spec.pagePadding.dp, vertical = 16.dp)
            .navigationBarsPadding()
            .border(0.5.dp, skin.border)
            .padding(top = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Seidl 2000 + Widmark · r = ${state.profile?.let { DecimalFormat("0.000").format(it.r) } ?: "—"} · β = 0.15‰/hr", color = skin.muted, fontFamily = Mono, fontSize = 10.sp)
        Text("Estimates ±30% variance. Never drive, even at sweet spot.", color = skin.red, fontFamily = Mono, fontSize = 10.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = actions::endSession, enabled = state.activeSession != null) {
                Text("Clear session", color = skin.muted, fontFamily = Mono, fontSize = 11.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Alerts", color = skin.muted, fontFamily = Mono, fontSize = 11.sp)
                Switch(
                    checked = state.settings.notificationsEnabled,
                    onCheckedChange = actions::setNotificationsEnabled,
                    colors = SwitchDefaults.colors(checkedThumbColor = skin.accent, checkedTrackColor = skin.accentMuted)
                )
            }
        }
    }
}

@Composable
private fun AccountPage(
    state: DrukUiState,
    actions: DrukViewModel,
    theme: ThemeChoice,
    onThemeChange: (ThemeChoice) -> Unit,
    spec: DesignSpec
) {
    val skin = LocalSkin.current
    SectionLabel("Theme", "(choose your look)", spec)
    DarkCard(modifier = Modifier.padding(horizontal = spec.pagePadding.dp), backgroundColor = skin.surface1, radius = spec.cardRadius, padding = spec.cardPadding) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            ThemeChoice.entries.forEach { option ->
                ThemeSwatch(option, selected = theme == option) { onThemeChange(option) }
            }
        }
    }
    SectionLabel("Account", "(profile and session controls)", spec)
    DarkCard(modifier = Modifier.padding(horizontal = spec.pagePadding.dp), backgroundColor = skin.surface1, radius = spec.cardRadius, padding = spec.cardPadding) {
        Text(state.profile?.name?.ifBlank { "You" } ?: "You", color = skin.text, fontFamily = Serif, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Meta("Weight", "${OneDecimal.format(state.profile?.weightKg ?: 0.0)}kg", Modifier.weight(1f))
            Meta("Height", "${OneDecimal.format(state.profile?.heightCm ?: 0.0)}cm", Modifier.weight(1f))
            Meta("Seidl r", DecimalFormat("0.000").format(state.profile?.r ?: 0.0), Modifier.weight(1f))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Pacing alerts", color = skin.muted, fontFamily = Mono, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Switch(
                checked = state.settings.notificationsEnabled,
                onCheckedChange = actions::setNotificationsEnabled,
                colors = SwitchDefaults.colors(checkedThumbColor = skin.accent, checkedTrackColor = skin.accentMuted)
            )
        }
        if (state.activeSession != null) {
            GhostButton("End current session", Modifier.fillMaxWidth()) { actions.endSession() }
        }
    }
    SessionHistoryViews(state, spec)
    Footer(state, actions, spec)
}

@Composable
private fun SessionHistoryViews(state: DrukUiState, spec: DesignSpec) {
    val skin = LocalSkin.current
    var selectedSessionId by remember(state.completedSessions) { mutableStateOf(state.completedSessions.firstOrNull()?.id) }
    val selectedSession = state.completedSessions.firstOrNull { it.id == selectedSessionId }
    SectionLabel("Previous sessions", "(calendar and sequence)", spec)
    DarkCard(modifier = Modifier.padding(horizontal = spec.pagePadding.dp), backgroundColor = skin.surface1, radius = spec.cardRadius, padding = spec.cardPadding) {
        CalendarPreview(state.completedSessions, selectedSessionId) { selectedSessionId = it }
        if (state.completedSessions.isEmpty()) {
            Text("No completed sessions yet.", color = skin.muted, fontFamily = Serif, fontStyle = FontStyle.Italic, fontSize = 14.sp)
        } else {
            selectedSession?.let { session ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(skin.surface2, RoundedCornerShape(10.dp))
                        .border(1.dp, skin.border, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Selected session", color = skin.accentMuted, fontFamily = Mono, fontSize = 10.sp, letterSpacing = 1.1.sp)
                    Text(DayFormat.format(Date(session.startedAtMillis)), color = skin.text, fontFamily = Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("${session.drinkName} · ${OneDecimal.format(session.drinkAbv)}% ABV · ${formatDuration((session.endedAtMillis ?: session.startedAtMillis) - session.startedAtMillis)}", color = skin.muted, fontFamily = Mono, fontSize = 11.sp)
                }
            }
            state.completedSessions.take(8).forEach { session ->
                val selected = session.id == selectedSessionId
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) skin.surface3 else Color.Transparent)
                        .clickable { selectedSessionId = session.id }
                        .padding(horizontal = 8.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(DayFormat.format(Date(session.startedAtMillis)), color = if (selected) skin.accent else skin.text, fontFamily = Mono, fontSize = 12.sp)
                        Text(session.drinkName, color = skin.muted, fontFamily = Mono, fontSize = 10.sp)
                    }
                    Text(formatDuration((session.endedAtMillis ?: session.startedAtMillis) - session.startedAtMillis), color = skin.muted, fontFamily = Mono, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun CalendarPreview(
    sessions: List<com.druk.app.domain.SessionLog>,
    selectedSessionId: Long?,
    onSelectSession: (Long) -> Unit
) {
    val skin = LocalSkin.current
    val month = java.util.Calendar.getInstance()
    val sessionByDay = sessions
        .filter {
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = it.startedAtMillis }
            cal.get(java.util.Calendar.YEAR) == month.get(java.util.Calendar.YEAR) &&
                cal.get(java.util.Calendar.MONTH) == month.get(java.util.Calendar.MONTH)
        }
        .groupBy {
            java.util.Calendar.getInstance().apply { timeInMillis = it.startedAtMillis }.get(java.util.Calendar.DAY_OF_MONTH)
        }
    val daysInMonth = month.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
    val first = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.DAY_OF_MONTH, 1)
    }
    val firstOffset = first.get(java.util.Calendar.DAY_OF_WEEK) - 1
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(MonthFormat.format(Date(month.timeInMillis)), color = skin.accentMuted, fontFamily = Mono, fontSize = 10.sp, letterSpacing = 1.2.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                Text(it, color = skin.muted, fontFamily = Mono, fontSize = 9.sp, modifier = Modifier.width(30.dp))
            }
        }
        repeat(6) { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(7) { day ->
                    val number = week * 7 + day + 1 - firstOffset
                    val session = sessionByDay[number]?.firstOrNull()
                    val active = session != null
                    val selected = session?.id == selectedSessionId
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    selected -> skin.accent.copy(alpha = 0.55f)
                                    active -> skin.accent.copy(alpha = 0.28f)
                                    else -> skin.surface2
                                }
                            )
                            .border(1.dp, if (active) skin.accent else skin.border, CircleShape)
                            .clickable(enabled = active) { session?.let { onSelectSession(it.id) } },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (number in 1..daysInMonth) number.toString() else "", color = if (active) skin.text else skin.muted, fontFamily = Mono, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DarkCard(
    modifier: Modifier = Modifier,
    background: Brush? = null,
    backgroundColor: Color = Color.Unspecified,
    radius: Int = 14,
    padding: Int = 16,
    content: @Composable ColumnScope.() -> Unit
) {
    val skin = LocalSkin.current
    val resolvedBackground = if (backgroundColor == Color.Unspecified) skin.surface2 else backgroundColor
    val base = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(radius.dp))
        .then(if (background != null) Modifier.background(background) else Modifier.background(resolvedBackground))
        .border(1.dp, skin.border, RoundedCornerShape(radius.dp))
        .padding(padding.dp)
    Column(modifier = base, verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
}

@Composable
private fun SectionLabel(title: String, small: String? = null, spec: DesignSpec) {
    val skin = LocalSkin.current
    Text(
        buildAnnotatedString {
            append(title.uppercase())
            if (small != null) {
                append(" ")
                withStyle(SpanStyle(color = skin.muted, fontSize = 10.sp, letterSpacing = 0.sp)) { append(small) }
            }
        },
        color = skin.accentMuted,
        fontFamily = Mono,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(start = spec.pagePadding.dp, end = spec.pagePadding.dp, top = 8.dp)
    )
}

@Composable
private fun SetupLabel(text: String, modifier: Modifier = Modifier) {
    val skin = LocalSkin.current
    Text(text.uppercase(), color = skin.accentMuted, fontFamily = Mono, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, modifier = modifier.fillMaxWidth().padding(bottom = 8.dp))
}

@Composable
private fun MonkField(label: String, value: String, modifier: Modifier = Modifier, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, fontFamily = Mono) },
        textStyle = TextStyle(fontFamily = Mono, fontSize = 16.sp, lineHeight = 18.sp),
        modifier = modifier.fillMaxWidth().height(64.dp).padding(bottom = 8.dp),
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
        textStyle = TextStyle(fontFamily = Mono, fontSize = 16.sp, lineHeight = 18.sp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.fillMaxWidth().height(64.dp).padding(bottom = 8.dp),
        singleLine = true,
        colors = fieldColors()
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = LocalSkin.current.text,
    unfocusedTextColor = LocalSkin.current.text,
    focusedContainerColor = LocalSkin.current.surface2,
    unfocusedContainerColor = LocalSkin.current.surface2,
    focusedBorderColor = LocalSkin.current.accent,
    unfocusedBorderColor = LocalSkin.current.border,
    focusedLabelColor = LocalSkin.current.accent,
    unfocusedLabelColor = LocalSkin.current.muted,
    cursorColor = LocalSkin.current.accent
)

@Composable
private fun AmberButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val skin = LocalSkin.current
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = skin.accent, contentColor = skin.bg)
    ) {
        Text(text, fontFamily = Serif, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ActionButton(text: String, bg: Color, fg: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val skin = LocalSkin.current
    Box(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, if (fg == skin.green) skin.green.copy(alpha = 0.72f) else skin.border, RoundedCornerShape(10.dp))
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
    val skin = LocalSkin.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) skin.surface3 else Color.Transparent)
            .border(1.dp, if (selected) skin.accent else skin.border, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) skin.accent else skin.muted, fontFamily = Mono, fontSize = 11.sp)
    }
}

@Composable
private fun SelectPill(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val skin = LocalSkin.current
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(if (selected) skin.surface3 else skin.surface2)
            .border(1.5.dp, if (selected) skin.accent else skin.border, RoundedCornerShape(9.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) skin.accent else skin.text, fontFamily = Mono, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FoodMini(food: FoodLevel, selected: Boolean, modifier: Modifier = Modifier, green: Boolean = false, onClick: () -> Unit) {
    val skin = LocalSkin.current
    val accent = if (green) skin.green else skin.accent
    Column(
        modifier = modifier
            .height(70.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) skin.surface3 else skin.surface2)
            .border(1.5.dp, if (selected) accent else skin.border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(food.emoji(), fontSize = 17.sp)
        Text(food.label, color = if (selected) accent else skin.muted, fontFamily = Mono, fontSize = 9.sp, lineHeight = 11.sp)
        Text("${(food.deficit * 100).toInt()}% off", color = skin.muted, fontFamily = Mono, fontSize = 8.sp, lineHeight = 10.sp)
    }
}

@Composable
private fun Meta(label: String, value: String, modifier: Modifier = Modifier) {
    val skin = LocalSkin.current
    Column(modifier = modifier) {
        Text(label.uppercase(), color = skin.muted, fontFamily = Mono, fontSize = 9.sp, letterSpacing = 0.8.sp)
        Text(value, color = skin.text, fontFamily = Mono, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun SchemeNumber(text: String) {
    Text(text, color = LocalSkin.current.muted, fontFamily = Mono, fontSize = 11.sp, lineHeight = 15.sp)
}

@Composable
private fun GuideStep(label: String, main: String, sub: AnnotatedBuilder.() -> Unit) {
    val skin = LocalSkin.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, skin.border)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(label.uppercase(), color = skin.accentMuted, fontFamily = Mono, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
        Text(main, color = skin.text, fontFamily = Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, lineHeight = 22.sp)
        val builder = AnnotatedBuilder(skin.text)
        builder.sub()
        Text(builder.value, color = skin.muted, fontFamily = Mono, fontSize = 11.sp, lineHeight = 17.sp)
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    val skin = LocalSkin.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = skin.muted, fontFamily = Mono, fontSize = 11.sp)
        Text(value, color = skin.text, fontFamily = Mono, fontSize = 11.sp)
    }
}

private class AnnotatedBuilder(private val strongColor: Color) {
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
                if (strong) withStyle(SpanStyle(color = strongColor, fontWeight = FontWeight.Bold)) { append(text) } else append(text)
            }
        }
}

private data class BacStatus(val label: String, val color: Color)

@Composable
private fun bacStatus(bac: Double): BacStatus {
    val skin = LocalSkin.current
    return when {
        bac <= 0.005 -> BacStatus("Clear", skin.muted)
        bac <= 0.03 -> BacStatus("Monk's Whisper 🌿", skin.green)
        bac <= 0.06 -> BacStatus("Druk zone ✦", skin.accent)
        bac <= 0.09 -> BacStatus("Buzzed 🔥", skin.orange)
        bac <= 0.15 -> BacStatus("Drunk 💀", skin.red)
        else -> BacStatus("Danger — stop", skin.red)
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

private fun TargetScheme.color(skin: Skin): Color {
    return when (this) {
        TargetScheme.Whisper -> skin.green
        TargetScheme.Druk -> skin.accent
        TargetScheme.LooseCannon -> skin.orange
        TargetScheme.DeepEnd -> skin.red
    }
}

private fun formatDuration(ms: Long): String {
    val minutes = (ms / 60_000L).coerceAtLeast(0)
    return "${minutes / 60}h ${minutes % 60}m"
}
