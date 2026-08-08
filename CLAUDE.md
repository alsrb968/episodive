# CLAUDE.md

이 파일은 Claude Code(claude.ai/code)가 이 저장소에서 작업할 때 참고하는 가이드입니다.

## 프로젝트 개요

Episodive는 Kotlin과 Jetpack Compose로 만든 Android 팟캐스트 앱으로, Podcast Index API를 사용합니다. Clean Architecture + MVI 패턴, Gradle 컨벤션 플러그인 기반 멀티모듈 구조(20개 모듈), Room DB 중심의 Offline-First 설계를 따릅니다.

### 기술 스택

- **Build**: Gradle 9.2.1, AGP 8.13.1, Kotlin 2.2.21, KSP 2.3.1
- **Target**: Min SDK 28, Target/Compile SDK 36, Java 11
- **UI**: Jetpack Compose (BOM 2025.12.00), Material3 1.5.0-alpha10
- **DI**: Hilt 2.57.2
- **Database**: Room 2.8.4, Paging 3.3.6, Auto-migrations (버전 8)
- **Network**: Retrofit 3.0.0, OkHttp 5.3.2, Gson 2.13.2
- **Async**: Kotlin Coroutines 1.10.2, Lifecycle 2.10.0
- **Background**: WorkManager 2.10.1, Hilt Worker
- **Media**: Media3 1.8.0 (ExoPlayer), MediaNotificationService
- **Image**: Coil 2.7.0, Palette API
- **Testing**: JUnit 4.13.2, MockK 1.14.6, Turbine 1.2.1, Robolectric 4.16

## 빌드 명령어

```bash
./gradlew build                          # 전체 빌드
./gradlew test                           # 유닛 테스트 실행
./gradlew testDebugUnitTest              # Debug 유닛 테스트만 실행
./gradlew connectedAndroidTest           # 기기 연결 테스트
./gradlew :core:database:test            # 특정 모듈 테스트
./gradlew lint                           # Lint 검사
./gradlew lintFix                        # Lint 자동 수정
./gradlew koverXmlReportDebug            # 커버리지 리포트 생성 (Kover)
```

### git worktree 작업 시 필수: local.properties 복사

`local.properties`는 `.gitignore` 대상이라 **worktree를 만들면 자동으로 복사되지 않는다.** 이 파일에는
SDK 경로(`sdk.dir`)뿐 아니라 **Podcast Index API 키**(`podcastIndex.apiKey`, `podcastIndex.secretKey`)가 들어
있고, `core/network/build.gradle.kts`가 이 키들을 컴파일 타임에 `BuildConfig.API_KEY` / `BuildConfig.SECRET_KEY`로
굽는다. 누락하면 빌드는 되지만 런타임에 키가 비어 **모든 API 호출이 HTTP 401**로 실패한다.

worktree에서 작업·빌드하기 전에 루트의 `local.properties`를 복사할 것:

```bash
cp <repo-root>/local.properties <worktree-dir>/local.properties
```

## 아키텍처

### 모듈 구조 (20개)

#### Core 모듈 (11개)

| 모듈 | 역할 |
|:----|:----|
| `:core:model` | 순수 Kotlin 도메인 모델 (Podcast, Episode, Category, UserData 등) |
| `:core:domain` | Repository 인터페이스 + 40개 이상의 Use Case |
| `:core:data` | Repository 구현체, RemoteUpdater 캐싱 패턴 |
| `:core:network` | Retrofit API 인터페이스 5개, EpisodiveInterceptor (SHA-1 인증) |
| `:core:database` | Room DB v8, Entity 12개, View 2개, DAO 5개 |
| `:core:datastore` | DataStore Preferences 기반 사용자 설정 관리 |
| `:core:player` | ExoPlayer 래퍼, @Player qualifier로 듀얼 플레이어(Main/Clip) |
| `:core:common` | 공유 유틸리티, EpisodiveDispatchers, EpisodivePlayers qualifier |
| `:core:designsystem` | 재사용 Compose 컴포넌트 20개 이상, 테마 시스템 |
| `:core:ui` | 도메인 특화 상위 레벨 UI 컴포넌트 |
| `:core:testing` | 테스트 데이터 팩토리, MainDispatcherRule, RoomDatabaseRule |

