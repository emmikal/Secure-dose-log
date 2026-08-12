package com.emmikal.securedoselog.security

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.emmikal.securedoselog.DoseLogApplication
import com.emmikal.securedoselog.MainActivity
import com.emmikal.securedoselog.R
import com.emmikal.securedoselog.databinding.ActivityLockBinding

class LockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockBinding
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private val allowedAuthenticators = BIOMETRIC_STRONG or DEVICE_CREDENTIAL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.unlock_dose_log))
            .setAllowedAuthenticators(allowedAuthenticators)
            .build()

        biometricPrompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            authCallback
        )

        binding.unlockButton.setOnClickListener {
            attemptUnlock()
        }
    }

    override fun onStart() {
        super.onStart()
        attemptUnlock()
    }

    private fun attemptUnlock() {
        val manager = BiometricManager.from(this)

        when (manager.canAuthenticate(allowedAuthenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                biometricPrompt.authenticate(promptInfo)
            }

            else -> {
                binding.lockStatusText.text =
                    getString(R.string.lock_no_auth_available)
            }
        }
    }

    private val authCallback = object : BiometricPrompt.AuthenticationCallback() {

        override fun onAuthenticationSucceeded(
            result: BiometricPrompt.AuthenticationResult
        ) {
            super.onAuthenticationSucceeded(result)

            (application as DoseLogApplication).unlockApp()

            startActivity(
                Intent(this@LockActivity, MainActivity::class.java)
            )

            finish()
        }

        override fun onAuthenticationError(
            errorCode: Int,
            errString: CharSequence
        ) {
            super.onAuthenticationError(errorCode, errString)

            Toast.makeText(
                this@LockActivity,
                errString,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}