package io.jacob.episodive.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.jacob.episodive.core.designsystem.theme.EpisodiveTheme
import io.jacob.episodive.core.designsystem.tooling.DevicePreviews
import io.jacob.episodive.core.model.Category

/**
 * 카테고리의 화면 표시명.
 *
 * [Category.label]은 Podcast Index가 내려주는 영문 원본이라 그대로 쓰면 한국어 화면에도 영어가 뜬다.
 * 표시만 로케일에 맞추고 모델의 label/id는 API·DB 매칭용으로 건드리지 않는다.
 * 배열은 enum 선언 순서와 1:1이므로 [Category.ordinal]로 조회하고, 어긋나면 원본 label로 폴백한다.
 */
@Composable
fun Category.displayName(): String =
    stringArrayResource(R.array.core_ui_categories).getOrNull(ordinal) ?: label

@Composable
fun CategoryItem(
    modifier: Modifier = Modifier,
    category: Category,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        onClick = onClick,
    ) {
        Text(
            text = category.displayName(),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@DevicePreviews
@Composable
private fun CategoryItemPreview() {
    EpisodiveTheme {
        CategoryItem(
            category = Category.ENTREPRENEURSHIP,
            onClick = {},
        )
    }
}
