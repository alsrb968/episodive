package io.jacob.episodive.core.player.datasource

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.Tracks
import androidx.media3.common.text.Cue
import androidx.media3.common.text.CueGroup
import androidx.media3.exoplayer.ExoPlayer
import app.cash.turbine.test
import io.jacob.episodive.core.domain.download.EpisodeDownloader
import io.jacob.episodive.core.testing.model.episodeTestData
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PlayerDataSourceImplTest {
    private val player = mockk<ExoPlayer>(relaxed = true)
    private val episodeDownloader = mockk<EpisodeDownloader>(relaxed = true)
    private val listenerSlot = slot<Player.Listener>()

    private lateinit var dataSource: PlayerDataSourceImpl

    private val episode = episodeTestData

    @Before
    fun setUp() {
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockk(relaxed = true)
        every { Uri.fromFile(any()) } returns mockk(relaxed = true)

        every { player.addListener(capture(listenerSlot)) } just Runs

        dataSource = PlayerDataSourceImpl(player, episodeDownloader)
    }

    @After
    fun tearDown() {
        dataSource.release()
        unmockkStatic(Uri::class)
    }

    // ---------------------------------------------------------------------
    // getPlayer / prepare / play / playClip / playClips
    // ---------------------------------------------------------------------

    @Test
    fun `Given player, When getPlayer called, Then same player instance returned`() {
        // When
        val result = dataSource.getPlayer()

        // Then
        assertEquals(player, result)
    }

    @Test
    fun `Given episodes, When prepare called, Then setMediaItems and prepare called on player`() {
        // Given
        val episodes = listOf(episode)

        // When
        dataSource.prepare(episodes, indexToPlay = 0, positionMs = 1000L)

        // Then
        verify {
            player.setMediaItems(any(), 0, 1000L)
            player.prepare()
        }
    }

    @Test
    fun `Given episode, When play called, Then setMediaItem, prepare, playWhenReady set on player`() {
        // When
        dataSource.play(episode)

        // Then
        val slot = slot<MediaItem>()
        verify {
            player.setMediaItem(capture(slot))
            player.prepare()
            player.playWhenReady = true
        }
        assertEquals(episode.id.toString(), slot.captured.mediaId)
        assertEquals(episode, slot.captured.localConfiguration?.tag)
    }

    @Test
    fun `Given episode with downloaded file, When play called, Then local file path resolved from downloader`() {
        // Given
        val downloadedEpisode = episode.copy(filePath = "5778530/42551776753.mp3")
        every { episodeDownloader.isFileDownloaded("5778530/42551776753.mp3") } returns true
        every { episodeDownloader.getDownloadDirectory() } returns "/data/downloads"

        // When
        dataSource.play(downloadedEpisode)

        // Then
        verify {
            episodeDownloader.isFileDownloaded("5778530/42551776753.mp3")
            episodeDownloader.getDownloadDirectory()
            player.setMediaItem(any())
        }
    }

    @Test
    fun `Given episodes and index, When play list called, Then setMediaItems, seekToDefaultPosition, prepare called`() {
        // Given
        val episodes = listOf(episode, episode.copy(id = 999L))

        // When
        dataSource.play(episodes, indexToPlay = 1)

        // Then
        verify {
            player.setMediaItems(any())
            player.seekToDefaultPosition(1)
            player.prepare()
            player.playWhenReady = true
        }
    }

    @Test
    fun `Given episodes and null index, When play list called, Then seekToDefaultPosition not called`() {
        // Given
        val episodes = listOf(episode)

        // When
        dataSource.play(episodes, indexToPlay = null)

        // Then
        verify(exactly = 0) { player.seekToDefaultPosition(any()) }
        verify {
            player.setMediaItems(any())
            player.prepare()
            player.playWhenReady = true
        }
    }

    @Test
    fun `Given clip episode, When playClip called, Then setMediaItem with clipping configuration applied`() {
        // Given
        val clipEpisode = episode.copy(
            clipStartTime = kotlin.time.Instant.fromEpochSeconds(100),
            clipDuration = kotlin.time.Duration.parse("PT10S"),
        )

        // When
        dataSource.playClip(clipEpisode)

        // Then
        val slot = slot<MediaItem>()
        verify {
            player.setMediaItem(capture(slot))
            player.prepare()
            player.playWhenReady = true
        }
        assertEquals(clipEpisode.clipStartPositionMs, slot.captured.clippingConfiguration.startPositionMs)
        assertEquals(clipEpisode.clipEndPositionMs, slot.captured.clippingConfiguration.endPositionMs)
    }

    @Test
    fun `Given clip episodes and index, When playClips called, Then setMediaItems, seekToDefaultPosition, prepare called`() {
        // Given
        val episodes = listOf(episode, episode.copy(id = 999L))

        // When
        dataSource.playClips(episodes, indexToPlay = 1)

        // Then
        verify {
            player.setMediaItems(any())
            player.seekToDefaultPosition(1)
            player.prepare()
            player.playWhenReady = true
        }
    }

    @Test
    fun `Given episode with transcriptUrl, When play called, Then subtitle configuration applied`() {
        // Given
        val episodeWithTranscript = episode.copy(transcriptUrl = "https://example.com/transcript.vtt")

        // When
        dataSource.play(episodeWithTranscript)

        // Then
        val slot = slot<MediaItem>()
        verify { player.setMediaItem(capture(slot)) }
        assertEquals(1, slot.captured.localConfiguration?.subtitleConfigurations?.size)
    }

    // ---------------------------------------------------------------------
    // playIndex / playOrPause / pause / resume / stop / next / previous
    // ---------------------------------------------------------------------

    @Test
    fun `Given valid index, When playIndex called, Then seekToDefaultPosition and playWhenReady set`() {
        // Given
        every { player.mediaItemCount } returns 5

        // When
        dataSource.playIndex(2)

        // Then
        verify {
            player.seekToDefaultPosition(2)
            player.playWhenReady = true
        }
    }

    @Test
    fun `Given out-of-range index, When playIndex called, Then player not touched`() {
        // Given
        every { player.mediaItemCount } returns 3

        // When
        dataSource.playIndex(5)

        // Then
        verify(exactly = 0) { player.seekToDefaultPosition(any()) }
    }

    @Test
    fun `Given player is playing, When playOrPause called, Then pause called`() {
        // Given
        every { player.isPlaying } returns true

        // When
        dataSource.playOrPause()

        // Then
        verify { player.pause() }
        verify(exactly = 0) { player.play() }
    }

    @Test
    fun `Given player is not playing, When playOrPause called, Then play called`() {
        // Given
        every { player.isPlaying } returns false

        // When
        dataSource.playOrPause()

        // Then
        verify { player.play() }
        verify(exactly = 0) { player.pause() }
    }

    @Test
    fun `Given player is playing, When pause called, Then player pause invoked`() {
        // Given
        every { player.isPlaying } returns true

        // When
        dataSource.pause()

        // Then
        verify { player.pause() }
    }

    @Test
    fun `Given player is not playing, When pause called, Then player pause not invoked`() {
        // Given
        every { player.isPlaying } returns false

        // When
        dataSource.pause()

        // Then
        verify(exactly = 0) { player.pause() }
    }

    @Test
    fun `Given player is not playing, When resume called, Then player play invoked`() {
        // Given
        every { player.isPlaying } returns false

        // When
        dataSource.resume()

        // Then
        verify { player.play() }
    }

    @Test
    fun `Given player is playing, When resume called, Then player play not invoked`() {
        // Given
        every { player.isPlaying } returns true

        // When
        dataSource.resume()

        // Then
        verify(exactly = 0) { player.play() }
    }

    @Test
    fun `When stop called, Then player stop and clearMediaItems invoked`() {
        // When
        dataSource.stop()

        // Then
        verify {
            player.stop()
            player.clearMediaItems()
        }
    }

    @Test
    fun `Given hasNextMediaItem true, When next called, Then seekToNextMediaItem and playWhenReady set`() {
        // Given
        every { player.hasNextMediaItem() } returns true

        // When
        dataSource.next()

        // Then
        verify {
            player.seekToNextMediaItem()
            player.playWhenReady = true
        }
    }

    @Test
    fun `Given hasNextMediaItem false, When next called, Then player not touched`() {
        // Given
        every { player.hasNextMediaItem() } returns false

        // When
        dataSource.next()

        // Then
        verify(exactly = 0) { player.seekToNextMediaItem() }
    }

    @Test
    fun `Given hasPreviousMediaItem true, When previous called, Then seekToPrevious and playWhenReady set`() {
        // Given
        every { player.hasPreviousMediaItem() } returns true

        // When
        dataSource.previous()

        // Then
        verify {
            player.seekToPrevious()
            player.playWhenReady = true
        }
    }

    @Test
    fun `Given hasPreviousMediaItem false, When previous called, Then player not touched`() {
        // Given
        every { player.hasPreviousMediaItem() } returns false

        // When
        dataSource.previous()

        // Then
        verify(exactly = 0) { player.seekToPrevious() }
    }

    // ---------------------------------------------------------------------
    // seek / shuffle / repeat / speed / volume
    // ---------------------------------------------------------------------

    @Test
    fun `Given position, When seekTo called, Then player seekTo invoked`() {
        // Given
        every { player.bufferedPosition } returns 2000L
        every { player.duration } returns 10000L

        // When
        dataSource.seekTo(1000L)

        // Then
        verify { player.seekTo(1000L) }
    }

    @Test
    fun `When seekBackward called, Then player seekBack invoked`() {
        // When
        dataSource.seekBackward()

        // Then
        verify { player.seekBack() }
    }

    @Test
    fun `When seekForward called, Then player seekForward invoked`() {
        // When
        dataSource.seekForward()

        // Then
        verify { player.seekForward() }
    }

    @Test
    fun `Given isShuffle true, When setShuffle called, Then shuffleModeEnabled set`() {
        // When
        dataSource.setShuffle(true)

        // Then
        verify { player.shuffleModeEnabled = true }
    }

    @Test
    fun `Given shuffleModeEnabled false, When shuffle called, Then shuffleModeEnabled toggled to true`() {
        // Given
        every { player.shuffleModeEnabled } returns false

        // When
        dataSource.shuffle()

        // Then
        verify { player.shuffleModeEnabled = true }
    }

    @Test
    fun `Given shuffleModeEnabled true, When shuffle called, Then shuffleModeEnabled toggled to false`() {
        // Given
        every { player.shuffleModeEnabled } returns true

        // When
        dataSource.shuffle()

        // Then
        verify { player.shuffleModeEnabled = false }
    }

    @Test
    fun `Given repeat mode, When setRepeat called, Then player repeatMode set`() {
        // When
        dataSource.setRepeat(Player.REPEAT_MODE_ALL)

        // Then
        verify { player.repeatMode = Player.REPEAT_MODE_ALL }
    }

    @Test
    fun `Given repeat mode OFF, When changeRepeat called, Then repeatMode changed to ONE`() {
        // Given
        every { player.repeatMode } returns Player.REPEAT_MODE_OFF

        // When
        dataSource.changeRepeat()

        // Then
        verify { player.repeatMode = Player.REPEAT_MODE_ONE }
    }

    @Test
    fun `Given repeat mode ONE, When changeRepeat called, Then repeatMode changed to ALL`() {
        // Given
        every { player.repeatMode } returns Player.REPEAT_MODE_ONE

        // When
        dataSource.changeRepeat()

        // Then
        verify { player.repeatMode = Player.REPEAT_MODE_ALL }
    }

    @Test
    fun `Given repeat mode ALL, When changeRepeat called, Then repeatMode changed to OFF`() {
        // Given
        every { player.repeatMode } returns Player.REPEAT_MODE_ALL

        // When
        dataSource.changeRepeat()

        // Then
        verify { player.repeatMode = Player.REPEAT_MODE_OFF }
    }

    @Test
    fun `Given speed within range, When setSpeed called, Then player playback speed set as given`() {
        // When
        dataSource.setSpeed(1.5f)

        // Then
        verify { player.setPlaybackSpeed(1.5f) }
    }

    @Test
    fun `Given speed above max, When setSpeed called, Then player playback speed coerced to max`() {
        // When
        dataSource.setSpeed(10f)

        // Then
        verify { player.setPlaybackSpeed(3.5f) }
    }

    @Test
    fun `Given speed below min, When setSpeed called, Then player playback speed coerced to min`() {
        // When
        dataSource.setSpeed(0f)

        // Then
        verify { player.setPlaybackSpeed(0.5f) }
    }

    @Test
    fun `Given volume within range, When setVolume called, Then player volume set as given`() {
        // When
        dataSource.setVolume(0.5f)

        // Then
        verify { player.volume = 0.5f }
    }

    @Test
    fun `Given volume above max, When setVolume called, Then player volume coerced to 1`() {
        // When
        dataSource.setVolume(2f)

        // Then
        verify { player.volume = 1f }
    }

    @Test
    fun `Given volume below min, When setVolume called, Then player volume coerced to 0`() {
        // When
        dataSource.setVolume(-1f)

        // Then
        verify { player.volume = 0f }
    }

    // ---------------------------------------------------------------------
    // addTrack / addClipTrack / removeTrack / clearPlayList
    // ---------------------------------------------------------------------

    @Test
    fun `Given episode and index, When addTrack called, Then addMediaItem with index invoked`() {
        // When
        dataSource.addTrack(episode, index = 1)

        // Then
        verify { player.addMediaItem(1, any()) }
    }

    @Test
    fun `Given episode and null index, When addTrack called, Then addMediaItem without index invoked`() {
        // When
        dataSource.addTrack(episode, index = null)

        // Then
        verify { player.addMediaItem(any<MediaItem>()) }
        verify(exactly = 0) { player.addMediaItem(any(), any()) }
    }

    @Test
    fun `Given episodes and index, When addTrack list called, Then addMediaItems with index invoked`() {
        // Given
        val episodes = listOf(episode, episode.copy(id = 999L))

        // When
        dataSource.addTrack(episodes, index = 0)

        // Then
        verify { player.addMediaItems(0, any()) }
    }

    @Test
    fun `Given episodes and null index, When addTrack list called, Then addMediaItems without index invoked`() {
        // Given
        val episodes = listOf(episode)

        // When
        dataSource.addTrack(episodes, index = null)

        // Then
        verify { player.addMediaItems(any<List<MediaItem>>()) }
    }

    @Test
    fun `Given clip episode and index, When addClipTrack called, Then addMediaItem with index invoked`() {
        // When
        dataSource.addClipTrack(episode, index = 2)

        // Then
        verify { player.addMediaItem(2, any()) }
    }

    @Test
    fun `Given clip episode and null index, When addClipTrack called, Then addMediaItem without index invoked`() {
        // When
        dataSource.addClipTrack(episode, index = null)

        // Then
        verify { player.addMediaItem(any<MediaItem>()) }
        verify(exactly = 0) { player.addMediaItem(any(), any()) }
    }

    @Test
    fun `Given clip episodes and index, When addClipTracks called, Then addMediaItems with index invoked`() {
        // Given
        val episodes = listOf(episode, episode.copy(id = 999L))

        // When
        dataSource.addClipTracks(episodes, index = 0)

        // Then
        verify { player.addMediaItems(0, any()) }
    }

    @Test
    fun `Given clip episodes and null index, When addClipTracks called, Then addMediaItems without index invoked`() {
        // Given
        val episodes = listOf(episode)

        // When
        dataSource.addClipTracks(episodes, index = null)

        // Then
        verify { player.addMediaItems(any<List<MediaItem>>()) }
    }

    @Test
    fun `Given index, When removeTrack called, Then removeMediaItem invoked`() {
        // When
        dataSource.removeTrack(3)

        // Then
        verify { player.removeMediaItem(3) }
    }

    @Test
    fun `When clearPlayList called, Then clearMediaItems invoked`() {
        // When
        dataSource.clearPlayList()

        // Then
        verify { player.clearMediaItems() }
    }

    // ---------------------------------------------------------------------
    // release / rehydrate
    // ---------------------------------------------------------------------

    @Test
    fun `When release called, Then player release and removeListener invoked`() {
        // When
        dataSource.release()

        // Then
        verify {
            player.release()
            player.removeListener(any())
        }
    }

    @Test
    fun `Given no current nowPlaying, When rehydrate called, Then nowPlaying and isPlaying updated`() = runTest {
        // Given: isPlaying kept false to avoid triggering the real-time progressUpdater
        // coroutine (see PlayerDataSourceImplTest class doc / task notes: progressUpdater is
        // intentionally not exercised here).
        every { player.isPlaying } returns false

        // When / Then
        dataSource.nowPlaying.test {
            assertEquals(null, awaitItem())
            dataSource.rehydrate(episode)
            assertEquals(episode, awaitItem())
        }
        dataSource.isPlaying.test {
            assertEquals(false, awaitItem())
        }
    }

    @Test
    fun `Given same episode already playing, When rehydrate called, Then nowPlaying flow not re-emitted`() = runTest {
        // Given
        every { player.isPlaying } returns false
        dataSource.rehydrate(episode)

        // When / Then: emits once for the first rehydrate, calling again with same id should not add new emission
        dataSource.nowPlaying.test {
            assertEquals(episode, awaitItem())
            dataSource.rehydrate(episode)
            expectNoEvents()
        }
    }

    // ---------------------------------------------------------------------
    // Player.Listener callbacks
    // ---------------------------------------------------------------------

    @Test
    fun `Given media items in player, When onTimelineChanged invoked, Then playlist flow updated with episodes`() = runTest {
        // Given
        val mediaItem = MediaItem.Builder()
            .setMediaId(episode.id.toString())
            .setUri(mockk<Uri>(relaxed = true))
            .setTag(episode)
            .build()
        every { player.mediaItemCount } returns 1
        every { player.getMediaItemAt(0) } returns mediaItem

        // When
        listenerSlot.captured.onTimelineChanged(
            mockk<Timeline>(relaxed = true),
            Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED,
        )

        // Then
        dataSource.playlist.test {
            assertEquals(listOf(episode), awaitItem())
        }
    }

    @Test
    fun `Given media item transition, When onMediaItemTransition invoked, Then nowPlaying indexOfList progress updated`() = runTest {
        // Given
        val mediaItem = MediaItem.Builder()
            .setMediaId(episode.id.toString())
            .setUri(mockk<Uri>(relaxed = true))
            .setTag(episode)
            .build()
        every { player.currentMediaItemIndex } returns 3

        // When
        listenerSlot.captured.onMediaItemTransition(
            mediaItem,
            Player.MEDIA_ITEM_TRANSITION_REASON_AUTO,
        )

        // Then
        dataSource.nowPlaying.test { assertEquals(episode, awaitItem()) }
        dataSource.indexOfList.test { assertEquals(3, awaitItem()) }
        dataSource.progress.test {
            val progress = awaitItem()
            assertEquals(episode.duration, progress.duration)
        }
    }

    @Test
    fun `Given null media item, When onMediaItemTransition invoked, Then nowPlaying set to null`() = runTest {
        // When
        listenerSlot.captured.onMediaItemTransition(
            null,
            Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED,
        )

        // Then
        dataSource.nowPlaying.test { assertEquals(null, awaitItem()) }
    }

    @Test
    fun `Given playback state, When onPlaybackStateChanged invoked, Then playback flow updated`() = runTest {
        // When
        listenerSlot.captured.onPlaybackStateChanged(Player.STATE_READY)

        // Then
        dataSource.playback.test { assertEquals(Player.STATE_READY, awaitItem()) }
    }

    @Test
    fun `Given repeat mode, When onRepeatModeChanged invoked, Then repeat flow updated`() = runTest {
        // When
        listenerSlot.captured.onRepeatModeChanged(Player.REPEAT_MODE_ONE)

        // Then
        dataSource.repeat.test { assertEquals(Player.REPEAT_MODE_ONE, awaitItem()) }
    }

    @Test
    fun `Given shuffle flag, When onShuffleModeEnabledChanged invoked, Then isShuffle flow updated`() = runTest {
        // When
        listenerSlot.captured.onShuffleModeEnabledChanged(true)

        // Then
        dataSource.isShuffle.test { assertEquals(true, awaitItem()) }
    }

    @Test
    fun `Given playback exception, When onPlayerError invoked, Then no exception thrown`() {
        // Given
        val error = mockk<PlaybackException>(relaxed = true)

        // When / Then (should not throw)
        listenerSlot.captured.onPlayerError(error)
    }

    @Test
    fun `Given tracks with text track, When onTracksChanged invoked, Then no exception thrown`() {
        // Given
        val trackGroup = mockk<Tracks.Group>(relaxed = true) {
            every { type } returns C.TRACK_TYPE_TEXT
        }
        val tracks = mockk<Tracks>(relaxed = true) {
            every { groups } returns com.google.common.collect.ImmutableList.of(trackGroup)
        }

        // When / Then (should not throw)
        listenerSlot.captured.onTracksChanged(tracks)
    }

    @Test
    fun `Given cue group with text, When onCues invoked, Then cue flow updated`() = runTest {
        // Given
        val cue = Cue.Builder().setText("hello").build()
        val cueGroup = CueGroup(listOf(cue), 0L)

        // When
        listenerSlot.captured.onCues(cueGroup)

        // Then
        dataSource.cue.test { assertEquals("hello", awaitItem()) }
    }

    @Test
    fun `Given empty cue group, When onCues invoked, Then cue flow updated with empty string`() = runTest {
        // Given
        val cueGroup = CueGroup(emptyList(), 0L)

        // When
        listenerSlot.captured.onCues(cueGroup)

        // Then
        dataSource.cue.test { assertEquals("", awaitItem()) }
    }

    @Test
    fun `Given media metadata, When onMediaMetadataChanged invoked, Then no exception thrown`() {
        // Given
        val metadata = MediaMetadata.Builder().setTitle("title").build()

        // When / Then (should not throw)
        listenerSlot.captured.onMediaMetadataChanged(metadata)
    }

    @Test
    fun `Given isPlaying flag, When onIsPlayingChanged invoked, Then isPlaying flow updated`() = runTest {
        // When
        listenerSlot.captured.onIsPlayingChanged(false)

        // Then
        dataSource.isPlaying.test { assertEquals(false, awaitItem()) }
    }
}
