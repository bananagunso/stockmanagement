package com.example.stockmanagement.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.stockmanagement.data.database.AppDatabase
import com.example.stockmanagement.data.database.DatabaseProvider
import com.example.stockmanagement.data.repository.SyncRepository
import com.example.stockmanagement.util.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SyncViewModel(
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    private val _syncEvent = MutableSharedFlow<String>()
    val syncEvent: SharedFlow<String> = _syncEvent

    fun syncData(groupId: Int = 1) {
        if (_isSyncing.value) return

        viewModelScope.launch {
            _isSyncing.value = true
            val result = syncRepository.sync(groupId)
            result.fold(
                onSuccess = { msg ->
                    _syncEvent.emit(msg)
                },
                onFailure = { error ->
                    _syncEvent.emit("同期エラー: ${error.message}")
                }
            )
            _isSyncing.value = false
        }
    }

    fun logout(
        context: Context,
        database: AppDatabase,
        tokenManager: TokenManager,
        onLoggedOut: () -> Unit
    ) {
        if (_isSyncing.value) return

        viewModelScope.launch(Dispatchers.IO) {
            _isSyncing.value = true
            try {
                // 1. 未同期データの最終自動同期を試行
                syncRepository.sync()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                // 2. ローカルDBを安全にクリア（異アカウントでのデータ混在・汚染防止）
                database.clearAllTables()

                // 3. マスタDB（MasterDatabase）に残った前ユーザーの他DBカード・設定情報を完全抹消
                val masterDb = DatabaseProvider.getMasterDatabase(context)
                masterDb.databaseInfoDao().deleteAll()
                masterDb.databaseInfoDao().setActive(1)
                DatabaseProvider.switchDatabase()

                // 4. 同期設定・タイムスタンプの消去
                context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
                    .edit().clear().apply()

                // 5. 認証トークンの消去
                tokenManager.clearToken()

                withContext(Dispatchers.Main) {
                    onLoggedOut()
                }
            } catch (e: Exception) {
                _syncEvent.emit("ログアウトエラー: ${e.message}")
            } finally {
                _isSyncing.value = false
            }
        }
    }
}

class SyncViewModelFactory(
    private val syncRepository: SyncRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SyncViewModel::class.java)) {
            return SyncViewModel(syncRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
