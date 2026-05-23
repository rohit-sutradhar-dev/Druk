package com.druk.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.druk.app.ui.theme.DrukGold
import com.druk.app.ui.theme.DrukInk
import com.druk.app.ui.theme.DrukPaper
import com.druk.app.ui.theme.DrukTeal
import com.druk.app.ui.theme.DrukTheme
import java.text.DecimalFormat
import kotlin.math.ceil
import kotlin.math.max

private const val METABOLISM_PER_HOUR = 0.015
private const val ABSORPTION_MINUTES = 45.0
private val BacFormat = DecimalFormat("0.000")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DrukTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DrukHomeScreen()
                }
            }
        }
    }
}

private data class BacTarget(
    val name: String,
    val range: ClosedFloatingPointRange<Double>,
    val description: String
)

private data class DrinkPreset(
    val name: String,
    val volumeMl: Double,
    val abv: Double
)

private data class DrinkEntry(
    val preset: DrinkPreset,
    val loggedAtMillis: Long
) {
    val gramsAlcohol: Double = preset.volumeMl * (preset.abv / 100.0) * 0.789
}

private enum class BodyProfile(
    val label: String,
    val widmarkFactor: Double
) {
    LowerWater("Lower body water", 0.55),
    Average("Average", 0.68),
    HigherWater("Higher body water", 0.75)
}

private val Targets = listOf(
    BacTarget("Light Buzz", 0.020..0.030, "Soft lift"),
    BacTarget("Social Glow", 0.030..0.040, "Warm and easy"),
    BacTarget("Druk", 0.040..0.060, "Balanced sweet spot"),
    BacTarget("Party Mode", 0.060..0.070, "Higher energy")
)

private val DrinkPresets = listOf(
    DrinkPreset("Beer", 355.0, 5.0),
    DrinkPreset("Wine", 150.0, 12.0),
    DrinkPreset("Shot", 45.0, 40.0),
    DrinkPreset("Cocktail", 180.0, 14.0)
)

@Composable
fun DrukHomeScreen(modifier: Modifier = Modifier) {
    var selectedTarget by remember { mutableStateOf(Targets[2]) }
    var weightInput by remember { mutableStateOf("75") }
    var bodyProfile by remember { mutableStateOf(BodyProfile.Average) }
    val drinks = remember { mutableStateListOf<DrinkEntry>() }
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    val weightKg = weightInput.toDoubleOrNull()?.coerceIn(35.0, 180.0) ?: 75.0
    val currentBac = estimateBac(
        drinks = drinks,
        weightKg = weightKg,
        widmarkFactor = bodyProfile.widmarkFactor,
        nowMillis = currentTime
    )
    val guidance = guidanceFor(currentBac, selectedTarget)
    val totalGrams = drinks.sumOf { it.gramsAlcohol }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DrukPaper)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Header()
        }

        item {
            SessionStatusCard(
                target = selectedTarget,
                bac = currentBac,
                guidance = guidance,
                drinkCount = drinks.size,
                totalGrams = totalGrams,
                onRefresh = { currentTime = System.currentTimeMillis() }
            )
        }

        item {
            ProfileCard(
                weightInput = weightInput,
                onWeightChange = { weightInput = it.filter { char -> char.isDigit() }.take(3) },
                bodyProfile = bodyProfile,
                onBodyProfileChange = { bodyProfile = it }
            )
        }

        item {
            TargetSelector(
                selectedTarget = selectedTarget,
                onTargetSelected = { selectedTarget = it }
            )
        }

        item {
            DrinkLogger(
                onDrinkLogged = {
                    drinks.add(0, DrinkEntry(it, System.currentTimeMillis()))
                    currentTime = System.currentTimeMillis()
                },
                onUndo = {
                    if (drinks.isNotEmpty()) {
                        drinks.removeAt(0)
                        currentTime = System.currentTimeMillis()
                    }
                },
                canUndo = drinks.isNotEmpty()
            )
        }

        item {
            Text(
                text = "Drink History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (drinks.isEmpty()) {
            item {
                EmptyHistory()
            }
        } else {
            items(drinks) { drink ->
                DrinkHistoryRow(drink = drink, nowMillis = currentTime)
            }
        }

        item {
            Disclaimer()
        }
    }
}

@Composable
private fun Header() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Druk",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = DrukInk
        )
        Text(
            text = "Choose a lane, log drinks, and keep the session intentional.",
            style = MaterialTheme.typography.bodyLarge,
            color = DrukInk.copy(alpha = 0.72f)
        )
    }
}

@Composable
private fun SessionStatusCard(
    target: BacTarget,
    bac: Double,
    guidance: String,
    drinkCount: Int,
    totalGrams: Double,
    onRefresh: () -> Unit
) {
    val progress = (bac / 0.08).toFloat().coerceIn(0f, 1f)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Estimated BAC",
                        style = MaterialTheme.typography.labelLarge,
                        color = DrukInk.copy(alpha = 0.62f)
                    )
                    Text(
                        text = "${BacFormat.format(bac)}%",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor(bac, target)
                    )
                }
                TargetBadge(target.name)
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(99.dp)),
                color = statusColor(bac, target),
                trackColor = DrukInk.copy(alpha = 0.10f)
            )

            Text(
                text = guidance,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = DrukInk
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricTile("Drinks", drinkCount.toString(), Modifier.weight(1f))
                MetricTile("Alcohol", "${totalGrams.toInt()}g", Modifier.weight(1f))
                TextButton(onClick = onRefresh) {
                    Text("Refresh")
                }
            }
        }
    }
}

