package com.example.rolecall.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.rolecall.R

// ─────────── Fonts ───────────
val HeadlineFont    = FontFamily(Font(R.font.momo_trust_sans_bold, FontWeight.Bold))
val SubHeadlineFont = FontFamily(Font(R.font.momo_trust_sans_semibold, FontWeight.SemiBold))
val BodyFont        = FontFamily(Font(R.font.baloo_tamma2, FontWeight.Normal))

// ─────────── Typography ───────────
private val RoleCallTypography = Typography(
    headlineLarge  = TextStyle(fontFamily = HeadlineFont,    fontWeight = FontWeight.Bold,     fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = HeadlineFont,    fontWeight = FontWeight.Bold,     fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall  = TextStyle(fontFamily = HeadlineFont,    fontWeight = FontWeight.Bold,     fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge     = TextStyle(fontFamily = SubHeadlineFont, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium    = TextStyle(fontFamily = SubHeadlineFont, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall     = TextStyle(fontFamily = SubHeadlineFont, fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge      = TextStyle(fontFamily = BodyFont,        fontWeight = FontWeight.Normal,   fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium     = TextStyle(fontFamily = BodyFont,        fontWeight = FontWeight.Normal,   fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall      = TextStyle(fontFamily = BodyFont,        fontWeight = FontWeight.Normal,   fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge     = TextStyle(fontFamily = BodyFont,        fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium    = TextStyle(fontFamily = BodyFont,        fontWeight = FontWeight.Medium,   fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall     = TextStyle(fontFamily = BodyFont,        fontWeight = FontWeight.Medium,   fontSize = 11.sp, lineHeight = 16.sp),
)

// ─────────── Color scheme ───────────
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