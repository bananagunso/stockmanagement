package com.example.stockmanagement.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String, email: String? = null, role: String? = "manager") {
        val editor = prefs.edit().putString("auth_token", token)
        if (!email.isNullOrEmpty()) {
            editor.putString("user_email", email)
        }
        if (!role.isNullOrEmpty()) {
            editor.putString("user_role", role)
        }
        editor.apply()
    }

    fun getToken(): String? {
        return prefs.getString("auth_token", null)
    }

    fun getEmail(): String? {
        return prefs.getString("user_email", null)
    }

    fun getRole(): String {
        return prefs.getString("user_role", "manager") ?: "manager"
    }

    fun clearToken() {
        prefs.edit().remove("auth_token").remove("user_email").remove("user_role").apply()
    }
}
