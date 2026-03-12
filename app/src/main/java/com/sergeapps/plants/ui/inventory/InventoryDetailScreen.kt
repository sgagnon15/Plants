package com.sergeapps.plants.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sergeapps.plants.vm.inventory.InventoryDetailViewModel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.setValue
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.platform.LocalFocusManager

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.Image
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.sergeapps.plants.helper.formatIsoUtcToLocal
import com.sergeapps.plants.ui.item.DetailRow
import androidx.compose.material3.TextField
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.app.DatePickerDialog
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.foundation.layout.FlowRow



@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InventoryDetailScreen(
    stockId: Int,
    initialItemNumber: String = "",
    onBack: () -> Unit,
    onOpenItemDetail: (Int) -> Unit,
    viewModel: InventoryDetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val detail    = state.detail
    val isAddMode = (stockId == 0)


    var isEditing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(stockId, initialItemNumber) {
        viewModel.load(stockId = stockId, initialItemNumber = initialItemNumber)

        if (stockId == 0) {
            isEditing = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détail inventaire") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isEditing) {
                                viewModel.saveChanges()
                            } else {
                                viewModel.startEditing()
                            }
                            isEditing = !isEditing
                        },
                        enabled = state.error == null
                    ) {
                        Icon(
                            imageVector = if (isEditing) Icons.Filled.Check else Icons.Filled.Edit,
                            contentDescription = if (isEditing) "Enregistrer" else "Modifier"
                        )
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { padding ->
        val listState = rememberLazyListState()

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.error != null) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = state.error ?: "",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                return@LazyColumn
            }

            if (detail == null && !isAddMode) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Chargement…",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                return@LazyColumn
            }

            item {
                val context = LocalContext.current
                val imageUrl = detail?.url
                    ?.takeIf { it.isNotBlank() }
                    ?.let { url ->
                        "$url?ts=${System.currentTimeMillis()}"
                    }

                if (imageUrl != null) {
                    val request = remember(imageUrl) {
                        ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(true)
                            .build()
                    }

                    AsyncImage(
                        model = request,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = "Pas d'image",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        detail?.botanicalvar?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        var locationMenuExpanded by remember { mutableStateOf(false) }

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            maxItemsInEachRow = 2
                        ) {

                            ExposedDropdownMenuBox(
                                expanded = locationMenuExpanded,
                                onExpandedChange = { if (isEditing) locationMenuExpanded = !locationMenuExpanded },
                                modifier = Modifier.weight(1.4f)
                            ) {
                                OutlinedTextField(
                                    value = state.editLocation,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Emplacement") },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationMenuExpanded)
                                    },
                                    enabled = isEditing
                                )

                                ExposedDropdownMenu(
                                    expanded = locationMenuExpanded,
                                    onDismissRequest = { locationMenuExpanded = false }
                                ) {
                                    state.locations.forEach { loc ->
                                        DropdownMenuItem(
                                            text = { Text(loc.location) },
                                            onClick = {
                                                locationMenuExpanded = false
                                                viewModel.selectLocation(loc.location)
                                            }
                                        )
                                    }
                                }
                            }

                            EditablePositionDropdownField(
                                value = state.editposition,
                                options = state.positions,
                                enabled = isEditing,
                                onValueChange = viewModel::updateposition,
                                modifier = Modifier.weight(1f)
                            )

                        }

                        val focusManager = LocalFocusManager.current
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        EditableVendorDropdownRow(
                            label = "Fournisseur",
                            value = state.vendorText,
                            options = state.vendors,
                            enabled = isEditing,
                            onValueChange = viewModel::onVendorChanged
                        )

                        DatePickerRow(
                            label = "Date d'achat",
                            value = state.purchaseDateText,
                            onChange = viewModel::onPurchaseDateChanged,
                            enabled = isEditing
                        )

                        DatePickerRow(
                            label = "Transplantation",
                            value = state.lastTransplantText,
                            onChange = viewModel::onLastTransplantChanged,
                            enabled = isEditing
                        )

                        DatePickerRow(
                            label = "Division",
                            value = state.lastDivisionText,
                            onChange = viewModel::onLastDivisionChanged,
                            enabled = isEditing
                        )

                        DatePickerRow(
                            label = "Fertilisation",
                            value = state.lastFeedingText,
                            onChange = viewModel::onLastFeedingChanged,
                            enabled = isEditing
                        )

                        PropertyRow(
                            label = "Prix payé",
                            value = state.purchasePriceText,
                            valueType = "number",
                            enabled = isEditing,
                            onChange = viewModel::onPurchasePriceChanged
                        )

                        DetailRow(
                            label = "Date de création",
                            value = formatIsoUtcToLocal(detail?.creationDate)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Supprimer cet inventaire ?") },
                text = { Text("Cette action est irréversible.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            viewModel.deleteStock(stockId = stockId) {
                                onBack()
                            }
                        }
                    ) {
                        Text("Supprimer")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text("Annuler")
                    }
                }
            )
        }
    }
}

