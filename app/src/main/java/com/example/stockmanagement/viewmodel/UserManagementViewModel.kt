package com.example.stockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.stockmanagement.data.database.MasterDatabase
import com.example.stockmanagement.data.network.ApiService
import com.example.stockmanagement.data.network.CreateGroupRequest
import com.example.stockmanagement.data.network.UserAddRequest
import com.example.stockmanagement.data.network.UserDeleteRequest
import com.example.stockmanagement.data.network.UserDto
import com.example.stockmanagement.data.network.UserListRequest
import com.example.stockmanagement.data.network.UserUpdateRoleRequest
import com.example.stockmanagement.util.TokenManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserManagementViewModel(
    private val masterDatabase: MasterDatabase,
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _users = MutableStateFlow<List<UserDto>>(emptyList())
    val users: StateFlow<List<UserDto>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _event = MutableSharedFlow<String>()
    val event: SharedFlow<String> = _event

    private suspend fun resolveValidRemoteGroupId(token: String): Int? {
        val activeInfo = masterDatabase.databaseInfoDao().getActive()
        if (activeInfo == null) {
            _event.emit("データベースが存在しません。先に『データベース切り替え』から作成してください。")
            return null
        }
        if (activeInfo.remoteGroupId != null) {
            return activeInfo.remoteGroupId
        }
        try {
            val response = apiService.createGroup("Bearer $token", CreateGroupRequest(activeInfo.displayName))
            if (response.isSuccessful && response.body()?.success == true && response.body()?.group != null) {
                val newRemoteId = response.body()!!.group!!.id
                masterDatabase.databaseInfoDao().update(activeInfo.copy(remoteGroupId = newRemoteId))
                return newRemoteId
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return activeInfo.remoteGroupId
    }

    private suspend fun fetchUsersInternal(targetGroupId: Int, token: String) {
        try {
            val response = apiService.getUsers("Bearer $token", UserListRequest(targetGroupId))
            if (response.isSuccessful && response.body()?.success == true) {
                _users.value = response.body()?.users ?: emptyList()
            } else {
                _event.emit(response.body()?.message ?: "ユーザー一覧の取得に失敗しました")
            }
        } catch (e: Exception) {
            _event.emit("通信エラーが発生しました: ${e.message}")
        }
    }

    fun loadUsers(groupId: Int = 1) {
        val token = tokenManager.getToken() ?: return
        _users.value = emptyList() // 古いキャッシュリストを即座にクリア
        _isLoading.value = true    // 同期的にローディングフラグを立てる
        viewModelScope.launch {
            try {
                val targetGroupId = resolveValidRemoteGroupId(token)
                if (targetGroupId != null) {
                    fetchUsersInternal(targetGroupId, token)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addUser(groupId: Int = 1, email: String, role: String) {
        val token = tokenManager.getToken() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val targetGroupId = resolveValidRemoteGroupId(token)
                if (targetGroupId == null) return@launch

                val response = apiService.addUser("Bearer $token", UserAddRequest(targetGroupId, email, role))
                if (response.isSuccessful && response.body()?.success == true) {
                    _event.emit("ユーザーを追加（招待）しました")
                    fetchUsersInternal(targetGroupId, token)
                } else {
                    _event.emit(response.body()?.message ?: "ユーザー追加に失敗しました")
                }
            } catch (e: Exception) {
                _event.emit("通信エラーが発生しました: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUserRole(groupId: Int = 1, userId: Int, newRole: String) {
        val token = tokenManager.getToken() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val targetGroupId = resolveValidRemoteGroupId(token)
                if (targetGroupId == null) return@launch

                val response = apiService.updateUserRole("Bearer $token", UserUpdateRoleRequest(targetGroupId, userId, newRole))
                if (response.isSuccessful && response.body()?.success == true) {
                    _event.emit("権限を変更しました")
                    fetchUsersInternal(targetGroupId, token)
                } else {
                    _event.emit(response.body()?.message ?: "権限変更に失敗しました")
                }
            } catch (e: Exception) {
                _event.emit("通信エラーが発生しました: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteUser(groupId: Int = 1, userId: Int) {
        val token = tokenManager.getToken() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val targetGroupId = resolveValidRemoteGroupId(token)
                if (targetGroupId == null) return@launch

                val response = apiService.deleteUser("Bearer $token", UserDeleteRequest(targetGroupId, userId))
                if (response.isSuccessful && response.body()?.success == true) {
                    _event.emit("ユーザーを除外しました")
                    fetchUsersInternal(targetGroupId, token)
                } else {
                    _event.emit(response.body()?.message ?: "ユーザー除外に失敗しました")
                }
            } catch (e: Exception) {
                _event.emit("通信エラーが発生しました: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class UserManagementViewModelFactory(
    private val masterDatabase: MasterDatabase,
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserManagementViewModel::class.java)) {
            return UserManagementViewModel(masterDatabase, apiService, tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
