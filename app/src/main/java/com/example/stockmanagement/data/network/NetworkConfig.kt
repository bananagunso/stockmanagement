package com.example.stockmanagement.data.network

import android.content.Context

object NetworkConfig {
    const val DEV_BASE_URL = "https://bananagunso-dev.f5.si/"
    const val PROD_BASE_URL = "https://bananagunso.f5.si/"
    
    var BASE_URL = DEV_BASE_URL

    fun loadSavedUrl(context: Context): String {
        val prefs = context.getSharedPreferences("network_config", Context.MODE_PRIVATE)
        val savedUrl = prefs.getString("custom_server_url", DEV_BASE_URL) ?: DEV_BASE_URL
        BASE_URL = savedUrl
        return savedUrl
    }

    fun saveUrl(context: Context, url: String) {
        var formattedUrl = url.trim()
        if (!formattedUrl.endsWith("/")) {
            formattedUrl += "/"
        }
        val prefs = context.getSharedPreferences("network_config", Context.MODE_PRIVATE)
        prefs.edit().putString("custom_server_url", formattedUrl).apply()
        BASE_URL = formattedUrl
        NetworkModule.reset()
    }

    fun resetToDefault(context: Context) {
        saveUrl(context, DEV_BASE_URL)
    }
}
