package io.jacob.episodive.feature.widget

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class EpisodiveWidgetLayoutTest {

    @Test
    fun `forSize below strip height returns NONE mode with no feed`() {
        val layout = EpisodiveWidgetLayout.forSize(DpSize(300.dp, 100.dp))

        assertEquals(FeedMode.NONE, layout.feedMode)
        assertEquals(0, layout.feedCount)
        assertEquals(0, layout.gridColumns)
        assertEquals(0, layout.feedThumbDp)
        assertEquals(0, layout.feedHeightDp)
        assertEquals(80, layout.nowPlayingThumbDp)
    }

    @Test
    fun `forSize NONE mode clamps now-playing thumb to floor when area is tiny`() {
        val layout = EpisodiveWidgetLayout.forSize(DpSize(300.dp, 50.dp))

        assertEquals(FeedMode.NONE, layout.feedMode)
        assertEquals(56, layout.nowPlayingThumbDp)
    }

    @Test
    fun `forSize narrow strip height returns STRIP mode with 4 columns`() {
        val layout = EpisodiveWidgetLayout.forSize(DpSize(300.dp, 175.dp))

        assertEquals(FeedMode.STRIP, layout.feedMode)
        assertEquals(4, layout.feedCount)
        assertEquals(4, layout.gridColumns)
        assertEquals(56, layout.feedThumbDp)
        assertEquals(84, layout.feedHeightDp)
        assertEquals(71, layout.nowPlayingThumbDp)
    }

    @Test
    fun `forSize wide strip height returns STRIP mode with 5 columns`() {
        val layout = EpisodiveWidgetLayout.forSize(DpSize(340.dp, 200.dp))

        assertEquals(FeedMode.STRIP, layout.feedMode)
        assertEquals(5, layout.feedCount)
        assertEquals(5, layout.gridColumns)
        assertEquals(56, layout.feedThumbDp)
    }

    @Test
    fun `forSize narrow grid height returns GRID mode with 3 columns and uncapped thumb`() {
        val layout = EpisodiveWidgetLayout.forSize(DpSize(200.dp, 355.dp))

        assertEquals(FeedMode.GRID, layout.feedMode)
        assertEquals(6, layout.feedCount)
        assertEquals(3, layout.gridColumns)
        assertEquals(48, layout.feedThumbDp)
        assertEquals(178, layout.feedHeightDp)
    }

    @Test
    fun `forSize wide grid height returns GRID mode with 4 columns and thumb capped at max`() {
        val layout = EpisodiveWidgetLayout.forSize(DpSize(400.dp, 400.dp))

        assertEquals(FeedMode.GRID, layout.feedMode)
        assertEquals(8, layout.feedCount)
        assertEquals(4, layout.gridColumns)
        assertEquals(80, layout.feedThumbDp)
        assertEquals(242, layout.feedHeightDp)
        assertEquals(96, layout.nowPlayingThumbDp)
    }

    @Test
    fun `feedHeightOf returns fixed height for STRIP regardless of thumb size`() {
        assertEquals(84, EpisodiveWidgetLayout.feedHeightOf(FeedMode.STRIP, 999))
    }

    @Test
    fun `feedHeightOf returns zero for NONE regardless of thumb size`() {
        assertEquals(0, EpisodiveWidgetLayout.feedHeightOf(FeedMode.NONE, 999))
    }

    @Test
    fun `feedHeightOf computes GRID height from thumb size`() {
        assertEquals(178, EpisodiveWidgetLayout.feedHeightOf(FeedMode.GRID, 48))
    }

    @Test
    fun `feedThumbPx returns zero when feedThumbDp is not positive`() {
        val layout = EpisodiveWidgetLayout(
            feedMode = FeedMode.NONE,
            feedCount = 4,
            gridColumns = 0,
            feedThumbDp = 0,
            nowPlayingThumbDp = 80,
        )

        assertEquals(0, layout.feedThumbPx(density = 2f))
    }

    @Test
    fun `feedThumbPx returns zero when feedCount is not positive`() {
        val layout = EpisodiveWidgetLayout(
            feedMode = FeedMode.STRIP,
            feedCount = 0,
            gridColumns = 0,
            feedThumbDp = 56,
            nowPlayingThumbDp = 80,
        )

        assertEquals(0, layout.feedThumbPx(density = 2f))
    }

    @Test
    fun `feedThumbPx scales with density when within budget`() {
        val layout = EpisodiveWidgetLayout.forSize(DpSize(300.dp, 175.dp))

        assertEquals(112, layout.feedThumbPx(density = 2f))
    }

    @Test
    fun `feedThumbPx floors to minimum px when scaled value is too small`() {
        val layout = EpisodiveWidgetLayout.forSize(DpSize(300.dp, 175.dp))

        assertEquals(72, layout.feedThumbPx(density = 1f))
    }

    @Test
    fun `feedThumbPx caps to bitmap budget when scaled value is too large`() {
        val layout = EpisodiveWidgetLayout(
            feedMode = FeedMode.GRID,
            feedCount = 1,
            gridColumns = 1,
            feedThumbDp = 80,
            nowPlayingThumbDp = 90,
        )

        assertEquals(400, layout.feedThumbPx(density = 100f))
    }
}
