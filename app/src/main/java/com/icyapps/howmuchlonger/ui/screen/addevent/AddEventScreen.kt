package com.icyapps.howmuchlonger.ui.screen.addevent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import android.util.Log
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.icyapps.howmuchlonger.ui.screen.addevent.intent.AddEventIntent
import com.icyapps.howmuchlonger.ui.screen.addevent.model.AddEventState
import com.icyapps.howmuchlonger.ui.theme.ContrailOneTypography
import com.icyapps.howmuchlonger.ui.theme.HowMuchLongerTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    onNavigateBack: () -> Unit,
    state: AddEventState,
    onProcessIntent: (AddEventIntent) -> Unit
) {
    // Observe saveCompleted state and handle navigation
    LaunchedEffect(state.saveCompleted) {
        if (state.saveCompleted) {
            onNavigateBack()
            // Reset the saveCompleted flag
            onProcessIntent(AddEventIntent.NavigateBack)
        }
    }

    Scaffold(
        topBar = {
            AddEventTopBar(
                isEditMode = state.eventId != null,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        AddEventForm(
            state = state,
            onTitleChange = { onProcessIntent(AddEventIntent.UpdateTitle(it)) },
            onDescriptionChange = { onProcessIntent(AddEventIntent.UpdateDescription(it)) },
            onDateChange = { onProcessIntent(AddEventIntent.UpdateDate(it)) },
            onToggleIncludeTime = { onProcessIntent(AddEventIntent.ToggleIncludeTime(it)) },
            onShowDatePicker = { onProcessIntent(AddEventIntent.ShowDatePicker) },
            onHideDatePicker = { onProcessIntent(AddEventIntent.HideDatePicker) },
            onShowTimePicker = { onProcessIntent(AddEventIntent.ShowTimePicker) },
            onHideTimePicker = { onProcessIntent(AddEventIntent.HideTimePicker) },
            onSaveClick = { onProcessIntent(AddEventIntent.SaveEvent) },
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
    onToggleIncludeTime: (Boolean) -> Unit,
    onShowDatePicker: () -> Unit,
    onHideDatePicker: () -> Unit,
    onShowTimePicker: () -> Unit,
    onHideTimePicker: () -> Unit,
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
            includeTime = state.includeTime,
            showDatePicker = state.showDatePicker,
            showTimePicker = state.showTimePicker,
            onToggleIncludeTime = onToggleIncludeTime,
            onShowDatePicker = onShowDatePicker,
            onHideDatePicker = onHideDatePicker,
            onShowTimePicker = onShowTimePicker,
            onHideTimePicker = onHideTimePicker,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventDateField(
    modifier: Modifier = Modifier,
    date: Long,
    onDateSelected: (Long) -> Unit,
    includeTime: Boolean = true,
    showDatePicker: Boolean = false,
    showTimePicker: Boolean = false,
    onToggleIncludeTime: (Boolean) -> Unit = {},
    onShowDatePicker: () -> Unit = {},
    onHideDatePicker: () -> Unit = {},
    onShowTimePicker: () -> Unit = {},
    onHideTimePicker: () -> Unit = {},
) {
    // Always extract hour/minute from date value
    val calendar = remember(date) {
        java.util.Calendar.getInstance().apply { timeInMillis = date }
    }
    val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
    val minute = calendar.get(java.util.Calendar.MINUTE)

    val dateFormat = remember(includeTime) {
        if (includeTime) SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        else SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }

    val formattedDate = remember(date, includeTime) {
        dateFormat.format(Date(date))
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = formattedDate,
            onValueChange = { },
            label = { Text(if (includeTime) "Date and Time" else "Date") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                Row {
                    IconButton(onClick = onShowDatePicker) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    }
                    if (includeTime) {
                        IconButton(onClick = onShowTimePicker) {
                            Icon(Icons.Default.Edit, contentDescription = "Select Time")
                        }
                    }
                }
            }
        )

        Row(
            modifier = Modifier.padding(top = 8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Checkbox(
                checked = includeTime,
                onCheckedChange = onToggleIncludeTime
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Include time")
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date)

        DatePickerDialog(
            onDismissRequest = onHideDatePicker,
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate ->
                            // Preserve the time part if includeTime is true
                            val newDate = if (includeTime) {
                                val calendar = java.util.Calendar.getInstance()
                                calendar.timeInMillis = date
                                calendar.timeInMillis = selectedDate
                                calendar.set(java.util.Calendar.HOUR_OF_DAY, hour)
                                calendar.set(java.util.Calendar.MINUTE, minute)
                                calendar.timeInMillis
                            } else {
                                val calendar = java.util.Calendar.getInstance()
                                calendar.timeInMillis = selectedDate
                                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                                calendar.set(java.util.Calendar.MINUTE, 0)
                                calendar.set(java.util.Calendar.SECOND, 0)
                                calendar.set(java.util.Calendar.MILLISECOND, 0)
                                calendar.timeInMillis
                            }
                            onDateSelected(newDate)
                        }
                        onHideDatePicker()
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onHideDatePicker) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker && includeTime) {
        val timePickerState = rememberTimePickerState(
            initialHour = hour,
            initialMinute = minute
        )

        androidx.compose.material3.AlertDialog(
            onDismissRequest = onHideTimePicker,
            confirmButton = {
                TextButton(
                    onClick = {
                        val newCalendar = java.util.Calendar.getInstance().apply {
                            timeInMillis = date
                            set(java.util.Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(java.util.Calendar.MINUTE, timePickerState.minute)
                        }
                        onDateSelected(newCalendar.timeInMillis)
                        onHideTimePicker()
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onHideTimePicker) {
                    Text("Cancel")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
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
            Text("Save Event", style = ContrailOneTypography)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddEventScreenPreview() {
    HowMuchLongerTheme {
        AddEventScreen(
            onNavigateBack = {},
            state = AddEventState(),
            onProcessIntent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditEventScreenPreview() {
    HowMuchLongerTheme {
        AddEventScreen(
            onNavigateBack = {},
            state = AddEventState(
                eventId = 1L,
                title = "Sample Event",
                description = "Sample Description"
            ),
            onProcessIntent = {}
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
            onToggleIncludeTime = {},
            onShowDatePicker = {},
            onHideDatePicker = {},
            onShowTimePicker = {},
            onHideTimePicker = {},
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
