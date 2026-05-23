package com.druk.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.druk.app.ui.DrukApp
import com.druk.app.ui.DrukViewModel
import com.druk.app.ui.theme.DrukTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DrukTheme {
                val viewModel: DrukViewModel = viewModel()
                val state by viewModel.state.collectAsStateWithLifecycle()
                val notificationPermission = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) {
                    viewModel.markPermissionRequested()
                }

                DrukApp(
                    state = state,
                    actions = viewModel,
                    requestNotificationPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !state.permissionRequested) {
                            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
            }
        }
    }
}

