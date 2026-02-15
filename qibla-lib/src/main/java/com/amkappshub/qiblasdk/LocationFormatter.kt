package com.amkappshub.qiblasdk

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

suspend fun getCityAndCountry(
    context: Context,
    location: Location?
): String? {
    if (location == null) return null

    // Helper to format the address string consistently
    fun formatAddress(address: Address?): String? {
        val city = address?.locality ?: address?.subAdminArea
        val country = address?.countryName
        return if (city != null || country != null) {
            listOfNotNull(city, country).joinToString(", ")
        } else null
    }

    // Switch to IO thread for background work
    return withContext(Dispatchers.IO) {
        val geocoder = Geocoder(context, Locale.getDefault())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // ✅ Android 13+: Convert the "Listener" API to a "Suspend" function
            suspendCancellableCoroutine { continuation ->
                try {
                    geocoder.getFromLocation(
                        location.latitude,
                        location.longitude,
                        1
                    ) { addresses ->
                        // Determine result
                        val result = formatAddress(addresses.firstOrNull())
                        // Resume the coroutine with the result
                        if (continuation.isActive) continuation.resume(result)
                    }
                } catch (e: Exception) {
                    if (continuation.isActive) continuation.resume(null)
                }
            }
        } else {
            // ✅ Android 12 and below: Blocking call (Safe here because we are in Dispatchers.IO)
            try {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1
                )
                formatAddress(addresses?.firstOrNull())
            } catch (e: Exception) {
                null
            }
        }
    }
}