#### Feature 모듈 (8개)

모든 feature 모듈은 `episodive.android.feature` 컨벤션 플러그인을 사용하며, core:common/domain/designsystem/model/ui/testing과 Compose/Paging/Hilt/Navigation이 자동으로 포함됩니다.

| 모듈 | 설명 |
|:----|:----|
| `:feature:onboarding` | 온보딩 플로우, 카테고리 선택 |
| `:feature:home` | 홈 피드: 최근/트렌딩/랜덤/라이브 콘텐츠 |
| `:feature:search` | FTS 지원 검색, 최근 검색 기록 |
| `:feature:library` | 구독 팟캐스트, 좋아요/재생 기록 에피소드 |
| `:feature:podcast` | 팟캐스트 상세 정보 및 에피소드 목록 |
| `:feature:player` | 오디오 플레이어 UI |
| `:feature:clip` | 사운드바이트 및 클립 탐색 |
| `:feature:channel` | 채널/카테고리 탐색 |

### 컨벤션 플러그인 (build-logic/convention/)

| 플러그인 | 역할 |
|:--------|:----|
| `episodive.android.application` | Application 모듈 설정 |
| `episodive.android.application.compose` | App 모듈 Compose 설정 |
| `episodive.android.library` | 표준 Android 라이브러리 |
| `episodive.android.library.compose` | 라이브러리 Compose 설정 |
| `episodive.android.feature` | Feature 템플릿 (library + compose + hilt + test + kover) |
| `episodive.android.room` | Room + KSP + 스키마 디렉토리 설정 |
| `episodive.android.test` | 테스트 의존성 |
| `episodive.android.application.jacoco` / `episodive.android.library.jacoco` | 커버리지 설정 (Kover 기반, plugin id는 하위 호환 위해 유지) |
| `episodive.hilt` | Hilt DI + KSP |
| `episodive.jvm.library` | 순수 Kotlin/JVM 라이브러리 |

## 핵심 아키텍처 패턴

### Clean Architecture 계층
```
UI (Feature) → Domain (Use Cases) → Data (Repositories) → Data Sources (Network/Database/DataStore)
```

### MVI 패턴
모든 feature 모듈에서 사용:
- **State**: `sealed interface` (Loading | Success | Error)
- **Action**: 사용자 의도를 표현하는 `sealed interface`
- **Effect**: 일회성 사이드 이펙트(네비게이션, 토스트)를 `SharedFlow`로 전달
- **ViewModel**: Action을 처리하고, State는 `StateFlow`, Effect는 `SharedFlow`로 방출

### Offline-First / RemoteUpdater 패턴
`RemoteUpdater` 추상 클래스가 캐시 갱신을 관리:
1. `CacheableQuery`(키 + TTL)로 캐시 만료 여부 확인
2. 만료 시 원격 API에서 데이터 페치
3. API 모델 → Entity 변환 후 타임스탬프와 함께 Room에 저장
4. DB에서 `Flow`로 반환 (단일 소스)

**CacheableQuery 종류:**
- `PodcastQuery`: FeedId, Medium, Trending, Recommended, Random, Recent
- `EpisodeQuery`: FeedId, Live, Random, Recent, RecentNew

### 백그라운드 에피소드 동기화
WorkManager + HiltWorker 기반 3시간 주기 동기화:
1. `EpisodeSyncScheduler`가 `PeriodicWorkRequest`로 주기적 동기화 예약
2. `EpisodeSyncWorker`가 `SyncNewEpisodesUseCase`를 실행하여 팔로우 팟캐스트의 새 에피소드 확인
3. 새 에피소드 발견 시 `EpisodeSyncNotificationHelper`로 썸네일 포함 알림 표시
4. Coil로 에피소드 이미지를 Bitmap 변환 후 `setLargeIcon`으로 알림에 포함

**관련 파일:** `:app` 모듈의 `sync/` 패키지 (EpisodeSyncScheduler, EpisodeSyncWorker, EpisodeSyncNotificationHelper)

