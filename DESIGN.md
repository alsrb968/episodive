# Episodive Design System

Episodive 앱의 시각 디자인·컴포넌트·화면 레이아웃을 실제 코드 기준으로 정리한 디자인 문서입니다.
Jetpack Compose + Material3 기반이며, 모든 수치·색상·모션 값은 소스에서 직접 확인한 값입니다.

> **스코프**: 이 문서는 안드로이드 앱 UI 전체(8개 feature 화면 + 디자인 시스템 + 도메인 UI + 앱 셸 + 홈스크린 위젯)를 다룹니다.
> Media3 미디어 알림(`MediaNotificationService`)의 시스템 UI는 OS 렌더링 영역이라 제외합니다.
>
> **버전 기준**: Compose BOM 2025.12.00 · Material3 1.5.0-alpha10 · Kotlin 2.2.21 (2026-07 기준)

---

## 1. 디자인 정체성

| 항목 | 값 |
|:----|:----|
| 제품명 | **Episodive** (팟캐스트 앱) |
| 시그니처 컬러 | **레드 `#F5332C`** (라이트/다크 공통 primary) |
| 폰트 | **Pretendard** (한글 최적화 산세리프, Light/Regular/Medium/Bold) |
| 아이콘 | **Tabler Icons** 커스텀 임베드 (일부만 Material Icons) |
| 무드 | 어두운 몰입형 재생 경험 + 앨범아트에서 추출한 색으로 화면을 물들이는 다이내믹 테마링 |
| 기준 그리드 | **16dp** 좌우 인셋 / 섹션 간 16dp |

디자인 골격은 Google **Now in Android** 샘플에서 파생됐습니다(`GradientColors`, `BackgroundTheme`, 스크롤바, `EpisodiveBackground` 등의 네이밍·구조가 그 흔적). 여기에 팟캐스트 재생 앱 특유의 요소(팔레트 기반 배경, 풀스크린 플레이어, 클립 세로 피드, Glance 위젯)를 얹었습니다.

---

## 2. 디자인 토큰

### 2.1 색상

Material3 `ColorScheme`을 라이트/다크 두 세트로 **직접 정의**합니다. Dynamic Color(Android 12+ Material You)는 코드상 존재하나 **기본 비활성**(`dynamicColor = false`)이라, 브랜드 레드가 항상 유지됩니다.

정의 위치: `core/designsystem/theme/Color.kt`, 스킴 조립: `theme/Theme.kt`

#### 브랜드 & 핵심 역할색

| 역할 | Light | Dark | 용도 |
|:----|:----|:----|:----|
| `primary` | `#F5332C` | `#F5332C` | 브랜드 레드. 재생 진행바, 강조 액션, 링크, 선택 상태 |
| `onPrimary` | `#FFFFFF` | `#FFFFFF` | primary 위 텍스트/아이콘 |
| `primaryContainer` | `#FFDAD6` | `#8B1E1A` | 하단 네비 인디케이터, 선택 칩/태그, 그라데이션 top |
| `onPrimaryContainer` | `#410002` | `#FFDAD6` | 하단 네비 **선택** 아이콘·라벨 |
| `secondary` | `#D63C2F` | `#FF5449` | 레드 변형 강조 |
| `secondaryContainer` | `#FFE0DE` | `#9C2B23` | 썸네일 플레이스홀더 기본색 |
| `tertiary` | `#E65100` | `#FF7043` | 레드-오렌지 보완색 |
| `error` | `#BA1A1A` | `#FF6B6B` | 오류 / 슬립타이머 취소 |

#### 뉴트럴 & 표면

| 역할 | Light | Dark |
|:----|:----|:----|
| `background` / `surface` | `#FFFBFF` | `#1C1B1B` |
| `onBackground` / `onSurface` | `#201A1A` | `#ECE0DF` |
| `surfaceVariant` | `#F5DDDA` | `#534341` |
| `onSurfaceVariant` | `#534341` | `#D8C2BE` |
| `outline` | `#857370` | `#9F8C89` |
| `surfaceContainerLowest → Highest` | `#FFFFFF … #EDDEDE` | `#171515 … #3F3C3C` |

> 뉴트럴 계열이 순수 회색이 아니라 **레드 틴트가 살짝 섞인 웜 그레이**(예: 다크 배경 `#1C1B1B`, 표면 variant `#534341`)입니다. 브랜드 레드와의 조화를 위한 의도적 선택입니다.

#### 시맨틱 매핑 규칙(코드에서 반복되는 패턴)

- **선택/활성** = `primary` 또는 `primaryContainer`
- **비활성 텍스트** = `onSurfaceVariant`, 또는 `onBackground.copy(alpha = …)`
- **disabled** = 컨테이너 `alpha 0.12f`, 콘텐츠 `alpha 0.38f`
- **하단 네비 비선택** = `onSurfaceVariant.copy(alpha = 0.6f)`
- **반투명 오버레이 배경**(스크롤 전 뒤로가기 버튼 등) = `onSurface.copy(alpha = 0.3f)`

### 2.2 타이포그래피

전 스타일 **Pretendard** 패밀리. Material3 baseline `Typography()`의 크기·행간을 그대로 쓰되 폰트·weight만 교체합니다. 정의: `theme/Type.kt`

| 그룹 | Weight | 앱에서의 용도 |
|:----|:----|:----|
| `displayLarge/Medium/Small` | **Bold** | (거의 미사용) |
| `headlineLarge/Medium/Small` | **Bold** | `headlineMedium` = **화면 타이틀**(홈/검색/라이브러리) · `headlineSmall` = **섹션 헤더**, 팟캐스트 제목 |
| `titleLarge/Medium/Small` | **Medium** | `titleLarge` = 재생중 에피소드명·배속·클립 제목 · `titleMedium` = 리스트 아이템 제목 · `titleSmall` = 서브섹션·미니플레이어 제목 |
| `bodyLarge/Medium/Small` | Regular | 본문 설명, 팟캐스트명, 메타데이터 |
| `labelLarge/Medium/Small` | Regular | `labelLarge` = 하단 네비 라벨 · `labelMedium` = 부제/시간 · `labelSmall` = 칩/태그 |

