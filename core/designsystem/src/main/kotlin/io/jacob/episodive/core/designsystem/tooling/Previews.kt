package io.jacob.episodive.core.designsystem.tooling

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "daylight ko",
    apiLevel = 35,
    device = "spec:width=1280,height=2856,dpi=480",
    locale = "ko",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Preview(
    name = "night en",
    apiLevel = 35,
    device = "spec:width=1280,height=2856,dpi=480",
    locale = "en",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    backgroundColor = 0xFF000000
)
annotation class DevicePreviews

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light theme")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark theme")
annotation class ThemePreviews