package com.icyapps.howmuchlonger.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.icyapps.howmuchlonger.data.model.EventEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class EventDaoTest {
    private lateinit var eventDao: EventDao
    private lateinit var db: EventDatabase

    private val testEvent1 = EventEntity(
        id = 1L,
        name = "Test Event 1",
        description = "Test Description 1",
        date = 1672531200000 // 2023-01-01
    )

    private val testEvent2 = EventEntity(
        id = 2L,
        name = "Test Event 2",
        description = "Test Description 2",
        date = 1675209600000 // 2023-02-01
    )

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, EventDatabase::class.java
        ).build()
        eventDao = db.eventDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetEventById() = runTest {
        // Given
        val eventId = eventDao.insertEvent(testEvent1.copy(id = 0))

        // When
        val loadedEvent = eventDao.getEventById(eventId)

        // Then
        assertEquals(testEvent1.name, loadedEvent?.name)
        assertEquals(testEvent1.description, loadedEvent?.description)
        assertEquals(testEvent1.date, loadedEvent?.date)
    }

    @Test
    fun getEventByIdReturnsNullWhenEventDoesNotExist() = runTest {
        // When
        val loadedEvent = eventDao.getEventById(999L)

        // Then
        assertNull(loadedEvent)
    }

    @Test
    fun getAllEventsReturnsAllEvents() = runTest {
        // Given
        eventDao.insertEvent(testEvent1.copy(id = 0))
        eventDao.insertEvent(testEvent2.copy(id = 0))

        // When
        val events = eventDao.getAllEvents().first()

        // Then
        assertEquals(2, events.size)
        assertEquals(testEvent1.name, events[0].name)
        assertEquals(testEvent2.name, events[1].name)
    }

    @Test
    fun updateEventUpdatesTheEvent() = runTest {
        // Given
        val eventId = eventDao.insertEvent(testEvent1.copy(id = 0))
        val updatedEvent = testEvent1.copy(
            id = eventId,
            name = "Updated Event",
            description = "Updated Description"
        )

        // When
        eventDao.updateEvent(updatedEvent)
        val loadedEvent = eventDao.getEventById(eventId)

        // Then
        assertEquals(updatedEvent.name, loadedEvent?.name)
        assertEquals(updatedEvent.description, loadedEvent?.description)
    }

    @Test
    fun deleteEventDeletesTheEvent() = runTest {
        // Given
        val eventId = eventDao.insertEvent(testEvent1.copy(id = 0))
        val loadedEvent = eventDao.getEventById(eventId)

        // When
        eventDao.deleteEvent(loadedEvent!!)
        val deletedEvent = eventDao.getEventById(eventId)

        // Then
        assertNull(deletedEvent)
    }

    @Test
    fun getTop3EventsReturnsTop3Events() = runTest {
        // Given
        eventDao.insertEvent(testEvent1.copy(id = 0))
        eventDao.insertEvent(testEvent2.copy(id = 0))
        eventDao.insertEvent(
            EventEntity(
                id = 0,
                name = "Test Event 3",
                description = "Test Description 3",
                date = 1677628800000 // 2023-03-01
            )
        )
        eventDao.insertEvent(
            EventEntity(
                id = 0,
                name = "Test Event 4",
                description = "Test Description 4",
                date = 1680307200000 // 2023-04-01
            )
        )

        // When
        val events = eventDao.getTop3Events().first()

        // Then
        assertEquals(3, events.size)
        assertEquals(testEvent1.name, events[0].name)
        assertEquals(testEvent2.name, events[1].name)
        assertEquals("Test Event 3", events[2].name)
    }

    @Test
    fun getAllEventsEmitsUpdatedListAfterInsert() = runTest {
        // Given - Clear the database
        // Initial state - empty database
        var events = eventDao.getAllEvents().first()
        assertEquals(0, events.size)

        // When - Insert first event
        eventDao.insertEvent(testEvent1.copy(id = 0))

        // Then - Check that the event was inserted
        events = eventDao.getAllEvents().first()
        assertEquals(1, events.size)
        assertEquals(testEvent1.name, events[0].name)

        // When - Insert second event
        eventDao.insertEvent(testEvent2.copy(id = 0))

        // Then - Check that both events are in the database
        events = eventDao.getAllEvents().first()
        assertEquals(2, events.size)
        assertEquals(testEvent1.name, events[0].name)
        assertEquals(testEvent2.name, events[1].name)
    }
}