@Composable
fun EditableRow(
    label: String,
    value: String,
    onChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = label,
            modifier = Modifier.width(100.dp)
        )

        TextField(
            value = value,
            onValueChange = onChange,
            textStyle = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            singleLine = true
        )
    }
}

@Composable
fun PropertyRow(
    label: String,
    value: String,
    valueType: String,
    enabled: Boolean = true,
    onChange: (String) -> Unit
) {
    val isNumber = valueType == "number"
    val focusManager = LocalFocusManager.current

    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        )
    }

    var hadFocus by remember { mutableStateOf(false) }
    var clearedOnFirstFocus by remember(value) { mutableStateOf(false) }

    LaunchedEffect(value) {
        if (!hadFocus && textFieldValue.text != value) {
            textFieldValue = TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(130.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (enabled) {
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    if (isNumber) {
                        val filteredText = newValue.text.filter { character ->
                            character.isDigit() || character == '.' || character == ','
                        }

                        textFieldValue = newValue.copy(text = filteredText)
                        onChange(filteredText)
                    } else {
                        textFieldValue = newValue
                        onChange(newValue.text)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(26.dp)
                    .onFocusChanged { focusState ->
                        val nowFocused = focusState.isFocused

                        if (nowFocused && !hadFocus) {
                            if (isNumber && !clearedOnFirstFocus) {
                                textFieldValue = TextFieldValue(
                                    text = "",
                                    selection = TextRange(0)
                                )
                                onChange("")
                                clearedOnFirstFocus = true
                            }
                        }

                        if (!nowFocused && hadFocus) {
                            textFieldValue = TextFieldValue(
                                text = textFieldValue.text,
                                selection = TextRange(0)
                            )
                            clearedOnFirstFocus = false
                        }

                        hadFocus = nowFocused
                    },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (isNumber) KeyboardType.Decimal else KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                    }
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
            )
        } else {
            Text(
                text = value,
                modifier = Modifier
                    .weight(1f)
                    .height(26.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DatePickerRow(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    enabled: Boolean = true
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = label,
            modifier = Modifier.width(130.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .height(26.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            BasicTextField(
                value = value,
                onValueChange = { newValue ->
                    if (enabled) {
                        onChange(newValue)
                    }
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                enabled = enabled,
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )

            Icon(
                imageVector = Icons.Filled.DateRange,
                contentDescription = "Choisir une date",
                modifier = Modifier
                    .size(16.dp)
                    .clickable {

                        if (!enabled) return@clickable

                        val calendar = Calendar.getInstance()

                        if (value.isNotBlank()) {
                            try {
                                val parsed = SimpleDateFormat(
                                    "yyyy-MM-dd",
                                    Locale.getDefault()
                                ).parse(value)

                                if (parsed != null) {
                                    calendar.time = parsed
                                }
                            } catch (_: Exception) {
                            }
                        }

                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                val pickedDate = String.format(
                                    Locale.getDefault(),
                                    "%04d-%02d-%02d",
                                    year,
                                    month + 1,
                                    day
                                )
                                onChange(pickedDate)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                tint = if (enabled)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.outline
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableVendorDropdownRow(
    label: String,
    value: String,
    options: List<String>,
    enabled: Boolean,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val filteredOptions = remember(value, options) {
        if (value.isBlank()) {
            options
        } else {
            options.filter { option ->
                option.contains(value, ignoreCase = true)
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(130.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ExposedDropdownMenuBox(
            expanded = expanded && enabled,
            onExpandedChange = {
                if (enabled) {
                    expanded = !expanded
                }
            },
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .height(26.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = {
                        onValueChange(it)
                        if (enabled) {
                            expanded = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = enabled,
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            ExposedDropdownMenu(
                expanded = expanded && filteredOptions.isNotEmpty(),
                onDismissRequest = { expanded = false }
            ) {
                filteredOptions.forEach { vendor ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = vendor,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        onClick = {
                            onValueChange(vendor)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditablePositionDropdownField(
    value: String,
    options: List<String>,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val filteredOptions = remember(value, options) {
        if (value.isBlank()) {
            options
        } else {
            options.filter {
                it.contains(value, ignoreCase = true)
            }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = {
            if (enabled) {
                expanded = !expanded
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                if (enabled) {
                    expanded = true
                }
            },
            label = { Text("Position") },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true,
            enabled = enabled,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded && enabled
                )
            }
        )

        ExposedDropdownMenu(
            expanded = expanded && filteredOptions.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            filteredOptions.forEach { position ->
                DropdownMenuItem(
                    text = { Text(position) },
                    onClick = {
                        onValueChange(position)
                        expanded = false
                    }
                )
            }
        }
    }
}