### 듀얼 플레이어 시스템
Hilt `@Player` qualifier로 ExoPlayer 인스턴스 두 개 관리:
- `@Player(EpisodivePlayers.Main)` — 전체 에피소드 재생
- `@Player(EpisodivePlayers.Clip)` — 사운드바이트/클립 재생

### 재생 위치 저장 규약 (필수)

재생 위치는 **`Progress.episodeId` 를 키로만** 저장한다. 저장 경로(`PlayerViewModel` 의 progress
콜렉터, `LastPlaySnapshot`, 수면 타이머)는 `playerRepository.progress` 하나만 구독한다.

**에피소드 id 를 `nowPlaying` 같은 다른 Flow 에서 가져와 `combine` 하지 마라.** `nowPlaying` 은
Room 왕복과 `flowOn(IO)` 를 거쳐 `progress` 보다 늦게 도착한다. 전환 순간 `(이전 에피소드, 새 위치)`
쌍이 만들어지고 그대로 저장되어 **이전 에피소드의 이어듣기 지점이 사라진다.** 실제로 겪은 버그다.

`PlayerDataSourceImpl` 쪽 규약:
- progress 발행은 `publishProgress()` 를 거치고 **반드시 `episodeId` 를 함께 싣는다.** 빠뜨리면
  그 경로의 저장이 조용히 멈춘다(오염이 아니라 무저장이라 눈치채기 어렵다).
- `episodeId` 는 `_nowPlaying` 이 아니라 `currentEpisode()`(= 플레이어에 실제로 올라 있는 항목)에서
  얻는다. `rehydrate` 가 `_nowPlaying` 을 플레이어와 무관하게 바꿀 수 있다.
- media3 는 `setMediaItems` 호출 스택 안에서 transition 콜백을 **인라인 실행**한다. 목록을 통째로
  갈아끼우는 경로(`prepare`/`play(list, index)`/`playClips`)는 `isPreparing` 으로 그동안의 발행을 막고,
  끝난 뒤 확정값을 한 번만 발행한다.
- `position == 0 이면 저장하지 않는다` 류 가드를 넣지 마라. "맨 앞으로 되감기" 와 "완료 후 처음부터
  다시 듣기" 가 0 을 저장해야 정상이다. `PlayerViewModelTest` 에 이를 지키는 계약 테스트가 있다.

## 중요 구현 세부사항

### 1. Enum 처리 (필수)

모든 Enum은 **enum name이 아닌 value 프로퍼티**를 사용합니다:

```kotlin
enum class Medium(val value: String) { PODCAST("podcast"), MUSIC("music"), ... }
```

**변환** — 반드시 `entries.find()` 사용, `valueOf()` 절대 금지:
```kotlin
fun String.toMedium(): Medium? = Medium.entries.find { it.value == this }
```

Room TypeConverter도 `value`로 저장:
```kotlin
@TypeConverter fun fromMedium(medium: Medium?): String? = medium?.value
@TypeConverter fun toMedium(value: String?): Medium? = value?.toMedium()
```

**이유**: API는 소문자(`"podcast"`)를 반환하지만 enum name은 대문자(`PODCAST`)입니다. `valueOf()`를 쓰면 예외가 발생합니다.

### 2. 데이터베이스 스키마 (Room v12)

**Entity (13개):** PodcastEntity, EpisodeEntity, FeedEntity, SoundbiteEntity, FollowedPodcastEntity, LikedEpisodeEntity, PlayedEpisodeEntity, SavedEpisodeEntity, PodcastGroupEntity, EpisodeGroupEntity, PodcastFtsEntity, EpisodeFtsEntity, RecentSearchEntity

**View (2개):** PodcastWithExtrasView, EpisodeWithExtrasView

**DAO (5개):** PodcastDao, EpisodeDao, FeedDao, SoundbiteDao, RecentSearchDao

**마이그레이션:** 1→8은 auto-migration(필요 시 spec 클래스), 8→12는 `migration/` 의 수동
`Migration` 객체이며 `DatabaseModule` 에서 `addMigrations` 로 등록합니다.

