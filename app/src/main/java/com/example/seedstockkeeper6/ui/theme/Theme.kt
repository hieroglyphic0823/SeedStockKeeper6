package com.example.seedstockkeeper6.ui.theme

import android.os.Build
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.example.seedstockkeeper6.ui.theme.ThemeFlavor
import com.example.seedstockkeeper6.ui.theme.flavorColorScheme


@Composable
fun SeedStockKeeper6Theme(
    flavor: ThemeFlavor? = null,                // ★ 追加：nullなら従来のlight/dark
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    // ★ 優先順位：Dynamic > Flavor > 既存 light/dark
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        flavor != null -> {
            flavorColorScheme(flavor, darkTheme)
        }
        darkTheme -> darkScheme
        else -> lightScheme
    }

    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !darkTheme
    val systemBarColor = if (darkTheme) Color.Black else Color.White

    SideEffect {
        Log.d("ThemeDebug", "darkTheme=$darkTheme, useDarkIcons=$useDarkIcons, barColor=$systemBarColor")
        systemUiController.setSystemBarsColor(
            color = systemBarColor,
            darkIcons = useDarkIcons
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

// ---------- Light Medium Contrast ----------
private val mediumContrastLightColorScheme = lightColorScheme(
    primary = primaryLight, // base: オリーブ
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,

    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,

    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,

    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,

    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,

    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,

    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

// ---------- Light High Contrast ----------
private val highContrastLightColorScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = Color.Black, // より強調
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = Color.Black,

    secondary = secondaryLight,
    onSecondary = Color.Black,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = Color.Black,

    tertiary = tertiaryLight,
    onTertiary = Color.Black,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = Color.Black,

    error = errorLight,
    onError = Color.Black,
    errorContainer = errorContainerLight,
    onErrorContainer = Color.Black,

    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = Color.Black, // コントラスト強化
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = Color.Black,

    outline = Color(0xFF3B3622),
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,

    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

// ---------- Dark Medium Contrast ----------
private val mediumContrastDarkColorScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,

    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,

    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,

    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,

    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,

    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,

    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

// ---------- Dark High Contrast ----------
private val highContrastDarkColorScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = Color.Black, // コントラストを最強に
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = Color.Black,

    secondary = secondaryDark,
    onSecondary = Color.Black,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = Color.Black,

    tertiary = tertiaryDark,
    onTertiary = Color.Black,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = Color.Black,

    error = errorDark,
    onError = Color.Black,
    errorContainer = errorContainerDark,
    onErrorContainer = Color.Black,

    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = Color.White, // 視認性MAX
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = Color.White,

    outline = Color(0xFFF9F0D2),
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,

    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color
)

val unspecified_scheme = ColorFamily(
    Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable() () -> Unit
) {
  val colorScheme = when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
          val context = LocalContext.current
          if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> darkScheme
      else -> lightScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = AppTypography,
    content = content
  )
}

// ---------- Onion Light Scheme ----------
val onionLightScheme = lightColorScheme(
    primary = primaryOnionLight,
    onPrimary = onPrimaryOnionLight,
    primaryContainer = primaryContainerOnionLight,
    onPrimaryContainer = onPrimaryContainerOnionLight,
    secondary = secondaryOnionLight,
    onSecondary = onSecondaryOnionLight,
    secondaryContainer = secondaryContainerOnionLight,
    onSecondaryContainer = onSecondaryContainerOnionLight,
    tertiary = tertiaryOnionLight,
    onTertiary = onTertiaryOnionLight,
    tertiaryContainer = tertiaryContainerOnionLight,
    onTertiaryContainer = onTertiaryContainerOnionLight,
    error = errorOnionLight,
    onError = onErrorOnionLight,
    errorContainer = errorContainerOnionLight,
    onErrorContainer = onErrorContainerOnionLight,
    background = backgroundOnionLight,
    onBackground = onBackgroundOnionLight,
    surface = surfaceOnionLight,
    onSurface = onSurfaceOnionLight,
    surfaceVariant = surfaceVariantOnionLight,
    onSurfaceVariant = onSurfaceVariantOnionLight,
    outline = outlineOnionLight,
    outlineVariant = outlineVariantOnionLight,
    scrim = scrimOnionLight,
    inverseSurface = inverseSurfaceOnionLight,
    inverseOnSurface = inverseOnSurfaceOnionLight,
    inversePrimary = inversePrimaryOnionLight,
    surfaceDim = surfaceDimOnionLight,
    surfaceBright = surfaceBrightOnionLight,
    surfaceContainerLowest = surfaceContainerLowestOnionLight,
    surfaceContainerLow = surfaceContainerLowOnionLight,
    surfaceContainer = surfaceContainerOnionLight,
    surfaceContainerHigh = surfaceContainerHighOnionLight,
    surfaceContainerHighest = surfaceContainerHighestOnionLight,
)

// ---------- Onion Dark Scheme ----------
val onionDarkScheme = darkColorScheme(
    primary = primaryOnionDark,
    onPrimary = onPrimaryOnionDark,
    primaryContainer = primaryContainerOnionDark,
    onPrimaryContainer = onPrimaryContainerOnionDark,
    secondary = secondaryOnionDark,
    onSecondary = onSecondaryOnionDark,
    secondaryContainer = secondaryContainerOnionDark,
    onSecondaryContainer = onSecondaryContainerOnionDark,
    tertiary = tertiaryOnionDark,
    onTertiary = onTertiaryOnionDark,
    tertiaryContainer = tertiaryContainerOnionDark,
    onTertiaryContainer = onTertiaryContainerOnionDark,
    error = errorOnionDark,
    onError = onErrorOnionDark,
    errorContainer = errorContainerOnionDark,
    onErrorContainer = onErrorContainerOnionDark,
    background = backgroundOnionDark,
    onBackground = onBackgroundOnionDark,
    surface = surfaceOnionDark,
    onSurface = onSurfaceOnionDark,
    surfaceVariant = surfaceVariantOnionDark,
    onSurfaceVariant = onSurfaceVariantOnionDark,
    outline = outlineOnionDark,
    outlineVariant = outlineVariantOnionDark,
    scrim = scrimOnionDark,
    inverseSurface = inverseSurfaceOnionDark,
    inverseOnSurface = inverseOnSurfaceOnionDark,
    inversePrimary = inversePrimaryOnionDark,
    surfaceDim = surfaceDimOnionDark,
    surfaceBright = surfaceBrightOnionDark,
    surfaceContainerLowest = surfaceContainerLowestOnionDark,
    surfaceContainerLow = surfaceContainerLowOnionDark,
    surfaceContainer = surfaceContainerOnionDark,
    surfaceContainerHigh = surfaceContainerHighOnionDark,
    surfaceContainerHighest = surfaceContainerHighestOnionDark,
)

// ---------- Renkon Light Scheme ----------
val renkonLightScheme = lightColorScheme(
    primary = primaryRenkonLight,
    onPrimary = onPrimaryRenkonLight,
    primaryContainer = primaryContainerRenkonLight,
    onPrimaryContainer = onPrimaryContainerRenkonLight,
    secondary = secondaryRenkonLight,
    onSecondary = onSecondaryRenkonLight,
    secondaryContainer = secondaryContainerRenkonLight,
    onSecondaryContainer = onSecondaryContainerRenkonLight,
    tertiary = tertiaryRenkonLight,
    onTertiary = onTertiaryRenkonLight,
    tertiaryContainer = tertiaryContainerRenkonLight,
    onTertiaryContainer = onTertiaryContainerRenkonLight,
    error = errorRenkonLight,
    onError = onErrorRenkonLight,
    errorContainer = errorContainerRenkonLight,
    onErrorContainer = onErrorContainerRenkonLight,
    background = backgroundRenkonLight,
    onBackground = onBackgroundRenkonLight,
    surface = surfaceRenkonLight,
    onSurface = onSurfaceRenkonLight,
    surfaceVariant = surfaceVariantRenkonLight,
    onSurfaceVariant = onSurfaceVariantRenkonLight,
    outline = outlineRenkonLight,
    outlineVariant = outlineVariantRenkonLight,
    scrim = scrimRenkonLight,
    inverseSurface = inverseSurfaceRenkonLight,
    inverseOnSurface = inverseOnSurfaceRenkonLight,
    inversePrimary = inversePrimaryRenkonLight,
    surfaceDim = surfaceDimRenkonLight,
    surfaceBright = surfaceBrightRenkonLight,
    surfaceContainerLowest = surfaceContainerLowestRenkonLight,
    surfaceContainerLow = surfaceContainerLowRenkonLight,
    surfaceContainer = surfaceContainerRenkonLight,
    surfaceContainerHigh = surfaceContainerHighRenkonLight,
    surfaceContainerHighest = surfaceContainerHighestRenkonLight,
)

// ---------- Renkon Dark Scheme ----------
val renkonDarkScheme = darkColorScheme(
    primary = primaryRenkonDark,
    onPrimary = onPrimaryRenkonDark,
    primaryContainer = primaryContainerRenkonDark,
    onPrimaryContainer = onPrimaryContainerRenkonDark,
    secondary = secondaryRenkonDark,
    onSecondary = onSecondaryRenkonDark,
    secondaryContainer = secondaryContainerRenkonDark,
    onSecondaryContainer = onSecondaryContainerRenkonDark,
    tertiary = tertiaryRenkonDark,
    onTertiary = onTertiaryRenkonDark,
    tertiaryContainer = tertiaryContainerRenkonDark,
    onTertiaryContainer = onTertiaryContainerRenkonDark,
    error = errorRenkonDark,
    onError = onErrorRenkonDark,
    errorContainer = errorContainerRenkonDark,
    onErrorContainer = onErrorContainerRenkonDark,
    background = backgroundRenkonDark,
    onBackground = onBackgroundRenkonDark,
    surface = surfaceRenkonDark,
    onSurface = onSurfaceRenkonDark,
    surfaceVariant = surfaceVariantRenkonDark,
    onSurfaceVariant = onSurfaceVariantRenkonDark,
    outline = outlineRenkonDark,
    outlineVariant = outlineVariantRenkonDark,
    scrim = scrimRenkonDark,
    inverseSurface = inverseSurfaceRenkonDark,
    inverseOnSurface = inverseOnSurfaceRenkonDark,
    inversePrimary = inversePrimaryRenkonDark,
    surfaceDim = surfaceDimRenkonDark,
    surfaceBright = surfaceBrightRenkonDark,
    surfaceContainerLowest = surfaceContainerLowestRenkonDark,
    surfaceContainerLow = surfaceContainerLowRenkonDark,
    surfaceContainer = surfaceContainerRenkonDark,
    surfaceContainerHigh = surfaceContainerHighRenkonDark,
    surfaceContainerHighest = surfaceContainerHighestRenkonDark,
)

// ---------- SweetPotato Light Scheme ----------
val sweetPotatoLightScheme = lightColorScheme(
    primary = primarySweetPotatoLight,
    onPrimary = onPrimarySweetPotatoLight,
    primaryContainer = primaryContainerSweetPotatoLight,
    onPrimaryContainer = onPrimaryContainerSweetPotatoLight,
    secondary = secondarySweetPotatoLight,
    onSecondary = onSecondarySweetPotatoLight,
    secondaryContainer = secondaryContainerSweetPotatoLight,
    onSecondaryContainer = onSecondaryContainerSweetPotatoLight,
    tertiary = tertiarySweetPotatoLight,
    onTertiary = onTertiarySweetPotatoLight,
    tertiaryContainer = tertiaryContainerSweetPotatoLight,
    onTertiaryContainer = onTertiaryContainerSweetPotatoLight,
    error = errorSweetPotatoLight,
    onError = onErrorSweetPotatoLight,
    errorContainer = errorContainerSweetPotatoLight,
    onErrorContainer = onErrorContainerSweetPotatoLight,
    background = backgroundSweetPotatoLight,
    onBackground = onBackgroundSweetPotatoLight,
    surface = surfaceSweetPotatoLight,
    onSurface = onSurfaceSweetPotatoLight,
    surfaceVariant = surfaceVariantSweetPotatoLight,
    onSurfaceVariant = onSurfaceVariantSweetPotatoLight,
    outline = outlineSweetPotatoLight,
    outlineVariant = outlineVariantSweetPotatoLight,
    scrim = scrimSweetPotatoLight,
    inverseSurface = inverseSurfaceSweetPotatoLight,
    inverseOnSurface = inverseOnSurfaceSweetPotatoLight,
    inversePrimary = inversePrimarySweetPotatoLight,
    surfaceDim = surfaceDimSweetPotatoLight,
    surfaceBright = surfaceBrightSweetPotatoLight,
    surfaceContainerLowest = surfaceContainerLowestSweetPotatoLight,
    surfaceContainerLow = surfaceContainerLowSweetPotatoLight,
    surfaceContainer = surfaceContainerSweetPotatoLight,
    surfaceContainerHigh = surfaceContainerHighSweetPotatoLight,
    surfaceContainerHighest = surfaceContainerHighestSweetPotatoLight,
)

// ---------- SweetPotato Dark Scheme ----------
val sweetPotatoDarkScheme = darkColorScheme(
    primary = primarySweetPotatoDark,
    onPrimary = onPrimarySweetPotatoDark,
    primaryContainer = primaryContainerSweetPotatoDark,
    onPrimaryContainer = onPrimaryContainerSweetPotatoDark,
    secondary = secondarySweetPotatoDark,
    onSecondary = onSecondarySweetPotatoDark,
    secondaryContainer = secondaryContainerSweetPotatoDark,
    onSecondaryContainer = onSecondaryContainerSweetPotatoDark,
    tertiary = tertiarySweetPotatoDark,
    onTertiary = onTertiarySweetPotatoDark,
    tertiaryContainer = tertiaryContainerSweetPotatoDark,
    onTertiaryContainer = onTertiaryContainerSweetPotatoDark,
    error = errorSweetPotatoDark,
    onError = onErrorSweetPotatoDark,
    errorContainer = errorContainerSweetPotatoDark,
    onErrorContainer = onErrorContainerSweetPotatoDark,
    background = backgroundSweetPotatoDark,
    onBackground = onBackgroundSweetPotatoDark,
    surface = surfaceSweetPotatoDark,
    onSurface = onSurfaceSweetPotatoDark,
    surfaceVariant = surfaceVariantSweetPotatoDark,
    onSurfaceVariant = onSurfaceVariantSweetPotatoDark,
    outline = outlineSweetPotatoDark,
    outlineVariant = outlineVariantSweetPotatoDark,
    scrim = scrimSweetPotatoDark,
    inverseSurface = inverseSurfaceSweetPotatoDark,
    inverseOnSurface = inverseOnSurfaceSweetPotatoDark,
    inversePrimary = inversePrimarySweetPotatoDark,
    surfaceDim = surfaceDimSweetPotatoDark,
    surfaceBright = surfaceBrightSweetPotatoDark,
    surfaceContainerLowest = surfaceContainerLowestSweetPotatoDark,
    surfaceContainerLow = surfaceContainerLowSweetPotatoDark,
    surfaceContainer = surfaceContainerSweetPotatoDark,
    surfaceContainerHigh = surfaceContainerHighSweetPotatoDark,
    surfaceContainerHighest = surfaceContainerHighestSweetPotatoDark,
)

// ---------- SweetP Light Scheme ----------
val sweetPLightScheme = lightColorScheme(
    primary = primarySweetPLight,
    onPrimary = onPrimarySweetPLight,
    primaryContainer = primaryContainerSweetPLight,
    onPrimaryContainer = onPrimaryContainerSweetPLight,
    secondary = secondarySweetPLight,
    onSecondary = onSecondarySweetPLight,
    secondaryContainer = secondaryContainerSweetPLight,
    onSecondaryContainer = onSecondaryContainerSweetPLight,
    tertiary = tertiarySweetPLight,
    onTertiary = onTertiarySweetPLight,
    tertiaryContainer = tertiaryContainerSweetPLight,
    onTertiaryContainer = onTertiaryContainerSweetPLight,
    error = errorSweetPLight,
    onError = onErrorSweetPLight,
    errorContainer = errorContainerSweetPLight,
    onErrorContainer = onErrorContainerSweetPLight,
    background = backgroundSweetPLight,
    onBackground = onBackgroundSweetPLight,
    surface = surfaceSweetPLight,
    onSurface = onSurfaceSweetPLight,
    surfaceVariant = surfaceVariantSweetPLight,
    onSurfaceVariant = onSurfaceVariantSweetPLight,
    outline = outlineSweetPLight,
    outlineVariant = outlineVariantSweetPLight,
    scrim = scrimSweetPLight,
    inverseSurface = inverseSurfaceSweetPLight,
    inverseOnSurface = inverseOnSurfaceSweetPLight,
    inversePrimary = inversePrimarySweetPLight,
    surfaceDim = surfaceDimSweetPLight,
    surfaceBright = surfaceBrightSweetPLight,
    surfaceContainerLowest = surfaceContainerLowestSweetPLight,
    surfaceContainerLow = surfaceContainerLowSweetPLight,
    surfaceContainer = surfaceContainerSweetPLight,
    surfaceContainerHigh = surfaceContainerHighSweetPLight,
    surfaceContainerHighest = surfaceContainerHighestSweetPLight,
)

// ---------- SweetP Dark Scheme ----------
val sweetPDarkScheme = darkColorScheme(
    primary = primarySweetPDark,
    onPrimary = onPrimarySweetPDark,
    primaryContainer = primaryContainerSweetPDark,
    onPrimaryContainer = onPrimaryContainerSweetPDark,
    secondary = secondarySweetPDark,
    onSecondary = onSecondarySweetPDark,
    secondaryContainer = secondaryContainerSweetPDark,
    onSecondaryContainer = onSecondaryContainerSweetPDark,
    tertiary = tertiarySweetPDark,
    onTertiary = onTertiarySweetPDark,
    tertiaryContainer = tertiaryContainerSweetPDark,
    onTertiaryContainer = onTertiaryContainerSweetPDark,
    error = errorSweetPDark,
    onError = onErrorSweetPDark,
    errorContainer = errorContainerSweetPDark,
    onErrorContainer = onErrorContainerSweetPDark,
    background = backgroundSweetPDark,
    onBackground = onBackgroundSweetPDark,
    surface = surfaceSweetPDark,
    onSurface = onSurfaceSweetPDark,
    surfaceVariant = surfaceVariantSweetPDark,
    onSurfaceVariant = onSurfaceVariantSweetPDark,
    outline = outlineSweetPDark,
    outlineVariant = outlineVariantSweetPDark,
    scrim = scrimSweetPDark,
    inverseSurface = inverseSurfaceSweetPDark,
    inverseOnSurface = inverseOnSurfaceSweetPDark,
    inversePrimary = inversePrimarySweetPDark,
    surfaceDim = surfaceDimSweetPDark,
    surfaceBright = surfaceBrightSweetPDark,
    surfaceContainerLowest = surfaceContainerLowestSweetPDark,
    surfaceContainerLow = surfaceContainerLowSweetPDark,
    surfaceContainer = surfaceContainerSweetPDark,
    surfaceContainerHigh = surfaceContainerHighSweetPDark,
    surfaceContainerHighest = surfaceContainerHighestSweetPDark,
)
