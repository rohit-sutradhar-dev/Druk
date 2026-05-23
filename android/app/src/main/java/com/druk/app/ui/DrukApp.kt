package com.druk.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.druk.app.domain.BacEngine
import com.druk.app.domain.BacPoint
import com.druk.app.domain.FoodLevel
import com.druk.app.domain.PacerKind
import com.druk.app.domain.Sex
import com.druk.app.domain.TargetScheme
import com.druk.app.ui.theme.DrukGold
import com.druk.app.ui.theme.DrukInk
import com.druk.app.ui.theme.DrukPaper
import com.druk.app.ui.theme.DrukTeal
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

private val BacFormat = DecimalFormat("0.000")
private val OneDecimal = DecimalFormat("0.0")
private val ClockFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

@Composable
fun DrukApp(
    state: DrukUiState,
    actions: DrukViewModel,
    requestNotificationPermission: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DrukPaper)
    ) {
        when {
            state.profile == null -> SetupScreen(state, actions)
            else -> MainScreen(state, actions, requestNotificationPermission)
        }
    }
}

@Composable
private fun SetupScreen(state: DrukUiState, actions: DrukViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Header("Druk", "Set up your personal pacing model.")
        }
        item {
            CardBlock {
                Text("About you", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = state.setup.name,
                    onValueChange = { value -> actions.updateSetup { it.copy(name = value) } },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Sex.entries.forEach { sex ->
                        FilterChip(
                            selected = state.setup.sex == sex,
                            onClick = { actions.updateSetup { it.copy(sex = sex) } },
                            label = { Text(sex.name) }
                        )
                    }
                }
                NumberField("Weight kg", state.setup.weightKg) { value -> actions.updateSetup { it.copy(weightKg = value) } }
                NumberField("Height cm", state.setup.heightCm) { value -> actions.updateSetup { it.copy(heightCm = value) } }
            }
        }
        item {
            CardBlock {
                Text("Your drink", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = state.setup.drinkName,
                    onValueChange = { value -> actions.updateSetup { it.copy(drinkName = value) } },
                    label = { Text("Drink name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                NumberField("ABV %", state.setup.drinkAbv) { value -> actions.updateSetup { it.copy(drinkAbv = value) } }
                NumberField("Calories per 30 ml", state.setup.caloriesPer30Ml) { value ->
                    actions.updateSetup { it.copy(caloriesPer30Ml = value) }
                }
                Button(onClick = actions::saveProfile, modifier = Modifier.fillMaxWidth()) {
                    Text("Save Profile")
                }
            }
        }
        item {
            Disclaimer()
        }
    }
}

@Composable
private fun MainScreen(
    state: DrukUiState,
    actions: DrukViewModel,
    requestNotificationPermission: () -> Unit
) {
    val timeline = (state.drinks.map { TimelineItem.Drink(it) } + state.meals.map { TimelineItem.Meal(it) })
        .sortedByDescending { it.timestampMillis }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Header(
                title = state.profile?.name ?: "Druk",
                subtitle = "${state.profile?.drinkName.orEmpty()} · ${OneDecimal.format(state.profile?.drinkAbv ?: 0.0)}% ABV"
            )
        }

        if (state.activeSession == null) {
            item { ReadyCard(state, actions, requestNotificationPermission) }
            item { HistoryCard(state) }
        } else {
            item { SessionCard(state, actions) }
            item { SchemeSelector(state, actions) }
            item { FoodSelector(state, actions) }
            item { PlanGuide(state) }
            item { DrinkControls(state, actions) }
            item { MealControls(state, actions) }
            item { BacCurve(state.actualCurve, state.targetCurve, state.settings.selectedScheme) }
            item { TimelineCard(timeline, actions) }
            item { SessionActions(state, actions) }
        }

        item { Disclaimer() }
    }
}

@Composable
private fun ReadyCard(
    state: DrukUiState,
    actions: DrukViewModel,
    requestNotificationPermission: () -> Unit
) {
    CardBlock {
        Text("Ready when you are.", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Start the clock when the first drink begins.", color = DrukInk.copy(alpha = 0.68f))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Pacing alerts", modifier = Modifier.weight(1f))
            Switch(
                checked = state.settings.notificationsEnabled,
                onCheckedChange = {
                    actions.setNotificationsEnabled(it)
                    if (it) requestNotificationPermission()
                }
            )
        }
        Button(
            onClick = {
                requestNotificationPermission()
                actions.startSession()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("First Drink Now")
        }
    }
}

