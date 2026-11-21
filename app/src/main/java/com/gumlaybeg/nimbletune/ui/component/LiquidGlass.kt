package com.gumlaybeg.nimbletune.ui.component

import android.os.Build
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow

val LocalLiquidGlassEnabled = staticCompositionLocalOf { true }
val LocalGlassPerformance = staticCompositionLocalOf { false }
val LocalReduceAnimation = staticCompositionLocalOf { false }
val LocalReduceDynamicBlur = staticCompositionLocalOf { false }

val LocalAppBackdrop = staticCompositionLocalOf<Backdrop> { error("No AppBackdrop provided") }
internal val LocalGlassExport = compositionLocalOf<LayerBackdrop?> { null }

internal val GlassSpring: AnimationSpec<Float> = spring(
    dampingRatio = 0.65f,
    stiffness = 400f,
)

fun isGlassSupported(sdkInt: Int = Build.VERSION.SDK_INT): Boolean = sdkInt >= Build.VERSION_CODES.S

private const val VIBRANCY = 1f
private const val BLUR_RADIUS_DP = 8f
private const val LENS_HEIGHT = 0.5f
private const val LENS_AMOUNT = 0.5f
private const val LENS_MAX_DP = 48f
private const val SURFACE_OPACITY = 0.4f

private const val FROST_BLUR_RADIUS_DP = 16f

internal val GLASS_EDGE_WIDTH = 0.5.dp
internal val GLASS_EDGE_COLOR = Color.White.copy(alpha = 0.10f)
private const val DARK_INDICATOR_OPACITY = 0.14f

@Composable
fun glassContentColor(): Color =
    if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) Color.Black else Color.White

@Composable
fun glassIndicatorColor(): Color =
    if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
        Color.White.copy(alpha = 0.5f)
    } else {
        Color.White.copy(alpha = DARK_INDICATOR_OPACITY)
    }

@Composable
fun Modifier.lightweightLiquidGlass(
    shape: CornerBasedShape,
    fallbackColor: Color,
): Modifier {
    val useGlass = LocalLiquidGlassEnabled.current && isGlassSupported()
    val glassTint = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
        Color(0xFFFAFAFA)
    } else {
        Color(0xFF121212)
    }

    return clip(shape)
        .background(
            color = if (useGlass) glassTint.copy(alpha = SURFACE_OPACITY) else fallbackColor,
            shape = shape,
        )
        .then(
            if (useGlass) {
                Modifier.drawBackdrop(
                    backdrop = emptyBackdrop(),
                    shape = { shape },
                    effects = {},
                    highlight = { Highlight.Default },
                    shadow = { null },
                )
            } else {
                Modifier
            },
        )
        .border(GLASS_EDGE_WIDTH, GLASS_EDGE_COLOR, shape)
}

@Composable
fun Modifier.liquidGlassRim(shape: CornerBasedShape): Modifier {
    if (!LocalLiquidGlassEnabled.current || !isGlassSupported()) return this
    return then(
        Modifier.drawBackdrop(
            backdrop = emptyBackdrop(),
            shape = { shape },
            effects = {},
            highlight = { Highlight.Default },
            shadow = { null },
        ),
    )
}

@Composable
fun Modifier.liquidGlass(
    shape: CornerBasedShape,
    surfaceTint: Color? = null,
): Modifier {
    if (!isGlassSupported()) {
        return background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f), shape)
            .border(GLASS_EDGE_WIDTH, GLASS_EDGE_COLOR, shape)
    }
    val reduceDynamicBlur = LocalReduceDynamicBlur.current
    if (reduceDynamicBlur) {
        return background(MaterialTheme.colorScheme.surface, shape)
            .border(GLASS_EDGE_WIDTH, GLASS_EDGE_COLOR, shape)
    }
    val glassPerformance = LocalGlassPerformance.current
    val backdrop = LocalAppBackdrop.current
    val exportedBackdrop = LocalGlassExport.current
    val density = LocalDensity.current
    val blurRadiusDp = if (glassPerformance) FROST_BLUR_RADIUS_DP else BLUR_RADIUS_DP
    val blurPx = with(density) { blurRadiusDp.dp.toPx() }
    val lensHeightPx = with(density) { (LENS_HEIGHT * LENS_MAX_DP).dp.toPx() }
    val lensAmountPx = with(density) { (LENS_AMOUNT * LENS_MAX_DP).dp.toPx() }
    val themeTint = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
        Color(0xFFFAFAFA)
    } else {
        Color(0xFF121212)
    }
    val surfaceTintColor = surfaceTint ?: themeTint.copy(alpha = SURFACE_OPACITY)

    return drawBackdrop(
        backdrop = backdrop,
        shape = { shape },
        effects = {
            colorControls(saturation = 1f + 0.5f * VIBRANCY)
            blur(blurPx)
            if (!glassPerformance && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                lens(
                    refractionHeight = lensHeightPx,
                    refractionAmount = lensAmountPx,
                    depthEffect = true,
                    chromaticAberration = true,
                )
            }
        },
        highlight = { Highlight.Default },
        shadow = { Shadow.Default },
        onDrawSurface = {
            drawRect(color = surfaceTintColor, size = size)
        },
        exportedBackdrop = exportedBackdrop,
    )
}