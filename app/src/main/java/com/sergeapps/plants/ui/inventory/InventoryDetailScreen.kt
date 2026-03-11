package com.sergeapps.plants.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
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
                        enabled = state.detail != null && state.error == null
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
                        Text(
                            text = "No. article: ${detail?.itemNumber}",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    detail?.stockId?.let { onOpenItemDetail(it) }
                                }
                                .padding(vertical = 4.dp)
                        )
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
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

                            OutlinedTextField(
                                value = state.editBinNum,
                                onValueChange = { viewModel.updateBinNum(it) },
                                label = { Text("Casier") },
                                modifier = Modifier.weight(1f),
                                readOnly = !isEditing
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
                        PropertyRow(
                            label = "Fournisseur",
                            value = detail?.vendor.orEmpty(),
                            onChange = viewModel::onVendorChanged
                        )

                        PropertyRow(
                            label = "Date d'achat",
                            value = detail?.purchaseDate.orEmpty(),
                            onChange = viewModel::onVendorChanged
                        )

                        PropertyRow(
                            label = "Transplantation",
                            value = detail?.lastTransplant.orEmpty(),
                            onChange = viewModel::onVendorChanged
                        )

                        PropertyRow(
                            label = "Division",
                            value = detail?.lastDivision.orEmpty(),
                            onChange = viewModel::onVendorChanged
                        )

                        PropertyRow(
                            label = "Fertilisation",
                            value = detail?.lastFeeding.orEmpty(),
                            onChange = viewModel::onVendorChanged
                        )

                        PropertyRow(
                            label = "Prix payé",
                            value = detail?.purchasePrice.toString(),
                            onChange = viewModel::onVendorChanged
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
    onChange: (String) -> Unit
) {

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

        BasicTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier
                .weight(1f)
                .height(26.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}