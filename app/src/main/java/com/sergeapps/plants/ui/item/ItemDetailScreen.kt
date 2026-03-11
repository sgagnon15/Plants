package com.sergeapps.plants.ui.item

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sergeapps.plants.vm.item.ItemDetailViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import coil.request.CachePolicy
import coil.request.ImageRequest
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExposedDropdownMenuBox
import com.sergeapps.plants.data.api.ItemDetailDto
import com.sergeapps.plants.vm.item.VendorUi
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.runtime.withFrameNanos
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.surfaceColorAtElevation
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.CameraAlt
import android.content.pm.PackageManager
import androidx.compose.material.icons.filled.ArrowBack
import androidx.core.content.ContextCompat
import android.app.Activity
import com.google.zxing.integration.android.IntentIntegrator
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.ui.graphics.graphicsLayer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Dp
import com.sergeapps.plants.R

data class InventoryRowUi(
    val stockId: Int,
    val location: String,
    val position: String?,
    val quantity: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: Int,
    onBack: () -> Unit,
    onOpenInventoryDetail: (Int) -> Unit,
    onAddToInventory: (String) -> Unit,
    viewModel: ItemDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    var showDeletePhotoDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val loadingCareAi by viewModel.loadingCareAi.collectAsState()
    var careExpanded by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri == null) {
                return@rememberLauncherForActivityResult
            }

            viewModel.onPickPhoto(uri)
            coroutineScope.launch {
                delay(80)
                viewModel.uploadPickedPhoto(context)
            }
        }
    )

    LaunchedEffect(itemId) {
        viewModel.load(itemId)
    }

    if (showDeletePhotoDialog) {
        AlertDialog(
            onDismissRequest = { showDeletePhotoDialog = false },
            title = { Text(text = "Supprimer la photo ?") },
            text = { Text(text = "Cette action est irréversible.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeletePhotoDialog = false
                        viewModel.deletePhoto()
                    }
                ) {
                    Text(text = "Supprimer")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeletePhotoDialog = false }
                ) {
                    Text(text = "Annuler")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text(if (state.isNewItem) "Nouvel article" else "Article") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    viewModel.save()
                                    onBack()
                                } catch (_: Exception) {
                                }
                            }
                        },
                        enabled = !state.isSaving
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.Check, contentDescription = "Enregistrer")
                        }
                    }
                }
            )
        }
    ) { padding ->

        val state by viewModel.state.collectAsState()

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) { Text("Chargement…") }
            }

            state.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) { Text("Erreur : ${state.error}") }
            }

            else -> {
                val itemForUi: ItemDetailDto = state.itemDetail ?: ItemDetailDto(
                    id = 0,
                    itemNumber = state.itemNumberText.ifBlank { "" },
                    botanicalVar = state.botanicalvarText.ifBlank { "" },
                    commonName = state.commonnameText.ifBlank { "" },
                    cultivar = state.cultivarText.ifBlank { "" },
                    origin = state.originText.ifBlank { "" },
                    vendor = state.vendorText.ifBlank { null },
                    creationDate = state.creationDateText.ifBlank { "" },
                    vendorUrl = null,
                    url = state.imageUrl,
                    thumbnailurl = state.thumbnailUrl,
                    quantity = state.quantity.toInt(),
                    pictureRotation = 0
                )

                val imageModel: Any? = when {
                    state.localSelectedPhotoUri != null -> state.localSelectedPhotoUri
                    careExpanded && !state.thumbnailUrl.isNullOrBlank() -> state.thumbnailUrl
                    !state.imageUrl.isNullOrBlank() -> state.imageUrl
                    !state.thumbnailUrl.isNullOrBlank() -> state.thumbnailUrl
                    else -> null
                }

                val canUploadPhoto = !state.isNewItem && (state.itemId ?: 0) > 0

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {

                    // ======================
                    // 🔒 HEADER FIXE
                    // ======================

                    PhotoHeroCard(
                        imageModel = imageModel,
                        hasRemotePhoto = !state.imageUrl.isNullOrBlank(),
                        photoVersion = state.photoVersion,
                        isUploading = state.isUploadingPhoto,
                        pictureRotation = state.pictureRotation,
                        imageHeight = if (careExpanded) 110.dp else 230.dp,
                        onPickPhoto = {
                            if (canUploadPhoto) pickImageLauncher.launch("image/*")
                        },
                        onAskDeletePhoto = { if (canUploadPhoto) showDeletePhotoDialog = true },
                        onCameraPhotoSaved = { uri ->
                            if (canUploadPhoto) {
                                viewModel.onPickPhoto(uri)
                                viewModel.uploadPickedPhoto(context)
                            }
                        },
                        createTempPhotoUri = { ctx -> viewModel.createTempPhotoUri(ctx) }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 🔒 Ici tu veux “image + no + description” fixe.
                    // Ton ItemHeaderCard contient déjà le no + description.
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // ✍️ Description (éditable)
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                DetailRowEditable(
                                    label = "No article",
                                    value = state.itemNumberText,
                                    onValueChange = viewModel::onItemNumberChanged
                                )

                                OutlinedTextField(
                                    value = state.botanicalvarText,
                                    onValueChange = viewModel::onDescriptionChanged,
                                    singleLine = false,
                                    textStyle = MaterialTheme.typography.titleMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text("Variété")
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    HorizontalDivider()

                    Spacer(modifier = Modifier.height(10.dp))

                    // ======================
                    // 📜 SECTION DU BAS SCROLLABLE
                    // ======================

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {

                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    DetailRowEditable(
                                        label = "Nom commun",
                                        value = state.commonnameText,
                                        onValueChange = viewModel::onCommonNameChanged
                                    )

                                    DetailRowEditable(
                                        label = "Genre",
                                        value = state.cultivarText,
                                        onValueChange = viewModel::onCultivarChanged
                                    )

                                    DetailRowEditable(
                                        label = "Origine",
                                        value = state.originText,
                                        onValueChange = viewModel::onOriginChanged
                                    )

                                    WikiRow(
                                        label = "Wiki",
                                        value = state.wikiText ?: "",
                                        onValueChange = viewModel::onWikiChanged
                                    )
                                }
                            }
                        }

                        item {
                            state.itemDetail?.let {
                                CareInstructionsCard(
                                    light = state.light,
                                    soil = state.soil,
                                    water = state.water,
                                    temperature = state.temperature,
                                    dormancy = state.dormancy,
                                    feed = state.feed,
                                    expanded = careExpanded,
                                    loadingCareAi = loadingCareAi,
                                    onExpandedChange = { careExpanded = it },
                                    onAiFill = {
                                        viewModel.fillCareInstructionsWithAI(
                                            state.itemDetail?.botanicalVar ?: state.botanicalvarText
                                        )
                                    },
                                    onLightChange = viewModel::onLightChanged,
                                    onSoilChange = viewModel::onSoilChanged,
                                    onWaterChange = viewModel::onWaterChanged,
                                    onTemperatureChange = viewModel::onTemperatureChanged,
                                    onDormancyChange = viewModel::onDormancyChanged,
                                    onFeedChange = viewModel::onFeedChanged
                                )                            }
                        }

                        item {
                            TotalInventoryCardDropdown(
                                totalQuantity = state.quantity.toDouble(),
                                rows = state.inventoryByLocation,
                                onRowSelected = { selected -> onOpenInventoryDetail(selected.stockId) },
                                itemNumber = ((state.itemDetail?.itemNumber ?: state.itemNumberText.toString() ?: itemId).toString()),
                                onAddToInventory = onAddToInventory
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun parseQuantityToDouble(
    quantityText: String?
): Double {
    val normalized = quantityText
        ?.trim()
        ?.replace(',', '.')
        ?: return 0.0

    return normalized.toDoubleOrNull() ?: 0.0
}

@Composable
private fun TotalInventoryCard(
    quantity: Double?,
    uom: String?
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inventaire",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = buildString {
                        if (quantity != null) {
                            append(formatQuantity(quantity))
                            if (!uom.isNullOrBlank()) {
                                append(" ")
                                append(uom)
                            }
                        } else {
                            append("—")
                        }
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        quantity == null -> MaterialTheme.colorScheme.onSurfaceVariant
                        quantity <= 0.0 -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }
        }
    }
}

fun formatQuantity(
    value: Double?
): String {
    if (value == null) {
        return "—"
    }

    val isInteger = (value % 1.0) == 0.0
    return if (isInteger) {
        value.toInt().toString()
    } else {
        String.format(java.util.Locale.CANADA_FRENCH, "%.2f", value)
    }
}

@Composable
private fun PhotoHeroCard(
    imageModel: Any?,
    hasRemotePhoto: Boolean,
    photoVersion: Long,
    pictureRotation: Int,
    isUploading: Boolean,
    imageHeight: Dp,
    onPickPhoto: () -> Unit,
    onAskDeletePhoto: () -> Unit,
    onCameraPhotoSaved: (Uri) -> Unit,
    createTempPhotoUri: (Context) -> Uri
) {
    val shape = RoundedCornerShape(20.dp)
    val context = LocalContext.current

    var pendingCameraPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var isMenuOpen by remember { mutableStateOf(false) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { wasSaved ->
        val uri = pendingCameraPhotoUri
        if (wasSaved && uri != null) {
            onCameraPhotoSaved(uri)
        }
        pendingCameraPhotoUri = null
    }

    val finalModel: Any? = when (imageModel) {
        is String -> {
            val url = imageModel
            val busted = if (url.contains("?")) "$url&v=$photoVersion" else "$url?v=$photoVersion"
            ImageRequest.Builder(context)
                .data(busted)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .diskCachePolicy(CachePolicy.DISABLED)
                .build()
        }
        else -> imageModel
    }

    Card(
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
                    .clip(shape)
            ) {
                if (finalModel != null) {
                    AsyncImage(
                        model = finalModel,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                rotationZ = pictureRotation.toFloat()
                            }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(onClick = onPickPhoto),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ajouter une photo",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        val uri = createTempPhotoUri(context)
                        pendingCameraPhotoUri = uri
                        takePictureLauncher.launch(uri)
                    }
                }

                // ✅ BOUTON CAMÉRA superposé en bas à droite
                IconButton(
                    onClick = {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasPermission) {
                            val uri = createTempPhotoUri(context)
                            pendingCameraPhotoUri = uri
                            takePictureLauncher.launch(uri)
                        } else {
                            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Prendre une photo")
                }

                // ✅ MENU (3 points) en haut à droite (si photo distante)
                if (hasRemotePhoto) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                    ) {
                        IconButton(
                            onClick = { isMenuOpen = true },
                            enabled = !isUploading
                        ) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
                        }

                        DropdownMenu(
                            expanded = isMenuOpen,
                            onDismissRequest = { isMenuOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Supprimer la photo") },
                                onClick = {
                                    isMenuOpen = false
                                    onAskDeletePhoto()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Remplacer la photo") },
                                onClick = {
                                    isMenuOpen = false
                                    onPickPhoto()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Photo,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )

                        }
                    }
                }

                if (isUploading) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.25f))
                            .padding(10.dp)
                    ) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}


