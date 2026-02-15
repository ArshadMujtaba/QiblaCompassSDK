/*
 * Based on logic from hj-qibla-compass
 * Copyright (c) 2025 Muhammad Hassan Jamil
 * Licensed under Apache License 2.0
 * Modified by AMK APPS HUB for UI/UX enhancements.
 */
package com.amkappshub.qiblasdk

import android.location.Location
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

enum class QiblaStatus {
    OK,
    CALIBRATING,
    LOW_ACCURACY,
    NO_SENSOR,
    LOCATION_DENIED,
    LOCATION_DISABLED
}


data class QiblaState(
    val azimuth: Float? = null,
    val qiblaDirection: Float? = null,
    val bearingDifference: Float? = null,
    val location: Location? = null,
    val cityAndCountry: String? = null,
    val qiblaDistanceKm: Double? = null,
    val status: QiblaStatus
)

/**
 * A composable Qibla compass that reacts to the current azimuth and optionally a Qibla bearing.
 *
 * @param modifier Modifier applied to the compass container. Apply padding/size/rotation using this.
 * @param azimuthDegrees Heading of the device in degrees where 0° represents magnetic North.
 * @param qiblaDirectionDegrees Bearing towards Kaaba in degrees if available.
 * @param dialPainter Painter for the dial background image.
 * @param indicatorPainter Painter used for the Qibla indicator.
 * @param dialTint Optional tint applied to the dial.
 * @param indicatorTint Optional tint applied to the Qibla indicator.
 * @param backgroundColor Background color for the compass container.
 * @param borderColor Color of the outer border.
 * @param borderWidth Border width for the compass container.
 * @param showNorthMarker Whether to render a marker at the north direction.
 * @param markerColor Color of the north marker.
 * @param markerContentColor Color of the north marker content.
 * @param location Optional device location.
 * @param animationDurationMillis Duration for dial and indicator animations in milliseconds, default is 320.
 * @param showInfoPanel Whether to display the information panel overlay on the compass.
 */
@Composable
fun QiblaCompass(
    modifier: Modifier = Modifier,
    azimuthDegrees: Float?,
    qiblaDirectionDegrees: Float?,
    indicatorPainter: Painter = painterResource(id = R.drawable.qibla),
    dialTint: Color? = null,
    indicatorTint: Color? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    borderWidth: Dp = 2.dp,
    showNorthMarker: Boolean = true,
    markerColor: Color = MaterialTheme.colorScheme.primary,
    markerContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    location: Location? = null,
    status: QiblaStatus,
    animationDurationMillis: Int = 320,
    showInfoPanel: Boolean = true,
    openLocationSettings: () -> Unit,
    onShowDisclaimer: () -> Unit
) {
    val context = LocalContext.current

    var dialPrevious by remember { mutableFloatStateOf(0f) }
    var indicatorPrevious by remember { mutableFloatStateOf(0f) }

    val dialTarget = azimuthDegrees?.let { -it.normalizeDegrees() }
    val dialAnimatedTarget = dialTarget?.sanitizeRotation(dialPrevious) ?: dialPrevious
    val dialRotation by animateFloatAsState(
        targetValue = dialAnimatedTarget,
        animationSpec = tween(
            durationMillis = animationDurationMillis,
            easing = FastOutSlowInEasing
        ),
        label = "dialRotation"
    )

    LaunchedEffect(dialRotation, dialTarget) {
        if (dialTarget != null) {
            dialPrevious = dialRotation
        }
    }

    val indicatorTarget = if (azimuthDegrees != null && qiblaDirectionDegrees != null) {
        (qiblaDirectionDegrees - azimuthDegrees).normalizeSignedDegrees()
    } else null
    val indicatorAnimatedTarget =
        indicatorTarget?.sanitizeRotation(indicatorPrevious) ?: indicatorPrevious
    val indicatorRotation by animateFloatAsState(
        targetValue = indicatorAnimatedTarget,
        animationSpec = tween(
            durationMillis = animationDurationMillis,
            easing = FastOutSlowInEasing
        ),
        label = "indicatorRotation"
    )

    LaunchedEffect(indicatorRotation, indicatorTarget) {
        if (indicatorTarget != null) {
            indicatorPrevious = indicatorRotation
        }
    }

    // 1️⃣ INIT: Try to load saved city from memory immediately!
    var cityAndCountry by remember {
        mutableStateOf(LocationStorage.getSavedCity(context))
    }

    LaunchedEffect(location) {
        // 2️⃣ FETCH: Get fresh city name from internet
        val freshCity = getCityAndCountry(context, location)

        // 3️⃣ UPDATE & SAVE: If we got a real result, update UI and Storage
        if (freshCity != null) {
            cityAndCountry = freshCity
            // We need to save fresh city
            location?.let { loc ->
                LocationStorage.saveCity(context, freshCity)
            }
        }
    }

    val qiblaState = remember(
        azimuthDegrees, qiblaDirectionDegrees,
        location, cityAndCountry, status
    ) {
        val bearingDifference = if (azimuthDegrees != null && qiblaDirectionDegrees != null) {
            bearingDifference(azimuthDegrees, qiblaDirectionDegrees)
        } else null

        QiblaState(
            azimuth = azimuthDegrees,
            qiblaDirection = qiblaDirectionDegrees,
            bearingDifference = bearingDifference,
            location = location,
            cityAndCountry = cityAndCountry,
            qiblaDistanceKm = calculateQiblaDistanceKm(location),
            status = status
        )
    }

    Display(
        modifier,
        indicatorRotation,
        indicatorTint,
        showInfoPanel,
        qiblaState,
        openLocationSettings,
        onShowDisclaimer
    )
}