**캐시 표의 정렬:** `feeds`·`soundbites` 는 원격이 준 순위를 `sortOrder` 에 담고 모든 쿼리가
그 컬럼으로 정렬합니다. `LIMIT`/`OFFSET` 페이징은 정렬이 정해져야만 성립하므로 새 쿼리를 더할
때도 `ORDER BY` 를 빠뜨리지 않습니다. `feeds` 의 기본키는 `(id, groupKey)` 복합키로, 같은 피드가
여러 목록에 동시에 속할 수 있습니다.

모든 Entity에는 캐시 무효화를 위한 `cachedAt: Instant`와 그룹 키가 있습니다.

**스키마를 바꾼 뒤에는** `./gradlew :core:database:kspDebugKotlin` 으로 `schemas/…/<버전>.json` 을
내보내고, 그 `createSql` 을 그대로 옮겨 마이그레이션을 씁니다. `Migration11to12Test` 가 그 방식의
본보기입니다 — 내보낸 스키마로 이전 버전 DB를 만들고 Room 이 여는 순간의 검증에 판정을 맡깁니다.

### 3. API 인증 (Podcast Index)

`EpisodiveInterceptor`가 헤더를 추가합니다:
- `X-Auth-Date`: 유닉스 타임스탬프
- `X-Auth-Key`: API 키
- `Authorization`: SHA-1(`apiKey + apiSecret + timestamp`)

API 인터페이스 (5개): `ChapterApi`, `EpisodeApi`, `FeedApi`, `PodcastApi`, `SoundbiteApi`

### 4. 응답 래퍼

`ResponseListWrapper<T>`는 엔드포인트별로 다른 JSON 필드명(`feeds`, `items`, `channels`)을 통합 처리합니다.

## 개발 워크플로우

### 데이터베이스 변경 시
1. Entity 수정 + DB 버전 증가
2. DAO 쿼리 수정
3. TypeConverter 추가/수정 (`entries.find` 패턴 사용)
4. 필요 시 DB View 수정
5. `:core:data`의 mapper 수정
6. 필요 시 auto-migration spec 추가
7. `RoomDatabaseRule`로 테스트 작성

### 새 API 엔드포인트 추가 시
1. `:core:network/model`에 응답 모델 추가
2. `:core:network/api`의 API 인터페이스에 추가
3. RemoteDataSource 구현 (인터페이스 + 구현체)
4. `:core:data`의 Repository 구현 업데이트
5. `:core:domain`에 Use Case 생성
6. MVI action/state로 ViewModel에 연결

### 새 Feature 추가 시
1. `episodive.android.feature` 플러그인으로 feature 모듈 생성
2. `settings.gradle.kts`에 추가
3. State/Action/Effect sealed interface + ViewModel 구현
4. state/effect를 수집하는 Composable 화면 작성
5. `:app` 모듈에서 네비게이션 연결

## 테스트

### 데이터베이스 테스트
```kotlin
@RunWith(RobolectricTestRunner::class)
class MyDaoTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    @get:Rule val databaseRule = RoomDatabaseRule()
    // :core:testing의 PodcastTestData/EpisodeTestData 사용
    // Flow 검증은 Turbine의 .test { } 사용
}
```

### 테스트 데이터 (`:core:testing`)
- `PodcastTestData.podcasts` — 샘플 팟캐스트 Entity 10개
- `EpisodeTestData.episodes` — 샘플 에피소드 Entity 10개
- `FeedTestData` — 트렌딩/최근/사운드바이트 피드
- `ChannelTestData` — 채널/카테고리 데이터

**규칙:** 항상 테스트 데이터 팩토리 사용. Flow 테스트는 Turbine 사용. 인라인 테스트 객체 생성 금지.

## CLI 도구 사용 가이드

Android 관련 작업은 **`android` (Antigravity CLI, `/usr/local/bin/android`) 를 우선 사용**한다. low-level QEMU/SDK 옵션이 필요할 때만 native `emulator`/`adb`/`sdkmanager` 로 폴백.

