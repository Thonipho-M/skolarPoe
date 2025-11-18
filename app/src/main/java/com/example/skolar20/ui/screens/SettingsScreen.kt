package com.example.skolar20.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.skolar20.R
import com.example.skolar20.data.local.OfflineDbHelper
import com.example.skolar20.data.local.syncPendingBookings
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
    var notificationsEnabled by remember { mutableStateOf(Preferences.isNotificationsEnabled(context)) }

    // Offline sync state
    var syncing by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf<String?>(null) }
    var pendingCount by remember { mutableStateOf(0) }

    // Load pending count on start
    LaunchedEffect(Unit) {
        try {
            val db = OfflineDbHelper(context)
            pendingCount = db.getPendingCount()
        } catch (e: Exception) {
            // Ignore
        }
    }

    // Language setup
    val savedLang = LocaleHelper.getLanguage(context)
    var expanded by remember { mutableStateOf(false) }

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

        HorizontalDivider()

        // Language selector
        Text(text = stringResource(id = R.string.settings_language), style = MaterialTheme.typography.titleMedium)

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedLangLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(id = R.string.settings_language_summary)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
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
                            LocaleHelper.setLocale(context, code)
                            (context as? ComponentActivity)?.recreate()
                        }
                    )
                }
            }
        }

        HorizontalDivider()

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

        // Notifications toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(id = R.string.settings_notifications))
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = {
                    notificationsEnabled = it
                    Preferences.setNotificationsEnabled(context, it)
                    info = if (it) "Notifications enabled" else "Notifications disabled"
                }
            )
        }

        HorizontalDivider()

        // Offline sync section
        Text(text = stringResource(id = R.string.settings_offline_bookings), style = MaterialTheme.typography.titleMedium)

        if (pendingCount > 0) {
            Text(
                text = "$pendingCount booking(s) waiting to sync",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Button(
            onClick = {
                scope.launch {
                    syncing = true
                    syncMessage = null
                    try {
                        val count = syncPendingBookings(context, auth)
                        syncMessage = if (count > 0) {
                            context.getString(R.string.settings_sync_complete, count)
                        } else {
                            context.getString(R.string.settings_no_pending)
                        }
                        // Refresh pending count
                        val db = OfflineDbHelper(context)
                        pendingCount = db.getPendingCount()
                    } catch (e: Exception) {
                        syncMessage = "Sync failed: ${e.message}"
                    } finally {
                        syncing = false
                    }
                }
            },
            enabled = !syncing,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (syncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(stringResource(id = if (syncing) R.string.settings_syncing else R.string.settings_sync))
        }

        if (syncMessage != null) {
            Text(
                syncMessage!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        HorizontalDivider()

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