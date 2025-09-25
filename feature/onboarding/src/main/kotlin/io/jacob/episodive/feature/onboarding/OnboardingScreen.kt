package io.jacob.episodive.feature.onboarding

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.jacob.episodive.core.designsystem.component.EpisodiveButton
import io.jacob.episodive.core.designsystem.component.EpisodiveIconToggleButton
import io.jacob.episodive.core.designsystem.component.StateImage
import io.jacob.episodive.core.designsystem.component.scrollbar.DecorativeScrollbar
import io.jacob.episodive.core.designsystem.component.scrollbar.scrollbarState
import io.jacob.episodive.core.designsystem.icon.EpisodiveIcons
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.tooling.DevicePreviews
import io.jacob.episodive.core.model.Category
import kotlinx.coroutines.flow.collectLatest

@Composable
fun OnboardingRoute(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
    onCompleted: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val moreCategories = stringResource(R.string.feature_onboarding_category_more_categories)
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is OnboardingEffect.ToastMoreCategories -> onShowSnackbar(moreCategories, null)
                is OnboardingEffect.NavigateToMain -> onCompleted()
            }
        }
    }

    when (val s = state) {
        is OnboardingState.Welcome -> {
            WelcomeScreen(
                modifier = modifier,
                onNextClick = { viewModel.sendAction(OnboardingAction.NextPage) },
            )
        }

        is OnboardingState.PreferredCategories -> {
            CategoryScreen(
                modifier = modifier,
                categories = s.categories,
                onCategoryCheckedChanged = { category, isSelected ->
                    viewModel.sendAction(OnboardingAction.ChooseCategory(category, isSelected))
                },
                onNextClick = { viewModel.sendAction(OnboardingAction.NextPage) },
            )
        }

        is OnboardingState.PreferredFeeds -> {}
        is OnboardingState.Completed -> {}
    }
}

//@Composable
//private fun OnboardingScreen(
//    modifier: Modifier = Modifier,
//    onNextClick: () -> Unit,
//) {
//    val pagerState = rememberPagerState(pageCount = { 4 })
//
//    Box(
//        modifier = modifier
//            .fillMaxSize()
//    ) {
//        HorizontalPager(
//            state = pagerState,
//            modifier = Modifier.fillMaxSize()
//        ) { page ->
//            when (page) {
//                0 -> WelcomeScreen()
//                1 -> CategorySelectionScreen()
//                2 -> PodcastSelectionScreen()
//            }
//        }
//
//        // 인디케이터
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.Center
//        ) {
//            repeat(3) { iteration ->
//                val color = if (pagerState.currentPage == iteration)
//                    Color.Blue else Color.Gray
//                Box(
//                    modifier = Modifier
//                        .padding(2.dp)
//                        .clip(CircleShape)
//                        .background(color)
//                        .size(8.dp)
//                )
//            }
//        }
//
//        EpisodiveButton(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//                .align(Alignment.BottomCenter),
//            shape = RoundedCornerShape(12.dp),
//            onClick = onNextClick,
//            text = { Text(text = stringResource(R.string.feature_onboarding_next)) },
//            enabled = true,
//        )
//    }
//}

@Composable
private fun WelcomeScreen(
    modifier: Modifier = Modifier,
    onNextClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
        Text(text = "Welcome Screen")

        EpisodiveButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(12.dp),
            onClick = onNextClick,
            text = { Text(text = stringResource(R.string.feature_onboarding_next)) },
            enabled = true,
        )
    }
}

@Composable
private fun CategoryScreen(
    modifier: Modifier = Modifier,
    categories: List<CategoryUiModel>,
    onCategoryCheckedChanged: (Category, Boolean) -> Unit,
    onNextClick: () -> Unit,
) {
    CategorySelection(
        modifier = modifier
            .fillMaxSize(),
        categories = categories,
        onCategoryCheckedChanged = onCategoryCheckedChanged,
        onNextClick = onNextClick,
    )
}

@Composable
private fun CategorySelection(
    modifier: Modifier = Modifier,
    categories: List<CategoryUiModel>,
    onCategoryCheckedChanged: (Category, Boolean) -> Unit,
    onNextClick: () -> Unit,
) {
    val lazyGridState = rememberLazyGridState()
    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()

    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
        LazyVerticalGrid(
            state = lazyGridState,
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp + systemBarsPadding.calculateTopPadding(),
                bottom = 32.dp + systemBarsPadding.calculateBottomPadding()
            ),
            modifier = Modifier
                .fillMaxSize()
                .testTag("onboarding:categorySelection"),
        ) {
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = stringResource(R.string.feature_onboarding_category_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
            }

            item(span = { GridItemSpan(2) }) {
                Text(
                    text = stringResource(R.string.feature_onboarding_category_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            items(
                items = categories,
                key = { it.category.id },
            ) {
                CategoryButton(
                    modifier = Modifier
                        .aspectRatio(1f),
                    category = it.category,
                    isSelected = it.isSelected,
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

        EpisodiveButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(12.dp),
            onClick = onNextClick,
            text = { Text(text = stringResource(R.string.feature_onboarding_next)) },
            enabled = true,
        )
    }
}

@Composable
private fun CategoryButton(
    modifier: Modifier = Modifier,
    category: Category,
    isSelected: Boolean,
    onClick: (Category, Boolean) -> Unit,
) {
    Surface(
        modifier = modifier
            .size(140.dp),
        shape = RoundedCornerShape(corner = CornerSize(16.dp)),
        selected = isSelected,
        onClick = {
            onClick(category, !isSelected)
        },
    ) {
        StateImage(
            modifier = Modifier
                .fillMaxSize(),
            imageUrl = category.imageUrl,
            contentDescription = category.label,
        )

        Box(
            modifier = Modifier.padding(12.dp),
        ) {
            Text(
                text = category.label,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .align(Alignment.BottomCenter),
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

@DevicePreviews
@Composable
private fun WelcomeScreenPreview() {
    EpisodiveTheme {
        WelcomeScreen(
            onNextClick = {},
        )
    }
}

@DevicePreviews
@Composable
private fun CategoryScreenPreview() {
    EpisodiveTheme {
        CategoryScreen(
            categories = Category.entries.map { CategoryUiModel(it, false) },
            onCategoryCheckedChanged = { _, _ -> },
            onNextClick = {},
        )
    }
}