@Composable
private fun Display(
    modifier: Modifier,
    indicatorRotation: Float,
    indicatorTint: Color?,
    showInfoPanel: Boolean,
    qiblaState: QiblaState,
    openLocationSettings: () -> Unit,
    onShowDisclaimer: () -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        // 1. Determine Layout Mode
        val isLandscape = maxWidth > maxHeight
        val isTablet = maxWidth > 600.dp

        // 2. Layout Switching
        if (isLandscape) {
            // --- LANDSCAPE MODE (Side-by-Side) ---
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Compass
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CompassContent(
                        indicatorRotation = indicatorRotation,
                        indicatorTint = indicatorTint,
                        isLandscape = true
                    )
                }

                // Right: Info Panel
                if (showInfoPanel) {
                    Box(
                        modifier = Modifier
                            .width(if (isTablet) 400.dp else 320.dp)
                            .fillMaxHeight()
                            .padding(end = 16.dp, top = 16.dp, bottom = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Center
                        ) {
                            QiblaInfoPanel(
                                state = qiblaState,
                                openLocationSettings = openLocationSettings,
                                onShowDisclaimer = onShowDisclaimer
                            )
                        }
                    }
                }
            }
        } else {
            // --- PORTRAIT MODE (Top-to-Bottom) ---
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Middle: Compass
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CompassContent(
                        indicatorRotation = indicatorRotation,
                        indicatorTint = indicatorTint,
                        isLandscape = false
                    )
                }

                // Bottom: Info Panel
                if (showInfoPanel) {
                    Box(
                        modifier = Modifier
                            .wrapContentHeight()
                            .padding(bottom = 16.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        QiblaInfoPanel(
                            state = qiblaState,
                            openLocationSettings = openLocationSettings,
                            onShowDisclaimer = onShowDisclaimer
                        )
                    }
                }
            }
        }
    }
}

/**
 * Extracted component to fix the "Composable expected" warning.
 */
