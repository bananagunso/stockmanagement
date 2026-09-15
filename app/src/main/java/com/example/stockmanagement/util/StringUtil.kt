package com.example.stockmanagement.util

object StringUtil {
    /**
     * 全角の英数字・記号を半角に変換します。
     * カタカナは変換しません。
     */
    fun toHalfWidth(input: String): String {
        return input.map { c ->
            when (c) {
                // 全角スペース
                '　' -> ' '
                // 全角英数記号 (！～～)
                in '！'..'～' -> c - 0xFEE0
                else -> c
            }
        }.joinToString("")
    }
}
