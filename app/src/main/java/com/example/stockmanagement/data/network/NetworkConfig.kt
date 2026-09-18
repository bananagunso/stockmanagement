package com.example.stockmanagement.data.network

object NetworkConfig {
    const val DEV_BASE_URL = "https://bananagunso-dev.f5.si/"
    const val PROD_BASE_URL = "https://bananagunso.f5.si/"
    
    // デバッグビルドかどうかで自動切り替え（または手動で書き換え）
    var BASE_URL = DEV_BASE_URL
}
