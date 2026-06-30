package io.jacob.episodive.core.data.download

import android.app.DownloadManager
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Environment
import android.util.Log
import io.jacob.episodive.core.domain.download.EpisodeDownloader
import io.jacob.episodive.core.model.DownloadProgress
import io.jacob.episodive.core.model.DownloadStatus
import io.jacob.episodive.core.model.Episode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import java.io.File

class EpisodeDownloaderImpl(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher,
) : EpisodeDownloader {

    private val downloadManager: DownloadManager by lazy {
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // enqueue/취소가 일어날 때마다 폴링을 (재)시작시키는 트리거.
    private val downloadEvents = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)

    override fun downloadEpisode(episode: Episode, filePath: String): Long {
        val uri = Uri.parse(episode.enclosureUrl)
        val request = DownloadManager.Request(uri).apply {
            setTitle(episode.title)
            setDescription(episode.feedTitle ?: "")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(false)

            val file = File(getDownloadDirectory(), filePath)
            file.parentFile?.mkdirs()
            setDestinationUri(Uri.fromFile(file))
        }

        val downloadId = downloadManager.enqueue(request)
        prefs.edit().putLong(downloadId.toString(), episode.id).apply()
        downloadEvents.tryEmit(Unit)
        return downloadId
    }

    override fun cancelDownload(downloadId: Long) {
        downloadManager.remove(downloadId)
        prefs.edit().remove(downloadId.toString()).apply()
        downloadEvents.tryEmit(Unit)
    }

    override fun cancelDownloadForEpisode(episodeId: Long) {
        val downloadIds = prefs.all
            .filterValues { it is Long && it == episodeId }
            .keys
            .mapNotNull { it.toLongOrNull() }
        downloadIds.forEach { cancelDownload(it) }
    }

    override fun deleteDownloadedFile(filePath: String) {
        val file = File(resolveDownloadDir(), filePath)
        if (file.exists()) {
            file.delete()
        }
    }

    override fun getDownloadDirectory(): String {
        val dir = resolveDownloadDir()
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir.absolutePath
    }

    override fun observeActiveDownloads(): Flow<Map<Long, DownloadProgress>> =
        downloadEvents
            .onStart { emit(Unit) }
            .flatMapLatest {
                flow {
                    // 활성(진행 중) 다운로드가 있는 동안에만 폴링한다.
                    // 모두 끝나면(또는 실패만 남으면) 루프를 종료하고, 다음 enqueue/취소 이벤트를 기다린다.
                    while (true) {
                        val active = queryActiveDownloads()
                        pruneCompletedMappings()
                        emit(active)
                        if (active.values.none { it.isInProgress }) break
                        delay(POLL_INTERVAL_MS)
                    }
                }
            }
            .distinctUntilChanged()
            .flowOn(ioDispatcher)

    override fun isFileDownloaded(filePath: String): Boolean {
        val file = File(resolveDownloadDir(), filePath)
        return file.exists() && file.length() > 0L
    }

    private fun queryActiveDownloads(): Map<Long, DownloadProgress> {
        val query = DownloadManager.Query().setFilterByStatus(
            DownloadManager.STATUS_PENDING or
                DownloadManager.STATUS_RUNNING or
                DownloadManager.STATUS_PAUSED or
                DownloadManager.STATUS_FAILED,
        )

        val result = mutableMapOf<Long, DownloadProgress>()
        try {
            downloadManager.query(query)?.use { cursor ->
                val idIndex = cursor.getColumnIndex(DownloadManager.COLUMN_ID)
                val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val downloadedIndex =
                    cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                val totalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

                while (cursor.moveToNext()) {
                    val downloadId = cursor.getLong(idIndex)
                    val episodeId = getEpisodeIdForDownload(downloadId) ?: continue
                    val status = when (cursor.getInt(statusIndex)) {
                        DownloadManager.STATUS_PENDING -> DownloadStatus.PENDING
                        DownloadManager.STATUS_PAUSED -> DownloadStatus.PAUSED
                        DownloadManager.STATUS_FAILED -> DownloadStatus.FAILED
                        else -> DownloadStatus.DOWNLOADING // RUNNING
                    }
                    result[episodeId] = DownloadProgress(
                        status = status,
                        downloadedBytes = if (downloadedIndex >= 0) cursor.getLong(downloadedIndex) else 0L,
                        totalBytes = if (totalIndex >= 0) cursor.getLong(totalIndex) else -1L,
                    )
                }
            }
        } catch (e: Exception) {
            // DownloadManager 조회 실패(provider 부하 등)가 재생 flow를 끊지 않도록 흡수한다.
            Log.w(TAG, "queryActiveDownloads failed", e)
        }
        return result
    }

    /** 완료(SUCCESSFUL)된 다운로드의 downloadId->episodeId 매핑을 정리해 prefs 무한 증가를 막는다. */
    private fun pruneCompletedMappings() {
        try {
            val completedIds = mutableListOf<String>()
            val query = DownloadManager.Query()
                .setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL)
            downloadManager.query(query)?.use { cursor ->
                val idIndex = cursor.getColumnIndex(DownloadManager.COLUMN_ID)
                while (cursor.moveToNext()) {
                    completedIds.add(cursor.getLong(idIndex).toString())
                }
            }
            if (completedIds.isNotEmpty()) {
                prefs.edit().apply { completedIds.forEach { remove(it) } }.apply()
            }
        } catch (e: Exception) {
            Log.w(TAG, "pruneCompletedMappings failed", e)
        }
    }

    private fun getEpisodeIdForDownload(downloadId: Long): Long? {
        val episodeId = prefs.getLong(downloadId.toString(), -1L)
        return if (episodeId == -1L) null else episodeId
    }

    private fun resolveDownloadDir(): File =
        context.getExternalFilesDir(Environment.DIRECTORY_PODCASTS)
            ?: context.filesDir.resolve("Podcasts")

    companion object {
        private const val TAG = "EpisodeDownloader"
        private const val PREFS_NAME = "episodive_download_mappings"
        private const val POLL_INTERVAL_MS = 800L
    }
}

private val DownloadProgress.isInProgress: Boolean
    get() = status == DownloadStatus.PENDING ||
            status == DownloadStatus.DOWNLOADING ||
            status == DownloadStatus.PAUSED
