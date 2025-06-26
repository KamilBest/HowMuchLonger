package com.icyapps.howmuchlonger.ui.screen.addevent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.icyapps.howmuchlonger.ui.screen.addevent.intent.AddEventIntent
import com.icyapps.howmuchlonger.ui.screen.addevent.model.AddEventState
import com.icyapps.howmuchlonger.ui.theme.HowMuchLongerTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    eventId: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: AddEventViewModel = hiltViewModel()
) {
    LaunchedEffect(eventId) {
        if (eventId != null) {
            viewModel.setEventId(eventId)
        }
    }

    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            AddEventTopBar(
                isEditMode = eventId != null,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        AddEventForm(
            state = state,
            onTitleChange = { viewModel.processIntent(AddEventIntent.UpdateTitle(it)) },
            onDescriptionChange = { viewModel.processIntent(AddEventIntent.UpdateDescription(it)) },
            onDateChange = { viewModel.processIntent(AddEventIntent.UpdateDate(it)) },
            onSaveClick = { viewModel.processIntent(AddEventIntent.SaveEvent) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEventTopBar(
    isEditMode: Boolean,
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = { Text(if (isEditMode) "Edit Event" else "Add New Event") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }
    )
}

@Composable
private fun AddEventForm(
    state: AddEventState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        EventTitleField(
            value = state.title,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth()
        )

        EventDescriptionField(
            value = state.description,
            onValueChange = onDescriptionChange,
            modifier = Modifier.fillMaxWidth()
        )

        EventDateField(
            date = state.date,
            onDateSelected = onDateChange,
            modifier = Modifier.fillMaxWidth()
        )

        state.error?.let {
            ErrorMessage(
                message = it,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        SaveEventButton(
            isLoading = state.isLoading,
            enabled = !state.isLoading && state.title.isNotBlank(),
            onClick = onSaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )
    }
}

@Composable
private fun EventTitleField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Event Title") },
        modifier = modifier
    )
}

@Composable
private fun EventDescriptionField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Description") },
        modifier = modifier,
        minLines = 3
    )
}

@Composable
private fun EventDateField(
    date: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val formattedDate = remember(date) {
        dateFormat.format(Date(date))
    }

    OutlinedTextField(
        value = formattedDate,
        onValueChange = { },
        label = { Text("Date and Time") },
        modifier = modifier,
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = {
                // TODO: Show date picker
            }) {
                Icon(Icons.Default.DateRange, contentDescription = "Select Date")
            }
        }
    )
}

@Composable
private fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        modifier = modifier
    )
}

@Composable
private fun SaveEventButton(
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text("Save Event")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddEventScreenPreview() {
    HowMuchLongerTheme {
        AddEventScreen(
            eventId = null,
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditEventScreenPreview() {
    HowMuchLongerTheme {
        AddEventScreen(
            eventId = 1L,
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddEventFormPreview() {
    HowMuchLongerTheme {
        AddEventForm(
            state = AddEventState(
                title = "Birthday Party",
                description = "Annual celebration with friends and family",
                date = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(15)
            ),
            onTitleChange = {},
            onDescriptionChange = {},
            onDateChange = {},
            onSaveClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EventTitleFieldPreview() {
    HowMuchLongerTheme {
        EventTitleField(
            value = "Birthday Party",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EventDescriptionFieldPreview() {
    HowMuchLongerTheme {
        EventDescriptionField(
            value = "Annual celebration with friends and family",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EventDateFieldPreview() {
    HowMuchLongerTheme {
        EventDateField(
            date = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(15),
            onDateSelected = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SaveEventButtonPreview() {
    HowMuchLongerTheme {
        SaveEventButton(
            isLoading = false,
            enabled = true,
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SaveEventButtonLoadingPreview() {
    HowMuchLongerTheme {
        SaveEventButton(
            isLoading = true,
            enabled = true,
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorMessagePreview() {
    HowMuchLongerTheme {
        ErrorMessage(
            message = "Failed to save event. Please try again."
        )
    }
}
