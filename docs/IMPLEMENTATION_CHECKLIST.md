# 구현 체크리스트

Episodive의 기능 구현 상태를 추적합니다.

**마지막 검증**: `359c7e8` (2026-08-08) — 섹션 더보기 병합 후 후속 결함 넷을 정리했습니다.

| 표기 | 의미 |
|:--:|:----|
| ✅ | 구현됨 — UI 노출부터 데이터 계층까지 경로가 연결됨 |
| 🟡 | 부분 구현 — 일부만 동작하거나 UI에 노출되지 않음 (사유를 함께 기재) |
| ⬜ | 미구현 |

> 갱신 규칙: 기능을 추가·변경한 PR은 이 문서의 해당 항목도 함께 고칩니다.
> 판정이 애매한 항목에는 근거(`파일:줄` 또는 PR 번호)를 남겨 재감사 비용을 줄입니다.

---

## 1. 사용자 여정

### 1.1 온보딩

- ✅ 앱 소개 (Welcome 페이지)
- ✅ 선호 카테고리 선택 — FlowRow + FilterChip, 한글 표시명 112종 (#72, #91)
- ✅ 선호 팟캐스트 팔로우 선택 (vertical)
    - `/podcasts/trending` + `/recent/feeds` 병렬 호출 병합 (Recommended)
    - ✅ 팔로우 토글 시 리스트 리로드·스크롤 튐 없음 (#80)
- ✅ 완료 화면 — 지연 후 홈 자동 진입

> 권한 요청은 온보딩 단계가 아니라 앱 콜드스타트 시점에 일어납니다 → [2.4](#24-딥링크--권한) 참조.

### 1.2 홈

- ✅ 최근 재생 에피소드 이어듣기 (horizontal)
- ✅ 선호 최근 팟캐스트 (horizontal) — `/recent/feeds`
- ✅ 랜덤 에피소드 (vertical, 6개) — `/episodes/random`
- ✅ 선호 인기 팟캐스트 (horizontal) — `/podcasts/trending`
- ✅ 팔로우 팟캐스트 (horizontal)
- ✅ 지역 인기 팟캐스트 (horizontal) — `/podcasts/trending`
- ✅ 해외 인기 팟캐스트 (horizontal) — `/podcasts/trending`
- ✅ 라이브 에피소드 (vertical) — `/episodes/live`
- ✅ 채널 리스트 (horizontal) — DB + `/search/byterm`
- ✅ 데이터 없는 섹션 자동 숨김 (#92)
- ✅ 스크롤 fling 종료 시 움찔거림 제거 (#79)
- ✅ 섹션별 '더보기' — 8개 섹션 전부. `HomeMoreRoute(section)` 전용 화면에서 팟캐스트 2열
  그리드 / 에피소드 리스트 / 채널 2열 그리드로 분기

### 1.3 검색

- ✅ 통합 검색창 — `/search/byterm`, `/episodes/byfeedid`
    - ✅ 팟캐스트 결과 (vertical)
    - ✅ 에피소드 결과 (vertical)
    - ✅ 검색 히스토리 — 검색어 + 팟캐스트/에피소드 클릭 기록, FlowRow 칩(칩별 삭제)
- ✅ 최근 에피소드 (horizontal) — `/recent/episodes`
- 🟡 인기 팟캐스트 — horizontal 노출까지만. vertical 상세와 Global/Korean/Categories 필터 칩 없음
  (VM이 `max`만 전달, `language`/`categories` 미사용)
- 🟡 카테고리 리스트 — State·Action·Effect는 있으나 화면이 `Category`를 import하지 않아 렌더 불가,
  `NavigateToCategory`는 빈 처리 (`SearchScreen.kt:85`)
- ⬜ 검색 상세 화면 — 라우트가 `SearchRoute` 단일이라 아래 두 항목 모두 진입점 없음
    - ⬜ 최근 팟캐스트 리스트 (`/recent/feeds`)
    - ⬜ 최근 에피소드 리스트 (vertical)

> 검색은 원격 API 기반입니다. `PodcastFtsEntity`/`EpisodeFtsEntity`는 DB에 정의돼 있으나 검색 경로에 연결돼 있지 않습니다.

### 1.4 라이브러리

로컬 DB 기반. 상세 목록은 모두 **vertical 페이징 리스트**입니다(메인의 캐러셀만 horizontal).

- ✅ 라이브러리 통합 검색 — 팟캐스트/에피소드/좋아요/저장/팔로우 대상 (`FindInLibraryUseCase`)
- ✅ 섹션 필터 칩 — All · 최근 청취 · 좋아요 · 저장 · 팔로우 · 선호하는 (6종)
- ✅ 섹션별 '더보기' — All 의 섹션 헤더(제목 포함 전체가 클릭 영역)를 누르면 필터 칩이 그
  탭으로 옮겨간다. 홈과 달리 새 화면으로 나가지 않는다 — 전체 목록이 이미 그 탭이다.
  All 이 아닌 탭에서는 `BackHandler` 가 뒤로가기를 가로채 All 로 되돌린다

| 섹션 | 메인 캐러셀 | 상세 목록 |
|:----|:--:|:----|
| 최근 청취 | ✅ | ✅ 날짜 구분선 포함 |
| 좋아요 | ✅ | ✅ 항목별 토글 |
| 저장 (에피소드) | ✅ | ✅ 토글 + Undo 스낵바 → [2.3](#23-저장--오프라인) |
| 팔로우 | ✅ | ✅ 언팔로우 + 상세 진입 |
| 선호하는 | — | ✅ 선호 카테고리 칩 편집 (#72) |

- ⬜ For you (추천 피드) — 코드에 없음

### 1.5 클립

- ✅ 구간 미리듣기 — VerticalPager 자동 재생, Clip 전용 플레이어
- ✅ 에피소드 좋아요
- ✅ 에피소드 재생
- ✅ 클립 자동 넘어가기 — 재생 종료 시 다음 페이지로 스크롤

### 1.6 팟캐스트 상세

- ✅ 이미지 / 제목 / 설명(HTML 렌더)
- ✅ 에피소드 목록 (Paging)
- ✅ 앨범 아트 기반 동적 테마
- ✅ 팟캐스트 공유 — 원본 웹사이트/RSS 링크 (`ACTION_SEND`)

### 1.7 채널 상세

- ✅ 채널별 팟캐스트 탐색 (`:feature:channel`) — 홈의 채널 리스트가 진입점

> 이 화면의 세부 기능은 아직 항목 단위로 감사하지 않았습니다.

### 1.8 에피소드 플레이어

- ✅ 이미지 / 제목 / 설명
- ✅ 진행 바 · 재생 컨트롤 (재생·일시정지, 이전·다음, −15초 / +30초)
- ✅ 재생 속도 조절
- ✅ 챕터
- ✅ 트랜스크립트 — 재생 중 자막 큐 오버레이
    - MIME이 `TEXT_VTT`로 고정돼 있어 SRT/JSON 트랜스크립트는 로드되지 않음
    - 전문 스크롤·검색 뷰어는 없음
- ✅ 플레이리스트
- ✅ 슬립 타이머 (#73) — 시간 프리셋 + 에피소드 끝까지, 만료 15초 전 페이드아웃
- ✅ 다운로드 상태·진행률 표시 (#77) — 링 진행률 버튼
- ✅ 포그라운드 서비스 재생 (MediaNotificationService)
- ✅ 마지막 재생 리스트·재생 위치 유지 — 프로세스 재시작 시 복원

---

## 2. 시스템 통합

### 2.1 백그라운드 동기화 · 알림

- ✅ 팔로우 팟캐스트 신규 에피소드 주기 동기화 — WorkManager, 3시간 (#70, #76)
    - ✅ 실패 시 지수 백오프 재시도 (30분 시작)
- ✅ 신규 에피소드 알림 — Coil로 썸네일을 Bitmap 변환해 `setLargeIcon`
- ✅ 알림 클릭 시 팟캐스트 상세 딥링크
- ✅ 알림 다국어 (한/영)

### 2.2 홈 화면 위젯

- ✅ Glance 기반 단일 리사이즈 위젯 (#74 → #75) — 재생 + 최근 피드 통합, 3x1~4x3
- ✅ 위젯에서 재생 제어
- ✅ 위젯 탭 시 자동 재생 딥링크 (콜드스타트 포함)
- ✅ Palette 기반 배경색 추출 · 로딩 상태

### 2.3 저장 · 오프라인

- ✅ 에피소드 저장 — 홈·검색·팟캐스트·플레이어·라이브러리에서 토글, `DownloadManager`로 다운로드
- ✅ 저장 해제 — 다운로드 취소 + 파일 삭제
- ✅ Undo 스낵바 (스와이프 dismiss 포함) (#71) — 저장 해제에만 적용
- ✅ 오프라인 재생 (#78) — 다운로드된 파일이 있으면 로컬 파일 우선, 없으면 스트리밍 폴백
- ⬜ Wi-Fi 전용 다운로드 설정 — `setAllowedOverMetered(true)` 고정, [4.1](#41-선행-과제-설정-화면)에 종속
- ⬜ 팔로우 팟캐스트 자동 저장

> 전용 다운로드 관리 화면은 없고 라이브러리 '저장' 탭이 그 역할을 겸합니다.

### 2.4 딥링크 · 권한

- ✅ 딥링크 처리 — 알림 딥링크, 위젯 자동 재생 딥링크 2종
- 🟡 런타임 권한 요청 (`POST_NOTIFICATIONS`) — 요청·허용은 동작하나
  rationale 화면과 거부 후속 UX가 없어 미허용 시 콜드스타트마다 재요청됨

---

## 3. 품질

### 3.1 오류 처리 & 복구 (#95)

- ✅ `DataError` 도메인 에러 타입 + 네트워크 예외 매핑
- ✅ 에러 화면 + 재시도 — 채널·홈·라이브러리·온보딩·팟캐스트·검색 6개 화면
- ✅ 캐시 유지 정책 — 갱신 실패 시 캐시가 있으면 유지, 없을 때만 에러 전파 (`RemoteUpdater`)
- ✅ 네트워크 타임아웃 명시 설정

### 3.2 로딩 & 상태 표현

- ✅ Shimmer 스켈레톤 (#94) — 홈·검색·팟캐스트·라이브러리·온보딩·채널·클립 전 화면
- ✅ 빈 상태 처리 — 데이터 없는 홈 섹션 숨김 (#92)

### 3.3 테스트 & CI

- ✅ 유닛 테스트 — 122개 파일 / `@Test` 1,083개
- ✅ Kover 커버리지 (#84) — CI 하한 80% (전환 후 overall 86.8%로 재baseline)
- ✅ GitHub Actions — `android.yml`, `publish.yml`
- ⬜ 테스트 공백 — `:core:designsystem`, `:core:testing` 테스트 0개

### 3.4 접근성

- 🟡 `contentDescription` — feature 모듈 51건 적용(6건은 의도적 `null`).
  `semantics` 커스터마이징과 TalkBack 실측 검증은 없음

### 3.5 아키텍처

- ✅ Navigation 3 전환 (#69)
- ✅ Offline-First — `RemoteUpdater` + `CacheableQuery` (Podcast·Episode 대상)
- ✅ v2 디자인 시스템 적용 (#90) — [DESIGN.md](../DESIGN.md)

---

## 4. 백로그

### 4.1 선행 과제: 설정 화면

설정 화면이 없어 아래 항목들이 함께 막혀 있습니다. 개별 기능보다 먼저 처리해야 합니다.

- ⬜ 설정 화면 신설 — 현재 DataStore 키는 `is_first_launch`, `categories`, `speed`, `last_playing_*` 뿐
- ⬜ 인앱 다크/라이트 테마 전환 — 시스템 다크모드 추종은 동작하나(✅ `darkScheme`/`lightScheme` 실재)
  사용자가 앱 안에서 고를 수단과 영속 설정이 없음. Dynamic Color도 `false` 고정
- ⬜ Wi-Fi 전용 다운로드 → [2.3](#23-저장--오프라인)
- ⬜ 팔로우 팟캐스트 자동 저장

### 4.2 재생 기능

- ⬜ 이퀄라이저
- ⬜ 구간 반복 (A-B repeat) — 재생목록 반복(`Repeat.OFF/ONE/ALL`)은 별개로 구현돼 있음
- ⬜ 북마크 / 타임스탬프 메모

### 4.3 공유 · 연동

- ⬜ 에피소드 공유 — 현재 공유는 팟캐스트 단위 원본 링크만
- ⬜ 클립 공유 (타임스탬프 포함)
- ⬜ 앱 딥링크 스킴 기반 공유
- ⬜ OPML import / export

### 4.4 분석

- ⬜ 청취 통계 (일별/주별/월별) — `PlayedEpisodeEntity`로 데이터 원천은 있으나 집계·화면 없음
- ⬜ 앱 분석 (Firebase Analytics 등)

---

## 5. 알려진 한계

구현했지만 범위를 좁힌 것들입니다. 미착수 항목([4](#4-백로그))과 구분합니다.

| 항목 | 한계 |
|:----|:----|
| 오류 처리 (#95) | 플레이어 화면에는 에러 화면·재시도가 적용되지 않음 |
| Paging 재시도 | 라이브러리 4개 탭·팟캐스트 에피소드·온보딩 추천의 Paging 스트림은 재시도 경로가 별도 |
| HTTP 응답 검증 | 200 응답의 논리적 실패(`status`/`description` 필드)를 검증하지 않음 |
| NetworkMonitor | 데이터 계층에 연결되지 않음 |
| 캐시 정책 | `RemoteUpdater`의 stale-while-error가 Podcast·Episode에만 적용 (Channel·RecentSearch·User·Player는 대상 밖) |
| 트랜스크립트 | VTT 전용, 전문 뷰어 없음 |
| 검색 결과 '더보기' | 착지할 전체 목록 화면이 없어 섹션 헤더에 버튼을 달지 않음 (`SearchScreen.kt:403`) |
| 홈 '더보기' 첫 진입 | 팟캐스트 섹션은 전체 목록용 캐시를 처음 채울 때 피드마다 상세 요청이 붙어 지연이 있음 (`PodcastRemoteUpdater.kt:55-66`). 근본 해결은 `feeds` PK 를 `(id, groupKey)` 복합키로 바꾸는 마이그레이션이라 별건으로 둔다 |
| 사운드바이트 정렬 | `soundbites` 는 원격이 준 순위를 보존할 컬럼이 없어 `episodeId` 오름차순으로 페이징한다. 순위를 살리려면 순서 컬럼 추가(스키마 변경)가 필요 |
| lint | `MainActivity.kt:91` 의 media3 `UnstableApi` opt-in 누락으로 `./gradlew lint` 가 실패한다(`#75` 부터). CI 는 `koverXmlReportDebug` 만 돌아 잡히지 않음 |
| 유닛 테스트 병렬 실행 | JDK 21 에서 `./gradlew test` 로 전 모듈을 한꺼번에 돌리면 ByteBuddy self-attach 경합으로 MockK 초기화가 깨진다. 모듈별 실행과 `--max-workers=1` 은 정상 |

### 미감사 영역

- 채널 상세 화면(`:feature:channel`) 내부 기능
- 접근성 실측 (TalkBack)
- 릴리스 빌드 / ProGuard / 성능·전력
