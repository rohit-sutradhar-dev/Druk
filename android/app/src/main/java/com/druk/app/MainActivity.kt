package com.druk.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.druk.app.ui.theme.DrukTheme

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

@Composable
fun DrukHomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Druk",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Pace your session around the zone you choose.",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Session")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DrukHomeScreenPreview() {
    DrukTheme {
        DrukHomeScreen()
    }
}

