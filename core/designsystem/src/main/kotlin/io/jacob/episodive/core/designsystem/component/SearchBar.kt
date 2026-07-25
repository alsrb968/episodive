package io.jacob.episodive.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import io.jacob.episodive.core.designsystem.icon.EpisodiveIcons
import io.jacob.episodive.core.designsystem.theme.EpisodiveShapes
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.theme.LocalDimensionTheme
import io.jacob.episodive.core.designsystem.tooling.DevicePreviews

@Composable
fun EpisodiveSearchBar(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    isExpandable: Boolean = true,
    isExpanded: Boolean = false,
    placeholder: @Composable () -> Unit = {},
    leadingIconOnCollapse: @Composable () -> Unit = {
        Icon(
            EpisodiveIcons.Search,
            contentDescription = "Search",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(21.dp),
        )
    },
    leadingIconOnExpand: @Composable () -> Unit = {
        Icon(
            EpisodiveIcons.ArrowBack,
            contentDescription = "Back",
            tint = MaterialTheme.colorScheme.onSurface,
        )
    },
    trailingIcon: @Composable () -> Unit = {
        Icon(
            imageVector = EpisodiveIcons.Close,
            contentDescription = "Clear",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    },
    contentOnCollapse: @Composable () -> Unit = {},
    contentOnExpand: @Composable (LazyListState) -> Unit = {},
    // 검색어가 비어 있을 때의 우측 슬롯. 동작이 붙어 있지 않은 아이콘을 놓지 않는다.
    trailingIconOnCollapse: @Composable () -> Unit = {},
) {
    var expanded by rememberSaveable { mutableStateOf(isExpanded) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val scrollState = rememberLazyListState()
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    // 원본은 접힘 `0 20 18`, 펼침 `14 16 16` (원본 줄 233·264). 사방 16dp 로 두면
    // 접힘 상태에서 좌우가 다른 리스트(20dp)와 어긋나고 위쪽에 불필요한 여백이 생긴다.
    val horizontalPadding by animateDpAsState(
        targetValue = if (expanded) 16.dp else 20.dp,
        label = "searchBarHorizontalPadding"
    )
    val topPadding by animateDpAsState(
        targetValue = if (expanded) 14.dp else 0.dp,
        label = "searchBarTopPadding"
    )
    val bottomPadding by animateDpAsState(
        targetValue = if (expanded) 16.dp else 18.dp,
        label = "searchBarBottomPadding"
    )
    val borderColor by animateColorAsState(
        targetValue = if (focused) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
        label = "borderColor"
    )

    LaunchedEffect(scrollState.isScrollInProgress) {
        if (scrollState.isScrollInProgress) {
            keyboardController?.hide()
        }
    }

    Column(
        modifier = modifier,
    ) {
        SearchBar(
            // 높이·테두리는 입력 필드에만 준다. 여기에 고정 높이를 걸면 펼쳤을 때
            // 전체 화면으로 커져야 할 결과 영역(최근 검색 등)이 56dp 로 잘려 사라진다.
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    top = topPadding,
                    bottom = bottomPadding,
                )
                .focusRequester(focusRequester),
            windowInsets = WindowInsets(0, 0, 0, 0),
            shape = EpisodiveShapes.searchBar,
            // 바깥 컨테이너는 화면 배경과 같은 색으로 둬 눈에 띄지 않게 하고, 보이는
            // 둥근 사각형은 입력 필드 하나만 그리게 한다. 둘 다 그리면 같은 자리에
            // 두 겹으로 겹쳐 보인다.
            colors = SearchBarDefaults.colors(
                containerColor = MaterialTheme.colorScheme.background,
                // M3 SearchBar 는 펼쳤을 때 입력 필드와 결과 사이에 divider 를 하나 그린다.
                // 이 화면의 구분선 규칙과 무관한 선이라 보이지 않게 둔다.
                dividerColor = Color.Transparent,
            ),
            inputField = {
                SearchBarDefaults.InputField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(LocalDimensionTheme.current.fieldHeight)
                        .clip(EpisodiveShapes.searchBar)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(width = 1.dp, color = borderColor, shape = EpisodiveShapes.searchBar),
                    query = query,
                    onQueryChange = onQueryChange,
                    onSearch = {
                        onSearch(query)
                        keyboardController?.hide()
                    },
                    expanded = expanded,
                    onExpandedChange = { if (isExpandable) expanded = it },
                    placeholder = placeholder,
                    interactionSource = interactionSource,
                    leadingIcon = {
                        if (expanded) {
                            IconButton(
                                onClick = { if (isExpandable) expanded = false }
                            ) {
                                leadingIconOnExpand()
                            }
                        } else {
                            leadingIconOnCollapse()
                        }
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    onQueryChange("")
                                    focusRequester.requestFocus()
                                }
                            ) {
                                trailingIcon()
                            }
                        } else {
                            trailingIconOnCollapse()
                        }
                    }
                )
            },
            expanded = expanded,
            onExpandedChange = { if (isExpandable) expanded = it },
        ) {
            contentOnExpand(scrollState)
        }

        if (!expanded) {
            contentOnCollapse()
        }
    }
}

@DevicePreviews
@Composable
private fun EpisodiveSearchBarCollapsePreview() {
    EpisodiveTheme {
        EpisodiveSearchBar(
            query = "search",
            onQueryChange = {},
            onSearch = {},
            isExpanded = false,
            contentOnCollapse = {
                Text(
                    text = "Collapsed content",
                    color = MaterialTheme.colorScheme.onBackground,
                )
            },
            contentOnExpand = { _ ->
                Text(
                    text = "Expanded content",
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        )
    }
}

@DevicePreviews
@Composable
private fun EpisodiveSearchBarExpandPreview() {
    EpisodiveTheme {
        EpisodiveSearchBar(
            query = "search",
            onQueryChange = {},
            onSearch = {},
            isExpanded = true,
            contentOnCollapse = {
                Text(
                    text = "Collapsed content",
                    color = MaterialTheme.colorScheme.onBackground,
                )
            },
            contentOnExpand = { _ ->
                Text(
                    text = "Expanded content",
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        )
    }
}