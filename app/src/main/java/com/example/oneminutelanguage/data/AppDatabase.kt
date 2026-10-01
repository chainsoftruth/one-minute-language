package com.example.oneminutelanguage.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [WordEntity::class, DailyStatsEntity::class, LessonProgressEntity::class, ReviewCardEntity::class],
    version = 5,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun dailyStatsDao(): DailyStatsDao
    abstract fun lessonProgressDao(): LessonProgressDao
    abstract fun reviewCardDao(): ReviewCardDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE words ADD COLUMN isDefault INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE words ADD COLUMN isEnabled INTEGER NOT NULL DEFAULT 1")
    }
}

// Nullable, no default: existing rows keep their isEnabled/isDefault and start as "not learned".
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE words ADD COLUMN learnedAt INTEGER")
    }
}

// One migration for all of v2.0: course progress, review cards, and a per-day exercise counter.
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `lesson_progress` (`lessonId` TEXT NOT NULL, `courseId` TEXT NOT NULL, `completedAt` INTEGER NOT NULL, `bestScore` INTEGER NOT NULL, `attempts` INTEGER NOT NULL, PRIMARY KEY(`lessonId`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `review_cards` (`cardId` TEXT NOT NULL, `courseId` TEXT NOT NULL, `dueAt` INTEGER NOT NULL, `intervalDays` REAL NOT NULL, `ease` REAL NOT NULL, `reps` INTEGER NOT NULL, `lapses` INTEGER NOT NULL, PRIMARY KEY(`cardId`))")
        db.execSQL("ALTER TABLE daily_stats ADD COLUMN exercisesDone INTEGER NOT NULL DEFAULT 0")
    }
}