@Composable
private fun ItemHeaderCard(
    title: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun LabeledValueRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.38f)
        )
        Text(
            text = value.ifBlank { "—" },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.62f)
        )
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(120.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun String?.orDash(): String {
    return if (this.isNullOrBlank()) "—" else this
}

private fun formatIsoDate(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    return try {
        val instant = Instant.parse(iso)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        formatter.withZone(ZoneId.systemDefault()).format(instant)
    } catch (_: Exception) {
        iso
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorPicker(
    vendorText: String,
    isLoading: Boolean,
    options: List<VendorUi>,
    onTextChanged: (String) -> Unit,
    onSelected: (String) -> Unit,
    onOpen: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var suppressFocusOnce by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun closeImeAndClearFocus() {
        expanded = false
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .heightIn(min = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ✅ LABEL – IDENTIQUE À DetailRow
            Text(
                text = "Fournisseur",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(120.dp)
            )

//            Spacer(modifier = Modifier.width(12.dp))

            // ✅ VALEUR – même zone que les autres champs
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = vendorText,
                    onValueChange = onTextChanged,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { closeImeAndClearFocus() }
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 2.dp)
                        .focusProperties {
                            if (suppressFocusOnce) canFocus = false
                        }
                )

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(18.dp)
                            .padding(start = 8.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .focusable(false)
                            .clickable {
                                suppressFocusOnce = true
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                if (!expanded) onOpen()
                                expanded = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ExpandMore,
                            contentDescription = "Choisir un fournisseur"
                        )
                    }
                }
            }
        }

        // 🔽 MENU
        val menuShape = RoundedCornerShape(14.dp)

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.exposedDropdownSize(matchTextFieldWidth = true),
            shape = menuShape,
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
            tonalElevation = 6.dp,
            shadowElevation = 10.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            if (!isLoading && options.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Aucun résultat") },
                    onClick = { expanded = false },
                    enabled = false
                )
            } else {
                options.forEach { vendor ->
                    val isSelected = vendor.name == vendorText

                    DropdownMenuItem(
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        text = {
                            Text(
                                text = vendor.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.LocalShipping,
                                contentDescription = null,
                                tint = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                else
                                    Color.Transparent
                            ),
                        onClick = {
                            onSelected(vendor.name)
                            closeImeAndClearFocus()
                        }
                    )
                }
            }
        }
    }

    LaunchedEffect(suppressFocusOnce) {
        if (suppressFocusOnce) {
            withFrameNanos { }
            suppressFocusOnce = false
        }
    }

    LaunchedEffect(expanded) {
        if (expanded) {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

}

fun createCameraPhotoUri(
    context: Context
): Uri {
    val cameraDirectory = File(context.cacheDir, "camera")

    if (!cameraDirectory.exists()) {
        cameraDirectory.mkdirs()
    }

    val photoFile = File(cameraDirectory, "item_photo_${System.currentTimeMillis()}.jpg")

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        photoFile
    )
}

