package io.jacob.episodive.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun OnboardingRoute(
    modifier: Modifier = Modifier,
//    viewModel: OnboardingViewModel = hiltViewModel(),
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
    onCompleted: () -> Unit,
) {

}