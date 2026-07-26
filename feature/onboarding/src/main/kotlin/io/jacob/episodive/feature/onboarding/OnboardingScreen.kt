package io.jacob.episodive.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.jacob.episodive.core.designsystem.component.EpisodiveButton
import io.jacob.episodive.core.designsystem.component.EpisodiveGradientBackground
import io.jacob.episodive.core.designsystem.component.LoadingWheel
import io.jacob.episodive.core.designsystem.component.scrollbar.DraggableScrollbar
import io.jacob.episodive.core.designsystem.component.scrollbar.scrollbarState
import io.jacob.episodive.core.designsystem.icon.EpisodiveIcons
import io.jacob.episodive.core.designsystem.screen.ErrorScreen
import io.jacob.episodive.core.designsystem.screen.LoadingScreen
import io.jacob.episodive.core.designsystem.theme.EpisodiveShapes
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.theme.GradientColors
import io.jacob.episodive.core.designsystem.theme.LocalDimensionTheme
import io.jacob.episodive.core.designsystem.tooling.DevicePreviews
import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.Podcast
import io.jacob.episodive.core.model.SelectableCategory
import io.jacob.episodive.core.testing.model.podcastTestDataList
import io.jacob.episodive.core.designsystem.component.EpisodiveFilterChip
import io.jacob.episodive.core.ui.PodcastDetailItem
import io.jacob.episodive.core.ui.displayName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

// v2: 토큰에 없는 온보딩 전용 일회성 값 (design/V2-SPEC.md 원본 줄 121~160)
private val WelcomeGlowOffsetX = (-60).dp
private val WelcomeGlowOffsetY = (-80).dp
private val WelcomeGlowSize = 320.dp
private val WelcomeGlowBlur = 8.dp
private val WelcomeCtaHeight = 58.dp
private val CategoryChipShape = RoundedCornerShape(22.dp)
private val CategoryChipHorizontalGap = 10.dp
private val CategoryChipVerticalGap = 4.dp
private val PagerIndicatorInactiveSize = 8.dp
private val PagerIndicatorActiveWidth = 26.dp
private val PagerIndicatorActiveShape = RoundedCornerShape(4.dp)
private val CtaIndicatorGap = 20.dp
private val CtaBottomPadding = 34.dp
private val CtaContentGap = 12.dp

@Composable
fun OnboardingRoute(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val followedPodcastIds by viewModel.followedPodcastIds.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { OnboardingPage.count })

    val moreCategories = stringResource(R.string.feature_onboarding_category_more_categories)
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is OnboardingEffect.ToastMoreCategories -> onShowSnackbar(moreCategories, null)
                is OnboardingEffect.MoveToPage ->
                    pagerState.animateScrollToPage(effect.page.ordinal)
            }
        }
    }

    when (val s = state) {
        is OnboardingState.Loading -> LoadingScreen()

        is OnboardingState.Success -> OnboardingScreen(
            modifier = modifier,
            pagerState = pagerState,
            categories = s.categories,
            podcasts = viewModel.recommendedPodcasts,
            followedPodcastIds = followedPodcastIds,
            onChooseCategory = { viewModel.sendAction(OnboardingAction.ChooseCategory(it)) },
            onChoosePodcast = { viewModel.sendAction(OnboardingAction.ChoosePodcast(it)) },
            onNextPage = { viewModel.sendAction(OnboardingAction.NextPage) },
        )

        is OnboardingState.Error -> ErrorScreen(message = s.message)
    }
}

