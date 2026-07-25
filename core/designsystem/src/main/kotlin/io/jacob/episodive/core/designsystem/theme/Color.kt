package io.jacob.episodive.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Episodive v2 — warm near-black 팔레트
//
// 디자인 원본(design/Episodive-v2.dc.html)의 CSS 변수를 그대로 옮긴다.
//   --bg  #16110E   --surf #1E1714   --card #271E1A
//   --fg  #F4EAE6   --mut  #A2938D
//   --red #F5372B   --line rgba(255,255,255,.07)
// 다크가 기준 테마다. 라이트는 디자인에 정의가 없어 기존 값을 유지하되
// 브랜드 primary만 v2 레드로 맞춘다.
// ---------------------------------------------------------------------------

// 브랜드 상수 — M3 롤에 담기지 않는 값들 (그라디언트, 미니플레이어 등에서 직접 참조)
val EpisodiveRed = Color(0xFFF5372B)
val EpisodiveRedDim = Color(0xFF4A1A14)
val EpisodiveRedContainer = Color(0xFF8B1E1A)
val EpisodiveOnRedContainer = Color(0xFFFFDAD6)
val EpisodiveRedLight = Color(0xFFFF9A90)

// 미니플레이어 / 이어듣기 히어로 그라디언트 양 끝
val EpisodiveHeroGradientStart = Color(0xFF5A271D)
val EpisodiveHeroGradientEnd = Color(0xFF2B1712)

// Red Primary - #F5372B 기반
val primaryLight = Color(0xFFF5372B)
val onPrimaryLight = Color(0xFFFFFFFF)
val primaryContainerLight = Color(0xFFFFDAD6)
val onPrimaryContainerLight = Color(0xFF410002)

// Secondary - Red의 변형
val secondaryLight = Color(0xFFD63C2F)
val onSecondaryLight = Color(0xFFFFFFFF)
val secondaryContainerLight = Color(0xFFFFE0DE)
val onSecondaryContainerLight = Color(0xFF370B09)

// Tertiary - Red-Orange 보완색
val tertiaryLight = Color(0xFFE65100)
val onTertiaryLight = Color(0xFFFFFFFF)
val tertiaryContainerLight = Color(0xFFFFDBCC)
val onTertiaryContainerLight = Color(0xFF2D0900)

val errorLight = Color(0xFFBA1A1A)
val onErrorLight = Color(0xFFFFFFFF)
val errorContainerLight = Color(0xFFFFDAD6)
val onErrorContainerLight = Color(0xFF410002)

// Neutral - Light Background 계열
val backgroundLight = Color(0xFFFFFBFF)
val onBackgroundLight = Color(0xFF201A1A)
val surfaceLight = Color(0xFFFFFBFF)
val onSurfaceLight = Color(0xFF201A1A)
val surfaceVariantLight = Color(0xFFF5DDDA)
val onSurfaceVariantLight = Color(0xFF534341)

val outlineLight = Color(0xFF857370)
val outlineVariantLight = Color(0xFFD8C2BE)
val scrimLight = Color(0xFF000000)
val inverseSurfaceLight = Color(0xFF362F2F)
val inverseOnSurfaceLight = Color(0xFFFBEEEE)
val inversePrimaryLight = Color(0xFFFFB3A6)

val surfaceDimLight = Color(0xFFE8D6D5)
val surfaceBrightLight = Color(0xFFFFFBFF)
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = Color(0xFFFFF0EF)
val surfaceContainerLight = Color(0xFFF9EAEA)
val surfaceContainerHighLight = Color(0xFFF3E4E4)
val surfaceContainerHighestLight = Color(0xFFEDDEDE)


// --- Dark: 디자인 원본 값 ---

// primary = --red / container = nav pill·재생 버튼 배경 (#8B1E1A)
val primaryDark = Color(0xFFF5372B)
val onPrimaryDark = Color(0xFFFFFFFF)
val primaryContainerDark = Color(0xFF8B1E1A)
val onPrimaryContainerDark = Color(0xFFFFDAD6)

// secondary — 선택된 칩과 nav pill이 primaryContainer와 같은 톤을 쓴다
val secondaryDark = Color(0xFFFF9A90)
val onSecondaryDark = Color(0xFF2B0402)
val secondaryContainerDark = Color(0xFF8B1E1A)
val onSecondaryContainerDark = Color(0xFFFFDAD6)

// tertiary — 강조 라벨("이어 듣기", 진행자명, 재생 중 표시)
val tertiaryDark = Color(0xFFFF9A90)
val onTertiaryDark = Color(0xFF2B0402)
val tertiaryContainerDark = Color(0xFF5A271D)
val onTertiaryContainerDark = Color(0xFFFFDAD6)

// error — 오프라인 배너 (bg #3A1A17 / text #FF9A90)
val errorDark = Color(0xFFFF9A90)
val onErrorDark = Color(0xFF2B0402)
val errorContainerDark = Color(0xFF3A1A17)
val onErrorContainerDark = Color(0xFFFF9A90)

// Neutral - warm near-black 엘리베이션 사다리
//
// 원본 CSS 는 R 이 B 보다 8~13 높은 웜 뉴트럴이라 화면 전체에 붉은 기가 돈다.
// 브랜드 레드(primary)와 겹쳐 배경까지 붉게 보이므로, 색조는 유지하되 R-B 격차를
// 대략 절반으로 줄여 중성 웜그레이에 가깝게 낮춘다. 밝기(평균 채널값)는 그대로다.
val backgroundDark = Color(0xFF14100F)          // --bg  (원본 #16110E)
val onBackgroundDark = Color(0xFFEFEAE8)        // --fg  (원본 #F4EAE6)
val surfaceDark = Color(0xFF14100F)             // --bg
val onSurfaceDark = Color(0xFFEFEAE8)           // --fg
val surfaceVariantDark = Color(0xFF24201D)      // --card (원본 #271E1A)
val onSurfaceVariantDark = Color(0xFF9A928E)    // --mut  (원본 #A2938D)

val outlineDark = Color(0xFF9A928E)
val outlineVariantDark = Color(0x12FFFFFF)      // --line: rgba(255,255,255,.07)
val scrimDark = Color(0xFF000000)
val inverseSurfaceDark = Color(0xFFEFEAE8)
val inverseOnSurfaceDark = Color(0xFF14100F)
val inversePrimaryDark = Color(0xFFF5372B)

val surfaceDimDark = Color(0xFF110F0E)
val surfaceBrightDark = Color(0xFF302B29)
val surfaceContainerLowestDark = Color(0xFF110F0E)   // 클립 화면 nav
val surfaceContainerLowDark = Color(0xFF1B1817)      // --surf: nav bar, 컴포넌트 카드
val surfaceContainerDark = Color(0xFF211F1E)         // 바텀시트
val surfaceContainerHighDark = Color(0xFF24201D)     // --card: 칩, 필드, 그리드 카드
val surfaceContainerHighestDark = Color(0xFF302B29)  // 스낵바
