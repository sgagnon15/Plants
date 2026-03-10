package com.sergeapps.plants.ui.location

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sergeapps.plants.vm.location.LocationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationsScreen(
    onBack: () -> Unit
) {
    val viewModel: LocationsViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emplacements") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.openAddDialog() }) {
                        Icon(Icons.Filled.Add, contentDescription = "Ajouter")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading && state.locations.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.error != null && state.locations.isEmpty() -> {
                    Text(
                        text = state.error ?: "Erreur",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.locations) { location ->

                            val usageCount: Int? = state.usageCountById[location.id]
                            val canDelete: Boolean = (usageCount == 0)

                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = location.location,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )

                                        if (canDelete) {
                                            IconButton(
                                                onClick = {
                                                    viewModel.openDeleteConfirm(
                                                        id = location.id,
                                                        name = location.location
                                                    )
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Delete,
                                                    contentDescription = "Supprimer"
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Nb casiers: ${location.nbBin}")
                                    Text(text = "Type: ${location.type.orEmpty()}")

                                    if (usageCount != null) {
                                        Text(text = "Utilisé: $usageCount")
                                    }
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }

            if (state.isDeleteConfirmOpen) {
                AlertDialog(
                    onDismissRequest = { viewModel.closeDeleteConfirm() },
                    title = { Text("Supprimer") },
                    text = { Text("Supprimer l'emplacement \"${state.deleteCandidateName}\" ?") },
                    confirmButton = {
                        TextButton(
                            onClick = { viewModel.deleteConfirmed() },
                            enabled = !state.isLoading
                        ) {
                            Text("Supprimer")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { viewModel.closeDeleteConfirm() },
                            enabled = !state.isLoading
                        ) {
                            Text("Annuler")
                        }
                    }
                )
            }
            if (state.isEditDialogOpen) {
                AlertDialog(
                    onDismissRequest = { viewModel.closeEditDialog() },
                    title = { Text("Modifier emplacement") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                            if (state.error != null) {
                                Text(
                                    text = state.error.orEmpty(),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            OutlinedTextField(
                                value = state.editLocationText,
                                onValueChange = viewModel::onEditLocationChanged,
                                label = { Text("Location") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = state.editNbBinText,
                                onValueChange = viewModel::onEditNbBinChanged,
                                label = { Text("Nb casiers (nbbin)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = state.editTypeText,
                                onValueChange = viewModel::onEditTypeChanged,
                                label = { Text("Type") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { viewModel.saveEdit() },
                            enabled = !state.isLoading
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Enregistrer")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { viewModel.closeEditDialog() },
                            enabled = !state.isLoading
                        ) {
                            Text("Annuler")
                        }
                    }
                )
            }

            if (state.error != null && state.locations.isNotEmpty()) {
                Text(
                    text = state.error ?: "",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp)
                )
            }
        }
    }

    if (state.isAddDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.closeAddDialog() },
            title = { Text("Nouvel emplacement") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = state.newLocationText,
                        onValueChange = viewModel::onNewLocationChanged,
                        label = { Text("Location") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.newNbBinText,
                        onValueChange = viewModel::onNewNbBinChanged,
                        label = { Text("Nb casiers (nbbin)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.createLocation() }) {
                    Text("Créer")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeAddDialog() }) {
                    Text("Annuler")
                }
            }
        )
    }
}