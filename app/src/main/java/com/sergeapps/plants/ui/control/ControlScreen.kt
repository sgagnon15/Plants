package com.sergeapps.plants.ui.control

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sergeapps.plants.vm.control.ControlUiState
import com.sergeapps.plants.vm.control.GeneralParamsUi
import com.sergeapps.plants.vm.control.HistoryRowUi
import com.sergeapps.plants.vm.control.ScheduleRowUi

private val DarkBg = Color.Black
private val LightGray = Color(0xFFCCCCCC)
private val FlowBlue = Color(0xFFB7CBFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlScreen(
    state: ControlUiState,
    onBack: () -> Unit,
    onZoneChange: (String) -> Unit,
    onAddSchedule: () -> Unit,
    onDeleteSchedule: (Int) -> Unit,
    onScheduleChange: (Int, ScheduleRowUi) -> Unit,
    onSearchHistory: () -> Unit,
    onWaterClick: () -> Unit = {},
    onFeedClick: () -> Unit = {},
    onControllerChange: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contrôle des plantes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    SectionCard(title = "Contrôleur") {
                        ControllerSelectorRow(
                            selectedController = state.selectedControllerName,
                            availableControllers = state.availableControllers.map { it.controllerName },
                            onControllerChange = onControllerChange
                        )
                    }
                }

                item {
                    SectionCard(title = "Actions") {
                        ActionHeader(
                            onWaterClick = onWaterClick,
                            onFeedClick = onFeedClick
                        )
                    }
                }

                item {
                    SectionCard(title = "Statut") {
                        ManualStatusRow(
                            status = state.currentStatus,
                            flow = state.waterFlow
                        )
                    }
                }

                item {
                    SectionCard(title = "Horaire") {
                        ZoneSelectorRow(
                            zone = state.zone,
                            availableZones = state.availableZones,
                            onZoneChange = onZoneChange,
                            onAddClick = onAddSchedule
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            state.scheduleRows.forEach { row ->
                                ScheduleEditorRow(
                                    row = row,
                                    onChange = { updatedRow ->
                                        onScheduleChange(row.id, updatedRow)
                                    },
                                    onDelete = {
                                        onDeleteSchedule(row.id)
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    SectionCard(title = "Historique") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.weight(1f))

                            IconButton(onClick = onSearchHistory) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Chercher historique",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        HistoryTable(rows = state.historyRows)
                    }
                }

                item {
                    SectionCard(title = "Paramètres généraux") {
                        GeneralParamsContent(
                            params = state.generalParams
                        )
                    }
                }

                state.errorMessage?.let { error ->
                    item {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun ActionHeader(
    onWaterClick: () -> Unit,
    onFeedClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onWaterClick,
            modifier = Modifier.size(72.dp)
        ) {
            Text("💧", fontSize = 34.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        IconButton(
            onClick = onFeedClick,
            modifier = Modifier.size(72.dp)
        ) {
            Text("🪴", fontSize = 30.sp)
        }
    }
}

@Composable
private fun ManualStatusRow(
    status: String,
    flow: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompactField(
            value = status,
            modifier = Modifier.width(120.dp),
            backgroundColor = LightGray
        )

        Spacer(modifier = Modifier.width(6.dp))

        CompactField(
            value = flow,
            modifier = Modifier.width(80.dp),
            backgroundColor = FlowBlue
        )

        Spacer(modifier = Modifier.width(6.dp))

        CompactField(
            value = "ml/min",
            modifier = Modifier.width(80.dp),
            backgroundColor = FlowBlue
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ZoneSelectorRow(
    zone: String,
    availableZones: List<String>,
    onZoneChange: (String) -> Unit,
    onAddClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Zone",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(48.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = zone,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                availableZones.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            onZoneChange(item)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = {},
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Valider",
                tint = Color(0xFF3EAF2C)
            )
        }

        IconButton(
            onClick = onAddClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Ajouter",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ScheduleEditorRow(
    row: ScheduleRowUi,
    onChange: (ScheduleRowUi) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        EditableCompactField(
            value = row.startTime,
            onValueChange = { onChange(row.copy(startTime = it)) },
            modifier = Modifier.width(80.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        EditableCompactField(
            value = row.duration,
            onValueChange = { onChange(row.copy(duration = it)) },
            modifier = Modifier.width(80.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = row.fertilizer,
            onCheckedChange = { onChange(row.copy(fertilizer = it)) }
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Supprimer",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun HistoryTable(
    rows: List<HistoryRowUi>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CompactField(
                    value = row.date,
                    modifier = Modifier.weight(1f)
                )

                CompactField(
                    value = row.state,
                    modifier = Modifier.weight(1f)
                )

                CompactField(
                    value = row.flow,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun GeneralParamsContent(
    params: GeneralParamsUi
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ParamRow("Départ arrosage auto", params.autoStart)
        ParamRow("Durée d'arrosage", params.waterDuration.toString())
        ParamRow("Durée arrosage manuel", params.manualDuration.toString())
        ParamRow("Fertilisation", if (params.feedEnabled) "Oui" else "Non")
        ParamRow("Durée fertilisation", params.feedDuration.toString())
        ParamRow("Fréquence MAJ", params.updateFrequency)
    }
}

@Composable
private fun ParamRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(190.dp)
        )

        CompactField(
            value = value,
            modifier = Modifier.width(110.dp)
        )
    }
}

@Composable
private fun CompactField(
    value: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = value,
            color = if (backgroundColor == LightGray || backgroundColor == FlowBlue) {
                Color.Black
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun EditableCompactField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp
        ),
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
    )
}

@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ControllerSelectorRow(
    selectedController: String,
    availableControllers: List<String>,
    onControllerChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedController,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            enabled = availableControllers.isNotEmpty(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableControllers.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        android.util.Log.d("ControlScreen", "Controller clicked: $item")
                        expanded = false
                        onControllerChange(item)
                    }
                )
            }
        }
    }
}

/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ControllerSelectorRow(
    selectedController: String,
    availableControllers: List<String>,
    onControllerChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = selectedController,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                availableControllers.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            onControllerChange(item)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}*/