@Composable
internal fun OnboardingScreen(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    categories: List<SelectableCategory>,
    podcasts: Flow<PagingData<Podcast>>,
    followedPodcastIds: Set<Long> = emptySet(),
    onChooseCategory: (Category) -> Unit,
    onChoosePodcast: (Podcast) -> Unit,
    onNextPage: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false,
        ) { page ->
            when (OnboardingPage.fromIndex(page)) {
                OnboardingPage.Welcome ->
                    WelcomeScreen()

                OnboardingPage.CategorySelection ->
                    CategorySelectionScreen(
                        modifier = modifier,
                        categories = categories,
                        onCategoryCheckedChanged = onChooseCategory,
                    )

                OnboardingPage.PodcastSelection ->
                    PodcastSelectionScreen(
                        modifier = modifier,
                        podcasts = podcasts,
                        followedPodcastIds = followedPodcastIds,
                        onToggleFollowedPodcast = onChoosePodcast,
                    )

                OnboardingPage.Completion ->
                    CompletionScreen()

                null -> {}
            }
        }

        if (pagerState.currentPage != OnboardingPage.lastIndex()) {
            val isWelcomePage = pagerState.currentPage == OnboardingPage.Welcome.ordinal

            EpisodiveGradientBackground(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.BottomCenter),
                gradientColors = GradientColors(
                    bottom = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 100.dp, bottom = CtaBottomPadding)
                        .align(Alignment.BottomCenter),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PagerIndicator(
                        modifier = Modifier
                            .fillMaxWidth(),
                        pageCount = OnboardingPage.count,
                        currentPage = pagerState.currentPage
                    )

                    Spacer(modifier = Modifier.height(CtaIndicatorGap))

                    EpisodiveButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isWelcomePage) WelcomeCtaHeight else LocalDimensionTheme.current.buttonHeight),
                        shape = EpisodiveShapes.pill,
                        onClick = onNextPage,
                        enabled = true,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(9.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = stringResource(
                                    if (isWelcomePage) {
                                        R.string.feature_onboarding_welcome_cta
                                    } else {
                                        R.string.feature_onboarding_next
                                    },
                                ),
                                style = MaterialTheme.typography.titleSmall,
                            )

                            if (isWelcomePage) {
                                Icon(
                                    imageVector = EpisodiveIcons.CaretRight,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }

                }
            }
        }
    }
}

@Composable
private fun WelcomeScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
        // v2: 좌상단 레드 radial 글로우
        Box(
            modifier = Modifier
                .offset(x = WelcomeGlowOffsetX, y = WelcomeGlowOffsetY)
                .size(WelcomeGlowSize)
                .blur(WelcomeGlowBlur)
                .background(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.32f),
                            0.68f to Color.Transparent,
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.feature_onboarding_undraw_relax_mode),
                contentDescription = "Welcome Image",
                modifier = Modifier
                    .fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(34.dp))

            Text(
                text = stringResource(R.string.feature_onboarding_welcome_title),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.feature_onboarding_welcome_description),
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = MaterialTheme.typography.bodyLarge.fontSize * 1.65f),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * 하단 CTA 오버레이(인디케이터 + 버튼)가 덮는 높이.
 *
 * 오버레이는 Pager 위에 `BottomCenter`로 떠 있어 스크롤 콘텐츠를 가린다. 스크롤 화면은 이만큼을
 * 하단 contentPadding 으로 비워야 마지막 항목이 버튼 뒤로 들어가지 않는다. 오버레이가 시스템
 * 내비게이션 영역까지 덮으므로 여기에 systemBars 하단 인셋을 더하면 이중이 된다.
 */
@Composable
private fun bottomOverlayPadding(): Dp =
    PagerIndicatorInactiveSize +
        CtaIndicatorGap +
        LocalDimensionTheme.current.buttonHeight +
        CtaBottomPadding +
        CtaContentGap

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategorySelectionScreen(
    modifier: Modifier = Modifier,
    categories: List<SelectableCategory>,
    onCategoryCheckedChanged: (Category) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
        LazyColumn(
            state = lazyListState,
            contentPadding = PaddingValues(
                top = systemBarsPadding.calculateTopPadding(),
                bottom = bottomOverlayPadding(),
            ),
            modifier = Modifier
                .fillMaxSize()
                .testTag("onboarding:categorySelection"),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 26.dp, bottom = 8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.feature_onboarding_category_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.feature_onboarding_category_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = CategoryChipHorizontalGap,
                        alignment = Alignment.CenterHorizontally,
                    ),
                    verticalArrangement = Arrangement.spacedBy(CategoryChipVerticalGap),
                ) {
                    categories.forEach {
                        EpisodiveFilterChip(
                            selected = it.isSelected,
                            onSelectedChange = { _ -> onCategoryCheckedChanged(it.category) },
                            shape = CategoryChipShape,
                            label = { Text(text = it.category.displayName()) },
                        )
                    }
                }
            }
        }
        lazyListState.DraggableScrollbar(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 12.dp)
                .align(Alignment.TopEnd),
            state = lazyListState.scrollbarState(itemsAvailable = categories.size),
            orientation = Orientation.Vertical,
            onThumbMoved = { thumbPosition ->
                scope.launch {
                    val itemIndex = (thumbPosition * categories.size).toInt()
                        .coerceIn(0, categories.size - 1)
                    lazyListState.scrollToItem(itemIndex)
                }
            }
        )
    }
}

