package io.jacob.episodive.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 원격이 준 순서를 보존할 `sortOrder` 컬럼을 `feeds`·`soundbites` 에 넣고, `feeds` 의 기본키를
 * `id` 단독에서 `(id, groupKey)` 복합키로 바꾼다.
 *
 * 복합키가 필요한 이유는 [io.jacob.episodive.core.database.model.FeedEntity] 에 적어 두었다.
 * 요약하면 `id` 단독 PK 에서는 한 피드가 한 그룹에만 존재할 수 있어, 목록끼리 서로의 행을
 * 덮어썼다.
 *
 * `feeds` 는 기본키를 바꾸므로 SQLite 관례대로 새 테이블을 만들어 옮긴다. DDL 은 Room 이 내보낸
 * `schemas/…/12.json` 의 `createSql` 을 그대로 옮긴 것이다 — 한 글자라도 어긋나면 실행 시점의
 * 스키마 검증이 실패한다. 옮겨온 행의 `sortOrder` 는 전부 0 이라 순위 정보가 없지만, 이 표는
 * TTL 10분짜리 순수 캐시라 곧 원격 순서로 덮인다. 그동안은 쿼리의 동점 처리(`, id`)가
 * 마이그레이션 이전과 같은 순서를 낸다.
 *
 * 두 뷰(`podcast_with_extras`, `episode_with_extras`) 는 `feeds` 를 참조하지 않으므로 테이블을
 * 지웠다 만들어도 뷰를 다시 만들 필요가 없다.
 */
val Migration11to12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `soundbites` ADD COLUMN `sortOrder` INTEGER NOT NULL DEFAULT 0")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `feeds_new` (" +
                "`id` INTEGER NOT NULL, `url` TEXT NOT NULL, `title` TEXT NOT NULL, " +
                "`newestItemPublishTime` INTEGER NOT NULL, `description` TEXT, `image` TEXT, " +
                "`itunesId` INTEGER, `language` TEXT NOT NULL, `categories` TEXT NOT NULL, " +
                "`groupKey` TEXT NOT NULL, `sortOrder` INTEGER NOT NULL DEFAULT 0, " +
                "`cachedAt` INTEGER NOT NULL, PRIMARY KEY(`id`, `groupKey`))"
        )
        db.execSQL(
            "INSERT INTO `feeds_new` (" +
                "`id`, `url`, `title`, `newestItemPublishTime`, `description`, `image`, " +
                "`itunesId`, `language`, `categories`, `groupKey`, `sortOrder`, `cachedAt`) " +
                "SELECT `id`, `url`, `title`, `newestItemPublishTime`, `description`, `image`, " +
                "`itunesId`, `language`, `categories`, `groupKey`, 0, `cachedAt` FROM `feeds`"
        )
        db.execSQL("DROP TABLE `feeds`")
        db.execSQL("ALTER TABLE `feeds_new` RENAME TO `feeds`")
    }
}
