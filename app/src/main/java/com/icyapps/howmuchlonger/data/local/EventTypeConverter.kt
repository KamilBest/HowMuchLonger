package com.icyapps.howmuchlonger.data.local

import androidx.room.TypeConverter
import com.icyapps.howmuchlonger.domain.model.EventType

class EventTypeConverter {
    @TypeConverter
    fun fromEventType(type: EventType): String = type.name

    @TypeConverter
    fun toEventType(value: String): EventType = EventType.valueOf(value)
}