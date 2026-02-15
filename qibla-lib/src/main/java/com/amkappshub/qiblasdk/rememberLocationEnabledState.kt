package com.amkappshub.qiblasdk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.location.LocationManagerCompat
import androidx.compose.runtime.State

/**
 * Monitors whether the user has enabled Location Services (GPS) on the device.
 * Returns true if enabled, false if disabled.
 */
@Composable
fun rememberLocationEnabledState(): State<Boolean> {
    val context = LocalContext.current
    val locationManager = remember { 
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    // Initial check
    val isEnabled = remember {
        mutableStateOf(LocationManagerCompat.isLocationEnabled(locationManager))
    }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // Update state when user toggles GPS in quick settings
                isEnabled.value = LocationManagerCompat.isLocationEnabled(locationManager)
            }
        }

        // Register for system changes
        context.registerReceiver(
            receiver,
            IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        )

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    return isEnabled
}