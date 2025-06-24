package com.icyapps.howmuchlonger.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.icyapps.howmuchlonger.data.model.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY date ASC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventById(id: Long): EventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(eventEntity: EventEntity): Long

    @Update
    suspend fun updateEvent(eventEntity: EventEntity)

    @Delete
    suspend fun deleteEvent(eventEntity: EventEntity)

    @Query("SELECT * FROM events ORDER BY date ASC LIMIT 3")
    fun getTop3Events(): Flow<List<EventEntity>>
} 