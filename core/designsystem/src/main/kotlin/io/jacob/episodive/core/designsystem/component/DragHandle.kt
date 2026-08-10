package io.jacob.episodive.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 시트 손잡이.
 *
 * 스스로 가운데에 선다. 스캐폴드의 `dragHandle` 슬롯은 알아서 중앙에 놓아주지만 슬롯 밖에서
 * 직접 부르는 자리도 있고(로딩 스켈레톤), 그때 부모의 정렬에 기대면 폭 40dp 짜리 막대가
 * 그대로 왼쪽에 붙는다. 실제로 겪은 버그다.
 */
@Composable
fun EpisodiveDragHandle(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        // clickable 은 막대에만 건다. 바깥 Box 에 걸면 시트 폭 전체가 눌리는 영역이 된다.
        Box(
            modifier = Modifier
                .padding(top = 12.dp, bottom = 18.dp)
                .height(4.dp)
                .width(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(2.dp)
                )
                .clickable {},
        )
    }
}