@Composable
private fun ProfileCard(
    weightInput: String,
    onWeightChange: (String) -> Unit,
    bodyProfile: BodyProfile,
    onBodyProfileChange: (BodyProfile) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = weightInput,
                onValueChange = onWeightChange,
                label = { Text("Weight kg") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Body profile",
                    style = MaterialTheme.typography.labelLarge,
                    color = DrukInk.copy(alpha = 0.70f)
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BodyProfile.entries.forEach { option ->
                        FilterChip(
                            selected = bodyProfile == option,
                            onClick = { onBodyProfileChange(option) },
                            label = {
                                Text(
                                    text = option.label,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TargetSelector(
    selectedTarget: BacTarget,
    onTargetSelected: (BacTarget) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Target",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Targets.forEach { target ->
            val selected = target == selectedTarget
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) DrukTeal else DrukInk.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onTargetSelected(target) },
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) DrukTeal.copy(alpha = 0.09f) else Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = target.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = target.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = DrukInk.copy(alpha = 0.65f)
                        )
                    }
                    Text(
                        text = "${BacFormat.format(target.range.start)}-${BacFormat.format(target.range.endInclusive)}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = DrukTeal,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DrinkLogger(
    onDrinkLogged: (DrinkPreset) -> Unit,
    onUndo: () -> Unit,
    canUndo: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Log Drink",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onUndo, enabled = canUndo) {
                    Text("Undo")
                }
            }
            DrinkPresets.forEach { preset ->
                Button(
                    onClick = { onDrinkLogged(preset) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("${preset.name}  ${preset.volumeMl.toInt()}ml at ${preset.abv.toInt()}%")
                }
            }
        }
    }
}

@Composable
private fun DrinkHistoryRow(drink: DrinkEntry, nowMillis: Long) {
    val minutesAgo = max(0, ((nowMillis - drink.loggedAtMillis) / 60000).toInt())
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(DrukGold)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = drink.preset.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${drink.preset.volumeMl.toInt()}ml at ${drink.preset.abv.toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = DrukInk.copy(alpha = 0.62f)
                )
            }
            Text(
                text = if (minutesAgo == 0) "now" else "${minutesAgo}m ago",
                style = MaterialTheme.typography.labelLarge,
                color = DrukInk.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun EmptyHistory() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = "No drinks logged yet.",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = DrukInk.copy(alpha = 0.65f)
        )
    }
}

@Composable
private fun Disclaimer() {
    Text(
        text = "BAC is an estimate, not a measurement. Do not use Druk to decide whether driving is safe.",
        style = MaterialTheme.typography.bodySmall,
        color = DrukInk.copy(alpha = 0.58f),
        modifier = Modifier.padding(bottom = 24.dp)
    )
}

@Composable
private fun MetricTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(DrukInk.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = DrukInk.copy(alpha = 0.58f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DrukInk
        )
    }
}

@Composable
private fun TargetBadge(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = DrukTeal,
        modifier = Modifier
            .background(DrukTeal.copy(alpha = 0.10f), RoundedCornerShape(99.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

private fun estimateBac(
    drinks: List<DrinkEntry>,
    weightKg: Double,
    widmarkFactor: Double,
    nowMillis: Long
): Double {
    val weightGrams = weightKg * 1000.0
    val rawBac = drinks.sumOf { drink ->
        val hoursSinceDrink = ((nowMillis - drink.loggedAtMillis).coerceAtLeast(0L)) / 3_600_000.0
        val absorption = (hoursSinceDrink * 60.0 / ABSORPTION_MINUTES).coerceIn(0.0, 1.0)
        val absorbedBac = (drink.gramsAlcohol * absorption / (weightGrams * widmarkFactor)) * 100.0
        absorbedBac
    }
    val sessionHours = drinks.maxOfOrNull {
        ((nowMillis - it.loggedAtMillis).coerceAtLeast(0L)) / 3_600_000.0
    } ?: 0.0
    return max(0.0, rawBac - (METABOLISM_PER_HOUR * sessionHours))
}

private fun guidanceFor(bac: Double, target: BacTarget): String {
    return when {
        bac < target.range.start -> "Below ${target.name}. A logged drink may move you toward target."
        bac in target.range -> "You are in the ${target.name} zone. Hold steady and sip slowly."
        bac < 0.080 -> {
            val waitMinutes = ceil(((bac - target.range.endInclusive) / METABOLISM_PER_HOUR) * 60.0)
                .toInt()
                .coerceAtLeast(10)
            "Above target. Wait about ${waitMinutes} min before another drink."
        }
        else -> "Alcohol recommendations paused. Switch to water and food."
    }
}

private fun statusColor(bac: Double, target: BacTarget): Color {
    return when {
        bac in target.range -> DrukTeal
        bac > 0.080 -> Color(0xFFB3261E)
        bac > target.range.endInclusive -> DrukGold
        else -> DrukInk
    }
}

@Preview(showBackground = true)
@Composable
private fun DrukHomeScreenPreview() {
    DrukTheme {
        DrukHomeScreen()
    }
}
