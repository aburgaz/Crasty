package com.crasty.app.ui.screens.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.crasty.app.R
import com.crasty.app.ui.components.DatePickerModal
import com.crasty.app.ui.components.TimePickerModal
import com.crasty.app.ui.components.fullDateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectEditorScreen(
    projectId: Long,
    onBack: () -> Unit,
    onSaved: (id: Long, isNew: Boolean) -> Unit,
    viewModel: ProjectEditorViewModel = viewModel(factory = ProjectEditorViewModel.factory(projectId)),
) {
    val state = viewModel.state
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (state.isNew) R.string.new_project else R.string.edit_project)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text(stringResource(R.string.field_project_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(stringResource(R.string.field_deadline), style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = { showDatePicker = true },
                    label = { Text(state.deadline.toLocalDate().format(fullDateFormatter)) },
                    leadingIcon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
                )
                AssistChip(
                    onClick = { showTimePicker = true },
                    label = { Text(state.deadline.toLocalTime().toString()) },
                    leadingIcon = { Icon(Icons.Outlined.Schedule, contentDescription = null) },
                )
            }
            if (state.conflictingTasks > 0) {
                Text(
                    pluralStringResource(R.plurals.error_deadline_conflicts, state.conflictingTasks, state.conflictingTasks),
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = { viewModel.save(onSaved) },
                enabled = state.name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }

    if (showDatePicker) {
        DatePickerModal(
            initial = state.deadline.toLocalDate(),
            onDismiss = { showDatePicker = false },
            onConfirm = viewModel::onDateChange,
        )
    }
    if (showTimePicker) {
        TimePickerModal(
            initial = state.deadline.toLocalTime(),
            onDismiss = { showTimePicker = false },
            onConfirm = viewModel::onTimeChange,
        )
    }
}
