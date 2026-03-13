package com.sergeapps.plants.ui.control

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.material3.CircularProgressIndicator


private val SectionBlue = Color(0xFF00A9E8)
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
    onFeedClick: () -> Unit = {}
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
        },
        containerColor = DarkBg
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(innerPadding)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                ActionHeader(
                    onWaterClick = onWaterClick,
                    onFeedClick = onFeedClick
                )
            }

            item {
                ManualStatusRow(
                    status = state.currentStatus,
                    flow = state.waterFlow
                )
            }

            item {
                SectionTitle("Horaire")
            }

            item {
                ZoneSelectorRow(
                    zone = state.zone,
                    availableZones = state.availableZones,
                    onZoneChange = onZoneChange,
                    onAddClick = onAddSchedule
                )
            }

            items(state.scheduleRows, key = { it.id }) { row ->
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

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle(
                        title = "Historique",
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = onSearchHistory) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Chercher historique",
                            tint = Color(0xFF94B843)
                        )
                    }
                }
            }

            item {
                HistoryTable(rows = state.historyRows)
            }

            item {
                SectionTitle("Paramètres généraux")
            }

            item {
                GeneralParamsCard(
                    params = state.generalParams
                )
            }

            state.errorMessage?.let { error ->
                item {
                    Text(
                        text = error,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
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
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
            return@Scaffold
        }
    }
}

@Composable
private fun ActionHeader(
    onWaterClick: () -> Unit,
    onFeedClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black),
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
        modifier = Modifier
            .fillMaxWidth()
            .background(LightGray)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompactField(
            value = status,
            modifier = Modifier.width(110.dp),
            backgroundColor = Color(0xFFE6E6E6)
        )

        Spacer(modifier = Modifier.width(6.dp))

        CompactField(
            value = "",
            modifier = Modifier.width(40.dp),
            backgroundColor = Color(0xFFE6E6E6)
        )

        Spacer(modifier = Modifier.width(6.dp))

        CompactField(
            value = flow,
            modifier = Modifier.width(70.dp),
            backgroundColor = FlowBlue
        )

        Spacer(modifier = Modifier.width(6.dp))

        CompactField(
            value = "ml/min",
            modifier = Modifier.width(70.dp),
            backgroundColor = FlowBlue
        )
    }
}

@Composable
private fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SectionBlue)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Bold
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
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF404040))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompactLabel("Zone", modifier = Modifier.width(40.dp))

        Spacer(modifier = Modifier.width(6.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.width(170.dp)
        ) {
            OutlinedTextField(
                value = zone,
                onValueChange = onZoneChange,
                singleLine = true,
                modifier = Modifier
                    .menuAnchor()
                    .height(34.dp),
                textStyle = TextStyle(fontSize = 12.sp, color = Color.White),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.LightGray,
                    focusedContainerColor = Color(0xFF5A5A5A),
                    unfocusedContainerColor = Color(0xFF5A5A5A)
                ),
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
                tint = Color(0xFF3BA6FF)
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
            modifier = Modifier.width(70.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        EditableCompactField(
            value = row.duration,
            onValueChange = { onChange(row.copy(duration = it)) },
            modifier = Modifier.width(70.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

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
                tint = Color.Red
            )
        }
    }
}

@Composable
private fun HistoryTable(
    rows: List<HistoryRowUi>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CompactField(
                    value = row.date,
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color(0xFF5A5A5A)
                )
                CompactField(
                    value = row.state,
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color(0xFF5A5A5A)
                )
                CompactField(
                    value = row.flow,
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color(0xFF5A5A5A)
                )
            }
        }
    }
}

@Composable
private fun GeneralParamsCard(
    params: GeneralParamsUi
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101010))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ParamRow("Départ arrosage auto", params.autoStart)
            ParamRow("Durée d'arrosage", params.waterDuration)
            ParamRow("Durée arrosage manuel", params.manualDuration)
            ParamRow("Fertilisation", if (params.feedEnabled) "Oui" else "Non")
            ParamRow("Durée fertilisation", params.feedDuration)
            ParamRow("Fréquence MAJ", params.updateFrequency)
        }
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
            color = Color.White,
            fontSize = 13.sp,
            modifier = Modifier.width(180.dp)
        )

        CompactField(
            value = value,
            modifier = Modifier.width(100.dp),
            backgroundColor = Color(0xFF303030)
        )
    }
}

@Composable
private fun CompactLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF5A5A5A))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun CompactField(
    value: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF5A5A5A)
) {
    Box(
        modifier = modifier
            .background(backgroundColor)
            .border(1.dp, Color.LightGray, RoundedCornerShape(2.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = value,
            color = Color.Black.takeOrWhite(backgroundColor),
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
            color = Color.White,
            fontSize = 12.sp
        ),
        modifier = modifier
            .background(Color(0xFF303030), RoundedCornerShape(2.dp))
            .border(1.dp, Color.LightGray, RoundedCornerShape(2.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    )
}

private fun Color.takeOrWhite(background: Color): Color {
    return if (background == FlowBlue || background == LightGray) Color.Black else Color.White
}