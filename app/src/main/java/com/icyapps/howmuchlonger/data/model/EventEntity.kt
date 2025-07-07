package com.icyapps.howmuchlonger.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.icyapps.howmuchlonger.domain.model.Event
import com.icyapps.howmuchlonger.domain.model.EventType
import androidx.room.TypeConverter

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val date: Long,
    val type: EventType = EventType.Normal
)

fun EventEntity.toDomainModel(): Event {
    return Event(
        id = id,
        name = name,
        description = description,
        date = date,
        type = type
    )
}

fun Event.toEntity(): EventEntity {
    return EventEntity(
        id = id,
        name = name,
        description = description,
        date = date,
        type = type
    )
}