**타입 위계 요약**: 화면 타이틀 `headlineMedium` → 섹션 헤더 `headlineSmall` → 서브섹션 `titleSmall` → 아이템 제목 `titleMedium/titleSmall` → 메타 `labelMedium/bodySmall`.

### 2.3 셰이프 (Shape)

`EpisodiveTheme`은 커스텀 `shapes`를 주입하지 **않으므로** `MaterialTheme.shapes.*`는 전부 **Material3 기본 토큰**입니다. 아래 값은 Material3 1.5.0-alpha10 `ShapeTokens`에서 직접 확인했습니다.

| 토큰 | Corner radius | 주 사용처 |
|:----|:----|:----|
| `extraSmall` | 4dp | 검색 결과 썸네일(40dp) |
| `small` | 8dp | 필터 칩, 드래그 핸들 배경 |
| `medium` | 12dp | 미니플레이어 카드, 카테고리 칩, 미니 썸네일(50dp) |
| `large` | 16dp | 검색바, 팔로우 버튼, 온보딩 다음 버튼 |
| `largeIncreased` | **20dp** | 에피소드 아이템/카드 썸네일(72dp), 진행중 카드 |
| `extraLarge` | 28dp | 팟캐스트/채널 그리드 커버, 정보 카드, 추천 카드 |
| `extraExtraLarge` | **48dp** | **앨범아트(풀 플레이어)·팟캐스트 헤더 커버** — 가장 큰 라운드 |

> 참고: `extraLargeIncreased` = 32dp도 존재하나 앱에서는 미사용. 커스텀 버튼은 `ButtonDefaults.shape`(M3 기본 = 완전 라운드 스타디움 형태)를 씁니다.

### 2.4 스페이싱 & 치수

| 상수 | 값 | 정의 |
|:----|:----|:----|
| 화면 좌우 인셋 | 16dp | 관례 |
| 섹션 헤더 패딩 | 16dp | `SectionHeader` |
| 캐러셀 아이템 간격 | 16dp (진행중 12dp) | `PodcastsSection`/`ChannelSection` |
| 미니플레이어 바 높이 | **70dp** | `DimensionTheme.playerBarHeight` (`theme/Theme.kt`) |
| 하단 네비게이션 바 높이 | 80dp *(M3 기본값)* | `NavigationBar` |
| 아이콘 기본 크기 | 24dp *(M3 기본값)* | `Icon` |
| 콘텐츠 하단 여백 | 70dp Spacer | 모든 스크롤 목록 끝(미니플레이어 가림 방지) |

`DimensionTheme`은 `LocalDimensionTheme` CompositionLocal로 제공되며, 현재 멤버는 `playerBarHeight` 하나입니다. 이 값(70dp)이 미니플레이어 높이이자 모든 목록 하단 Spacer의 기준입니다.

### 2.5 아이콘

`EpisodiveIcons` 오브젝트(`icon/EpisodiveIcons.kt`)가 앱 전역 아이콘을 이름으로 매핑합니다. 대부분 **Tabler Icons**를 `ImageVector`로 임베드했고, 다운로드 2종만 Material Icons입니다.

**시그니처 아이콘 선택**(일반적이지 않은 개성 있는 매핑):
- **Home** = `Blob`(둥근 얼룩) / HomeFilled = `BlobFilled`
- **Library** = `Fountain`(분수) / LibraryFilled = `FountainFilled`
- **Clip** = `Container` / ClipFilled = `ContainerFilled`
- **Search** = `Zoom` / SearchFilled = `ZoomFilled`
- Play/Pause = `PlayerPlay`/`PlayerPause`, Replay15 = `RewindBackward15`, Forward30 = `RewindForward30`
- Like = `HeartPlus`/`HeartFilled`, Follow = `UsersPlus`/`UsersMinus`, Save = `DeviceFloppy`
- Owner = `CreativeCommonsBy`, PublicationDate = `CalendarTime`, WorldShare(외부 링크)

하단 탭은 **비선택=아웃라인 / 선택=Filled** 쌍을 사용합니다.

### 2.6 모션

앱 전반의 애니메이션 스펙(소스 확인값):

| 위치 | 스펙 |
|:----|:----|
| 미니플레이어 등장/퇴장 | 아래→위 슬라이드, `tween(300)` |
| 필터 칩 색 전환 | `animateColorAsState` (기본 spec) |
| 검색바 접힘↔펼침 패딩 | `animateDpAsState` 16dp ↔ 0dp |
| 시커 썸 반경 | `animateDpAsState` 6dp ↔ 10dp (드래그 시 확대), gap 2dp↔4dp |
| 배속 다이얼 스냅 | `spring(dampingRatio = MediumBouncy, stiffness = Low)` |
| 배속 다이얼 fling | `exponentialDecay(frictionMultiplier = 3f)` → `spring(LowBouncy, Medium)` |
| 웨이브 애니메이션(재생 표시) | `infiniteRepeatable(tween(300~500ms 랜덤, Linear), Reverse)`, 바마다 100ms 위상차 |
| 페이드 탑바 제목 | `fadeIn()`/`fadeOut()` |
| 스크롤바 fade-out | Active→Inactive 후 2000ms 뒤 Dormant(Transparent), `SpringSpec(stiffness = Low)` |
| 카드 펼침/접힘 | `animateContentSize()` |
| 채널 헤더 패럴럭스 | 스크롤에 따라 alpha 감소 + scale 1.0→1.1 |

---

## 3. 시그니처 디자인 패턴

Episodive를 특징짓는 핵심 패턴들입니다.

### 3.1 팔레트 기반 다이내믹 컬러 (`StateImage`)

`component/Image.kt`의 `StateImage`가 이미지 로딩(로딩/에러/성공 상태 처리)과 함께 **AndroidX Palette로 대표색(dominant color)을 추출**합니다.

- 영역별 샘플링: `DominantRegion.Top/Bottom`(상·하단 10%), `Left/Right`(좌·우 10%), `Center`(중앙 50%×50%), `Full`(전체)
- `brightnessAdjustment`로 밝기 보정(음수=검정 보간으로 어둡게, 양수=흰색 보간으로 밝게)
- 색 추출이 필요하면 `allowHardware(false)`로 비트맵 접근 허용, 기본 요청 크기 300px, `ContentScale.Crop`
- 에러 시 `placeholderBrush`(기본 `secondaryContainer` 솔리드) 배경 + `PhotoExclamation` 폴백 아이콘

