package io.jacob.episodive.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import io.jacob.episodive.core.designsystem.R

val Pretendard = FontFamily(
    Font(R.font.pretendard_light, FontWeight.Light),
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_bold, FontWeight.Bold),
)

// Default Material 3 typography values
val baseline = Typography()

val EpisodiveTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = Pretendard, fontWeight = FontWeight.Bold),
    displayMedium = baseline.displayMedium.copy(fontFamily = Pretendard, fontWeight = FontWeight.Bold),
    displaySmall = baseline.displaySmall.copy(fontFamily = Pretendard, fontWeight = FontWeight.Bold),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = Pretendard, fontWeight = FontWeight.Bold),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = Pretendard, fontWeight = FontWeight.Bold),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = Pretendard, fontWeight = FontWeight.Bold),
    titleLarge = baseline.titleLarge.copy(fontFamily = Pretendard, fontWeight = FontWeight.Medium),
    titleMedium = baseline.titleMedium.copy(fontFamily = Pretendard, fontWeight = FontWeight.Medium),
    titleSmall = baseline.titleSmall.copy(fontFamily = Pretendard, fontWeight = FontWeight.Medium),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = Pretendard),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = Pretendard),
    bodySmall = baseline.bodySmall.copy(fontFamily = Pretendard),
    labelLarge = baseline.labelLarge.copy(fontFamily = Pretendard),
    labelMedium = baseline.labelMedium.copy(fontFamily = Pretendard),
    labelSmall = baseline.labelSmall.copy(fontFamily = Pretendard),
)