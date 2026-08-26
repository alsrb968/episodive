package io.jacob.episodive.core.data.opml

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import io.jacob.episodive.core.common.Dispatcher
import io.jacob.episodive.core.common.EpisodiveDispatchers
import io.jacob.episodive.core.domain.datasource.OpmlFileDataSource
import io.jacob.episodive.core.model.opml.OpmlOutline
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * `Uri.parse` 는 여기서만 부른다 — 이 데이터소스가 SAF(uri 문자열)를 아는 유일한 자리이고,
 * 그 위(Repository/UseCase)로는 그냥 문자열로 전달된다.
 */
class OpmlFileDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @Dispatcher(EpisodiveDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : OpmlFileDataSource {

    override suspend fun write(destinationUri: String, outlines: List<OpmlOutline>) {
        withContext(ioDispatcher) {
            val uri = Uri.parse(destinationUri)
            // 모드를 "wt" 로 **명시한다.** 기본값 "w" 는 MODE_TRUNCATE 를 포함하지 않아,
            // 이미 있는 문서를 골라 덮어쓰면 새 내용이 짧을 때 옛 내용의 꼬리가 그대로
            // 남는다. 실제로 확인했다 — 팔로우 40개짜리 파일에 7개를 덮어썼더니 파일
            // 크기가 그대로였고 `</opml>` 뒤에 옛 outline 들이 이어져, 그 파일을 다시
            // 가져오면 XML 파싱이 실패한다.
            //
            // openOutputStream 이 null 을 돌려주는 경우(제공자가 거부)를 조용히 넘기면
            // 사용자는 저장이 끝난 줄 알고 빈 파일을 받는다 — 예외로 바꿔 올린다.
            val outputStream = context.contentResolver.openOutputStream(uri, "wt")
                ?: throw IOException("OPML 파일을 열 수 없습니다: $destinationUri")

            outputStream.use {
                OpmlWriter.write(
                    outputStream = it,
                    outlines = outlines,
                    dateCreated = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now()),
                )
            }
        }
    }

    override suspend fun read(sourceUri: String): List<OpmlOutline> {
        return withContext(ioDispatcher) {
            val uri = Uri.parse(sourceUri)
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw IOException("OPML 파일을 열 수 없습니다: $sourceUri")

            inputStream.use { OpmlReader.read(it) }
        }
    }
}
