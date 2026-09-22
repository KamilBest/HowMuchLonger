package com.icyapps.howmuchlonger.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.icyapps.howmuchlonger.domain.model.Event
import com.icyapps.howmuchlonger.domain.model.EventType

@Entity(
    tableName = "events",
    indices = [Index(value = ["type", "countryCode", "date", "name"], unique = true)]
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val date: Long,
    val type: EventType = EventType.Normal,
    val countryCode: String? = null,
    val endDate: Long? = null
)

fun EventEntity.toDomainModel(): Event {
    return Event(
        id = id,
        name = name,
        description = description,
        date = date,
        type = type,
        endDate = endDate
    )
}

fun Event.toEntity(): EventEntity {
    return EventEntity(
        id = id,
        name = name,
        description = description,
        date = date,
        type = type,
        endDate = endDate
    )
}
