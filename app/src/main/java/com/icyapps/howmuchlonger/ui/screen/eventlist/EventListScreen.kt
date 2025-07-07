package com.icyapps.howmuchlonger.ui.screen.eventlist

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.icyapps.howmuchlonger.ui.theme.Accent
import com.icyapps.howmuchlonger.ui.theme.Background
import com.icyapps.howmuchlonger.ui.theme.ContrailOneTypography
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
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier.clip(RoundedCornerShape(8.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(text = text, style = ContrailOneTypography, modifier = Modifier.padding(8.dp))
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.clip(RoundedCornerShape(8.dp)),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Background,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.primary,
                style = ContrailOneTypography,
                modifier = Modifier.padding(8.dp)
            )
        }
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
            style = ContrailOneTypography,
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
            style = ContrailOneTypography,
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (events.isNotEmpty()) {
            if (selectedTab == EventListTab.UPCOMING) {
                // Show closest event prominently for upcoming events
                item {
                    Column {
                        // "Closest Event" label outside the card
                        Text(
                            text = "Closest Event",
                            style = ContrailOneTypography,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        ClosestEventCard(
                            event = events.first(),
                            onDelete = { onDeleteEvent(events.first().id) },
                            onEdit = { onEditEvent(events.first().id) }
                        )
                    }
                }

                // Show remaining events under "Next Events" label
                if (events.size > 1) {
                    item {
                        Text(
                            text = "Next Events:",
                            style = ContrailOneTypography,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(events.drop(1)) { event ->
                        EventItem(
                            event = event,
                            onDelete = { onDeleteEvent(event.id) },
                            onEdit = { onEditEvent(event.id) }
                        )
                    }
                }
            } else {
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
        shape = RoundedCornerShape(20.dp),
        onClick = onEdit
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClosestEventCard(
    event: Event,
    onDelete: () -> Unit,
    onEdit: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(20.dp),
        onClick = onEdit
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = event.name,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))

            CountdownText(
                targetTimeInMs = event.date,
                style = MaterialTheme.typography.titleMedium,
                color = Accent
            )
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

@Preview
@Composable
private fun ClosestEventCardPreview() {
    HowMuchLongerTheme {
        ClosestEventCard(
            event = Event(
                id = 1L,
                name = "Birthday Party",
                description = "Annual celebration with friends and family",
                date = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2)
            ),
            onDelete = {},
            onEdit = {}
        )
    }
}
