package com.icyapps.howmuchlonger.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.icyapps.howmuchlonger.data.model.EventEntity

@Database(
    entities = [EventEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(EventTypeConverter::class)
abstract class EventDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE events ADD COLUMN countryCode TEXT DEFAULT NULL")
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_events_type_countryCode_date_name " +
                        "ON events(type, countryCode, date, name)"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE events ADD COLUMN endDate INTEGER DEFAULT NULL")
            }
        }
    }
}
