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
import io.jacob.episodive.core.model.mapper.toDurationMillis
import io.jacob.episodive.core.testing.model.episodeTestData
import io.jacob.episodive.core.testing.model.episodeTestDataList
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

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
            // T7 (C1/C2 회귀): transition 이 실은 progress 는 반드시 전환된 에피소드의 id 를
            // 함께 싣고, 위치는 0 으로 리셋되어야 한다 (이전 에피소드의 위치가 새 에피소드로 새는 것 방지).
            assertEquals(episode.id, progress.episodeId)
            assertEquals(Duration.ZERO, progress.position)
        }
    }

    @Test
    fun `Given seek driven transition, When onMediaItemTransition invoked, Then progress position comes from player currentPosition`() = runTest {
        // 탐색으로 항목이 바뀐 경우엔 플레이어가 이미 목표 위치에 가 있으므로 그 값을 싣는다.
        // 여기서 0 을 하드코딩하면 방금 이동한 지점이 곧바로 0 으로 저장된다.
        val mediaItem = MediaItem.Builder()
            .setMediaId(episode.id.toString())
            .setUri(mockk<Uri>(relaxed = true))
            .setTag(episode)
            .build()
        every { player.currentMediaItemIndex } returns 0
        every { player.currentPosition } returns 45_000L

        // When
        listenerSlot.captured.onMediaItemTransition(
            mediaItem,
            Player.MEDIA_ITEM_TRANSITION_REASON_SEEK,
        )

        // Then
        dataSource.progress.test {
            val progress = awaitItem()
            assertEquals(45_000L.toDurationMillis(), progress.position)
            assertEquals(episode.id, progress.episodeId)
        }
    }

    @Test
    fun `Given repeat transition, When onMediaItemTransition invoked, Then progress position is zero`() = runTest {
        // 한 곡 반복도 같은 항목을 처음부터 다시 재생하므로 0 이 확정이다.
        // AUTO 와 같은 분기를 공유하지만, 누군가 REPEAT 만 else 로 옮기면 잡아낸다.
        val mediaItem = MediaItem.Builder()
            .setMediaId(episode.id.toString())
            .setUri(mockk<Uri>(relaxed = true))
            .setTag(episode)
            .build()
        every { player.currentMediaItemIndex } returns 0
        every { player.currentPosition } returns 3_500_000L

        // When
        listenerSlot.captured.onMediaItemTransition(
            mediaItem,
            Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT,
        )

        // Then
        dataSource.progress.test {
            val progress = awaitItem()
            assertEquals(Duration.ZERO, progress.position)
            assertEquals(episode.id, progress.episodeId)
        }
    }

    @Test
    fun `Given auto transition to next item, When onMediaItemTransition invoked, Then progress position is zero not the previous item position`() = runTest {
        // 자동 전환은 새 항목을 처음부터 재생한다. 이 시점의 player.currentPosition 이 아직
        // 이전 항목의 끝 위치일 수 있는데, 그 값을 새 에피소드에 실으면 듣지도 않은
        // 에피소드가 사실상 완료 처리되어 이어듣기 지점이 사라진다.
        val mediaItem = MediaItem.Builder()
            .setMediaId(episode.id.toString())
            .setUri(mockk<Uri>(relaxed = true))
            .setTag(episode)
            .build()
        every { player.currentMediaItemIndex } returns 1
        // 이전 항목의 끝 무렵 위치가 그대로 보이는 상황을 재현한다.
        every { player.currentPosition } returns 3_500_000L

        // When
        listenerSlot.captured.onMediaItemTransition(
            mediaItem,
            Player.MEDIA_ITEM_TRANSITION_REASON_AUTO,
        )

        // Then
        dataSource.progress.test {
            val progress = awaitItem()
            assertEquals(Duration.ZERO, progress.position)
            assertEquals(episode.id, progress.episodeId)
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

    // ---------------------------------------------------------------------
    // C1~C4 회귀 테스트: 재생 위치 오염 방지
    // ---------------------------------------------------------------------

    @Test
    fun `Given media3 fires transition inline inside setMediaItems, When prepare called, Then final progress keeps restored position and episodeId`() = runTest {
        // Given: media3 1.8.0 은 setMediaItems 호출 스택 안에서 onMediaItemTransition 을
        // 인라인 실행한다(C4). 그 콜백이 position 을 0 으로 덮은 뒤에도, prepare() 가 그 뒤에서
        // 실제 복원 위치로 다시 확정해야 한다.
        val mediaItem = MediaItem.Builder()
            .setMediaId(episode.id.toString())
            .setUri(mockk<Uri>(relaxed = true))
            .setTag(episode)
            .build()
        every {
            player.setMediaItems(any(), any<Int>(), any<Long>())
        } answers {
            listenerSlot.captured.onMediaItemTransition(
                mediaItem,
                Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED,
            )
        }

        // When
        dataSource.prepare(listOf(episode), indexToPlay = 0, positionMs = 60_000L)

        // Then
        dataSource.progress.test {
            val progress = awaitItem()
            assertEquals(60_000L.toDurationMillis(), progress.position)
            assertEquals(episode.id, progress.episodeId)
        }
    }

    @Test
    fun `Given media3 fires transition inline during prepare, When isPreparing guard is active, Then no progress is published mid setMediaItems callback`() = runTest {
        // Given: (B) isPreparing 가드가 실제로 콜백 도중 발행을 막는지 직접 확인한다.
        // progress 는 StateFlow 라 구독 시점에 따라(특히 단일 스레드 테스트 스케줄러에서는)
        // 중간 방출이 컨플레이션으로 사라질 수 있다 — Turbine 으로 뒤늦게 구독하면 최종값만
        // 보이는 게 그 예다. 대신 콜백이 실행되는 바로 그 순간의 값을 직접 스냅샷한다.
        val mediaItem = MediaItem.Builder()
            .setMediaId(episode.id.toString())
            .setUri(mockk<Uri>(relaxed = true))
            .setTag(episode)
            .build()
        var progressDuringCallback: io.jacob.episodive.core.model.Progress? = null
        every {
            player.setMediaItems(any(), any<Int>(), any<Long>())
        } answers {
            listenerSlot.captured.onMediaItemTransition(
                mediaItem,
                Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED,
            )
            // isPreparing 가드가 없으면 이 시점에 이미 (episodeId=episode.id, position=0) 이
            // 발행되어 있다 — Unconfined 로 즉시(비동기 스케줄링 없이) 현재 값을 읽는다.
            progressDuringCallback = kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.Unconfined) {
                dataSource.progress.first()
            }
        }

        // When
        dataSource.prepare(listOf(episode), indexToPlay = 0, positionMs = 60_000L)

        // Then: 콜백이 실행되는 순간에는 아직 아무것도 발행되지 않아야 한다(episodeId=null 유지).
        assertEquals(null, progressDuringCallback?.episodeId)

        // 그리고 prepare() 가 끝난 뒤에는 복원 위치가 최종적으로 확정되어 있어야 한다.
        dataSource.progress.test {
            val progress = awaitItem()
            assertEquals(60_000L.toDurationMillis(), progress.position)
            assertEquals(episode.id, progress.episodeId)
        }
    }

    @Test
    fun `Given player duration is TIME_UNSET, When seekTo called, Then progress duration is not negative`() = runTest {
        // Given
        every { player.duration } returns C.TIME_UNSET
        every { player.bufferedPosition } returns 0L

        // When
        dataSource.seekTo(1000L)

        // Then
        dataSource.progress.test {
            val progress = awaitItem()
            assertEquals(false, progress.duration.isNegative())
        }
    }

    @Test
    fun `Given player already holds a different episode, When rehydrate called, Then nowPlaying keeps the playing episode`() = runTest {
        // Given: C3 회귀 방지. rehydrate 의 가드(player.currentMediaItem 기준)가 없으면
        // 비동기 DB 복원값(A)이 실제로 재생 중인 B 를 밀어내고 nowPlaying 을 오염시킨다.
        every { player.isPlaying } returns false
        val otherEpisode = episode.copy(id = 999L)

        // player.currentMediaItem 이 아직 비어 있는 상태에서 B 를 먼저 rehydrate 해
        // nowPlaying 을 B 로 세팅한다.
        dataSource.rehydrate(otherEpisode)

        // 이제 플레이어가 실제로 B 를 들고 있는 상태를 재현한다.
        val otherMediaItem = MediaItem.Builder()
            .setMediaId(otherEpisode.id.toString())
            .setUri(mockk<Uri>(relaxed = true))
            .setTag(otherEpisode)
            .build()
        every { player.currentMediaItem } returns otherMediaItem

        // When: 뒤늦게 도착한 A 로 rehydrate 를 시도한다.
        dataSource.rehydrate(episode)

        // Then: 가드가 없으면 nowPlaying 이 A 로 바뀐다 — 이 assert 가 그걸 잡아낸다.
        dataSource.nowPlaying.test {
            assertEquals(otherEpisode, awaitItem())
        }
    }

    @Test
    fun `Given nowPlaying rehydrated to A while player already holds B, When seekTo called, Then progress episodeId follows the player not nowPlaying`() = runTest {
        // Given: C3 회귀 방지(교차 오염). rehydrate(A) 는 player.currentMediaItem 이 아직 비어
        // 있을 때 허용되므로(가드를 통과) _nowPlaying 이 A 로 세팅될 수 있다. 그 직후 player 가
        // 실제로는 B 를 들고 있는 상태(예: 비동기 전환 콜백이 아직 _nowPlaying 을 못 따라잡은 틈)를
        // 재현한다. seekTo 가 _nowPlaying(=A) 대신 currentEpisodeId()(=B, player 기준) 를 써야만
        // 위치가 B 의 것으로 정확히 저장된다.
        every { player.isPlaying } returns false
        every { player.bufferedPosition } returns 0L
        every { player.duration } returns 600_000L
        val otherEpisode = episode.copy(id = 999L)

        // player.currentMediaItem 이 아직 비어 있는 상태에서 A 를 rehydrate 해 nowPlaying=A.
        dataSource.rehydrate(episode)

        // 실제로는 player 가 B 를 들고 있는 상태로 어긋난다(_nowPlaying 갱신 전).
        val otherMediaItem = MediaItem.Builder()
            .setMediaId(otherEpisode.id.toString())
            .setUri(mockk<Uri>(relaxed = true))
            .setTag(otherEpisode)
            .build()
        every { player.currentMediaItem } returns otherMediaItem

        // When
        dataSource.seekTo(300_000L)

        // Then: episodeId 가 nowPlaying(A) 이 아니라 실제로 player 가 들고 있는 B 여야 한다.
        dataSource.progress.test {
            val progress = awaitItem()
            assertEquals(otherEpisode.id, progress.episodeId)
        }
    }

    // ---------------------------------------------------------------------
    // 3차 수정 회귀 테스트: play/playClips isPreparing 가드 대칭 적용, seekTo duration 폴백
    // ---------------------------------------------------------------------

    @Test
    fun `Given play list with non-first index, When setMediaItems fires inline transition, Then no emission carries first item id`() = runTest {
        // Given: media3 는 1-인자 setMediaItems(list) 호출 스택 안에서도 목록 첫 항목에 대해
        // onMediaItemTransition 을 인라인 실행한다. seekToDefaultPosition(indexToPlay) 로 목표에
        // 도달하기 전이므로, 가드가 없으면 "재생하지도 않을 첫 항목(A) + 위치 0" 이 발행되어
        // A 의 이어듣기 지점이 지워진다.
        val episodeA = episodeTestDataList[0]
        val episodeB = episodeTestDataList[1]
        val itemA = MediaItem.Builder()
            .setMediaId(episodeA.id.toString())
            .setUri(mockk<Uri>(relaxed = true))
            .setTag(episodeA)
            .build()
        every {
            player.setMediaItems(any())
        } answers {
            listenerSlot.captured.onMediaItemTransition(
                itemA,
                Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED,
            )
        }

        // When / Then: play() 호출 전에 구독해 모든 방출을 수집한다.
        dataSource.progress.test {
            awaitItem() // 초기값

            dataSource.play(episodes = listOf(episodeA, episodeB), indexToPlay = 1)

            val emissions = cancelAndConsumeRemainingEvents()
                .filterIsInstance<app.cash.turbine.Event.Item<io.jacob.episodive.core.model.Progress>>()
                .map { it.value }

            // 사용자가 요청한 것은 B 인데, 방출 중 A 의 id 가 실린 것이 하나라도 있으면
            // A 의 이어듣기 지점이 지워진다.
            assertEquals(false, emissions.any { it.episodeId == episodeA.id })

            val last = emissions.last()
            assertEquals(episodeB.id, last.episodeId)
            assertEquals(Duration.ZERO, last.position)
        }
    }

    @Test
    fun `Given playClips with non-first index, When setMediaItems fires inline transition, Then no emission carries first item id`() = runTest {
        // playClips 도 play(episodes, indexToPlay) 와 같은 가드를 적용받아야 한다.
        val episodeA = episodeTestDataList[0]
        val episodeB = episodeTestDataList[1]
        val itemA = MediaItem.Builder()
            .setMediaId(episodeA.id.toString())
            .setUri(mockk<Uri>(relaxed = true))
            .setTag(episodeA)
            .build()
        every {
            player.setMediaItems(any())
        } answers {
            listenerSlot.captured.onMediaItemTransition(
                itemA,
                Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED,
            )
        }

        dataSource.progress.test {
            awaitItem() // 초기값

            dataSource.playClips(episodes = listOf(episodeA, episodeB), indexToPlay = 1)

            val emissions = cancelAndConsumeRemainingEvents()
                .filterIsInstance<app.cash.turbine.Event.Item<io.jacob.episodive.core.model.Progress>>()
                .map { it.value }

            assertEquals(false, emissions.any { it.episodeId == episodeA.id })

            val last = emissions.last()
            assertEquals(episodeB.id, last.episodeId)
            assertEquals(Duration.ZERO, last.position)
        }
    }

    @Test
    fun `Given progress still carries the previous episode's duration, When seekTo called on the new episode with unset player duration, Then progress duration falls back to the new episode's duration`() = runTest {
        // Given: A 에서 B 로 전환된 직후. player.duration 은 아직 TIME_UNSET 이고, _progress 에는
        // 직전 A 의 duration 이 남아 있다. 이때 _progress.value.duration 으로 폴백하면 A 의 짧은/긴
        // duration 이 그대로 B 에 실려, 짧은 길이 + 큰 위치 조합이 완료 판정을 잘못 뒤집는다.
        val episodeA = episodeTestDataList[0]
        val episodeB = episodeTestDataList[1]
        check(episodeA.duration != episodeB.duration) { "테스트 데이터의 duration 이 같으면 폴백 오류를 구분할 수 없다" }

        // A 로 전환되어 _progress.duration 이 A 의 길이로 채워진 상태를 만든다.
        val itemA = MediaItem.Builder()
            .setMediaId(episodeA.id.toString())
            .setUri(mockk<Uri>(relaxed = true))
            .setTag(episodeA)
            .build()
        every { player.currentMediaItemIndex } returns 0
        listenerSlot.captured.onMediaItemTransition(itemA, Player.MEDIA_ITEM_TRANSITION_REASON_AUTO)

        // 이제 플레이어는 실제로 B 를 들고 있고, 아직 prepare 전이라 duration 이 TIME_UNSET 이다.
        val itemB = MediaItem.Builder()
            .setMediaId(episodeB.id.toString())
            .setUri(mockk<Uri>(relaxed = true))
            .setTag(episodeB)
            .build()
        every { player.currentMediaItem } returns itemB
        every { player.duration } returns C.TIME_UNSET
        every { player.bufferedPosition } returns 0L

        // When
        dataSource.seekTo(1000L)

        // Then
        dataSource.progress.test {
            val progress = awaitItem()
            assertEquals(episodeB.duration, progress.duration)
        }
    }

    // ---------------------------------------------------------------------
    // 4차 수정 회귀 테스트: 클립은 처음부터 클립 길이를 발행한다
    //
    // 클립은 ClippingConfiguration 으로 잘라 올리므로 실제로 흐르는 길이는 에피소드 전체가
    // 아니라 클립 길이다. 준비 전에 메타에서 길이를 가져올 때 전체 길이를 실으면, 클립 화면의
    // 남은 시간이 에피소드 전체 길이로 보였다가 progressUpdater 가 player.duration 을 읽는
    // 순간 클립 길이로 튄다.
    // ---------------------------------------------------------------------

    /** 전체 1시간짜리 에피소드에서 30초만 잘라낸 클립. 두 길이가 뚜렷이 달라야 폴백 오류가 드러난다. */
    private val clipEpisode = episodeTestData.copy(
        duration = 60.minutes,
        clipStartTime = Instant.fromEpochSeconds(100),
        clipDuration = 30.seconds,
    )

    /** media3 가 setMediaItem 호출 스택 안에서 transition 콜백을 인라인 실행하는 것을 흉내낸다. */
    private fun answerSetMediaItemWithInlineTransition() {
        every { player.setMediaItem(any()) } answers {
            listenerSlot.captured.onMediaItemTransition(
                firstArg<MediaItem>(),
                Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED,
            )
        }
    }

    @Test
    fun `Given clip episode, When playClip fires inline transition, Then progress duration is the clip duration`() =
        runTest {
            // Given
            answerSetMediaItemWithInlineTransition()

            // When
            dataSource.playClip(clipEpisode)

            // Then: 첫 발행부터 클립 길이여야 한다. 여기서 전체 길이가 나오면 화면의 남은 시간이
            // 재생이 시작되는 순간 1시간에서 30초로 튄다.
            dataSource.progress.test {
                val progress = awaitItem()
                assertEquals(clipEpisode.clipDuration, progress.duration)
                assertEquals(clipEpisode.id, progress.episodeId)
            }
        }

    @Test
    fun `Given non-clip episode, When play fires inline transition, Then progress duration is the whole episode duration`() =
        runTest {
            // 클립 판정이 일반 재생까지 잡아채면 플레이어 화면의 길이가 무너진다.
            // 잘라 올리지 않은 아이템은 전체 길이 그대로여야 한다.
            answerSetMediaItemWithInlineTransition()

            // When
            dataSource.play(clipEpisode)

            // Then
            dataSource.progress.test {
                val progress = awaitItem()
                assertEquals(clipEpisode.duration, progress.duration)
            }
        }

    @Test
    fun `Given clip episodes, When playClips called, Then the confirmed emission carries the clip duration`() =
        runTest {
            // playClips 는 인라인 transition 을 isPreparing 으로 막고 확정값을 직접 발행한다.
            // 그 확정 발행도 클립 길이를 실어야 한다.
            val other = episodeTestDataList[1].copy(
                clipStartTime = Instant.fromEpochSeconds(0),
                clipDuration = 15.seconds,
            )

            // When
            dataSource.playClips(episodes = listOf(clipEpisode, other), indexToPlay = 0)

            // Then
            dataSource.progress.test {
                val progress = awaitItem()
                assertEquals(clipEpisode.clipDuration, progress.duration)
                assertEquals(clipEpisode.id, progress.episodeId)
            }
        }

    @Test
    fun `Given clip episodes without clip metadata, When playClips called, Then progress duration falls back to the episode duration`() =
        runTest {
            // 클립 정보가 없으면 플레이어도 잘라 올리지 않는다. 그 경우 길이는 에피소드 전체다.
            val withoutClip = episodeTestData
            check(!withoutClip.hasClip) { "이 테스트는 클립 메타가 없는 데이터를 전제한다" }

            // When
            dataSource.playClips(episodes = listOf(withoutClip), indexToPlay = 0)

            // Then
            dataSource.progress.test {
                val progress = awaitItem()
                assertEquals(withoutClip.duration, progress.duration)
            }
        }

    @Test
    fun `Given a clipped media item with unset player duration, When seekTo called, Then progress duration falls back to the clip duration`() =
        runTest {
            // Given: 준비 전이라 player.duration 이 TIME_UNSET 이다. 이때 메타에서 길이를
            // 가져오는데, 잘라 올린 아이템이면 전체 길이가 아니라 클립 길이를 써야 한다.
            val clippedItem = MediaItem.Builder()
                .setMediaId(clipEpisode.id.toString())
                .setUri(mockk<Uri>(relaxed = true))
                .setTag(clipEpisode)
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(clipEpisode.clipStartPositionMs)
                        .setEndPositionMs(clipEpisode.clipEndPositionMs)
                        .build()
                )
                .build()
            every { player.currentMediaItem } returns clippedItem
            every { player.duration } returns C.TIME_UNSET
            every { player.bufferedPosition } returns 0L

            // When
            dataSource.seekTo(1000L)

            // Then
            dataSource.progress.test {
                val progress = awaitItem()
                assertEquals(clipEpisode.clipDuration, progress.duration)
            }
        }
}
