package com.example.stockmanagement.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmanagement.data.database.DatabaseProvider
import com.example.stockmanagement.data.database.MasterDatabase
import com.example.stockmanagement.data.entity.DatabaseInfoEntity
import com.example.stockmanagement.data.network.ApiService
import com.example.stockmanagement.data.network.CreateGroupRequest
import com.example.stockmanagement.util.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class DatabaseSettingsViewModel(
    private val masterDatabase: MasterDatabase,
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _event = MutableSharedFlow<String>()
    val event: SharedFlow<String> = _event.asSharedFlow()

    val databaseList: StateFlow<List<DatabaseInfoEntity>> =
        masterDatabase.databaseInfoDao().getAll()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // サーバーからグループ一覧を取得して同期
    suspend fun refreshGroups(): List<DatabaseInfoEntity> {
        val token = tokenManager.getToken() ?: return masterDatabase.databaseInfoDao().getAllList()
        _isRefreshing.value = true
        try {
            val response = apiService.getGroups("Bearer $token")
            if (response.isSuccessful && response.body()?.success == true) {
                val remoteGroups = response.body()?.groups ?: emptyList()
                val remoteGroupIds = remoteGroups.map { it.id }.toSet()
                val localDbs = masterDatabase.databaseInfoDao().getAllList()

                // 1. サーバー上に存在する正当なグループを同調・挿入・更新
                remoteGroups.forEach { remote ->
                    val existing = localDbs.find { it.remoteGroupId == remote.id }
                    if (existing == null) {
                        val fileName = "db_remote_${remote.id}.db"
                        masterDatabase.databaseInfoDao().insert(
                            DatabaseInfoEntity(
                                displayName = remote.display_name,
                                fileName = fileName,
                                remoteGroupId = remote.id
                            )
                        )
                    } else {
                        masterDatabase.databaseInfoDao().update(
                            existing.copy(
                                displayName = remote.display_name,
                                remoteGroupId = remote.id
                            )
                        )
                    }
                }

                // 2. このユーザーが所属していない（未バインドローカルDBや前アカウントのゴミ）エントリを全削除
                localDbs.forEach { local ->
                    if (local.remoteGroupId == null || local.remoteGroupId !in remoteGroupIds) {
                        masterDatabase.databaseInfoDao().delete(local)
                    }
                }

                // 3. アクティブなDBが未選択の場合、先頭のリモートDBを自動アクティブ化
                val currentActive = masterDatabase.databaseInfoDao().getActive()
                if (currentActive == null && remoteGroups.isNotEmpty()) {
                    val firstRemote = remoteGroups.first()
                    val updatedList = masterDatabase.databaseInfoDao().getAllList()
                    val target = updatedList.find { it.remoteGroupId == firstRemote.id }
                    if (target != null) {
                        masterDatabase.databaseInfoDao().setActive(target.id)
                        DatabaseProvider.switchDatabase()
                    }
                }
            } else {
                _event.emit("グループ一覧取得エラー: " + response.message())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _event.emit("通信エラー: ${e.message}")
        } finally {
            _isRefreshing.value = false
        }
        return masterDatabase.databaseInfoDao().getAllList()
    }

    fun launchRefreshGroups() {
        viewModelScope.launch {
            refreshGroups()
        }
    }

    fun createDatabase(name: String) {
        val token = tokenManager.getToken()
        viewModelScope.launch {
            _isRefreshing.value = true
            if (token != null) {
                try {
                    val response = apiService.createGroup("Bearer $token", CreateGroupRequest(name))
                    if (response.isSuccessful && response.body()?.success == true && response.body()?.group != null) {
                        val group = response.body()!!.group!!
                        val fileName = "db_remote_${group.id}.db"
                        masterDatabase.databaseInfoDao().insert(
                            DatabaseInfoEntity(
                                displayName = group.display_name,
                                fileName = fileName,
                                remoteGroupId = group.id
                            )
                        )
                        val inserted = masterDatabase.databaseInfoDao().getAllList().find { it.remoteGroupId == group.id }
                        if (inserted != null) {
                            masterDatabase.databaseInfoDao().setActive(inserted.id)
                            DatabaseProvider.switchDatabase()
                        }
                        _isRefreshing.value = false
                        return@launch
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            _isRefreshing.value = false
        }
    }

    fun renameDatabase(info: DatabaseInfoEntity, newName: String) {
        viewModelScope.launch {
            masterDatabase.databaseInfoDao().update(
                info.copy(displayName = newName)
            )
        }
    }

    fun switchDatabase(info: DatabaseInfoEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isRefreshing.value = true
            var targetInfo = info
            val token = tokenManager.getToken()

            // サーバー未バインドのローカルDBの場合、自動的にサーバー側グループを作成して同期IDを取得
            if (targetInfo.remoteGroupId == null && targetInfo.displayName != "デフォルト" && token != null) {
                try {
                    val response = apiService.createGroup("Bearer $token", CreateGroupRequest(targetInfo.displayName))
                    if (response.isSuccessful && response.body()?.success == true && response.body()?.group != null) {
                        val remoteId = response.body()!!.group!!.id
                        targetInfo = targetInfo.copy(remoteGroupId = remoteId)
                        masterDatabase.databaseInfoDao().update(targetInfo)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            masterDatabase.databaseInfoDao().setActive(targetInfo.id)
            DatabaseProvider.switchDatabase()

            // 切り替え先DBにおける実際のユーザー権限（ロール）を更新
            if (token != null) {
                if (targetInfo.remoteGroupId != null) {
                    try {
                        val response = apiService.getGroups("Bearer $token")
                        if (response.isSuccessful && response.body()?.success == true) {
                            val remoteGroup = response.body()?.groups?.find { it.id == targetInfo.remoteGroupId }
                            val newRole = remoteGroup?.role ?: "manager"
                            tokenManager.saveToken(token, tokenManager.getEmail(), newRole)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    // 自作ローカルDBの場合は作成者（manager）ロールをセット
                    tokenManager.saveToken(token, tokenManager.getEmail(), "manager")
                }
            }

            _isRefreshing.value = false
            withContext(Dispatchers.Main) {
                onComplete()
            }
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
