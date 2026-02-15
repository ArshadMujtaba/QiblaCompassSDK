package com.amkappshub.qiblasdk

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import android.provider.Settings
import kotlin.text.get

@Composable
fun QiblaScreen(
) {
    val context = LocalContext.current
    var showDisclaimer by remember { mutableStateOf(false) }

    var hasLocationPermission by remember { mutableStateOf(context.hasLocationPermission()) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        hasLocationPermission = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // 1. Monitor location on/off state
    val isLocationEnabled by rememberLocationEnabledState()


    // 🔴 ACTUALLY launch permission request
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val azimuthState = rememberCompassAzimuth()
    val locationState = rememberLocationState(
        hasLocationPermission,
        isLocationEnabled
    )
    val qiblaDirection = rememberQiblaDirection(
        locationState.value
    ).value

    val status = when {
        !hasLocationPermission ->
            QiblaStatus.LOCATION_DENIED

        !isLocationEnabled ->
            QiblaStatus.LOCATION_DISABLED

        locationState.value == null ->
            QiblaStatus.CALIBRATING // Waiting for fix

        azimuthState.value == null ->
            QiblaStatus.CALIBRATING

        else ->
            QiblaStatus.OK
    }


    QiblaCompass(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        azimuthDegrees = azimuthState.value,
        qiblaDirectionDegrees = qiblaDirection,
        location = locationState.value,
        indicatorPainter = painterResource(R.drawable.qibla_compass_image),
        indicatorTint = MaterialTheme.colorScheme.onSurface,
        status = status,
        showInfoPanel = true,
        animationDurationMillis = 260,
        openLocationSettings = {
            openLocationSettings(context)
        },
        onShowDisclaimer = {
            showDisclaimer = true
        }
    )

    if (showDisclaimer) {
        QiblaDisclaimerBottomSheet(
            onDismiss = { showDisclaimer = false }
        )
    }
}

fun openLocationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    context.startActivity(intent)
}
