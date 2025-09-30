package io.jacob.episodive.core.player

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.test.core.app.ApplicationProvider
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class ExoPlayerRule : TestWatcher() {
    lateinit var player: ExoPlayer
        private set

    override fun starting(description: Description?) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
        player = ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setSeekBackIncrementMs(15_000L)
            .setSeekForwardIncrementMs(30_000L)
            .build()
    }

    override fun finished(description: Description?) {
        player.release()
    }
}