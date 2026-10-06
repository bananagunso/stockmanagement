package com.example.stockmanagement.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.stockmanagement.data.network.NetworkConfig
import com.example.stockmanagement.util.StringUtil
import com.example.stockmanagement.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val email by viewModel.email.collectAsState()
    val isCodeSent by viewModel.isCodeSent.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var inputCode by remember { mutableStateOf("") }
    var showServerConfigDialog by remember { mutableStateOf(false) }
    var currentServerUrl by remember { mutableStateOf(NetworkConfig.loadSavedUrl(context)) }

    LaunchedEffect(Unit) {
        currentServerUrl = NetworkConfig.loadSavedUrl(context)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("在庫管理") },
                actions = {
                    TextButton(onClick = {
                        currentServerUrl = NetworkConfig.loadSavedUrl(context)
                        showServerConfigDialog = true
                    }) {
                        Text("⚙️ サーバー設定")
                    }
                }
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isCodeSent) "認証コード入力" else "ログイン",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                if (!isCodeSent) {
                    // メールアドレス入力フェーズ（英数字・記号キーボード固定 ＋ 自動半角変換）
                    OutlinedTextField(
                        value = email,
                        onValueChange = { input ->
                            val halfWidthEmail = StringUtil.toHalfWidth(input).replace(" ", "")
                            viewModel.setEmail(halfWidthEmail)
                        },
                        label = { Text("メールアドレス") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.requestCode() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = email.contains("@") && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("認証コードを送信")
                        }
                    }
                } else {
                    // コード入力フェーズ（数字テンキー固定 ＋ 数字のみ・最大6桁制御）
                    Text(
                        text = "${email} 宛に送信された6桁のコードを入力してください",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = inputCode,
                        onValueChange = { input ->
                            val digitsOnly = StringUtil.toHalfWidth(input).filter { it.isDigit() }
                            if (digitsOnly.length <= 6) {
                                inputCode = digitsOnly
                            }
                        },
                        label = { Text("6桁のコード") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.verifyCode(inputCode, onLoginSuccess) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = inputCode.length == 6 && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("ログイン")
                        }
                    }

                    TextButton(onClick = { viewModel.reset() }) {
                        Text("メールアドレスをやり直す")
                    }
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showServerConfigDialog) {
        AlertDialog(
            onDismissRequest = { showServerConfigDialog = false },
            title = { Text("接続先サーバー設定") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Web / DB サーバーの Base URL を設定してください。",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = currentServerUrl,
                        onValueChange = { currentServerUrl = it },
                        label = { Text("サーバー URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    TextButton(
                        onClick = {
                            currentServerUrl = NetworkConfig.DEV_BASE_URL
                        }
                    ) {
                        Text("デフォルトアドレスに戻す")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (currentServerUrl.isNotBlank() && currentServerUrl.startsWith("http")) {
                            NetworkConfig.saveUrl(context, currentServerUrl)
                            showServerConfigDialog = false
                            Toast.makeText(context, "接続先サーバーを変更しました", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "有効な URL (http:// または https://) を入力してください", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { showServerConfigDialog = false }) {
                    Text("キャンセル")
                }
            }
        )
    }
}
