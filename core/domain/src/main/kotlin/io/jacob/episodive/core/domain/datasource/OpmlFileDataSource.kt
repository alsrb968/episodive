package io.jacob.episodive.core.domain.datasource

import io.jacob.episodive.core.model.opml.OpmlOutline

/**
 * OPML 파일 하나를 읽고 쓴다.
 *
 * `Repository` 로 부르지 않는 이유: 이 저장소에서 `Repository` 는 "도메인 개념의 단일
 * 소스"(Podcast/Episode/User/Player)를 뜻하고 전부 RemoteUpdater + Local/Remote DataSource
 * 조합으로 캐시를 관리한다. 파일 하나를 읽고 쓰는 것은 그 계약에 들지 않는다.
 *
 * [write]/[read] 가 `Uri` 가 아닌 `String` 을 받는 이유: 문자열로 감싸면 이 계층이 SAF(Storage
 * Access Framework)를 몰라도 된다. (`:core:domain` 도 android 라이브러리라 기술적으로 `Uri` 를
 * 쓸 수는 있다 — 이건 경계를 좁게 두려는 선택이다.)
 */
interface OpmlFileDataSource {
    /** [destinationUri] 가 가리키는 곳에 OPML 2.0 문서를 쓴다. */
    suspend fun write(destinationUri: String, outlines: List<OpmlOutline>)

    /** [sourceUri] 의 OPML 을 읽어 피드 목록을 돌려준다. 폴더 노드는 걸러진다. */
    suspend fun read(sourceUri: String): List<OpmlOutline>
}