@Composable
fun ItemPhotoWithCameraButton(
    modifier: Modifier = Modifier,
    onCameraClick: () -> Unit,
    photoContent: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        photoContent()

        IconButton(
            onClick = onCameraClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
        ) {
            Icon(
                imageVector = Icons.Filled.PhotoCamera,
                contentDescription = "Prendre une photo"
            )
        }
    }
}

@Composable
private fun CareInstructionsCard(
    light: String?,
    soil: String?,
    water: String?,
    temperature: String?,
    dormancy: String?,
    feed: String?,
    expanded: Boolean,
    loadingCareAi: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onAiFill: () -> Unit,
    onLightChange: (String) -> Unit,
    onSoilChange: (String) -> Unit,
    onWaterChange: (String) -> Unit,
    onTemperatureChange: (String) -> Unit,
    onDormancyChange: (String) -> Unit,
    onFeedChange: (String) -> Unit
) {

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandedChange(!expanded) }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Instructions de soins",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onAiFill() },
                        enabled = !loadingCareAi
                    ) {

                        if (loadingCareAi) {

                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )

                        } else {

                            Icon(
                                painter = painterResource(id = R.drawable.ic_chatgpt),
                                contentDescription = "Remplir avec ChatGPT",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(40.dp)
                            )

                        }
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Réduire" else "Développer"
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CareInstructionRow(
                        label = "Lumière",
                        value = light ?: "",
                        onValueChange = onLightChange
                    )

                    CareInstructionRow(
                        label = "Sol",
                        value = soil ?: "",
                        onValueChange = onSoilChange
                    )

                    CareInstructionRow(
                        label = "Arrosage",
                        value = water ?: "",
                        onValueChange = onWaterChange
                    )

                    CareInstructionRow(
                        label = "Température",
                        value = temperature ?: "",
                        onValueChange = onTemperatureChange
                    )

                    CareInstructionRow(
                        label = "Dormance",
                        value = dormancy ?: "",
                        onValueChange = onDormancyChange
                    )

                    CareInstructionRow(
                        label = "Fertilisation",
                        value = feed ?: "",
                        onValueChange = onFeedChange
                    )
                }
            }
        }
    }
}

