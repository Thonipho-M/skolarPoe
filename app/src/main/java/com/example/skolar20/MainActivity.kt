package com.example.skolar20

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.skolar20.ui.App
import com.example.skolar20.ui.theme.Skolar20Theme
import com.example.skolar20.util.LocaleHelper
import com.example.skolar20.util.Preferences
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

class MainActivity : AppCompatActivity() {

    private val REQUEST_POST_NOTIFICATIONS = 101

    // Wrap context with saved locale BEFORE anything else so resources pick correct language
    override fun attachBaseContext(newBase: Context) {
        val wrapped = LocaleHelper.applySavedLocale(newBase)
        super.attachBaseContext(wrapped)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Request POST_NOTIFICATIONS on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), REQUEST_POST_NOTIFICATIONS)
            }
        }

        enableEdgeToEdge()

        maybeAuthenticateWithBiometrics()


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showApp() {
        setContent {
            Skolar20Theme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    App()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun maybeAuthenticateWithBiometrics() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val biometricsEnabled = Preferences.isBiometricEnabled(this)

        // If no user logged in OR user disabled biometrics -> just show app normally
        if (currentUser == null || !biometricsEnabled) {
            showApp()
            return
        }

        val biometricManager = BiometricManager.from(this)
        val canAuth = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
                    or BiometricManager.Authenticators.BIOMETRIC_WEAK
        )

        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            // Device has no biometrics or not enrolled
            showApp()
            return
        }

        val executor = ContextCompat.getMainExecutor(this)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_title))
            .setSubtitle(getString(R.string.biometric_subtitle))
            .setNegativeButtonText(getString(R.string.biometric_use_password))
            .build()

        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    showApp()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Fallback: sign out and show login screen inside App()
                    auth.signOut()
                    showApp()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Failed attempt but keep prompt – user can try again
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }
}
