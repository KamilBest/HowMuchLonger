package com.icyapps.howmuchlonger.ui.screen.eventlist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.icyapps.howmuchlonger.R
import com.icyapps.howmuchlonger.domain.model.Event
import com.icyapps.howmuchlonger.domain.model.EventType
import com.icyapps.howmuchlonger.ui.components.CountdownText
import com.icyapps.howmuchlonger.ui.screen.eventlist.intent.EventListIntent
import com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListState
import com.icyapps.howmuchlonger.ui.screen.eventlist.model.EventListTab
import com.icyapps.howmuchlonger.ui.theme.*
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    onNavigateToAddEvent: (Long?) -> Unit,
    onNavigateToEditEvent: (Long) -> Unit = {},
    state: EventListState,
    onProcessIntent: (EventListIntent) -> Unit
) {
    LaunchedEffect(Unit) { onProcessIntent(EventListIntent.LoadEvents) }
    BoxWithConstraints {
        val expanded = maxWidth >= 600.dp
        val success = state as? EventListState.Success
        BackHandler(enabled = success?.showCalendar == true) {
            onProcessIntent(EventListIntent.ToggleCalendar)
        }
        Scaffold(
            topBar = {
                EventListTopBar(
                    expanded,
                    success,
                    { onProcessIntent(EventListIntent.SwitchTab(EventListTab.UPCOMING)) },
                    { onProcessIntent(EventListIntent.SwitchTab(it)) },
                    { onProcessIntent(EventListIntent.ToggleHolidays) },
                    { onProcessIntent(EventListIntent.ToggleCalendar) }
                )
            },
            floatingActionButton = {
                if (success != null && (success.showCalendar || success.selectedTab == EventListTab.UPCOMING)) {
                    val initialDate = success
                        .takeIf { it.showCalendar }
                        ?.selectedCalendarDate
                        ?.let(::calendarInitialTimestamp)
                    AddEventButton { onNavigateToAddEvent(initialDate) }
                }
            }
        ) { insets ->
            Column(Modifier.fillMaxSize().padding(insets)) {
                if (!expanded && success != null && !success.showCalendar) {
                    EventListTabs(
                        success.selectedTab,
                        { onProcessIntent(EventListIntent.SwitchTab(it)) },
                        Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                EventListContent(
                    state,
                    expanded,
                    onNavigateToEditEvent,
                    { onProcessIntent(EventListIntent.ChangeCalendarMonth(it)) },
                    { onProcessIntent(EventListIntent.SelectCalendarDate(it)) }
                )
            }
        }
    }
}

@Composable
private fun EventListTabs(
    selected: EventListTab,
    onSelected: (EventListTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        EventListTab.entries.forEach { tab ->
            val text = stringResource(
                when (tab) {
                    EventListTab.UPCOMING -> R.string.upcoming
                    EventListTab.PAST -> R.string.past
                }
            )
            TabButton(text, selected == tab, { onSelected(tab) }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun TabButton(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val shape = RoundedCornerShape(12.dp)
    val content: @Composable RowScope.() -> Unit = {
        Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
    if (selected) {
        Button(
            onClick,
            modifier.heightIn(min = 44.dp),
            shape = shape,
            contentPadding = PaddingValues(horizontal = 6.dp),
            content = content
        )
    } else {
        OutlinedButton(
            onClick,
            modifier.heightIn(min = 44.dp),
            shape = shape,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            contentPadding = PaddingValues(horizontal = 6.dp),
            content = content
        )
    }
}

@Composable
private fun EventListContent(
    state: EventListState,
    expanded: Boolean,
    onEdit: (Long) -> Unit,
    onChangeMonth: (Long) -> Unit,
    onSelectDate: (LocalDate) -> Unit
) {
    when (state) {
        EventListState.Loading -> LoadingIndicator()
        is EventListState.Error -> ErrorMessage(state.message)
        is EventListState.Success -> when {
            state.showCalendar -> {
                val calendarEvents = state.allEvents.filter {
                    state.includeHolidays || it.type != EventType.Holiday
                }
                CalendarContent(
                    calendarEvents, state.calendarMonth, state.selectedCalendarDate, expanded,
                    { onChangeMonth(-1) }, { onChangeMonth(1) }, onSelectDate, onEdit
                )
            }
            state.events.isEmpty() -> EmptyListMessage(state.selectedTab)
            else -> EventsList(state.events, state.selectedTab, onEdit)
        }
    }
}

@Composable
private fun EmptyListMessage(tab: EventListTab) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            stringResource(if (tab == EventListTab.UPCOMING) R.string.no_upcoming_events else R.string.no_past_events),
            style = ContrailOneTypography,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(24.dp)
        )
    }
}

@Composable
private fun EventsList(
    events: List<Event>,
    tab: EventListTab,
    onEdit: (Long) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(300.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (tab == EventListTab.UPCOMING) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(stringResource(R.string.closest), style = MaterialTheme.typography.titleMedium)
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                EventItem(events.first(), emphasized = true, onEdit = editableAction(events.first(), onEdit))
            }
            if (events.size > 1) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        stringResource(R.string.next_events),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(events.drop(1), key = { it.id }) { event ->
                    EventItem(event, onEdit = editableAction(event, onEdit))
                }
            }
        } else {
            items(events, key = { it.id }) { event ->
                EventItem(event, onEdit = editableAction(event, onEdit))
            }
        }
    }
}

private fun editableAction(event: Event, action: (Long) -> Unit): (() -> Unit)? =
    if (event.type == EventType.Normal) ({ action(event.id) }) else null

@Composable
private fun EventItem(
    event: Event,
    emphasized: Boolean = false,
    onEdit: (() -> Unit)? = null
) {
    val locale = LocalLocale.current.platformLocale
    val holiday = event.type == EventType.Holiday
    val now = System.currentTimeMillis()
    val endBoundary = eventEndBoundary(event)
    val past = endBoundary <= now
    val activeRange = event.endDate != null && event.date <= now && !past
    val countdownTarget = if (event.endDate != null && event.date <= now) endBoundary else event.date
    val container = when {
        holiday -> HolidayEventCardBackground
        past -> PastEventCardBackground
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (holiday) HolidayEventContent else MaterialTheme.colorScheme.onSurfaceVariant
    val shape = RoundedCornerShape(20.dp)
    Card(
        modifier = Modifier.fillMaxWidth()
            .alpha(if (past) .68f else 1f)
            .clip(shape)
            .then(if (onEdit != null) Modifier.clickable(onClick = onEdit) else Modifier),
        colors = CardDefaults.cardColors(containerColor = container, contentColor = contentColor),
        shape = shape
    ) {
        Column(
            Modifier.fillMaxWidth().padding(if (emphasized) 20.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (past) PastEventBadge()
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (holiday) {
                    Icon(Icons.Default.DateRange, stringResource(R.string.holiday), Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    event.name,
                    style = if (emphasized) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }
            if (holiday) {
                Text(stringResource(R.string.holiday), style = MaterialTheme.typography.labelMedium)
            } else if (event.description.isNotBlank()) {
                Text(event.description, style = MaterialTheme.typography.bodyMedium)
            }
            CountdownText(
                countdownTarget,
                isPastTab = past,
                endsIn = activeRange,
                calendarDaysOnly = holiday,
                color = contentColor,
                style = if (emphasized) {
                    MaterialTheme.typography.titleLarge
                } else {
                    MaterialTheme.typography.titleMedium
                }
            )
            Text(
                formatEventDate(event, locale),
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = .72f)
            )
        }
    }
}

@Composable
private fun PastEventBadge() {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.Transparent,
        contentColor = HolidayEventContent,
        border = BorderStroke(1.dp, HolidayEventContent.copy(alpha = .55f))
    ) {
        Row(
            Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Default.Check, null, Modifier.size(15.dp))
            Text(stringResource(R.string.event_ended), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun CalendarContent(
    events: List<Event>,
    month: YearMonth,
    selected: LocalDate,
    expanded: Boolean,
    previous: () -> Unit,
    next: () -> Unit,
    onSelected: (LocalDate) -> Unit,
    onEdit: (Long) -> Unit
) {
    val eventsByDate = remember(events) { eventsByCalendarDate(events) }
    val dayEvents = eventsByDate[selected].orEmpty()
    if (expanded) {
        Row(Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CalendarMonth(
                month, selected, eventsByDate, onSelected, previous, next,
                Modifier.widthIn(max = 520.dp).weight(.9f)
            )
            DayEvents(
                selected, dayEvents, onEdit,
                Modifier.weight(1.1f).fillMaxHeight().verticalScroll(rememberScrollState())
            )
        }
    } else {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CalendarMonth(month, selected, eventsByDate, onSelected, previous, next)
            DayEvents(selected, dayEvents, onEdit)
            Spacer(Modifier.height(72.dp))
        }
    }
}

@Composable
private fun CalendarMonth(
    month: YearMonth,
    selected: LocalDate,
    events: Map<LocalDate, List<Event>>,
    onSelected: (LocalDate) -> Unit,
    previous: () -> Unit,
    next: () -> Unit,
    modifier: Modifier = Modifier
) {
    val locale = LocalLocale.current.platformLocale
    val maxDate = remember { LocalDate.now().plusYears(1) }
    val firstWeekDay = WeekFields.of(locale).firstDayOfWeek
    val leading = (month.atDay(1).dayOfWeek.value - firstWeekDay.value + 7) % 7
    val days = remember(month, firstWeekDay) {
        List(42) { index ->
            (index - leading + 1).takeIf { it in 1..month.lengthOfMonth() }?.let(month::atDay)
        }
    }
    val label = remember(month, locale) {
        month.format(DateTimeFormatter.ofPattern("LLLL yyyy", locale)).replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(locale) else it.toString()
        }
    }
    Card(
        modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f))
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(previous) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, stringResource(R.string.previous_month)) }
                Text(
                    label,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                IconButton(next, enabled = month < YearMonth.from(maxDate)) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, stringResource(R.string.next_month))
                }
            }
            Row(Modifier.fillMaxWidth()) {
                List(7) { firstWeekDay.plus(it.toLong()) }.forEach { day ->
                    Text(
                        day.getDisplayName(TextStyle.NARROW_STANDALONE, locale),
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f).padding(vertical = 6.dp)
                    )
                }
            }
            days.chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth()) {
                    week.forEachIndexed { column, date ->
                        if (date == null) Spacer(Modifier.weight(1f).aspectRatio(1f))
                        else CalendarDay(
                            date = date,
                            selected = date == selected,
                            events = events[date].orEmpty(),
                            connectsPreviousCell = column > 0 && week[column - 1] != null,
                            connectsNextCell = column < 6 && week[column + 1] != null,
                            enabled = !date.isAfter(maxDate),
                            onClick = { onSelected(date) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            CalendarLegend(Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate,
    selected: Boolean,
    events: List<Event>,
    connectsPreviousCell: Boolean,
    connectsNextCell: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    val rangeEvents = events.filter { it.type == EventType.Normal && it.endDate != null }
    val rangeConnections = calendarRangeConnections(
        date = date,
        rangeEvents = rangeEvents,
        hasPreviousCell = connectsPreviousCell,
        hasNextCell = connectsNextCell
    )
    Box(
        modifier = modifier.aspectRatio(1f)
    ) {
        if (rangeEvents.isNotEmpty()) {
            CalendarRangeBand(
                color = Accent,
                connectsPrevious = rangeConnections.previous,
                connectsNext = rangeConnections.next,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Box(
            Modifier.matchParentSize().padding(2.dp).clip(shape)
                .then(if (date == LocalDate.now() && !selected) Modifier.border(1.dp, MaterialTheme.colorScheme.primary, shape) else Modifier)
                .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                .clickable(enabled = enabled, onClick = onClick)
                .alpha(if (enabled) 1f else .32f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.height(6.dp)) {
                if (events.any {
                        it.type == EventType.Normal && (it.endDate == null || selected)
                    }
                ) {
                    CalendarDot(Accent)
                }
                if (events.any { it.type == EventType.Holiday }) {
                    CalendarDot(if (selected) MaterialTheme.colorScheme.onPrimary else HolidayEventContent)
                }
            }
        }
    }
}

@Composable
private fun CalendarLegend(modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        LegendItem(Accent, stringResource(R.string.event_legend))
        Spacer(Modifier.width(20.dp))
        RangeLegendItem(Accent, stringResource(R.string.range_legend))
        Spacer(Modifier.width(20.dp))
        LegendItem(HolidayEventContent, stringResource(R.string.holiday_legend))
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        CalendarDot(color)
        Text(text, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun CalendarDot(color: Color) = Box(Modifier.size(6.dp).clip(CircleShape).background(color))

@Composable
private fun CalendarRangeBand(
    color: Color,
    connectsPrevious: Boolean = false,
    connectsNext: Boolean = false,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(
        topStart = if (connectsPrevious) 0.dp else 14.dp,
        bottomStart = if (connectsPrevious) 0.dp else 14.dp,
        topEnd = if (connectsNext) 0.dp else 14.dp,
        bottomEnd = if (connectsNext) 0.dp else 14.dp
    )
    Box(
        modifier
            .fillMaxWidth()
            .height(28.dp)
            .padding(
                start = if (connectsPrevious) 0.dp else 5.dp,
                end = if (connectsNext) 0.dp else 5.dp
            )
            .clip(shape)
            .background(color.copy(alpha = .16f))
    )
}

@Composable
private fun RangeLegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier
                .width(20.dp)
                .height(9.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = .2f))
        )
        Text(text, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun DayEvents(
    date: LocalDate,
    events: List<Event>,
    onEdit: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val locale = LocalLocale.current.platformLocale
    val label = date.format(DateTimeFormatter.ofPattern("d MMMM", locale))
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.events_on_date, label), style = MaterialTheme.typography.titleMedium)
        if (events.isEmpty()) {
            Text(
                stringResource(R.string.no_events_on_day),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(vertical = 24.dp)
            )
        } else events.forEach { event ->
            EventItem(event, onEdit = editableAction(event, onEdit))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventListTopBar(
    expanded: Boolean,
    state: EventListState.Success?,
    onResetView: () -> Unit,
    onTabSelected: (EventListTab) -> Unit,
    onToggleHolidays: () -> Unit,
    onToggleCalendar: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.height(48.dp).clickable(onClick = onResetView),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painterResource(R.drawable.logo), stringResource(R.string.app_logo),
                        Modifier.height(42.dp).widthIn(max = 200.dp)
                    )
                }
                if (expanded && state != null && !state.showCalendar) {
                    Spacer(Modifier.width(24.dp))
                    EventListTabs(state.selectedTab, onTabSelected, Modifier.widthIn(max = 360.dp).weight(1f))
                }
            }
        },
        actions = {
            if (state != null) {
                HolidayFilterButton(
                    includeHolidays = state.includeHolidays,
                    onToggleHolidays = onToggleHolidays
                )
                IconButton(
                    onClick = onToggleCalendar,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (state.showCalendar) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else Color.Transparent,
                        contentColor = if (state.showCalendar) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(Icons.Default.DateRange, stringResource(R.string.open_calendar))
                }
            }
        }
    )
}

@Composable
private fun HolidayFilterButton(
    includeHolidays: Boolean,
    onToggleHolidays: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = { expanded = true }
        ) {
            Icon(
                painterResource(R.drawable.ic_filter_list),
                contentDescription = stringResource(R.string.filters)
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.show_holidays)) },
                leadingIcon = {
                    Checkbox(
                        checked = includeHolidays,
                        onCheckedChange = null,
                        colors = CheckboxDefaults.colors(checkedColor = HolidayEventContent)
                    )
                },
                onClick = {
                    onToggleHolidays()
                    expanded = false
                }
            )
        }
    }
}