@Composable
private fun CompassContent(
    indicatorRotation: Float,
    indicatorTint: Color?,
    isLandscape: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Ensure the compass stays a perfect square based on the smallest dimension
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isLandscape) 0.8f else 1f)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            val scale = 0.8f

            // Crosshairs
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(scale),
                thickness = 4.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            VerticalDivider(
                modifier = Modifier.fillMaxHeight(scale),
                thickness = 4.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // The Needle/Compass Image
            Image(
                painter = painterResource(R.drawable.qibla_compass_image),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize(scale)
                    .graphicsLayer { rotationZ = indicatorRotation },
                colorFilter = ColorFilter.tint(
                    indicatorTint ?: MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}



@Composable
fun QiblaInfoPanel(
    modifier: Modifier = Modifier,
    state: QiblaState,
    openLocationSettings: () -> Unit,
    onShowDisclaimer: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 12.dp),
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- 1. MAIN DIRECTION & TURN GUIDANCE ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                state.qiblaDirection?.let { direction ->
                    Row(
                        modifier = Modifier.wrapContentWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${direction.normalizeDegrees().toInt()}°",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = direction.toCardinalDirection(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                state.bearingDifference?.let { diff ->
                    AnimatedTurnGuidance(diff)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // --- 2. META DATA ROW ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // City
                state.cityAndCountry?.let {
                    MetaInfoItem(
                        icon = Icons.Rounded.LocationOn,
                        label = "Location",
                        value = it.split(",").firstOrNull() ?: it
                    )
                } ?: MetaInfoItem(Icons.Rounded.LocationOn, "Location", "--")

                VerticalDivider(
                    Modifier.height(32.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Distance
                state.qiblaDistanceKm?.let {
                    MetaInfoItem(
                        icon = Icons.Rounded.Map,
                        label = "Distance",
                        value = "${it.toInt()} km"
                    )
                } ?: MetaInfoItem(Icons.Rounded.Map, "Distance", "--")

                VerticalDivider(
                    Modifier.height(32.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Accuracy Button
                FilledTonalButton(
                    onClick = onShowDisclaimer,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    // Use a lighter container to make it secondary to the status button below
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                ) {
                    Icon(
                        Icons.Rounded.Info,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Info")
                }
            }

            // --- 3. DYNAMIC STATUS ACTION (Loading OR Error) ---
            // This replaces the old AnimatedVisibility block
            AnimatedContent(
                targetState = state.status,
                label = "StatusAnimation",
                transitionSpec = {
                    (fadeIn() + expandVertically()).togetherWith(fadeOut() + shrinkVertically())
                }
            ) { currentStatus ->
                when {
                    // CASE A: Location Disabled -> Show Error Button
                    currentStatus == QiblaStatus.LOCATION_DISABLED -> {
                        FilledTonalButton(
                            onClick = openLocationSettings,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            modifier = Modifier.fillMaxWidth(0.7f) // Don't span full width
                        ) {
                            Icon(
                                Icons.Rounded.LocationOff,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Enable Location")
                        }
                    }

                    // CASE B: Calibrating -> Show Expressive Progress
                    currentStatus == QiblaStatus.CALIBRATING || state.location == null -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            // M3 Expressive Style: Round Caps + Specific sizing
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.5.dp,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                strokeCap = StrokeCap.Round,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "Calibrating...",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // CASE C: OK -> Show Nothing (Layout collapses)
                    else -> {
                        Spacer(Modifier.height(0.dp))
                    }
                }
            }
        }
    }
}


/* ---------------------------------------------------------
   ✨ POLISHED HELPER COMPONENTS
   --------------------------------------------------------- */

@Composable
private fun AnimatedTurnGuidance(diff: Float) {
    val absDiff = abs(diff)
    val isAligned = absDiff < 2f

    // Animate color: Green if aligned, Tertiary if turning
    val containerColor by animateColorAsState(
        targetValue = if (isAligned) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
        animationSpec = tween(300), label = "color"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isAligned) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer,
        animationSpec = tween(300), label = "textColor"
    )

    val text = when {
        isAligned -> stringResource(R.string.qibla_compass_aligned) // "You are facing Qibla"
        diff > 0 -> "Turn Right -> ${absDiff.normalizeDegrees().toInt()}°" // Use Icons if preferred
        else -> "${absDiff.normalizeDegrees().toInt()}° <- Turn Left"
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(100), // Fully pill shaped
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun MetaInfoItem(
    icon: ImageVector, label: String, value: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StatusMessage(status: QiblaStatus) {
    val (text, color) = when (status) {
        QiblaStatus.CALIBRATING -> Pair("Calibrating Compass...", MaterialTheme.colorScheme.primary)
        QiblaStatus.LOCATION_DENIED -> Pair(
            "Location Permission Needed",
            MaterialTheme.colorScheme.error
        )

        QiblaStatus.LOCATION_DISABLED -> Pair(
            "Enable Location Services",
            MaterialTheme.colorScheme.error
        )

        else -> Pair("", Color.Unspecified)
    }

    if (text.isNotEmpty()) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = color,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}
