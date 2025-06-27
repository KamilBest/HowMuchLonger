package com.icyapps.howmuchlonger.ui.screen.eventlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.icyapps.howmuchlonger.domain.model.Event
import com.icyapps.howmuchlonger.ui.components.CountdownText
import com.icyapps.howmuchlonger.ui.screen.eventlist.intent.EventListIntent
import com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListState
import com.icyapps.howmuchlonger.ui.theme.HowMuchLongerTheme
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    onNavigateToAddEvent: () -> Unit,
    onNavigateToEditEvent: (Long) -> Unit = {},
    viewModel: EventListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.processIntent(EventListIntent.LoadEvents)
    }

    Scaffold(
        topBar = { EventListTopBar() },
        floatingActionButton = { 
            AddEventButton(onClick = onNavigateToAddEvent)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            EventListContent(
                state = state,
                onDeleteEvent = { eventId ->
                    viewModel.processIntent(EventListIntent.DeleteEvent(eventId))
                },
                onEditEvent = onNavigateToEditEvent
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventListTopBar() {
    TopAppBar(
        title = { Text("Upcoming Events") }
    )
}

@Composable
private fun AddEventButton(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick) {
        Icon(Icons.Default.Add, contentDescription = "Add Event")
    }
}

@Composable
private fun EventListContent(
    state: EventListState,
    onDeleteEvent: (Long) -> Unit,
    onEditEvent: (Long) -> Unit
) {
    when (state) {
        is EventListState.Loading -> LoadingIndicator()
        is EventListState.Error -> ErrorMessage(message = state.message)
        is EventListState.Success -> {
            if (state.upcomingEvents.isEmpty() && state.pastEvents.isEmpty()) {
                EmptyListMessage()
            } else {
                EventsList(
                    upcomingEvents = state.upcomingEvents,
                    pastEvents = state.pastEvents,
                    onDeleteEvent = onDeleteEvent,
                    onEditEvent = onEditEvent
                )
            }
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
        )
    }
}

@Composable
private fun EmptyListMessage() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "No events yet. Add your first event!",
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
        )
    }
}

@Composable
private fun EventsList(
    upcomingEvents: List<Event>,
    pastEvents: List<Event>,
    onDeleteEvent: (Long) -> Unit,
    onEditEvent: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (upcomingEvents.isNotEmpty()) {
            item {
                Text(
                    text = "Upcoming:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(upcomingEvents) { event ->
                EventItem(
                    event = event,
                    onDelete = { onDeleteEvent(event.id) },
                    onEdit = { onEditEvent(event.id) }
                )
            }
        }

        if (pastEvents.isNotEmpty()) {
            item {
                Text(
                    text = "Past events:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
            items(pastEvents) { event ->
                EventItem(
                    event = event,
                    onDelete = { onDeleteEvent(event.id) },
                    onEdit = { onEditEvent(event.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventItem(
    event: Event,
    onDelete: () -> Unit,
    onEdit: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))

                CountdownText(targetTimeInMs = event.date)
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Event",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EventListScreenPreview() {
    HowMuchLongerTheme {
        EventListScreen(
            onNavigateToAddEvent = {},
            onNavigateToEditEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EventsListPreview() {
    HowMuchLongerTheme {
        EventsList(
            upcomingEvents = listOf(
                Event(
                    id = 1L,
                    name = "Birthday Party",
                    description = "Annual celebration with friends and family",
                    date = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(15)
                ),
                Event(
                    id = 2L,
                    name = "Dentist Appointment",
                    description = "Regular checkup",
                    date = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(5)
                )
            ),
            pastEvents = listOf(
                Event(
                    id = 3L,
                    name = "Project Deadline",
                    description = "Final submission for the quarterly project",
                    date = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2)
                )
            ),
            onDeleteEvent = {},
            onEditEvent = {}
        )
    }
}

@Preview
@Composable
private fun EventItemPreview() {
    HowMuchLongerTheme {
        EventItem(
            event = Event(
                id = 1L,
                name = "Birthday Party",
                description = "Annual celebration with friends and family",
                date = System.currentTimeMillis()
            ),
            onDelete = {},
            onEdit = {}
        )
    }
}

@Preview
@Composable
private fun EmptyListPreview() {
    HowMuchLongerTheme {
        EmptyListMessage()
    }
}

@Preview
@Composable
private fun ErrorMessagePreview() {
    HowMuchLongerTheme {
        ErrorMessage(message = "Failed to load events. Please try again.")
    }
}

@Preview
@Composable
private fun LoadingIndicatorPreview() {
    HowMuchLongerTheme {
        LoadingIndicator()
    }
}