@Composable
private fun AddEventButton(onClick: () -> Unit) = FloatingActionButton(onClick) {
    Icon(Icons.Default.Add, stringResource(R.string.add_event))
}

@Composable
private fun LoadingIndicator() = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    CircularProgressIndicator()
}

@Composable
private fun ErrorMessage(message: String) = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(
        message.ifBlank { stringResource(R.string.unknown_error) },
        style = ContrailOneTypography,
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(24.dp)
    )
}

private fun eventDate(event: Event): LocalDate =
    Instant.ofEpochMilli(event.date).atZone(ZoneId.systemDefault()).toLocalDate()

private fun eventEndDate(event: Event): LocalDate =
    event.endDate
        ?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
        ?: eventDate(event)

private fun eventEndBoundary(event: Event): Long =
    when {
        event.type == EventType.Holiday -> eventDate(event)
            .plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        event.endDate != null -> event.endDate.let {
            eventEndDate(event)
                .plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }
        else -> event.date
    }

internal data class CalendarRangeConnections(
    val previous: Boolean,
    val next: Boolean
)

internal fun calendarRangeConnections(
    date: LocalDate,
    rangeEvents: List<Event>,
    hasPreviousCell: Boolean,
    hasNextCell: Boolean
): CalendarRangeConnections = CalendarRangeConnections(
    previous = hasPreviousCell && rangeEvents.any { eventDate(it) < date },
    next = hasNextCell && rangeEvents.any { eventEndDate(it) > date }
)