@Composable
private fun SessionCard(state: DrukUiState, actions: DrukViewModel) {
    val color = when (state.pacerState.kind) {
        PacerKind.DrinkNow -> DrukTeal
        PacerKind.Wait -> DrukGold
        PacerKind.Stop -> Color(0xFFB3261E)
        else -> DrukInk
    }
    CardBlock {
        Text("Estimated BAC", style = MaterialTheme.typography.labelLarge, color = DrukInk.copy(alpha = 0.62f))
        Text(
            "${BacFormat.format(state.currentBac)}%",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text("${OneDecimal.format(state.currentBac * 10)}‰", color = DrukInk.copy(alpha = 0.58f))
        LinearProgressIndicator(
            progress = { (state.currentBac / 0.12).toFloat().coerceIn(0f, 1f) },
            color = color,
            trackColor = DrukInk.copy(alpha = 0.10f),
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
        )
        Text(state.pacerState.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        Text(state.pacerState.detail, color = DrukInk.copy(alpha = 0.75f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Metric("Elapsed", formatDuration(state.elapsedMillis), Modifier.weight(1f))
            Metric("Drinks", state.drinks.size.toString(), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Metric("Calories", state.drinks.sumOf { it.calories }.toInt().toString(), Modifier.weight(1f))
            Metric("Sober in", if (state.soberHours <= 0.05) "Now" else "${OneDecimal.format(state.soberHours)}h", Modifier.weight(1f))
        }
        OutlinedButton(onClick = actions::undoLatest, enabled = state.drinks.isNotEmpty() || state.meals.isNotEmpty()) {
            Text("Undo Last Log")
        }
    }
}

@Composable
private fun SchemeSelector(state: DrukUiState, actions: DrukViewModel) {
    Section("Drinking schemes") {
        TargetScheme.entries.forEach { scheme ->
            val selected = state.settings.selectedScheme == scheme
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) DrukTeal else DrukInk.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { actions.selectScheme(if (selected) null else scheme) },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(scheme.label, fontWeight = FontWeight.Bold)
                        Text(scheme.feel, color = DrukInk.copy(alpha = 0.64f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    Text("${BacFormat.format(scheme.targetBac)}%", color = DrukTeal, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun FoodSelector(state: DrukUiState, actions: DrukViewModel) {
    Section("Stomach level") {
        FoodLevel.entries.forEach { food ->
            FilterChip(
                selected = state.settings.foodLevel == food,
                onClick = { actions.selectFood(food) },
                label = { Text("${food.label} · ${food.lagMinutes}m lag · ${(food.deficit * 100).toInt()}% off") }
            )
        }
    }
}

@Composable
private fun PlanGuide(state: DrukUiState) {
    val plan = state.selectedPlan
    val scheme = state.settings.selectedScheme
    Section("Your drinking guide") {
        if (plan == null || scheme == null) {
            Text("Select a scheme to show the plan.", color = DrukInk.copy(alpha = 0.64f))
        } else {
            Text("Step 1 — Ramp up", fontWeight = FontWeight.Bold, color = DrukTeal)
            Text("Drink ${plan.totalMlToReach} ml over about ${plan.rampMinutes} minutes.")
            Text("That is one 30 ml peg roughly every ${plan.rampIntervalMinutes} minutes.")
            Spacer(Modifier.height(6.dp))
            Text("Step 2 — Hold", fontWeight = FontWeight.Bold, color = DrukTeal)
            Text("Then 30 ml every ${plan.maintenanceMinutes} minutes to hold ${scheme.label}.")
            Spacer(Modifier.height(6.dp))
            Text("To stop", fontWeight = FontWeight.Bold, color = DrukTeal)
            Text("Put the glass down. Estimated sober time from target: ${OneDecimal.format(plan.soberHoursAfterStopping)}h.")
            Spacer(Modifier.height(6.dp))
            Text(
                "Effective 30 ml peg: ${OneDecimal.format(plan.effectiveGramsPerPeg)}g · Distribution mass: ${OneDecimal.format(plan.distributionMassKg)}kg",
                style = MaterialTheme.typography.bodySmall,
                color = DrukInk.copy(alpha = 0.62f)
            )
        }
    }
}

@Composable
private fun DrinkControls(state: DrukUiState, actions: DrukViewModel) {
    Section("Log drink") {
        Text("Preferred drink: ${state.profile?.drinkName.orEmpty()}")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(30.0, 60.0, 90.0).forEach { ml ->
                Button(onClick = { actions.logDrink(ml) }, modifier = Modifier.weight(1f)) {
                    Text("${ml.toInt()}ml")
                }
            }
        }
        NumberField("Custom ml", state.customDrink.volumeMl) { value -> actions.updateDrinkDraft { it.copy(volumeMl = value) } }
        NumberField("Custom ABV optional", state.customDrink.abv) { value -> actions.updateDrinkDraft { it.copy(abv = value) } }
        OutlinedButton(onClick = { actions.logDrink() }, modifier = Modifier.fillMaxWidth()) {
            Text("Log Custom Drink")
        }
    }
}

@Composable
private fun MealControls(state: DrukUiState, actions: DrukViewModel) {
    Section("Log meal") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FoodLevel.entries.drop(1).forEach { food ->
                FilterChip(
                    selected = state.mealDraft.foodLevel == food,
                    onClick = { actions.updateMealDraft { it.copy(foodLevel = food) } },
                    label = { Text(food.label) }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(0, 30, 60, 120).forEach { offset ->
                FilterChip(
                    selected = state.mealDraft.offsetMinutes == offset,
                    onClick = { actions.updateMealDraft { it.copy(offsetMinutes = offset) } },
                    label = { Text(if (offset == 0) "Now" else "${offset}m ago") }
                )
            }
        }
        OutlinedButton(onClick = actions::logMeal, modifier = Modifier.fillMaxWidth()) {
            Text("Log Meal")
        }
    }
}

@Composable
private fun BacCurve(actual: List<BacPoint>, target: List<BacPoint>, scheme: TargetScheme?) {
    Section("BAC curve") {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(DrukInk.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            val maxX = max((actual + target).maxOfOrNull { it.hours } ?: 10.0, 10.0)
            val maxY = max((actual + target).maxOfOrNull { it.bac } ?: 0.12, 0.12)
            fun map(point: BacPoint): Offset {
                val x = (point.hours / maxX).toFloat() * size.width
                val y = size.height - (point.bac / maxY).toFloat() * size.height
                return Offset(x, y)
            }

            fun drawLine(points: List<BacPoint>, color: Color, width: Float) {
                if (points.size < 2) return
                val path = Path().apply {
                    moveTo(map(points.first()).x, map(points.first()).y)
                    points.drop(1).forEach { lineTo(map(it).x, map(it).y) }
                }
                drawPath(path, color = color, style = Stroke(width = width, cap = StrokeCap.Round))
            }
            drawLine(target, DrukGold.copy(alpha = 0.75f), 3f)
            drawLine(actual, DrukTeal, 5f)
        }
        Text(
            text = "Green: actual estimate · Gold: ${scheme?.label ?: "target"}",
            style = MaterialTheme.typography.bodySmall,
            color = DrukInk.copy(alpha = 0.60f)
        )
    }
}

@Composable
private fun TimelineCard(items: List<TimelineItem>, actions: DrukViewModel) {
    Section("Timeline") {
        if (items.isEmpty()) {
            Text("No drinks or meals logged yet.", color = DrukInk.copy(alpha = 0.64f))
        } else {
            items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        when (item) {
                            is TimelineItem.Drink -> {
                                Text("Drink · ${item.value.volumeMl.toInt()}ml · ${OneDecimal.format(item.value.abv)}%")
                                Text("${OneDecimal.format(item.value.gramsAlcohol)}g · ${item.value.foodLevel.label}", color = DrukInk.copy(alpha = 0.60f))
                            }
                            is TimelineItem.Meal -> {
                                Text("Meal · ${item.value.foodLevel.label}")
                                Text("Stomach level updated", color = DrukInk.copy(alpha = 0.60f))
                            }
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(ClockFormat.format(Date(item.timestampMillis)), style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { actions.deleteTimelineItem(item) }) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(state: DrukUiState) {
    Section("Session history") {
        if (state.completedSessions.isEmpty()) {
            Text("No completed sessions yet.", color = DrukInk.copy(alpha = 0.64f))
        } else {
            state.completedSessions.take(5).forEach {
                Text("${ClockFormat.format(Date(it.startedAtMillis))} · ${formatDuration((it.endedAtMillis ?: it.startedAtMillis) - it.startedAtMillis)}")
            }
        }
    }
}

@Composable
private fun SessionActions(state: DrukUiState, actions: DrukViewModel) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(onClick = actions::endSession, modifier = Modifier.weight(1f)) {
            Text("End Session")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Alerts")
            Switch(checked = state.settings.notificationsEnabled, onCheckedChange = actions::setNotificationsEnabled)
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        CardBlock(content = content)
    }
}

@Composable
private fun CardBlock(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

@Composable
private fun Header(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = DrukInk)
        Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = DrukInk.copy(alpha = 0.70f))
    }
}

@Composable
private fun NumberField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> onChange(input.filter { it.isDigit() || it == '.' }.take(6)) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(DrukInk.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = DrukInk.copy(alpha = 0.60f))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Disclaimer() {
    Text(
        text = "BAC is an estimate, not a measurement. Never use Druk to decide whether driving is safe.",
        style = MaterialTheme.typography.bodySmall,
        color = DrukInk.copy(alpha = 0.58f),
        modifier = Modifier.padding(bottom = 24.dp)
    )
}

private fun formatDuration(ms: Long): String {
    val minutes = (ms / 60_000L).coerceAtLeast(0)
    return "${minutes / 60}h ${minutes % 60}m"
}
