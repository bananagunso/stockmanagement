package com.example.stockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmanagement.data.network.AuthResponse
import com.example.stockmanagement.data.network.AuthRequestCodeRequest
import com.example.stockmanagement.data.network.AuthVerifyCodeRequest
import com.example.stockmanagement.data.network.ApiService
import com.example.stockmanagement.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _isCodeSent = MutableStateFlow(false)
    val isCodeSent: StateFlow<Boolean> = _isCodeSent

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun setEmail(email: String) {
        _email.value = email
    }

    // 1. 認証コードをリクエスト
    fun requestCode() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = apiService.requestCode(AuthRequestCodeRequest(_email.value))
                if (response.isSuccessful && response.body()?.success == true) {
                    _isCodeSent.value = true
                } else {
                    _errorMessage.value = response.body()?.message ?: "コードの送信に失敗しました"
                }
            } catch (e: Exception) {
                _errorMessage.value = "通信エラーが発生しました"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 2. コードを検証してログイン
    fun verifyCode(code: String, onLoginSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = apiService.verifyCode(AuthVerifyCodeRequest(_email.value, code))
                val authResponse = response.body()
                if (response.isSuccessful && authResponse?.success == true && authResponse.token != null) {
                    tokenManager.saveToken(authResponse.token)
                    onLoginSuccess()
                } else {
                    _errorMessage.value = authResponse?.message ?: "認証に失敗しました"
                }
            } catch (e: Exception) {
                _errorMessage.value = "通信エラーが発生しました"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun reset() {
        _isCodeSent.value = false
        _errorMessage.value = null
    }
}