이 추출색이 **미니/풀 플레이어 배경, 팟캐스트 헤더 그라데이션, 채널 카드 하단 밴드, 위젯 배경**에 주입되어 "지금 보는 콘텐츠의 색으로 화면이 물드는" 경험을 만듭니다.

### 3.2 사선 이중 그라데이션 배경 (`EpisodiveGradientBackground`)

`component/Background.kt`. 수직축에서 **11.06° 기울어진** linearGradient 두 개를 겹칩니다.

- top 그라데이션: `0f → topColor`, `0.724f → Transparent` (상단에서 시작해 중반 이후 소멸)
- bottom 그라데이션: `0.2552f → Transparent`, `1f → bottomColor` (중반 전부터 등장)
- top 색으로 **앨범/커버 추출색**을 주입하는 패턴이 플레이어·팟캐스트·채널 헤더·온보딩에서 반복

### 3.3 페이딩 마퀴 텍스트 (`FadingEdgeText` + `fadingEdgeMarquee`)

`component/Text.kt` + `FadingMarquee.kt`. 긴 제목을 좌우 페이드(12dp) + 무한 마퀴로 흘립니다.

- `BlendMode.DstIn` 그라데이션 마스크로 좌우 12dp 페이드
- `basicMarquee(iterations = MAX, initialDelayMillis = 2000, repeatDelayMillis = 2000)` — 2초 지연 후 스크롤
- 앞뒤에 non-breaking space를 3개씩 붙여 반복 시 간격 확보
- 사용처: 플레이어 에피소드명·팟캐스트명, 미니플레이어 제목

### 3.4 스크롤 연동 페이드 탑바 (`FadeTopBarLayout`)

`component/Layout.kt`. 커버 이미지 위에 겹쳐진 상단바가 스크롤에 따라 페이드 인.

- 임계값 넘기 전: 상단바 투명(`surface.copy(alpha = 0f)`), 뒤로가기 버튼만 **반투명 원형 배경**(`onSurface.copy(alpha = 0.3f)`)
- 임계값(팟캐스트 900px, 채널 300px) 초과: 상단바 불투명 + 제목 `fadeIn`
- 사용처: 팟캐스트 상세, 채널 상세

### 3.5 가로 캐러셀 규칙

모든 `~Section`(가로 목록)의 공통 규칙:
- `LazyRow`, 아이템 간격 16dp, `contentPadding horizontal 16dp`
- `rememberSnapFlingBehavior(SnapPosition.Start)` — 항목 스냅
- `overscrollEffect = null` — 가장자리 stretch 움찔거림 제거 (코드에 반복 주석 존재)

### 3.6 오버레이 미니플레이어

미니플레이어는 하단 네비 바 위, NavHost 위에 **오버레이**됩니다(Scaffold의 `Box`에 NavHost와 함께 겹쳐 배치). 재생 중인 트랙이 있을 때만 아래→위 슬라이드로 등장하고, 목록들은 70dp 하단 여백으로 가림을 피합니다.

---

## 4. 컴포넌트 라이브러리 (`core:designsystem`)

접두사 `core/designsystem/src/main/kotlin/io/jacob/episodive/core/designsystem/component/` (별도 표기 없으면).

### 4.1 버튼 (`Button.kt`)

M3 3종 래퍼, 각각 `content` 슬롯 버전 + `text`/`leadingIcon` 편의 버전.

| 컴포넌트 | 색상 | 특징 |
|:----|:----|:----|
| `EpisodiveButton` | `ButtonDefaults.buttonColors()` (primary) | Filled |
| `EpisodiveOutlinedButton` | contentColor `onSurface`, 테두리 `1.dp` | Outlined |
| `EpisodiveTextButton` | contentColor `onBackground` | Text |

- leadingIcon 있으면 `ButtonWithIconContentPadding`, 아이콘 박스 `sizeIn(maxHeight = IconSize)`(18dp), 텍스트 앞 `IconSpacing`(8dp)
- 셰이프 기본 = `ButtonDefaults.shape` *(M3 기본, 완전 라운드)*

### 4.2 아이콘 버튼 (`IconButton.kt`)

| 컴포넌트 | 셰이프 | 색상 |
|:----|:----|:----|
| `EpisodiveIconToggleButton` | `CircleShape` | checked: 컨테이너 `primary`/콘텐츠 `onPrimary` · uncheck: 컨테이너 `onPrimary.copy(alpha 0.1f)`/콘텐츠 `onBackground` |
| `EpisodiveIconButton` | `CircleShape` | 투명 컨테이너, 콘텐츠 `onBackground` |
| `EpisodiveIconProgressButton` | `CircleShape` | 컨테이너 `primaryContainer`/콘텐츠 `primary`. 아이콘 위 `CircularProgressIndicator`(strokeWidth 2dp) — `isLoading` 시 무한 회전, 아니면 결정형 진행률 |

`EpisodiveIconProgressButton`은 **재생 버튼**(버퍼링/다운로드 진행률을 원형 링으로 표현)에 쓰입니다.

### 4.3 칩 & 태그

**`EpisodiveFilterChip`** (`Chip.kt`) — 셰이프 `small`(8dp), 라벨 `labelSmall`
- 컨테이너 색 `animateColorAsState`: 선택 `primaryContainer` / 미선택 `surfaceVariant` / 비활성 `Transparent`
- 사용처: 라이브러리 섹션 필터(All/Liked/Saved…), 온보딩 카테고리 선택

**`EpisodiveTopicTag`** (`Tag.kt`) — `TextButton` 기반, 라벨 `labelSmall`
- followed `primaryContainer` / unfollowed `surfaceVariant.copy(alpha 0.5f)`

### 4.4 탭 (`Tab.kt`)

`EpisodiveTab` + `EpisodiveTabRow` — 투명 배경, 텍스트 `labelLarge` 중앙정렬(상단 패딩 7dp), 인디케이터 `SecondaryIndicator` height 2dp / color `onSurface`.

### 4.5 검색바 (`SearchBar.kt`)

