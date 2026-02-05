package com.metaclean.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

fun getDarkColorScheme(colorTheme: String) = when(colorTheme) {
    "yellow" -> darkColorScheme(
        primary = YellowPrimaryDark,
        secondary = YellowSecondaryDark,
        tertiary = YellowTertiaryDark,
        primaryContainer = Color(0xFF6D4C00),
        secondaryContainer = Color(0xFF8D6E00),
        onPrimary = Color(0xFF3E2700),
        onSecondary = Color(0xFF3E2700)
    )
    "green" -> darkColorScheme(
        primary = GreenPrimaryDark,
        secondary = GreenSecondaryDark,
        tertiary = GreenTertiaryDark,
        primaryContainer = Color(0xFF1B5E20),
        secondaryContainer = Color(0xFF2E7D32)
    )
    "blue" -> darkColorScheme(
        primary = BluePrimaryDark,
        secondary = BlueSecondaryDark,
        tertiary = BlueTertiaryDark,
        primaryContainer = Color(0xFF0D47A1),
        secondaryContainer = Color(0xFF1565C0)
    )
    "purple" -> darkColorScheme(
        primary = PurplePrimaryDark,
        secondary = PurpleSecondaryDark,
        tertiary = PurpleTertiaryDark,
        primaryContainer = Color(0xFF4A148C),
        secondaryContainer = Color(0xFF6A1B9A)
    )
    "red" -> darkColorScheme(
        primary = RedPrimaryDark,
        secondary = RedSecondaryDark,
        tertiary = RedTertiaryDark,
        primaryContainer = Color(0xFFB71C1C),
        secondaryContainer = Color(0xFFC62828)
    )
    "orange" -> darkColorScheme(
        primary = OrangePrimaryDark,
        secondary = OrangeSecondaryDark,
        tertiary = OrangeTertiaryDark,
        primaryContainer = Color(0xFFBF360C),
        secondaryContainer = Color(0xFFE65100)
    )
    else -> darkColorScheme(
        primary = YellowPrimaryDark,
        secondary = YellowSecondaryDark,
        tertiary = YellowTertiaryDark
    )
}

fun getLightColorScheme(colorTheme: String) = when(colorTheme) {
    "yellow" -> lightColorScheme(
        primary = YellowPrimary,
        secondary = YellowSecondary,
        tertiary = YellowTertiary,
        primaryContainer = Color(0xFFFFE082),
        secondaryContainer = Color(0xFFFFF9C4),
        onPrimaryContainer = Color(0xFF6D4C00),
        onSecondaryContainer = Color(0xFF8D6E00)
    )
    "green" -> lightColorScheme(
        primary = GreenPrimary,
        secondary = GreenSecondary,
        tertiary = GreenTertiary,
        primaryContainer = Color(0xFFC8E6C9),
        secondaryContainer = Color(0xFFE8F5E9),
        onPrimaryContainer = Color(0xFF1B5E20),
        onSecondaryContainer = Color(0xFF2E7D32)
    )
    "blue" -> lightColorScheme(
        primary = BluePrimary,
        secondary = BlueSecondary,
        tertiary = BlueTertiary,
        primaryContainer = Color(0xFFBBDEFB),
        secondaryContainer = Color(0xFFE3F2FD),
        onPrimaryContainer = Color(0xFF0D47A1),
        onSecondaryContainer = Color(0xFF1565C0)
    )
    "purple" -> lightColorScheme(
        primary = PurplePrimary,
        secondary = PurpleSecondary,
        tertiary = PurpleTertiary,
        primaryContainer = Color(0xFFE1BEE7),
        secondaryContainer = Color(0xFFF3E5F5),
        onPrimaryContainer = Color(0xFF4A148C),
        onSecondaryContainer = Color(0xFF6A1B9A)
    )
    "red" -> lightColorScheme(
        primary = RedPrimary,
        secondary = RedSecondary,
        tertiary = RedTertiary,
        primaryContainer = Color(0xFFFFCDD2),
        secondaryContainer = Color(0xFFFFEBEE),
        onPrimaryContainer = Color(0xFFB71C1C),
        onSecondaryContainer = Color(0xFFC62828)
    )
    "orange" -> lightColorScheme(
        primary = OrangePrimary,
        secondary = OrangeSecondary,
        tertiary = OrangeTertiary,
        primaryContainer = Color(0xFFFFE0B2),
        secondaryContainer = Color(0xFFFFF3E0),
        onPrimaryContainer = Color(0xFFBF360C),
        onSecondaryContainer = Color(0xFFE65100)
    )
    else -> lightColorScheme(
        primary = YellowPrimary,
        secondary = YellowSecondary,
        tertiary = YellowTertiary
    )
}

@Composable
fun MetaCleanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorTheme: String = "yellow",
    dynamicColor: Boolean = false,  // Disabled to use our custom themes
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> getDarkColorScheme(colorTheme)
        else -> getLightColorScheme(colorTheme)
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
