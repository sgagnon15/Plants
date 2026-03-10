package com.sergeapps.plants.ui.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sergeapps.plants.vm.item.ItemsListViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import coil.compose.SubcomposeAsyncImage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsListScreen(
    onBack: () -> Unit,
    onOpenItem: (Int) -> Unit,
    viewModel: ItemsListViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()


    val shouldLoadMore = remember {
        derivedStateOf {
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            val lastVisibleIndex = visibleItems.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount

            val threshold = 3
            lastVisibleIndex >= (totalItems - 1 - threshold)
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            viewModel.loadMore()
        }
    }

   LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Articles") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onOpenItem(0)
                },
                containerColor = MaterialTheme.colorScheme.primary,      // ✅ bleu principal
                contentColor = MaterialTheme.colorScheme.onPrimary       // ✅ couleur du "+"
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Nouvel article"
                )
            }
        }
    ) { padding ->        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = state.filter,
                onValueChange = { newValue ->
                    viewModel.onFilterChanged(newValue)
                },
                label = { Text("Filtre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else if (state.filter.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onFilterChanged("") }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Effacer le filtre"
                            )
                        }
                    }
                }
            )

            if (state.isLoading) {
                Text("Chargement…")
            } else if (state.error != null) {
                Text("Erreur: ${state.error}")
                Text("Va dans Paramètres pour configurer URL/Port/Clé d'API.")
            } else {
                val listVersion = remember(state.items) { System.currentTimeMillis() }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = state.items,
                        key = { it.id }
                    ) { row ->
                        ItemRow(
                            itemId = row.id,
                            pictureRotation = row.pictureRotation,
                            itemNumber = row.itemNumber,
                            botanicalvar = row.botanicalvar,
                            quantity = row.quantity,
                            commonname = row.commonname,
                            vendor = row.vendor,
                            imageUrl = row.imageUrl,
                            thumbnailUrl = row.thumbnailUrl,
                            listVersion = listVersion,
                            onClick = onOpenItem
                        )
                    }

                    // Footer "Charger plus"
                    item {
                        if (state.isLoadingMore) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ItemRow(
    itemId: Int,
    pictureRotation: Int,
    itemNumber: String,
    botanicalvar: String,
    quantity: Double?,
    commonname: String,
    vendor: String,
    imageUrl: String?,
    thumbnailUrl: String?,
    listVersion: Long,
    onClick: (Int) -> Unit
) {
    Card(
        onClick = { onClick(itemId) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            val context = LocalContext.current

            val baseUrl = thumbnailUrl ?: imageUrl
            val finalUrl = baseUrl?.let { url ->
                if (url.contains("?")) "$url&v=$listVersion" else "$url?v=$listVersion"
            }

            val request = ImageRequest.Builder(context)
                .data(finalUrl)
                .listener(
                    onError = { req, result ->
                        android.util.Log.e("COIL", "FAILED url=${req.data}", result.throwable)
                    },
                    onSuccess = { req, _ ->
                        android.util.Log.i("COIL", "OK url=${req.data}")
                    }
                )
                .build()

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (finalUrl != null) {
                    SubcomposeAsyncImage(
                        model = request,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                rotationZ = pictureRotation.toFloat()
                            },
                        contentScale = ContentScale.Crop,
                        loading = {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        },
                        error = {
                            // Optionnel: icône ou rien
                        }
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = botanicalvar,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = formatQuantity(quantity),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "($commonname)",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}