`EpisodiveSearchBar` — M3 `SearchBar`, 셰이프 `large`(16dp).
- 접힘↔펼침 전환: 좌우 패딩 `animateDpAsState` 16dp → 0dp(펼치면 전체폭)
- leading: 축소 `Search` / 확장 `ArrowBack`, trailing: query 있을 때 `Close`
- 스크롤 시작 시 키보드 자동 숨김

### 4.6 플레이어 컨트롤 컴포넌트

**`EpisodiveSeeker`** (`Seeker.kt`, 서드파티 `dev.vivvvek.seeker` 래핑) — 오디오 탐색 바
- 트랙 높이 **4dp**, 썸 반경 6dp→10dp(드래그 시), 챕터 간격(gap) 2dp→4dp
- 색: progress `primary`, readAhead(버퍼) `primaryContainer`, track `outline`, thumb `primary`
- **챕터를 `Segment`로 분할 표시**, currentSegment 변화 시 챕터명/인덱스 콜백
- `isControllable = false`면 높이 4dp 순수 진행바(thumb 투명) — 미니플레이어에서 사용

**`EpisodiveDial`** (`Dial.kt`) — 배속 선택 회전 다이얼(Canvas 커스텀)
- 높이 80dp, `range 0.5f..3.5f`, 30 steps, stepWidth 30dp
- 선택 눈금 3dp+`primary`, 미선택 2dp+페이드, 5스텝마다 숫자(12sp)
- 물리 기반: fling `exponentialDecay(friction 3f)` → `spring` 스냅
- 좌우 160dp 구간 선형 페이드
- ⚠️ 코드상 일부 좌표가 px/dp 혼용(y=70 등) — 정확한 픽셀 위치는 소스 참조

### 4.7 오버레이/시트 컴포넌트

| 컴포넌트 | 파일 | 사양 |
|:----|:----|:----|
| `EpisodiveSwipeDismissSnackbarHost` | `Snackbar.kt` | 세로 드래그로 dismiss(임계 80px) |
| `EpisodiveDragHandle` | `DragHandle.kt` | 40×4dp, `onSurfaceVariant`, `small`(8dp) 라운드, 상하 16dp 패딩 — 바텀시트 핸들 |
| `EpisodiveViewToggleButton` | `ViewToggle.kt` | 텍스트 `titleLarge` + 원형 38dp(`surfaceContainerHigh`) 배경 확장/축소 아이콘 |

### 4.8 이미지 / 로딩 / 애니메이션

| 컴포넌트 | 파일 | 사양 |
|:----|:----|:----|
| `StateImage` | `Image.kt` | Coil + Palette 대표색 추출 (§3.1). 로딩 중에는 `placeholderBrush`(팟캐스트별 시드색을 `surfaceContainerHigh` 쪽으로 섞은 그라디언트)를 깐다 — 스켈레톤과 톤을 맞춰 전환이 조용하다 |
| `LoadingWheel` | `LoadingWheel.kt` | `CircularProgressIndicator` 32dp / stroke 3dp, `primary` + `surfaceContainerHigh` 트랙. **인라인 전용** — 화면 전체 로딩에는 스켈레톤을 쓴다 |
| `ErrorScreen` | `screen/` | 화면 중앙 오류 텍스트 |

**스켈레톤 (`Skeleton.kt`)** — 콘텐츠 로딩의 기본 표현. 원형 스피너는 무엇이 올지 알려주지 않아 대기가 길게 느껴지고, 데이터가 도착하는 순간 화면이 통째로 바뀐다.

| 심볼 | 사양 |
|:----|:----|
| `Modifier.shimmerSweep` | 오프스크린 레이어 한 장 위를 `BlendMode.SrcAtop` 스윕. 띠 폭 0.6, 각도 18°, 1400ms. 하이라이트 `onSurface α0.07` (다크는 밝은 빛, 라이트는 어두운 빛) |
| `SkeletonContainer` | **화면당 하나.** 빛·등장 페이드(120ms 지연 후 220ms)·접근성(`clearAndSetSemantics` + "불러오는 중")을 묶는다 |
| `SkeletonBox` / `SkeletonLine` / `SkeletonCover` | 블록 / 텍스트 줄(자리는 `lineHeight`, 잉크는 72%) / 커버(`coverForSize` 사다리) |

제약: 컨테이너에 배경을 칠하면 레이어 전체가 균일하게 쓸린다 — 배경은 바깥에서. 스크롤 리스트 전체에 걸면 스크롤마다 레이어가 무효화된다. 도메인 카드 스켈레톤은 `core/ui` 의 실제 컴포넌트 바로 아래에 둔다(치수 드리프트 방지).
| `WaveAnimationIcon` | `AnimationIcon.kt` | 5개 막대(각 2dp, 16dp), 300~500ms 랜덤 웨이브, 100ms 위상차. 재생 파형 표시 |
| `ClipAnimationIconText` | `IconText.kt` | 반투명 pill(`onBackground alpha 0.3f`, CircleShape) 안에 웨이브+시간 |

### 4.9 스크롤바 (`scrollbar/`)

Now in Android 방식 커스텀 스크롤바.
- Draggable thumb 12dp / Decorative thumb 2dp, 셰이프 `RoundedCornerShape(16dp)`
- 색 상태: Active `onSurface.copy(0.5f)` → Inactive `onSurface.copy(0.2f)` → 2초 후 Dormant(Transparent)
- 트랙 롱프레스 스크롤, 드래그 fast-scroll 지원
- 사용처: 온보딩, 검색, 팟캐스트 상세(우측 `DraggableScrollbar`)

### 4.10 레이아웃 & 앱바

