package com.icyapps.howmuchlonger.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.icyapps.howmuchlonger.data.model.EventEntity

@Database(
    entities = [EventEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(EventTypeConverter::class)
abstract class EventDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
} 