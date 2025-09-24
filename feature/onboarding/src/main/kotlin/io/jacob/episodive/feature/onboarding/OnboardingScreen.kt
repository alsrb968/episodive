package io.jacob.episodive.feature.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.jacob.episodive.core.designsystem.component.EpisodiveIconToggleButton
import io.jacob.episodive.core.designsystem.component.StateImage
import io.jacob.episodive.core.designsystem.icon.EpisodiveIcons
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.tooling.ThemePreviews
import io.jacob.episodive.core.model.Category

@Composable
fun OnboardingRoute(
    modifier: Modifier = Modifier,
//    viewModel: OnboardingViewModel = hiltViewModel(),
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
    onCompleted: () -> Unit,
) {

}

@Composable
private fun CategoryButton(
    modifier: Modifier = Modifier,
    category: Category,
    imageUrl: String,
    isSelected: Boolean,
    onClick: (Category, Boolean) -> Unit,
) {
    Surface(
        modifier = modifier
            .size(140.dp),
        shape = RoundedCornerShape(corner = CornerSize(16.dp)),
        color = MaterialTheme.colorScheme.surface,
        selected = isSelected,
        onClick = {
            onClick(category, !isSelected)
        },
    ) {
        StateImage(
            modifier = Modifier
                .fillMaxSize(),
            imageUrl = imageUrl,
            contentDescription = category.label,
        )

        Box(
            modifier = Modifier.padding(12.dp),
        ) {
            Text(
                text = category.label,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .align(Alignment.Center),
                color = MaterialTheme.colorScheme.onSurface,
            )
            EpisodiveIconToggleButton(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.TopEnd),
                checked = isSelected,
                onCheckedChange = { checked -> onClick(category, checked) },
                icon = {
                    Icon(
                        imageVector = EpisodiveIcons.Add,
                        contentDescription = category.label,
                    )
                },
                checkedIcon = {
                    Icon(
                        imageVector = EpisodiveIcons.Check,
                        contentDescription = category.label,
                    )
                },
            )
        }
    }
}

@ThemePreviews
@Composable
private fun CategoryButtonPreview() {
    EpisodiveTheme {
        CategoryButton(
            category = Category.ENTREPRENEURSHIP,
            imageUrl = "https://www.example.com/image.jpg",
            isSelected = true,
            onClick = { _, _ -> },
        )
    }
}