| 컴포넌트 | 파일 | 사양 |
|:----|:----|:----|
| `EpisodiveScaffold` | `Layout.kt` | 타이틀 `headlineMedium` 상단바 + subTitle 슬롯, 네비바 inset 제외 |
| `SectionHeader` | `Layout.kt` | 헤더 Row 좌우 `screenPadding`(20dp), 제목 `titleMedium`, 우측 옵션 액션(48dp), 하단 14dp Spacer. 액션이 붙으면 헤더 높이가 그만큼 커지므로 로딩 자리는 `SectionHeaderSkeleton(hasAction = true)` 로 같은 자리를 예약 |
| `SubSectionHeader` | `Layout.kt` | 제목 `titleSmall`/`onSurfaceVariant` |
| `FadeTopBarLayout` | `Layout.kt` | 스크롤 연동 페이드 탑바 (§3.4) |
| `EpisodiveTopAppBar` / `EpisodiveCenterTopAppBar` | `TopAppBar.kt` | 좌측/중앙 정렬 상단바, 아이콘 tint `onSurface` |
| `EpisodiveBackground` | `Background.kt` | `LocalBackgroundTheme` 기반 앱 배경 Surface |
| `HtmlTextContainer` | `HtmlTextContainer.kt` | 팟캐스트 설명 HTML 파싱 + 이메일/URL/전화 자동 링크(`primary` 밑줄) |

### 4.11 하단 네비게이션 바 (`Navigation.kt`)

`EpisodiveNavigationBar` + `EpisodiveNavigationBarItem` — M3 `NavigationBar`, `tonalElevation = 0dp`.

| 상태 | 색상 |
|:----|:----|
| 선택 아이콘·라벨 | `onPrimaryContainer` |
| 비선택 아이콘·라벨 | `onSurfaceVariant.copy(alpha = 0.6f)` |
| 인디케이터(pill) | `primaryContainer` |

라벨 `labelLarge`, 선택 시 Filled 아이콘으로 교체. 높이·아이콘 크기는 M3 기본값(80dp / 24dp).

---

## 5. 도메인 UI 카탈로그 (`core:ui`)

콘텐츠 카드/아이템 컴포넌트. 접두사 `core/ui/src/main/kotlin/io/jacob/episodive/core/ui/`.

### 5.1 팟캐스트 (`Podcast.kt`)

| 컴포넌트 | 형태 | 썸네일 | 셰이프 | 텍스트 | 액션 |
|:----|:----|:----|:----|:----|:----|
| `PodcastItem` | 세로 카드 | 정사각(width 140dp) | `extraLarge`(28dp) | 제목 `bodyMedium` 2줄 + 부제 `labelMedium` 1줄 | 없음 |
| `PodcastDetailItem` | 가로 상세 행 | 96dp | `extraLarge` | 제목 `titleMedium` + 메타 FlowRow(owner/발행일/에피소드수) + 설명 4줄 | 팔로우 토글(34dp) |
| `PodcastSimpleItem` | 검색결과 행 | 50dp | `medium`(12dp) | 제목 `titleSmall` + 저자 `labelSmall` | 팔로우 Outlined 버튼 |

- `PodcastItem`은 텍스트 영역 최소높이를 폰트 라인하이트로 동적 계산해 아이템 높이를 정렬(`rememberPodcastTextSectionMinHeight`)

### 5.2 에피소드 (`Episode.kt`)

| 컴포넌트 | 형태 | 크기 | 특징 |
|:----|:----|:----|:----|
| `EpisodeItem` | 리스트 행 | height 72dp, 썸네일 72dp(`largeIncreased`) | 우측 세로 2버튼: 재생 진행 버튼(32dp) + 좋아요 토글(32dp) |
| `PlayingEpisodeItem` | 진행중 카드 | 192×84dp(`largeIncreased`, `onSurface 10%` 배경) | 하단 진행바 `LinearProgressIndicator` 4dp(gapSize -4dp) |
| `PlayedEpisodeItem` | 히스토리 행 | 썸네일 68dp | 진행률 바(완료 `onSurface`/진행중 `primary`) + 퍼센트 |
| `EpisodeDetailItem` | 추천 세로 카드 | width 200dp, 썸네일 200dp(`extraLarge`) | 설명 4줄 |
| `EpisodeClipItem` | 전체화면 클립 | 커버 250dp Card(elevation 24dp) | 배경 blur(20dp)+60% 오버레이, 좋아요·재생 토글 |

### 5.3 채널 / 카테고리 / 챕터

- **`ChannelItem`** (`Channel.kt`): width 250dp, 정사각 이미지 + 하단 80dp 색 밴드(**Bottom 추출색 `alpha 0.5`**) 위 설명 3줄 중앙정렬
- **`CategoryItem`** (`Category.kt`): `Surface` 칩, 셰이프 `medium`(12dp), `surfaceVariant`, 라벨 `bodyMedium`
- **`ChapterItem`** (`Chapter.kt`): 플레이어 챕터 행 — 인디케이터(선택 시 `primary`) + 제목 `bodyMedium` + 시작시각 `primary`, 하단 `HorizontalDivider`(선택 시 primary)

`PodcastItem`·`ChannelItem` 은 기본 폭(각 118dp·250dp)을 `modifier` 보다 **앞**에 두어, 그리드처럼
폭을 바깥에서 정하는 호출부가 `fillMaxWidth` 로 덮어쓸 수 있다.

### 5.4 섹션 컨테이너

`SectionHeader`(§4.10) + 항목 나열을 묶은 래퍼. 홈·검색·보관함이 공유한다.

| 컴포넌트 | 파일 | 항목 배치 |
|:----|:----|:----|
| `PodcastsSection` | `Podcast.kt` | 가로 `LazyRow`(스냅, `carouselSpacing`) |
| `EpisodesSection` | `Episode.kt` | 세로 `Column`(`listItemSpacing`) |
| `ChannelSection` | `Channel.kt` | 가로 `LazyRow`(스냅, `gridSpacing`) |

**`onMore` 계약**: `(() -> Unit)?` 이며 **null 이면 더보기 아이콘을 그리지 않는다.** 어포던스
유무를 콜백 유무로만 결정해서, 갈 곳이 없는 화면(검색 결과)에 눌러도 아무 일이 없는
버튼이 생기지 않게 한다. 아이콘은 `EpisodiveIcons.CaretRight`, contentDescription 은
`core_ui_section_more_format`("See all %1$s")로 섹션 제목을 담는다 — 한 화면에 더보기가
여럿이라 제목이 없으면 스크린리더에서 구분되지 않는다.

**클릭 영역은 헤더 행 전체**다(§4.10). 화살표만 눌리면 화면에서 가장 크고 먼저 눈에 들어오는
제목이 죽은 영역이 되고, 사용자는 그걸 몇 번 눌러 본 뒤에야 옆의 작은 아이콘을 찾는다.

