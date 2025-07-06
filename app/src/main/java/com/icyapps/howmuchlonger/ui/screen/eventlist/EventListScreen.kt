package com.icyapps.howmuchlonger.ui.screen.eventlist

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.icyapps.howmuchlonger.R
import com.icyapps.howmuchlonger.domain.model.Event
import com.icyapps.howmuchlonger.ui.components.CountdownText
import com.icyapps.howmuchlonger.ui.screen.eventlist.intent.EventListIntent
import com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListState
import com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListTab
import com.icyapps.howmuchlonger.ui.theme.HowMuchLongerTheme
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    onNavigateToAddEvent: () -> Unit,
    onNavigateToEditEvent: (Long) -> Unit = {},
    state: EventListState,
    onProcessIntent: (EventListIntent) -> Unit
) {
    LaunchedEffect(Unit) {
        onProcessIntent(EventListIntent.LoadEvents)
    }

    Scaffold(
        topBar = { EventListTopBar() },
        floatingActionButton = {
            AddEventButton(onClick = onNavigateToAddEvent)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state is EventListState.Success) {
                EventListTabs(
                    selectedTab = state.selectedTab,
                    onTabSelected = { tab ->
                        onProcessIntent(EventListIntent.SwitchTab(tab))
                    }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                EventListContent(
                    state = state,
                    onDeleteEvent = { eventId ->
                        onProcessIntent(EventListIntent.DeleteEvent(eventId))
                    },
                    onEditEvent = onNavigateToEditEvent
                )
            }
        }
    }
}

@Composable
private fun EventListTabs(
    selectedTab: EventListTab,
    onTabSelected: (EventListTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TabButton(
            text = "Upcoming",
            selected = selectedTab == EventListTab.UPCOMING,
            onClick = { onTabSelected(EventListTab.UPCOMING) },
            modifier = Modifier.weight(1f)
        )

        TabButton(
            text = "Past",
            selected = selectedTab == EventListTab.PAST,
            onClick = { onTabSelected(EventListTab.PAST) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(text = text)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventListTopBar() {
    TopAppBar(
        title = {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo"
            )
        }
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
            if (state.events.isEmpty()) {
                EmptyListMessage(selectedTab = state.selectedTab)
            } else {
                EventsList(
                    events = state.events,
                    selectedTab = state.selectedTab,
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
private fun EmptyListMessage(selectedTab: EventListTab) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = when (selectedTab) {
                EventListTab.UPCOMING -> "No upcoming events. Add your first event!"
                EventListTab.PAST -> "No past events. Add your first event!"
            },
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
        )
    }
}

@Composable
private fun EventsList(
    events: List<Event>,
    selectedTab: EventListTab,
    onDeleteEvent: (Long) -> Unit,
    onEditEvent: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (events.isNotEmpty()) {
            item {
                Text(
                    text = when (selectedTab) {
                        EventListTab.UPCOMING -> "Upcoming Events:"
                        EventListTab.PAST -> "Past Events:"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(events) { event ->
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
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
            onNavigateToEditEvent = {},
            state = EventListState.Success(),
            onProcessIntent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EventsListPreview() {
    HowMuchLongerTheme {
        EventsList(
            events = listOf(
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
            selectedTab = EventListTab.UPCOMING,
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
        EmptyListMessage(selectedTab = EventListTab.UPCOMING)
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
