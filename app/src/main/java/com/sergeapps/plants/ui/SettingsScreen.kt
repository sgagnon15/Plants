package com.sergeapps.plants.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.sergeapps.plants.vm.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    var updateMessage by remember { mutableStateOf<String?>(null) }

    val updateLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            updateMessage = "Résultat mise à jour: ${result.resultCode}"
        }

    fun checkAndStartUpdate() {
        updateMessage = "Vérification en cours…"

        val appUpdateManager = AppUpdateManagerFactory.create(context)
        val updateInfoTask = appUpdateManager.appUpdateInfo

        updateInfoTask
            .addOnSuccessListener { info ->
                val updateAvailable =
                    info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE

                // FLEXIBLE = non-bloquant. Tu peux changer à IMMEDIATE si tu veux forcer.
                val updateType = AppUpdateType.FLEXIBLE

                if (updateAvailable && info.isUpdateTypeAllowed(updateType)) {
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        updateLauncher,
                        AppUpdateOptions.newBuilder(updateType).build()
                    )
                } else {
                    updateMessage = "Aucune mise à jour disponible."
                }
            }
            .addOnFailureListener { e ->
                updateMessage = "Erreur vérification: ${e.message ?: "inconnue"}"
            }
    }

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.baseUrl,
                onValueChange = viewModel::setBaseUrl,
                label = { Text("URL (ex: https://homeapi.ddns.net)") },
                singleLine = true
            )

            OutlinedTextField(
                value = state.port,
                onValueChange = viewModel::setPort,
                label = { Text("Port (ex: 443)") },
                singleLine = true
            )

            OutlinedTextField(
                value = state.apiKey,
                onValueChange = viewModel::setApiKey,
                label = { Text("Clé d'API (X-API-Key)") },
                singleLine = true
            )

            Button(onClick = viewModel::save) { Text("Enregistrer") }
            state.savedMessage?.let { Text(it) }

            Card(modifier = Modifier.fillMaxSize().padding(top = 4.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Mise à jour")
                    Button(onClick = { checkAndStartUpdate() }) {
                        Text("Vérifier la mise à jour")
                    }
                    updateMessage?.let { Text(it) }
                }
            }
        }
    }
}

fun Context.findActivity(): Activity {
    var currentContext: Context = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    error("Activity introuvable depuis ce Context")
}