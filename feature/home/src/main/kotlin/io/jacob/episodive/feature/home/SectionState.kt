package io.jacob.episodive.feature.home

import io.jacob.episodive.core.model.DataError
import io.jacob.episodive.core.model.asDataError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import timber.log.Timber

/**
 * 홈의 섹션 하나가 지금 어느 단계에 있는지.
 *
 * 홈은 서로 다른 소스 아홉 개를 한 화면에 모으는데, 값(`List<T>`)을 그대로 `combine` 하면
 * **가장 느린 하나가 화면 전체를 붙잡는다** — `combine` 은 모든 입력이 첫 값을 낼 때까지
 * 아무것도 내보내지 않는다. 랜덤 에피소드처럼 유독 느린 소스 하나 때문에 이미 도착한 여덟 개가
 * 스켈레톤 뒤에 갇히던 문제가 실제로 있었다.
 *
 * 그래서 각 소스를 이 타입으로 감싼다. [asSectionState] 가 구독하자마자 [Loading] 을 흘려보내
 * `combine` 이 첫 프레임부터 값을 내고, 화면은 도착한 섹션부터 채운다.
 *
 * 어느 섹션인지를 가리키는 [io.jacob.episodive.feature.home.navigation.HomeSection] 과는 다른
 * 것이다 — 저쪽은 "무엇", 이쪽은 "그것이 지금 어디까지 왔는가" 다.
 */
sealed interface SectionState<out T> {
    /** 아직 첫 응답이 오지 않았다. 화면은 이 자리에 스켈레톤을 그려 자리를 잡아 둔다. */
    data object Loading : SectionState<Nothing>

    /** 응답이 도착했다. 비어 있을 수도 있고, 그때 화면은 섹션을 통째로 건너뛴다. */
    data class Success<out T>(val items: List<T>) : SectionState<T>

    /**
     * 이 소스만 실패했다.
     *
     * `RemoteUpdater` 는 캐시가 하나도 없을 때만 예외를 올린다(있으면 오래된 캐시라도 그대로
     * 흘려보낸다). 그러니 여기까지 왔다는 것은 대신 보여줄 것이 없다는 뜻이고, 화면은
     * [Success] 가 비었을 때와 마찬가지로 섹션을 건너뛴다.
     */
    data class Error(val error: DataError) : SectionState<Nothing>
}

/**
 * 값 흐름을 섹션 상태 흐름으로 감싼다.
 *
 * `onStart` 의 [SectionState.Loading] 이 이 함수의 존재 이유다. 이것이 있어야 `combine` 이
 * 느린 소스를 기다리지 않는다. 빼면 감싸기 전과 똑같이 전체가 다시 묶인다.
 *
 * `catch` 도 반드시 여기, 소스마다 하나씩 있어야 한다. `combine` 바깥에 한 벌만 두면 소스
 * 하나가 던지는 순간 **홈 전체가 오류 화면으로 넘어간다** — 트렌딩 하나가 실패했다고 이어듣기까지
 * 사라질 이유는 없다.
 */
internal fun <T> Flow<List<T>>.asSectionState(): Flow<SectionState<T>> =
    map<List<T>, SectionState<T>> { SectionState.Success(it) }
        .onStart { emit(SectionState.Loading) }
        .catch { e ->
            Timber.e(e, "홈 섹션 하나를 불러오지 못했다")
            emit(SectionState.Error(e.asDataError()))
        }

/**
 * 아직 오지 않았거나 실패한 섹션을 빈 목록으로 본다.
 *
 * **자리를 잡을 필요가 없는 곳에서만 쓴다.** 목록 안의 섹션은 로딩과 빈 값을 구분해야 하므로
 * (하나는 스켈레톤, 하나는 건너뛰기) 이 확장이 아니라 상태 자체를 보고 갈라야 한다.
 */
internal val <T> SectionState<T>.itemsOrEmpty: List<T>
    get() = if (this is SectionState.Success) items else emptyList()

/**
 * 화면에 실제로 내놓을 것이 있는가.
 *
 * 로딩 중이거나, 비어 온 응답이거나, 실패한 섹션은 모두 "없다" 다 — 화면이 이 셋을 똑같이
 * 건너뛰기 때문이다. 홈 전체를 오류로 덮을지 가르는 기준이 "모든 섹션이 실패했는가" 가 아니라
 * 이것이어야 한다. 아홉 중 셋은 로컬만 읽어 네트워크로 실패하지 않으므로, 실패 개수로 세면
 * 오프라인에서도 조건이 참이 되지 않는다.
 */
internal val SectionState<*>.hasContent: Boolean
    get() = this is SectionState.Success && items.isNotEmpty()
