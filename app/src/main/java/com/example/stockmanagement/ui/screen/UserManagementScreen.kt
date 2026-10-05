package com.example.stockmanagement.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stockmanagement.data.network.UserDto
import com.example.stockmanagement.util.StringUtil
import com.example.stockmanagement.viewmodel.UserManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    viewModel: UserManagementViewModel,
    groupId: Int = 1,
    currentUserEmail: String = "",
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val users by viewModel.users.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var inputEmail by remember { mutableStateOf("") }
    var inputRole by remember { mutableStateOf("user") } // "manager" or "user"

    var editingUser by remember { mutableStateOf<UserDto?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<UserDto?>(null) }

    LaunchedEffect(groupId) {
        viewModel.loadUsers(groupId)
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ユーザー管理") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("戻る") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                inputEmail = ""
                inputRole = "user"
                showAddDialog = true
            }) {
                Text("+ 招待", fontSize = 16.sp)
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (users.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("登録されているメンバーはいません", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(users) { user ->
                        UserItemRow(
                            user = user,
                            isSelf = user.email.equals(currentUserEmail, ignoreCase = true),
                            onEditRole = { editingUser = user },
                            onDelete = { showDeleteConfirm = user }
                        )
                    }
                }
            }
        }
    }

    // --- ユーザー追加（招待）ダイアログ ---
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("ユーザーの追加（招待）") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = inputEmail,
                        onValueChange = { input ->
                            inputEmail = StringUtil.toHalfWidth(input).replace(" ", "")
                        },
                        label = { Text("メールアドレス") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Text("権限", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = inputRole == "user",
                                onClick = { inputRole = "user" }
                            )
                            Text("作業員 (user)")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = inputRole == "manager",
                                onClick = { inputRole = "manager" }
                            )
                            Text("管理者 (manager)")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputEmail.contains("@")) {
                            viewModel.addUser(groupId = groupId, email = inputEmail, role = inputRole)
                            showAddDialog = false
                        } else {
                            Toast.makeText(context, "有効なメールアドレスを入力してください", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) { Text("追加") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("キャンセル") }
            }
        )
    }

    // --- 権限変更ダイアログ ---
    if (editingUser != null) {
        var selectedRole by remember { mutableStateOf(editingUser!!.role) }

        AlertDialog(
            onDismissRequest = { editingUser = null },
            title = { Text("権限の変更") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("対象: ${editingUser!!.email}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedRole == "user",
                            onClick = { selectedRole = "user" }
                        )
                        Text("作業員 (user)")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedRole == "manager",
                            onClick = { selectedRole = "manager" }
                        )
                        Text("管理者 (manager)")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateUserRole(groupId = groupId, userId = editingUser!!.id, newRole = selectedRole)
                        editingUser = null
                    }
                ) { Text("変更保存") }
            },
            dismissButton = {
                TextButton(onClick = { editingUser = null }) { Text("キャンセル") }
            }
        )
    }

    // --- ユーザー除外確認ダイアログ ---
    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("ユーザーの除外") },
            text = { Text("「${showDeleteConfirm!!.email}」をグループから除外しますか？") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteUser(groupId = groupId, userId = showDeleteConfirm!!.id)
                        showDeleteConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("除外") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("キャンセル") }
            }
        )
    }
}

@Composable
fun UserItemRow(
    user: UserDto,
    isSelf: Boolean = false,
    onEditRole: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isSelf) "${user.email} (あなた)" else user.email,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (user.role == "manager") "管理者 (manager)" else "作業員 (user)",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (user.role == "manager") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            }

            if (!isSelf) {
                OutlinedButton(
                    onClick = onEditRole,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("権限", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("除外", fontSize = 12.sp)
                }
            }
        }
    }
}
