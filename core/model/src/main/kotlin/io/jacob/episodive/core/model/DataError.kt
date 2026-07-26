package io.jacob.episodive.core.model

/**
 * 화면이 사용자에게 설명할 수 있는 수준으로 추린 실패 원인.
 *
 * 예외 메시지를 그대로 화면에 올리면 세 가지가 한꺼번에 깨진다 — 현지화가 되지 않고, 사용자가
 * 무엇을 해야 할지 알 수 없고("timeout"), HTTP 스택 내부가 밖으로 샌다. 그래서 데이터 계층의
 * 예외를 여기서 몇 갈래로 접어 올린다.
 *
 * 갈래를 더 잘게 쪼개지 않는 이유는 **사용자가 취할 행동이 같으면 같은 원인**이기 때문이다.
 * 연결이 끊긴 것과 DNS 가 실패한 것은 원인이 다르지만 사용자에겐 똑같이 "네트워크를 확인하라"다.
 */
sealed interface DataError {
    /** 기기가 네트워크에 닿지 못한다. 사용자가 직접 조치할 수 있는 유일한 갈래다. */
    data object Offline : DataError

    /** 연결은 됐지만 응답이 제때 오지 않았다. 다시 시도하면 될 가능성이 있다. */
    data object Timeout : DataError

    /** 서버가 5xx 로 응답했다. 사용자가 할 수 있는 건 기다렸다 다시 시도하는 것뿐이다. */
    data object Server : DataError

    /** 인증 실패(401·403). API 키 설정 문제이지 사용자 잘못이 아니다. */
    data object Unauthorized : DataError

    /** 요청한 대상이 없다. 재시도해도 결과가 같다 — 화면이 재시도 버튼을 감출 근거가 된다. */
    data object NotFound : DataError

    /** 위 어디에도 들지 않는 것. 원인 추적을 위해 원본을 들고 있는다. */
    data class Unexpected(val throwable: Throwable?) : DataError
}

/**
 * 다시 시도해서 결과가 달라질 수 있는 실패인지.
 *
 * [DataError.NotFound] 와 [DataError.Unauthorized] 는 같은 요청을 반복해도 같은 답이 온다.
 * 이런 화면에 재시도 버튼을 두면 눌러도 아무 일이 없어 앱이 고장 난 것처럼 보인다.
 */
val DataError.isRetryable: Boolean
    get() = when (this) {
        DataError.Offline, DataError.Timeout, DataError.Server -> true
        DataError.NotFound, DataError.Unauthorized -> false
        is DataError.Unexpected -> true
    }

/**
 * 판별이 끝난 [DataError] 를 Flow 밖으로 실어 나르기 위한 예외.
 *
 * 데이터 계층에서 한 번만 판별하고 그 결과를 그대로 올린다. 화면마다 예외 종류를 다시 들여다보게
 * 하면 판별 규칙이 여러 벌로 갈라지고, 무엇보다 화면이 HTTP 스택을 알아야 한다.
 */
class DataErrorException(
    val error: DataError,
    override val cause: Throwable? = null,
) : Exception(cause)

/**
 * 화면에서 잡은 예외를 표시 가능한 원인으로 되돌린다.
 *
 * 데이터 계층을 거쳐 온 것이면 이미 판별된 값이 들어 있고, 그렇지 않은 것(화면·도메인 계층에서
 * 난 예외)은 [DataError.Unexpected] 로 떨어진다.
 */
fun Throwable.asDataError(): DataError =
    (this as? DataErrorException)?.error ?: DataError.Unexpected(this)