@Composable
private fun CareInstructionRow(
    label: String,
    value: String?,
    onValueChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        Text(
            text = label,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall
        )

        OutlinedTextField(
            value = value ?: "",
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontStyle = FontStyle.Italic
            )
        )
    }
}

@Composable
private fun TotalInventoryCardDropdown(
    totalQuantity: Double,
    rows: List<InventoryRowUi>,
    onRowSelected: (InventoryRowUi) -> Unit,
    itemNumber: String,
    onAddToInventory: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val totalText = buildString {
        if (totalQuantity != 0.0) {
            append(formatQuantity(totalQuantity))
        } else {
            append("—")
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(0.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { expanded = true }
                    .padding(vertical = 0.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Inventaire",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = totalText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { onAddToInventory(itemNumber) }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32)
                        )
                    }
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                if (rows.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Aucun inventaire") },
                        onClick = { expanded = false },
                        enabled = false
                    )
                } else {
                    rows.forEach { row ->
                        val left = buildString {
                            append(row.location)
                            if (!row.position.isNullOrBlank()) {
                                append(" / Casier ")
                                append(row.position)
                            }
                        }

                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = left,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            onClick = {
                                expanded = false
                                onRowSelected(row)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailEditableRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean,
    readOnly: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(120.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        BasicTextField(
            value = value,
            onValueChange = { if (!readOnly) onValueChange(it) },
            singleLine = singleLine,
            textStyle = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DetailRowEditable(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    placeholder: String = "—"
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(120.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        BasicTextField(
            value = value,
            onValueChange = { if (!readOnly) onValueChange(it) },
            singleLine = singleLine,
            textStyle = MaterialTheme.typography.bodySmall.copy(
                color = if (readOnly)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 2.dp),
            decorationBox = { innerTextField ->
                if (value.isBlank()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                innerTextField()
            }
        )
    }
}

private class ZxingScanContract :
    ActivityResultContract<ZxingScanRequest, String?>() {

    override fun createIntent(context: Context, input: ZxingScanRequest): Intent {
        val integrator = IntentIntegrator(input.activity)
            .setPrompt(input.prompt)
            .setBeepEnabled(true)
            .setOrientationLocked(false)
            .setBarcodeImageEnabled(false)

        if (input.formats.isNotEmpty()) {
            integrator.setDesiredBarcodeFormats(input.formats)
        }

        return integrator.createScanIntent()
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        val result = IntentIntegrator.parseActivityResult(resultCode, intent)
        return result?.contents
    }
}

private data class ZxingScanRequest(
    val activity: Activity,
    val formats: Collection<String>,
    val prompt: String
)

@Composable
private fun ScanTextRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    iconContentDescription: String,
    onScanClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(120.dp)
        )

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 2.dp)
            )

            IconButton(
                onClick = onScanClick,
                modifier = Modifier.size(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoCamera,
                    contentDescription = iconContentDescription
                )
            }
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val rowModifier =
        if (onClick != null) {
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { onClick() }
                .padding(vertical = 6.dp)
        } else {
            modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        }

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(120.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun WikiRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(120.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.weight(1f)
            )

            if (value.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .size(18.dp)
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(value))
                            context.startActivity(intent)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Ouvrir le lien Wiki",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}