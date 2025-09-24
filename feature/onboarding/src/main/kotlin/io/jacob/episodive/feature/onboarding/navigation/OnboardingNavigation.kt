package io.jacob.episodive.feature.onboarding.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.jacob.episodive.feature.onboarding.OnboardingRoute
import kotlinx.serialization.Serializable

@Serializable
data object OnboardingRoute

fun NavGraphBuilder.onboardingScreen(
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
    onCompleted: () -> Unit,
) {
    composable<OnboardingRoute> {
        OnboardingRoute(
            onShowSnackbar = onShowSnackbar,
            onCompleted = onCompleted,
        )
    }
}