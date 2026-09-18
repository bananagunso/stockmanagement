package com.example.stockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stockmanagement.data.network.ApiService
import com.example.stockmanagement.util.TokenManager

class AuthViewModelFactory(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(apiService, tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