private fun eventsByCalendarDate(events: List<Event>): Map<LocalDate, List<Event>> =
    buildMap<LocalDate, MutableList<Event>> {
    events.forEach { event ->
        val start = eventDate(event)
        val end = eventEndDate(event).coerceAtLeast(start)
        var date = start
        while (!date.isAfter(end)) {
            getOrPut(date) { mutableListOf() }.add(event)
            date = date.plusDays(1)
        }
    }
    }

private fun calendarInitialTimestamp(date: LocalDate): Long {
    val dateTime = if (date == LocalDate.now()) {
        LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0)
    } else {
        date.atTime(12, 0)
    }
    return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

private fun formatEventDate(event: Event, locale: Locale): String {
    val zone = ZoneId.systemDefault()
    val start = Instant.ofEpochMilli(event.date).atZone(zone)
    val endDate = event.endDate?.let { Instant.ofEpochMilli(it).atZone(zone) }
    return if (endDate != null) {
        val endFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", locale)
        val startFormatter = DateTimeFormatter.ofPattern(
            if (start.toLocalTime() == LocalTime.MIDNIGHT) "dd.MM.yyyy" else "dd.MM.yyyy, HH:mm",
            locale
        )
        "${start.format(startFormatter)} – ${endDate.format(endFormatter)}"
    } else {
        start.format(
            DateTimeFormatter.ofPattern(
                if (event.type == EventType.Holiday) "d MMMM yyyy" else "d MMMM yyyy, HH:mm",
                locale
            )
        )
    }
}

private fun previewEvents() = listOf(
    Event(1, "Wakacje", "Wyjazd nad morze", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(8)),
    Event(
        2, "Święto Konstytucji 3 Maja", "Constitution Day",
        System.currentTimeMillis() + TimeUnit.DAYS.toMillis(16), EventType.Holiday
    )
)

@Preview(showBackground = true, widthDp = 390, heightDp = 844, locale = "pl")
@Composable
private fun PortraitPreview() = HowMuchLongerTheme {
    EventListScreen({}, state = EventListState.Success(previewEvents(), allEvents = previewEvents()), onProcessIntent = {})
}

@Preview(showBackground = true, widthDp = 900, heightDp = 460, locale = "pl")
@Composable
private fun LandscapePreview() = HowMuchLongerTheme {
    EventListScreen(
        {},
        state = EventListState.Success(previewEvents(), allEvents = previewEvents(), showCalendar = true),
        onProcessIntent = {}
    )
}