@Composable
private fun PodcastSelectionScreen(
    modifier: Modifier = Modifier,
    podcasts: Flow<PagingData<Podcast>>,
    followedPodcastIds: Set<Long> = emptySet(),
    onToggleFollowedPodcast: (Podcast) -> Unit,
) {
    val podcastsPaging = podcasts.collectAsLazyPagingItems()
    val lazyListState = rememberLazyListState()
    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
        val podcastsSize = podcastsPaging.itemCount
        val refreshState = podcastsPaging.loadState.refresh

        // itemCount 만 보고 로딩을 그리면 결과가 0건인 카테고리 조합에서 스피너가 영원히 돈다.
        // 로딩이 끝났는지(NotLoading)와 실패했는지(Error)를 갈라 안내 문구를 대신 띄운다.
        if (podcastsSize == 0) {
            when (refreshState) {
                is LoadState.NotLoading, is LoadState.Error -> Text(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 32.dp),
                    text = stringResource(
                        if (refreshState is LoadState.Error) {
                            R.string.feature_onboarding_podcast_error
                        } else {
                            R.string.feature_onboarding_podcast_empty
                        }
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                is LoadState.Loading -> LoadingWheel(
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            return
        }

        LazyColumn(
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = systemBarsPadding.calculateTopPadding() + 16.dp,
                bottom = bottomOverlayPadding(),
            ),
            modifier = Modifier
                .fillMaxSize()
                .testTag("onboarding:podcastSelection"),
        ) {
            item {
                Column {
                    Text(
                        text = stringResource(R.string.feature_onboarding_podcast_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.feature_onboarding_podcast_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            items(
                count = podcastsPaging.itemCount,
                key = { podcastsPaging.peek(it)?.id ?: it },
            ) { index ->
                val podcast = podcastsPaging[index] ?: return@items
                // PagingData 는 팔로우 상태를 무효화하지 않으므로 followedPodcastIds 로 오버레이한다.
                PodcastDetailItem(
                    podcast = podcast,
                    isFollowed = podcast.id in followedPodcastIds,
                    onClick = { onToggleFollowedPodcast(podcast) },
                    onToggleFollowed = { onToggleFollowedPodcast(podcast) },
                )
            }
        }
        lazyListState.DraggableScrollbar(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 12.dp)
                .align(Alignment.TopEnd),
            state = lazyListState.scrollbarState(itemsAvailable = podcastsSize),
            orientation = Orientation.Vertical,
            onThumbMoved = { thumbPosition ->
                scope.launch {
                    val itemIndex = (thumbPosition * podcastsSize).toInt()
                        .coerceIn(0, podcastsSize - 1)
                    lazyListState.scrollToItem(itemIndex)
                }
            }
        )
    }
}

@Composable
private fun CompletionScreen(
    modifier: Modifier = Modifier,
) {
    val thickStrokeWidth = with(LocalDensity.current) { 6.dp.toPx() }
    val thickStroke =
        remember(thickStrokeWidth) { Stroke(width = thickStrokeWidth, cap = StrokeCap.Round) }

    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = modifier
                .padding(horizontal = 50.dp),
            verticalArrangement = Arrangement.spacedBy(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.feature_onboarding_undraw_to_the_moon),
                contentDescription = "Welcome Image",
            )

            LinearWavyProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth(),
                stroke = thickStroke,
                trackColor = MaterialTheme.colorScheme.outline,
                trackStroke = thickStroke,
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = stringResource(R.string.feature_onboarding_completion_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

        }
    }
}

@Composable
private fun PagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterHorizontally),
    ) {
        repeat(pageCount) { page ->
            val isActive = page == currentPage
            Box(
                modifier = Modifier
                    .width(if (isActive) PagerIndicatorActiveWidth else PagerIndicatorInactiveSize)
                    .height(PagerIndicatorInactiveSize)
                    .clip(if (isActive) PagerIndicatorActiveShape else CircleShape)
                    .background(
                        if (isActive)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceContainerHigh
                    )
            )
        }
    }
}

@DevicePreviews
@Composable
private fun WelcomeScreenPreview() {
    EpisodiveTheme {
        WelcomeScreen()
    }
}

@DevicePreviews
@Composable
private fun CategorySelectionScreenPreview() {
    EpisodiveTheme {
        CategorySelectionScreen(
            categories = Category.entries.map { SelectableCategory(it, false) },
            onCategoryCheckedChanged = {},
        )
    }
}

@DevicePreviews
@Composable
private fun PodcastSelectionScreenPreview() {
    EpisodiveTheme {
        PodcastSelectionScreen(
            podcasts = flowOf(PagingData.from(podcastTestDataList)),
            onToggleFollowedPodcast = {},
        )
    }
}

@DevicePreviews
@Composable
private fun CompletionScreenPreview() {
    EpisodiveTheme {
        CompletionScreen()
    }
}