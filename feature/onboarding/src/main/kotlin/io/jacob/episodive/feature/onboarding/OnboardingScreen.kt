package io.jacob.episodive.feature.onboarding

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.jacob.episodive.core.designsystem.component.EpisodiveIconToggleButton
import io.jacob.episodive.core.designsystem.component.SectionHeader
import io.jacob.episodive.core.designsystem.component.StateImage
import io.jacob.episodive.core.designsystem.component.scrollbar.DecorativeScrollbar
import io.jacob.episodive.core.designsystem.component.scrollbar.scrollbarState
import io.jacob.episodive.core.designsystem.icon.EpisodiveIcons
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.tooling.DevicePreviews
import io.jacob.episodive.core.designsystem.tooling.ThemePreviews
import io.jacob.episodive.core.model.Category

@Composable
fun OnboardingRoute(
    modifier: Modifier = Modifier,
//    viewModel: OnboardingViewModel = hiltViewModel(),
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
    onCompleted: () -> Unit,
) {
    OnboardingScreen(
        modifier = modifier,
        categories = Category.entries.toList(),
        onCategoryCheckedChanged = { category, isSelected ->
//            viewModel.onCategoryCheckedChanged(category, isSelected)
        },
        onContinueClick = {
//            viewModel.onboardingCompleted()
//            onCompleted()
        },
    )
}

@Composable
private fun OnboardingScreen(
    modifier: Modifier = Modifier,
    categories: List<Category>,
    onCategoryCheckedChanged: (Category, Boolean) -> Unit,
    onContinueClick: () -> Unit,
) {
    SectionHeader(
        modifier = modifier
            .fillMaxSize(),
        text = "Choose your taste",
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            text = "Please select three or more podcast genres that you enjoy",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        CategorySelection(
            categories = categories,
            onCategoryCheckedChanged = onCategoryCheckedChanged,
        )
    }
}

@Composable
private fun CategorySelection(
    modifier: Modifier = Modifier,
    categories: List<Category>,
    onCategoryCheckedChanged: (Category, Boolean) -> Unit,
) {
    val lazyGridState = rememberLazyGridState()

    Box(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        LazyVerticalGrid(
            state = lazyGridState,
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("onboarding:categorySelection"),
        ) {
            items(
                items = categories,
                key = { it.id },
            ) {
                CategoryButton(
                    modifier = Modifier
                        .aspectRatio(1f),
                    category = it,
                    imageUrl = "https://plus.unsplash.com/premium_photo-1683888229278-51d1fa1b8ddb?q=80&w=3132&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                    isSelected = false,
                    onClick = onCategoryCheckedChanged
                )
            }
        }
        lazyGridState.DecorativeScrollbar(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 12.dp)
                .align(Alignment.TopEnd),
            state = lazyGridState.scrollbarState(itemsAvailable = categories.size),
            orientation = Orientation.Vertical,
        )
    }
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

@DevicePreviews
@Composable
private fun OnboardingScreenPreview() {
    EpisodiveTheme {
        OnboardingScreen(
            categories = Category.entries,
            onCategoryCheckedChanged = { _, _ -> },
            onContinueClick = {},
        )
    }
}