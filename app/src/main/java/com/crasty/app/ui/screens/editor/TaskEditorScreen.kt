package com.crasty.app.ui.screens.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.crasty.app.R
import com.crasty.app.domain.model.RepeatUnit
import com.crasty.app.ui.components.DatePickerModal
import com.crasty.app.ui.components.TimePickerModal
import com.crasty.app.ui.components.dateTimeFormatter
import com.crasty.app.ui.components.fullDateFormatter
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorScreen(
    taskId: Long,
    initialDate: LocalDate,
    projectId: Long,
    onDone: () -> Unit,
) {
    val viewModel: TaskEditorViewModel =
        viewModel(factory = TaskEditorViewModel.factory(taskId, initialDate, projectId))
    val state = viewModel.state
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (state.isNew) R.string.new_task else R.string.edit_task)) },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (!state.isNew) {
                        IconButton(onClick = { viewModel.delete(onDone) }) {
                            Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.delete))
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .imePadding()
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                state.project?.let { project ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Folder, contentDescription = null)
                        Text(
                            stringResource(R.string.task_project, project.name, project.deadline.format(dateTimeFormatter)),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                OutlinedTextField(
                    value = state.title,
                    onValueChange = viewModel::onTitleChange,
                    label = { Text(stringResource(R.string.field_title)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = viewModel::onNotesChange,
                    label = { Text(stringResource(R.string.field_notes)) },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                AssistChip(
                    onClick = { showDatePicker = true },
                    label = {
                        val date = state.date.format(fullDateFormatter)
                        Text(if (state.recurrence != null) stringResource(R.string.starts_on, date) else date)
                    },
                    leadingIcon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = { showTimePicker = true },
                        label = { Text(state.reminderTime?.toString() ?: stringResource(R.string.no_reminder)) },
                        leadingIcon = { Icon(Icons.Outlined.Notifications, contentDescription = null) },
                    )
                    if (state.reminderTime != null) {
                        IconButton(onClick = { viewModel.onReminderChange(null) }) {
                            Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.remove_reminder))
                        }
                    }
                }
                if (state.canRepeat) {
                    RepeatSection(
                        repeat = state.repeat,
                        interval = state.repeatInterval,
                        unit = state.repeatUnit,
                        onRepeatChange = viewModel::onRepeatChange,
                        onIntervalChange = viewModel::onRepeatIntervalChange,
                        onUnitChange = viewModel::onRepeatUnitChange,
                    )
                }
                if (state.violatesDeadline) {
                    Text(
                        stringResource(R.string.error_after_deadline, state.project!!.deadline.format(dateTimeFormatter)),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            Button(
                onClick = { viewModel.save(onDone) },
                enabled = state.title.isNotBlank() && !state.violatesDeadline,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }

    if (showDatePicker) {
        DatePickerModal(
            initial = state.date,
            onDismiss = { showDatePicker = false },
            onConfirm = viewModel::onDateChange,
        )
    }
    if (showTimePicker) {
        TimePickerModal(
            initial = state.reminderTime ?: LocalTime.of(9, 0),
            onDismiss = { showTimePicker = false },
            onConfirm = viewModel::onReminderChange,
        )
    }
}

/** "Repetir" + cada cuánto (N días / semanas / meses). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepeatSection(
    repeat: Boolean,
    interval: Int,
    unit: RepeatUnit,
    onRepeatChange: (Boolean) -> Unit,
    onIntervalChange: (Int) -> Unit,
    onUnitChange: (RepeatUnit) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.Repeat, contentDescription = null)
            Text(stringResource(R.string.repeat), modifier = Modifier.weight(1f))
            Switch(checked = repeat, onCheckedChange = onRepeatChange)
        }
        if (!repeat) return@Column
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.repeat_every))
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { onIntervalChange(interval - 1) }, enabled = interval > 1) {
                Icon(Icons.Outlined.Remove, contentDescription = null)
            }
            Text("$interval", style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = { onIntervalChange(interval + 1) }) {
                Icon(Icons.Outlined.Add, contentDescription = null)
            }
        }
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            RepeatUnit.entries.forEachIndexed { index, option ->
                val label = when (option) {
                    RepeatUnit.DAYS -> R.plurals.unit_days
                    RepeatUnit.WEEKS -> R.plurals.unit_weeks
                    RepeatUnit.MONTHS -> R.plurals.unit_months
                }
                SegmentedButton(
                    selected = option == unit,
                    onClick = { onUnitChange(option) },
                    shape = SegmentedButtonDefaults.itemShape(index, RepeatUnit.entries.size),
                ) {
                    Text(pluralStringResource(label, interval))
                }
            }
        }
    }
}
