package com.example.stockmanagement.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmanagement.data.database.DatabaseProvider
import com.example.stockmanagement.data.database.MasterDatabase
import com.example.stockmanagement.data.entity.DatabaseInfoEntity
import com.example.stockmanagement.data.network.ApiService
import com.example.stockmanagement.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class DatabaseSettingsViewModel(
    private val masterDatabase: MasterDatabase,
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val databaseList: StateFlow<List<DatabaseInfoEntity>> =
        masterDatabase.databaseInfoDao().getAll()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // サーバーからグループ一覧を取得して同期
    fun refreshGroups() {
        val token = tokenManager.getToken() ?: return
        
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val response = apiService.getGroups("Bearer $token")
                if (response.isSuccessful && response.body()?.success == true) {
                    val remoteGroups = response.body()?.groups ?: emptyList()
                    val localDbs = databaseList.value

                    remoteGroups.forEach { remote ->
                        // まだローカルに紐付いたグループがない場合、新しく作成
                        if (localDbs.none { it.remoteGroupId == remote.id }) {
                            val fileName = "db_remote_${remote.id}.db"
                            masterDatabase.databaseInfoDao().insert(
                                DatabaseInfoEntity(
                                    displayName = remote.display_name,
                                    fileName = fileName,
                                    remoteGroupId = remote.id
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun createDatabase(name: String) {
        viewModelScope.launch {
            val fileName = "db_${System.currentTimeMillis()}.db"
            masterDatabase.databaseInfoDao().insert(
                DatabaseInfoEntity(displayName = name, fileName = fileName)
            )
        }
    }

    fun renameDatabase(info: DatabaseInfoEntity, newName: String) {
        viewModelScope.launch {
            masterDatabase.databaseInfoDao().update(
                info.copy(displayName = newName)
            )
        }
    }

    fun switchDatabase(id: Int) {
        viewModelScope.launch {
            masterDatabase.databaseInfoDao().setActive(id)
            DatabaseProvider.switchDatabase()
            // ここでアプリのリロードが必要な場合がある（Navigationでの制御）
        }
    }

    fun deleteDatabase(context: Context, info: DatabaseInfoEntity) {
        viewModelScope.launch {
            // 現在アクティブなものは削除させない（安全のため）
            if (info.isActive) return@launch

            // 1. ファイルを物理削除
            val dbFile = context.getDatabasePath(info.fileName)
            val dbWal = File(dbFile.path + "-wal")
            val dbShm = File(dbFile.path + "-shm")
            if (dbFile.exists()) dbFile.delete()
            if (dbWal.exists()) dbWal.delete()
            if (dbShm.exists()) dbShm.delete()

            // 2. マスタから削除
            masterDatabase.databaseInfoDao().delete(info)
        }
    }
}