### 매핑 (선호 → 폴백)
| 작업 | 우선 (`android`) | 폴백 (native) |
|:----|:----|:----|
| AVD 목록 | `android emulator list` | `emulator -list-avds` |
| 에뮬 부팅 (준비 완료까지 대기 포함) | `android emulator start <AVD>` | `emulator -avd <AVD> [flags...] &` |
| 에뮬 종료 | `android emulator stop <AVD>` | `adb emu kill` |
| AVD 생성/삭제 | `android emulator create` / `remove` | `avdmanager create/delete avd` |
| APK 배포·실행 | `android run --apks app.apk --activity=...` | `adb install -r app.apk && adb shell am start ...` |
| 스크린샷 | `android screen capture -o /tmp/x.png` | `adb exec-out screencap -p > /tmp/x.png` |
| UI 트리 inspect (스크린샷보다 빠름) | `android layout -p` | `adb shell uiautomator dump` |
| SDK 패키지 관리 | `android sdk install/list/update` | `sdkmanager` |
| 환경 정보 | `android info` | `echo $ANDROID_HOME` |
| 공식 문서 검색 | `android docs search <keyword>` | (수동 web 검색) |

### native 만 가능한 케이스 (폴백 필수)
- 에뮬 audio/gpu/cpu/memory 등 QEMU 옵션 (`-no-audio`, `-gpu host`, `-cores`, `-memory`)
- `-no-snapshot-load` 등 부팅 모드 세부 제어
- `adb shell` 직접 명령 (`am broadcast`, `dumpsys`, `appwidget grantbind`, `settings put`, `input keyevent` 등)
- `adb logcat` 스트리밍/필터
- WorkManager/MediaSession 같은 시스템 서비스 dumpsys

### 일반 원칙
1. 일반 부팅·배포·스크린샷은 `android` 사용 (자동 대기 + 안전한 default)
2. QEMU 플래그가 필요하면 그때만 `emulator -avd ...` 직접 실행
3. ADB shell 시스템 명령은 `adb` 직접 (래퍼 없음)
4. 에뮬을 `emulator -no-audio` 같은 플래그로 띄운 뒤에는 audio/animation 등 OS 레벨 동작 누락 가능 — 사운드/애니 검증 필요한 작업이면 `android emulator start` 로 깨끗이 부팅

## mccm:Commit Conventions

- language: 한글
- title-format: {제목}
- title-max-length: 50
- body: 선택
- branch-prefixes: feat/, fix/, refactor/, test/, docs/, chore/, ci/
- branch-format: {prefix}/{english-slug}

## mccm:PR Conventions

- language: 한글
- title-format: {타입}: {제목}
- types: feat, fix, refactor, test, docs, chore, ci
- title-max-length: 70
- body-format: ## 변경 사항\n- ...\n\n## 테스트\n...
- label-map: feat→feature, fix→bugfix, refactor→refactoring, test→test, docs→documentation, chore→chore, ci→ci/cd
- auto-assignee: true

## mccm:Cleanup Conventions

- default-branch: main
- protected-branches: main, master

## 코드 편집 규칙

**필수: Import 순서 규칙**
- 반드시 실제 코드를 먼저 수정/추가한 후, import를 마지막에 추가
- 코드 작성 전에 import를 추가하지 않음
- lint 충돌 방지를 위한 규칙

## 공통 패턴

### Coroutine Dispatcher
```kotlin
@Dispatcher(EpisodiveDispatchers.IO) val ioDispatcher: CoroutineDispatcher
// Repository의 IO 작업에는 withContext(ioDispatcher) 사용
```

### StateFlow vs SharedFlow
- `StateFlow` — UI 상태 (항상 현재 값 유지)
- `SharedFlow` — 일회성 이펙트 (네비게이션, 토스트)

### 네비게이션
- Single Activity, Compose Navigation
- 하단 바: Home, Search, Library, Clip
- `TYPESAFE_PROJECT_ACCESSORS`로 타입 안전 라우트 사용

## graphify

The knowledge graph at graphify-out/ — god nodes, community structure, cross-file relationships — is
**local-only and not committed**. Generate it once per clone with `graphify update .` (AST-only, no API
cost); the rules below apply only once it exists. See `## graphify 로컬 셋업`.

Rules:
- For codebase questions, first run `graphify query "<question>"` when graphify-out/graph.json exists. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).

## Codebase Analysis Policy