목적지는 화면마다 다르다. 홈은 전용 화면(`HomeMoreRoute`)으로 나가지만, **보관함은 상단
필터 칩을 그 탭으로 옮길 뿐 화면을 이동하지 않는다** — 각 섹션의 전체 목록이 이미 그 탭이다.

각 섹션의 로딩 자리(`*SectionSkeleton`)는 `hasAction` 을 그대로 넘겨 헤더 높이를 맞춘다.

---

## 6. 앱 셸 & 네비게이션

### 6.1 진입 & 초기화 (`MainActivity`)

- `installSplashScreen()` → `enableEdgeToEdge()` (엣지투엣지)
- 스플래시: **SplashScreen API**, `windowSplashScreenAnimatedIcon = ic_launcher_foreground`. 데이터 준비까지 유지(`shouldKeepSplashScreen`). 라이트에서 밝은 상태바/네비바, 다크에서 반대
- `MediaController` 비동기 바인딩(미니플레이어·알림 제어), 딥링크·위젯 autoplay 처리
- 최상위: `EpisodiveTheme { EpisodiveApp(appState) }`

### 6.2 최상위 Scaffold (`EpisodiveApp`)

- **온보딩 분기**: 첫 실행이면 `OnboardingRoute`만 전체화면(Scaffold 미사용)
- **Scaffold**:
  - `bottomBar` = `EpisodiveNavigationBar`
  - `contentWindowInsets`에서 상태바 inset 제외 → 콘텐츠가 상태바 뒤까지 확장(edge-to-edge)
  - 본문 `Box`에 **`EpisodiveNavHost` + `PlayerBar`(미니플레이어)를 겹쳐 배치**
  - 스낵바는 미니플레이어 위(`padding bottom = 70dp`)에 표시, 스와이프 dismiss
- Android 13+ `POST_NOTIFICATIONS` 런타임 요청

### 6.3 네비게이션 (Navigation3)

전통적 Navigation-Compose가 아니라 **`androidx.navigation3`**(NavDisplay + entryProvider)를 사용합니다.

- **하단 4탭**: Home(시작) / Search / Library / Clip — 각 `selectedIcon`/`unselectedIcon`/`navKey`
- **멀티 백스택**: 탭별 독립 `NavBackStack` 맵. 탭 재탭 시 해당 스택 루트로 복귀
- 상세 화면: `PodcastRoute(id)`, `ChannelRoute(id)`는 현재 탭 스택에 push
- `SaveableStateHolder` + `ViewModelStore` 데코레이터로 탭 상태 보존

### 6.4 미니플레이어 (`PlayerBar`)

- `Card`, 높이 **70dp**, 셰이프 `medium`(12dp), `padding(horizontal 6dp, bottom 6dp)`, elevation 4dp
- **배경색 = 앨범아트 Top 추출색**(brightnessAdjustment -0.5f, 더 어둡게)
- 내부: 썸네일 50dp + 제목/팟캐스트명(FadingEdgeText) + 좋아요 토글(32dp) + 재생 토글(primary)
- 하단에 `EpisodiveSeeker`(`isControllable = false`, 높이 4dp 순수 진행바)
- 탭하면 풀스크린 플레이어(`PlayerBottomSheet`) 펼침

---

## 7. 화면별 명세

> 화면 타이틀은 로컬라이즈됩니다(기본 영문 / `values-ko` 한글 제공). 예: Home→홈, Search→검색, Library→보관함, Clip→클립. 아래는 한글 값 기준입니다.

### 7.1 플레이어 (`feature:player`) — 크라운 주얼

**풀스크린 플레이어**(`PlayerScreen`): `ModalBottomSheet`(`skipPartiallyExpanded = true`, dragHandle 없음, scrim 투명)로 표시. 본문은 단일 `LazyColumn`(`overscrollEffect = null`):

1. **히어로 블록**(화면 높이 92%, `EpisodiveGradientBackground` top=앨범아트 추출색):
   - 상단바: 좌 `CaretDown`(접기), 우 좋아요 토글, 배경 투명
   - **앨범아트**: `padding(horizontal 24dp)` 정사각, `StateImage` size=600px, 셰이프 `extraExtraLarge`(48dp), Top 추출색·brightness -0.2f. 위아래 weight로 수직 중앙
   - **PushUpCue**: 아트 하단에 transcript 자막을 아래→위 슬라이드+페이드(`AnimatedContent` tween 300)
   - 텍스트: 팟캐스트명 `bodyLarge`/`onSurfaceVariant` + 에피소드명 `titleLarge`/`onSurface` (둘 다 FadingEdgeText 1줄, 중앙)
   - **시크바 영역**: `EpisodiveSeeker` + 3열(현재위치 55dp | 챕터명 중앙 | 전체길이 55dp, `labelMedium`)
   - **컨트롤 패널**:
     - 1행: Replay15(48dp/아이콘32dp) · SkipPrevious(48/40) · **재생·정지 토글(68dp 원형/아이콘36dp, `onBackground` 배경)** · SkipNext · Forward30
     - 2행: 배속 텍스트버튼(`"1.0x"` titleLarge) · 슬립타이머(32dp, 임박 시 `primary`로 lerp) · 저장/다운로드 토글(진행 시 진행률 링) · 재생목록(32dp)
2. **에피소드 정보 카드**: `Card`(`extraLarge`, animateContentSize), `EpisodiveViewToggleButton` 펼침/접힘, HTML 설명(접힘 3줄)
3. **챕터 카드**(있을 때): 접힘 시 현재 챕터 주변 최대 5개, 초과 시 더보기
4. **팟캐스트 정보 카드**: HTML 설명 + `PodcastSimpleItem`
5. 하단 Spacer 50dp

**중첩 바텀시트 3종**(모두 `ModalBottomSheet` + `EpisodiveDragHandle`):
- **배속**: 현재 배속(`headlineMedium`) + `EpisodiveDial` + 프리셋 원형 5개(0.5/1/1.5/2/3.5x, 48dp)
- **재생목록**: 부분 확장 가능, `LazyColumn`(간격 16dp), 현재 곡 로딩 표시
- **슬립타이머**: 타이머(`m:ss`) + 프리셋 FlowRow(30s~60분, 48dp 원형) + "에피소드 끝까지" + 취소(errorContainer)

