package com.example.rolecall.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.rolecall.R

// ─────────── Fonts (local files) ───────────
val HeadlineFont    = FontFamily(Font(R.font.balsamiq_sans, FontWeight.Bold))
val SubHeadlineFont = FontFamily(Font(R.font.baloo_thambi2, FontWeight.SemiBold))
val BodyFont        = FontFamily(Font(R.font.baloo_tamma2, FontWeight.Normal))

// ─────────── Typography ───────────
private val RoleCallTypography = Typography(
    headlineLarge  = TextStyle(fontFamily = HeadlineFont,    fontWeight = FontWeight.Bold,     fontSize = 32.sp),
    headlineMedium = TextStyle(fontFamily = HeadlineFont,    fontWeight = FontWeight.Bold,     fontSize = 28.sp),
    headlineSmall  = TextStyle(fontFamily = HeadlineFont,    fontWeight = FontWeight.Bold,     fontSize = 24.sp),
    titleLarge     = TextStyle(fontFamily = SubHeadlineFont, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    titleMedium    = TextStyle(fontFamily = SubHeadlineFont, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    titleSmall     = TextStyle(fontFamily = SubHeadlineFont, fontWeight = FontWeight.Medium,   fontSize = 14.sp),
    bodyLarge      = TextStyle(fontFamily = BodyFont,        fontWeight = FontWeight.Normal,   fontSize = 16.sp),
    bodyMedium     = TextStyle(fontFamily = BodyFont,        fontWeight = FontWeight.Normal,   fontSize = 14.sp),
    bodySmall      = TextStyle(fontFamily = BodyFont,        fontWeight = FontWeight.Normal,   fontSize = 12.sp),
    labelLarge     = TextStyle(fontFamily = BodyFont,        fontWeight = FontWeight.Medium,   fontSize = 14.sp),
    labelMedium    = TextStyle(fontFamily = BodyFont,        fontWeight = FontWeight.Medium,   fontSize = 12.sp),
    labelSmall     = TextStyle(fontFamily = BodyFont,        fontWeight = FontWeight.Medium,   fontSize = 11.sp),
)

// ─────────── Color scheme (using constants from Color.kt) ───────────
private val RoleCallDarkColorScheme = darkColorScheme(
    primary            = UiInteractive,
    onPrimary          = androidx.compose.ui.graphics.Color.White,
    primaryContainer   = UiInteractive.copy(alpha = 0.3f),
    secondary          = AccentSuccess,
    onSecondary        = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = AccentSuccess.copy(alpha = 0.3f),
    background         = FoundationDark,
    onBackground       = PrimaryText,
    surface            = FoundationSurface,
    onSurface          = PrimaryText,
    surfaceVariant     = FoundationSurface,
    onSurfaceVariant   = SecondaryText,
    error              = AccentAlert,
    onError            = androidx.compose.ui.graphics.Color.White,
    outline            = Border,
)

// ─────────── Theme composable ───────────
@Composable
fun RoleCallTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RoleCallDarkColorScheme,
        typography = RoleCallTypography,
        content = content
    )
}