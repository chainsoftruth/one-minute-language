package com.example.oneminutelanguage

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import com.example.oneminutelanguage.data.AppDatabase
import com.example.oneminutelanguage.data.MIGRATION_4_5
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), AppDatabase::class.java)

    // R9: a v1.5 install keeps its words (and learnedAt) when it upgrades to v2.0.
    @Test
    fun migrate4To5_keepsWordsAndAddsExercisesDone() {
        helper.createDatabase("migration-test", 4).use { db ->
            db.execSQL("INSERT INTO words (language1Word, language2Word, dateAdded, isDefault, isEnabled, learnedAt) VALUES ('House', 'het huis', 1, 0, 1, 42)")
            db.execSQL("INSERT INTO daily_stats (date, widgetViewCount) VALUES ('2026-10-01', 7)")
        }
        val db = helper.runMigrationsAndValidate("migration-test", 5, true, MIGRATION_4_5)
        db.query("SELECT language2Word, learnedAt FROM words").use {
            assertEquals(1, it.count)
            it.moveToFirst()
            assertEquals("het huis", it.getString(0))
            assertEquals(42L, it.getLong(1))
        }
        db.query("SELECT widgetViewCount, exercisesDone FROM daily_stats").use {
            it.moveToFirst()
            assertEquals(7, it.getInt(0))
            assertEquals(0, it.getInt(1))
        }
    }
}