### 7.2 홈 (`feature:home`)

**`BottomSheetScaffold` 2단 레이아웃** — 화면을 (a) 고정 상단 + (b) 드래그 가능한 바텀시트로 분리.

- peek 높이를 화면·상단바·content 높이 측정으로 **동적 계산**, `rememberStandardBottomSheetState(PartiallyExpanded, skipHiddenState)`
- 상단바: 타이틀 "홈" `headlineMedium`(pinned)
- 고정 영역: 재생중이던 에피소드가 있으면 `PlayingEpisodesSection`("계속 듣기")
- 바텀시트 본문(`LazyColumn`, 드래그 핸들 = `EpisodiveDragHandle`) 섹션 순서:

| # | 섹션 | 컴포넌트 | 더보기 목적지 |
|:--|:----|:----|:----|
| 1 | 내가 최근 들은 | `PodcastsSection` (가로) | 팟캐스트 그리드 |
| 2 | 랜덤 에피소드 | `EpisodesSection` (세로) | 에피소드 리스트 |
| 3 | 내 트렌딩 피드 | `PodcastsSection` | 팟캐스트 그리드 |
| 4 | 구독 팟캐스트 | `PodcastsSection` | 팟캐스트 그리드 |
| 5 | 국내 인기 | `PodcastsSection` | 팟캐스트 그리드 |
| 6 | 해외 인기 | `PodcastsSection` | 팟캐스트 그리드 |
| 7 | 라이브 에피소드 | `EpisodesSection` | 에피소드 리스트 |
| 8 | 채널 | `ChannelSection` | 채널 그리드 (비페이징) |

1~6번 뒤에만 `HorizontalDivider`(16dp 인셋). 끝에 70dp Spacer.

3번과 5번은 조회 조건이 다르다 — 3번은 사용자 언어 + 관심 카테고리, 5번은 언어만(카테고리 없음).

#### 홈 더보기 (`HomeMoreRoute`)

섹션 헤더 우측 `CaretRight` 를 누르면 그 섹션의 전체 목록으로 이동한다. 화면은 하나이고
`HomeSection` 인자로 세 레이아웃을 분기한다.

- 상단바는 `FadeTopBarLayout` 이 아니라 `EpisodiveScaffold` — 히어로 없이 첫 줄부터 목록이라
  "무엇의 목록인지"가 처음부터 보여야 한다. 제목은 홈 섹션 제목을 그대로 쓴다.
- 팟캐스트·채널은 2열 그리드(`gridSpacing`), 에피소드는 세로 리스트(`listItemSpacing`).
  좌우 `screenPadding`, 하단은 `playerBarSpace` 만큼 비워 미니플레이어가 마지막 항목을 가리지 않는다.
- 팟캐스트·에피소드는 Paging, 채널은 비페이징(`ChannelRepository` 에 PagingSource 가 없다).
- 오류 시 재시도 버튼을 둔다 — 첫 페이지가 50~100건이라 실패 비용이 크다.
- 홈 미리보기와는 별도 캐시 그룹을 쓴다(`QueryScope.FULL`). 한 그룹을 공유하면 먼저 캐시를
  채운 쪽의 개수에 갇힌다.

### 7.3 온보딩 (`feature:onboarding`)

`HorizontalPager`(**`userScrollEnabled = false`**, 버튼으로만 진행) 4페이지:
- **Welcome**: 그라데이션 배경 + 일러스트(`undraw_relax_mode`) + 타이틀 `titleLarge`
- **카테고리 선택**: `LazyColumn` + `FlowRow` 필터 칩(가로 12dp/세로 8dp)
- **팟캐스트 선택**: Paging 추천을 `PodcastDetailItem`으로 나열, 팔로우 오버레이
- **완료**: 일러스트(`undraw_to_the_moon`) + `LinearWavyProgressIndicator` + 자동 진입
- 하단 고정 CTA: `EpisodiveGradientBackground`(bottom=primaryContainer) + PagerIndicator(현재 24dp 알약/나머지 8dp 원) + 다음 버튼(셰이프 `medium`)

### 7.4 검색 (`feature:search`)

`EpisodiveScaffold`(타이틀 "검색") + `EpisodiveSearchBar`.
- **접힘**: 글로벌 인기 피드(가로 캐러셀) + 글로벌 최근 에피소드(세로)
- **펼침**: 검색 결과(팟캐스트 섹션 + 에피소드 목록) 또는 **최근 검색 기록**(Query/Podcast/Episode 3타입, 개별·전체 삭제)

### 7.5 라이브러리 (`feature:library`)

`EpisodiveScaffold`(타이틀 "보관함" / en "Library"), subTitle에 **필터 칩 줄 ↔ 검색바** `AnimatedContent` 전환(`FindOrFilter`).
- **탭 = 필터 칩**(가로): All / RecentlyListened / Liked / Saved / Followed / Preferred
- **All**: 요약 섹션들을 가로 캐러셀로(최근청취 250dp / 좋아요·저장 `EpisodeDetailItem` / 구독 / 선호 카테고리)
- **개별 탭**: **Paging + 날짜 구분자**(`SeparatedUiModel` — surface 배경 날짜 헤더 `titleMedium`), 아이템 `animateItem()`
- **Preferred**: `FlowRow` 카테고리 칩 전체
- **섹션 더보기**: All 의 섹션 헤더(제목 포함 전체가 클릭 영역)를 누르면 상단 필터가 그 탭으로
  옮겨간다. 홈과 달리 **새 화면으로 나가지 않는다** — 전체 목록이 이미 그 탭이다
- **뒤로가기**: All 이 아닌 탭에서는 `BackHandler` 가 가로채 All 로 되돌린다. 더보기로 들어간
  탭은 화면이 아니라 필터라, 놔두면 방금 좁힌 목록이 아니라 보관함 자체를 벗어난다

### 7.6 팟캐스트 상세 (`feature:podcast`)

