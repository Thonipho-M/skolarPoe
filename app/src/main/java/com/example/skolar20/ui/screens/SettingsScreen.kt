package com.example.skolar20.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import com.example.skolar20.R
import com.example.skolar20.util.LocaleHelper
import com.example.skolar20.util.Preferences
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val scope = rememberCoroutineScope()

    var info by remember { mutableStateOf<String?>(null) }
    var biometricEnabled by remember { mutableStateOf(Preferences.isBiometricEnabled(context)) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var syncing by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf<String?>(null) }

    // Language setup
    val savedLang = Preferences.getSelectedLanguage(context) ?: "en"
    var expanded by remember { mutableStateOf(false) }

    // Important: use stringResource inside composition so it resolves per locale
    val languages = listOf(
        "en" to stringResource(id = R.string.lang_en),
        "zu" to stringResource(id = R.string.lang_zu)
    )

    var selectedLangCode by remember { mutableStateOf(savedLang) }
    var selectedLangLabel by remember {
        mutableStateOf(languages.find { it.first == savedLang }?.second ?: languages.first().second)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = stringResource(id = R.string.settings_title), style = MaterialTheme.typography.titleLarge)

        Text(
            text = auth.currentUser?.email ?: stringResource(id = R.string.settings_not_signed_in),
            style = MaterialTheme.typography.bodyMedium
        )

        Divider()

        // Language selector
        Text(text = stringResource(id = R.string.settings_language), style = MaterialTheme.typography.titleMedium)

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            // <-- menuAnchor() is the important fix
            TextField(
                value = selectedLangLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text(text = stringResource(id = R.string.settings_language_summary)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()    // <-- anchors dropdown to this field
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                languages.forEach { (code, label) ->
                    DropdownMenuItem(
                        text = { Text(text = label) },
                        onClick = {
                            expanded = false
                            selectedLangCode = code
                            selectedLangLabel = label
                            // persist selection & apply immediately
                            Preferences.setSelectedLanguage(context, code)
                            LocaleHelper.setLocale(context, code)
                            (context as? ComponentActivity)?.recreate()
                        }
                    )
                }
            }
        }

        Divider()

        // Biometric toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(id = R.string.settings_biometric))
            Switch(
                checked = biometricEnabled,
                onCheckedChange = {
                    biometricEnabled = it
                    Preferences.setBiometricEnabled(context, it)
                    info = if (it) {
                        context.getString(R.string.settings_biometric_enabled_msg)
                    } else {
                        context.getString(R.string.settings_biometric_disabled_msg)
                    }
                }
            )
        }

        // Notifications toggle (demo only)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(id = R.string.settings_notifications))
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
        }

        // Offline bookings sync
        Button(
            onClick = {
                scope.launch {
                    syncing = true
                    syncMessage = null
                    try {
                        // Debug: list pending before
                        val db = com.example.skolar20.data.local.OfflineDbHelper(context)
                        val pendingList = db.debugListAsStrings()
                        Log.d("DEBUG_SYNC", "Pending rows: ${pendingList.size}")
                        pendingList.forEach { Log.d("DEBUG_SYNC", it) }

                        val count = com.example.skolar20.data.local.syncPendingBookings(context, auth)
                        syncMessage = context.getString(R.string.settings_sync_complete, count)
                    } catch (e: Exception) {
                        syncMessage = e.localizedMessage ?: "Sync failed"
                    } finally {
                        syncing = false
                    }
                }
            },

        )
        {
            Text(
                if (syncing)
                    stringResource(id = R.string.settings_syncing)
                else
                    stringResource(id = R.string.settings_sync)
            )
        }

        if (syncMessage != null) {
            Text(syncMessage!!, style = MaterialTheme.typography.bodySmall)
        }

        Divider()

        // Sign out
        Button(
            onClick = {
                auth.signOut()
                info = context.getString(R.string.settings_signed_out)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.settings_sign_out))
        }

        if (info != null) {
            Text(info!!, color = MaterialTheme.colorScheme.primary)
        }
    }
}
