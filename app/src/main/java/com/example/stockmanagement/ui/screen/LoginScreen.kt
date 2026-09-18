package com.example.stockmanagement.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.stockmanagement.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val email by viewModel.email.collectAsState()
    val isCodeSent by viewModel.isCodeSent.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var inputCode by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
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
                text = if (isCodeSent) "認証コード入力" else "ログイン・登録",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            if (!isCodeSent) {
                // メールアドレス入力フェーズ
                OutlinedTextField(
                    value = email,
                    onValueChange = { viewModel.setEmail(it) },
                    label = { Text("メールアドレス") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading
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
                // コード入力フェーズ
                Text(
                    text = "${email} 宛に送信された6桁のコードを入力してください",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = inputCode,
                    onValueChange = { if (it.length <= 6) inputCode = it },
                    label = { Text("6桁のコード") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading
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