`FadeTopBarLayout`(offset 900px) — 스크롤 시 상단바 페이드 인.
- **헤더**: `EpisodiveGradientBackground`(top=커버 추출색) + 커버 220dp(`extraExtraLarge`, brightness -0.2f) + 저자 `titleMedium`/secondary + 제목 `headlineSmall` + **팔로우 버튼**(셰이프 `large`) + HTML 설명
- 구분선 → "전체 N개 에피소드" `titleLarge` → **에피소드 목록**(Paging, `EpisodeItem`). 클릭 시 보이는 목록 스냅샷을 재생 큐로 전달
- 우측 `DraggableScrollbar`

### 7.7 클립 (`feature:clip`) — 틱톡형 세로 피드

**`VerticalPager`**(세로 스와이프, Paging 연동, `pageSpacing 32dp`, `contentPadding vertical 80dp/horizontal 24dp`로 위아래 peek).
- 자동화: 첫 진입 자동재생 · settledPage 변경 시 재생 · `ENDED` 시 다음 페이지 auto-scroll · lifecycle 연동 재생/정지
- `EpisodeClipItem`: blur(20dp) 배경 + 250dp 커버 Card(elevation 24dp) + 제목 3줄/설명 6줄 + 하단(남은시간 웨이브 + 좋아요·재생 토글)

### 7.8 채널 (`feature:channel`)

`FadeTopBarLayout`(offset 300px) + 배경 채널 이미지 **패럴럭스**(스크롤 시 alpha↓ + scale 1.0→1.1).
- **`LazyVerticalGrid` `Fixed(2)`** — 팟캐스트 2열 그리드
- 헤더(full-span): 그라데이션 배경 + 채널명 `headlineSmall` + "N개 팟캐스트"
- 셀: `Box`(surface, padding 16dp) 안에 `PodcastItem`(fillMaxWidth)
- 푸터: 소개 + 웹사이트 링크(`WorldShare` 아이콘, `primary`, 외부 브라우저)

---

## 8. 홈스크린 위젯 (`feature:widget`)

**Glance(App Widget)** 기반 재생+피드 위젯. 앱 컬러 스킴을 그대로 브릿지해 위젯에서도 브랜드 레드가 유지됩니다.

- **테마**(`theme/EpisodiveGlanceTheme.kt`): core/designsystem의 Material3 라이트/다크 스킴을 Glance `ColorProviders`로 bridge. Glance가 노출 안 하는 확장 토큰(`surfaceContainer` 등)은 `ColorProvider(day, night)`로 별도 브릿지
- **배경**: 아트워크 추출색 **솔리드**, 16dp 라운드(그라데이션/스크림 없음). 피드 영역만 더 어두운 추출색
- **적응형 레이아웃**(`EpisodiveWidgetLayout`, `SizeMode.Exact` 측정):

| 높이 | 모드 | 피드 표현 |
|:----|:----|:----|
| 1행 | `NONE` | now-playing만 (제목 1줄) |
| 2행 | `STRIP` | 썸네일 1행(3열 4개 / 4열 5개), 썸네일 56dp |
| 3행 | `GRID` | 2×N 그리드(3열 6개 / 4열 8개), 썸네일 44~80dp 가변 |

- **now-playing 헤더**: 썸네일(56~96dp 가변, 항상 피드보다 큼) + 제목(14sp Bold White, 1~2줄) + 부제(12sp Medium) + 컨트롤 행(되감기·빨리감기 34dp + 재생/정지 34dp), 우상단 브랜드 배지
- 그리드 마진 14dp, 비트맵 페이로드는 RemoteViews ~1MB Binder 한도 내로 px 상한 계산
- **로딩**: 추출 배경색 위 흰색 `CircularProgressIndicator`
- 탭 동작: 재생 정보 있으면 플레이어 화면(open_player), 없으면 앱만 열기

---

## 9. 접근성 & 공통 UX 참고

- **Pull-to-refresh 없음** — 별도 새로고침 제스처 미사용(Offline-First + 캐시 TTL로 자동 갱신)
- **Paging3 무한스크롤**: 팟캐스트 에피소드, 라이브러리 탭, 온보딩 추천, 클립
- **되돌리기 스낵바**: 좋아요/저장 취소를 `onShowSnackbar` undo로 제공(홈·라이브러리·팟캐스트·플레이어)
- **오프라인 배너**: 미연결 시 하단 스낵바 "⚠️ You aren't connected to the internet"
- **엣지투엣지**: 전 화면 `enableEdgeToEdge()`, 상태바/네비바 뒤까지 콘텐츠 확장
- **다크 모드**: 시스템 설정 자동 추종(`isSystemInDarkTheme()`), 미리보기는 다크 우선(`DevicePreviews`)

---

## 10. 파일 맵 (부록)

| 영역 | 경로 |
|:----|:----|
| 색상 토큰 | `core/designsystem/theme/Color.kt` |
| 테마 조립 | `core/designsystem/theme/Theme.kt` |
| 타이포 | `core/designsystem/theme/Type.kt` |
| 치수 | `core/designsystem/theme/Dimension.kt` |
| 그라데이션/배경 | `core/designsystem/theme/Gradient.kt`, `component/Background.kt` |
| 아이콘 | `core/designsystem/icon/EpisodiveIcons.kt` (+ `icon/tabler/`) |
| 디자인시스템 컴포넌트 | `core/designsystem/component/*.kt` |
| 도메인 UI 카드 | `core/ui/{Podcast,Episode,Channel,Category,Chapter}.kt` |
| 앱 셸 | `app/MainActivity.kt`, `app/ui/EpisodiveApp.kt` |
| 네비게이션 | `app/navigation/{BottomBarDestination,EpisodiveNavHost,EpisodiveNavigationState,EpisodiveNavigator}.kt` |
| 화면 | `feature/{onboarding,home,search,library,podcast,player,clip,channel}/…Screen.kt` |
| 미니플레이어 | `feature/player/PlayerBar.kt` |
| 위젯 | `feature/widget/…`, `feature/widget/theme/EpisodiveGlanceTheme.kt` |
| 스플래시/브랜딩 | `app/res/values/themes.xml`, `app/res/values/strings.xml` |

---

*이 문서는 코드베이스(2026-07 기준) 분석으로 작성됐습니다. 수치·색상은 소스 확인값이며, "M3 기본값"으로 표기한 항목(네비바 높이 80dp, 아이콘 24dp 등)은 Material3 라이브러리 디폴트에 의존합니다.*