- 코드 구조, 함수 관계, 호출 흐름, 의존성 파악 시 반드시 graphify `query` / `explain` / `path`를 먼저 사용한다.
- Read/Grep으로 여러 파일을 직접 순회하기 전에 graphify로 먼저 조회한다.
- 조회 결과가 불충분하거나 최신 변경이 의심될 때만 Read/Grep으로 fallback한다.
- 구조적 변경 시 커밋 전 `/graphify --update`를 실행한다.

## graphify 로컬 셋업

**graphify 산출물(`graphify-out/`)은 커밋하지 않는다.** 28MB짜리 자동 생성물(그중 `graph.html`이
4.7MB)이라 PR diff를 덮고 브랜치마다 충돌한다. `.gitignore` 대상이며 **각자 로컬에서 생성**한다.

**Claude PreToolUse 가드는 `.claude/settings.json`에 커밋되어 있다.** clone하면 그대로 적용되므로
설정할 것이 없다. 반면 **그래프와 git 훅은 로컬 자산**이므로(훅은 `.git/hooks`에 있어 버전 관리가 안 된다)
각 기여자가 **clone 후 한 번** 직접 만든다.

```bash
graphify install                # graphify 0.9.12 (버전 고정 — 아래 Grep 가드가 이 버전 동작에 기댄다)
graphify update .               # 그래프 최초 생성 (AST-only, API 비용 없음). 이후 갱신도 같은 명령
graphify hook install           # post-commit·post-checkout 훅 설치
rm -f .git/hooks/post-checkout  # post-checkout 훅은 제거한다 (아래 참고)
```

**`graphify claude install`은 실행하지 않는다.** 이 명령은 `.claude/settings.json`을 자기 형식으로
덮어써서, 커밋된 설정의 두 가지 보완(머신 독립 경로, 아래 Grep 가드)을 날려버린다. 이미 실행했다면
`git checkout -- .claude/settings.json`으로 되돌린다.

graphify가 PATH에 없어도 훅은 조용히 통과하고, 가드는 `graphify-out/graph.json`이 없으면 발화하지
않는다. graphify를 설치하지 않은 기여자도 작업에 지장이 없다.

### PreToolUse 가드 구성

| 매처 | 실행 대상 | 비고 |
|:----|:----|:----|
| `Bash` | `graphify hook-guard search` | `command` 필드에서 grep류 명령을 감지 |
| `Read\|Glob` | `graphify hook-guard read` | 입력 문자열의 파일 확장자를 감지 |
| `Grep` | `scripts/graphify-grep-guard.sh` | 저장소 자체 스크립트 |

`Grep`만 자체 스크립트인 이유: `graphify hook-guard`(0.9.12)는 tool_input에서 **파일 확장자를 찾아낼
때만** 발화한다. Grep 툴의 입력은 `{"pattern":"class EpisodeDao","path":"core"}` 형태라 확장자가 없어
`search`·`read` 어느 쪽도 반응하지 않는다(실측 확인, `glob` 파라미터를 붙여도 동일). Grep은 그 자체가
코드 검색이므로 자체 스크립트가 패턴과 무관하게 항상 안내한다.

### 동작 규칙

- post-commit 훅은 커밋 **후** `git diff HEAD~1`로 바뀐 **코드** 파일만 감지해 그래프를 재생성한다. 산출물이 무시 대상이라 재생성돼도 작업 트리는 깨끗하게 유지된다 — 별도 커밋할 것이 없다.
- 문서/이미지만 변경한 경우 훅이 무시하므로 그때만 `graphify update .`(또는 `/graphify --update`)를 수동 실행한다.
- **post-checkout 훅은 제거한다.** `graphify hook install`이 함께 설치하지만, 이 훅은 브랜치 전환마다 **전체 재빌드**를 돌리고 `GRAPHIFY_SKIP_HOOK`을 무시한다. post-commit 훅만으로 충분하다. (그래프가 누적으로 부풀면 `graphify update . --force`로 리셋 가능.)
- 그래프는 로컬 전용이므로 브랜치를 오가면 현재 체크아웃과 어긋날 수 있다. 조회 결과가 최신 코드와 다르면 `graphify update .`로 갱신한 뒤 다시 조회한다.
