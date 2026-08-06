package com.example.stockmanagement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.stockmanagement.navigation.AppNavigation
import com.example.stockmanagement.ui.theme.StockManagementTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            StockManagementTheme {
                AppNavigation()
            }
        }
    }
}