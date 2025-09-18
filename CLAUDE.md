# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Episodive is an Android podcast application built with Kotlin and Jetpack Compose that uses the Podcast Index API. The app follows modern Android development practices with a multi-module architecture using Gradle convention plugins.

## Build Commands

### Core Commands
- `./gradlew build` - Build all modules
- `./gradlew test` - Run unit tests for all modules
- `./gradlew check` - Run all verification tasks (tests, lint, etc.)
- `./gradlew testDebugUnitTest` - Run debug unit tests only
- `./gradlew connectedAndroidTest` - Run instrumentation tests on connected devices

### Module-specific Testing
- `./gradlew :core:database:test` - Test database module only
- `./gradlew :core:network:test` - Test network module only
- `./gradlew :core:data:test` - Test data module only
- `./gradlew :core:domain:test` - Test domain module only

### Code Quality
- `./gradlew lint` - Run lint analysis
- `./gradlew lintFix` - Auto-fix lint issues where possible

### Coverage
- `./gradlew createDebugCoverageReport` - Generate test coverage reports (uses Jacoco)

## Architecture

### Module Structure
The project follows a clean architecture approach with the following core modules:

- **`:app`** - Main Android application module with Compose UI
- **`:core:model`** - Domain models and data classes (Podcast, Episode, Category, etc.)
- **`:core:domain`** - Business logic interfaces and repository contracts
- **`:core:data`** - Repository implementations coordinating between network and database
- **`:core:network`** - Remote data sources using Retrofit for Podcast Index API
- **`:core:database`** - Local storage using Room database with DAOs and entities
- **`:core:designsystem`** - Shared UI components and theming
- **`:core:testing`** - Shared test utilities and test data

### Key Architecture Patterns

**Data Flow**: Remote API → Network DataSource → Repository → Domain → UI
**Local Storage**: Room Database with TypeConverters for complex types
**Dependency Injection**: Hilt for DI across all modules
**Testing**: Robolectric for database tests, MockK for mocking, Turbine for Flow testing

### Build Logic
Uses Gradle convention plugins in `build-logic/convention/` for consistent module configuration:
- `episodive.android.library` - Standard Android library setup
- `episodive.android.room` - Room database configuration
- `episodive.android.hilt` - Hilt dependency injection
- `episodive.android.test` - Test dependencies and configuration
- `episodive.android.library.jacoco` - Code coverage setup

### Important Implementation Details

**Enum Handling**: The `Medium` enum uses value properties (e.g., `Podcast("podcast")`) and requires custom TypeConverters in Room that convert using `entries.find { it.value == stringValue }` rather than `valueOf()`.

**Database Entities**: Main entities include `PodcastEntity`, `EpisodeEntity`, and user interaction entities like `FollowedPodcastEntity`, `LikedEpisodeEntity`, `SavedEpisodeEntity`, etc.

**API Integration**: Uses Retrofit with custom interceptors for the Podcast Index API, with response wrappers (`ResponseWrapper`, `ResponseListWrapper`) for consistent API responses.

## Development Workflow

When working with this codebase:

1. **Making Database Changes**: Always update both the entity and corresponding DAO, and add/update TypeConverters as needed
2. **Adding New Endpoints**: Create response models in `:core:network`, implement in API interfaces, then add to data sources
3. **Testing Database Code**: Use the existing test data classes in `:core:testing` and follow the Robolectric + Turbine pattern for DAO testing
4. **Repository Pattern**: Always implement repository interfaces in `:core:domain` and provide implementations in `:core:data`

## Test Data

The `:core:testing` module provides test data factories:
- `PodcastTestData` - Sample podcast data
- `EpisodeTestData` - Sample episode data
- `FeedTestData` - Sample feed data

Use these for consistent test data across modules rather than creating inline test objects.