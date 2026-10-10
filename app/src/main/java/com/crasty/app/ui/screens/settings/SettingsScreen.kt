package com.crasty.app.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.crasty.app.R
import com.crasty.app.data.settings.ThemeMode
import java.time.LocalDate

/** Ajustes: tema (claro/oscuro) y exportar/importar para pasar los datos a otro móvil. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val context = LocalContext.current
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var confirmImport by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.let(viewModel::export) }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let(viewModel::import) }

    LaunchedEffect(viewModel.result) {
        val message = when (val result = viewModel.result) {
            is BackupResult.Exported -> context.getString(R.string.export_done, result.count)
            is BackupResult.Imported -> context.getString(R.string.import_done, result.count)
            BackupResult.Failed -> context.getString(R.string.backup_error)
            null -> return@LaunchedEffect
        }
        snackbarHostState.showSnackbar(message)
        viewModel.resultShown()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            SectionHeader(stringResource(R.string.appearance))
            ThemeMode.entries.forEach { mode ->
                ListItem(
                    modifier = Modifier.selectable(
                        selected = mode == themeMode,
                        role = Role.RadioButton,
                        onClick = { viewModel.setThemeMode(mode) },
                    ),
                    leadingContent = { RadioButton(selected = mode == themeMode, onClick = null) },
                    headlineContent = {
                        Text(
                            stringResource(
                                when (mode) {
                                    ThemeMode.SYSTEM -> R.string.theme_system
                                    ThemeMode.LIGHT -> R.string.theme_light
                                    ThemeMode.DARK -> R.string.theme_dark
                                },
                            ),
                        )
                    },
                )
            }

            SectionHeader(stringResource(R.string.backup))
            ListItem(
                modifier = Modifier.clickable { exportLauncher.launch("crasty-${LocalDate.now()}.json") },
                leadingContent = { Icon(Icons.Outlined.FileUpload, contentDescription = null) },
                headlineContent = { Text(stringResource(R.string.export_tasks)) },
                supportingContent = { Text(stringResource(R.string.export_description)) },
            )
            ListItem(
                modifier = Modifier.clickable { confirmImport = true },
                leadingContent = { Icon(Icons.Outlined.FileDownload, contentDescription = null) },
                headlineContent = { Text(stringResource(R.string.import_tasks)) },
                supportingContent = { Text(stringResource(R.string.import_description)) },
            )
        }
    }

    if (confirmImport) {
        AlertDialog(
            onDismissRequest = { confirmImport = false },
            title = { Text(stringResource(R.string.import_tasks)) },
            text = { Text(stringResource(R.string.import_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmImport = false
                    importLauncher.launch(arrayOf("application/json", "application/octet-stream", "text/plain"))
                }) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmImport = false }) { Text(stringResource(android.R.string.cancel)) }
            },
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
    )
}
