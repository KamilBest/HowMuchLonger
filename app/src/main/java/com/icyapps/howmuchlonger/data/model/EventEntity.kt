package com.icyapps.howmuchlonger.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.icyapps.howmuchlonger.domain.model.Event

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val date: Long
)

fun EventEntity.toDomainModel(): Event {
    return Event(
        id = id,
        name = name,
        description = description,
        date = date
    )
}

fun Event.toEntity(): EventEntity {
    return EventEntity(
        id = id,
        name = name,
        description = description,
        date = date
    )
}