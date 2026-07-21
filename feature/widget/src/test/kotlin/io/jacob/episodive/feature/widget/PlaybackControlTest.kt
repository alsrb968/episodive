package io.jacob.episodive.feature.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaybackControlTest {

    @Test
    fun `fromName returns matching enum for each valid name`() {
        PlaybackControl.entries.forEach { control ->
            assertEquals(control, PlaybackControl.fromName(control.name))
        }
    }

    @Test
    fun `fromName returns null for unknown name`() {
        assertNull(PlaybackControl.fromName("UNKNOWN_CONTROL"))
    }

    @Test
    fun `fromName returns null for null input`() {
        assertNull(PlaybackControl.fromName(null))
    }

    @Test
    fun `fromName is case sensitive`() {
        assertNull(PlaybackControl.fromName("play_pause"))
    }
}
