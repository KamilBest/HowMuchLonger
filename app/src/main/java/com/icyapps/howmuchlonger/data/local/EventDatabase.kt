package com.icyapps.howmuchlonger.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.icyapps.howmuchlonger.data.model.EventEntity

@Database(
    entities = [EventEntity::class],
    version = 1,
    exportSchema = false
)
abstract class EventDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
} 