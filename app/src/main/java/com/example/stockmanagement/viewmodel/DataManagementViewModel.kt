package com.example.stockmanagement.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmanagement.data.database.AppDatabase
import com.example.stockmanagement.data.entity.AttributeEntity
import com.example.stockmanagement.data.entity.CategoryAttributeEntity
import com.example.stockmanagement.data.entity.CategoryEntity
import com.example.stockmanagement.data.entity.ItemAttributeValueEntity
import com.example.stockmanagement.data.entity.ItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.PrintWriter

class DataManagementViewModel(
    private val database: AppDatabase
) : ViewModel() {

    private val _event = MutableSharedFlow<String>()
    val event: SharedFlow<String> = _event

    // --- CSV Export ---
    fun exportCsv(outputStream: OutputStream) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val items = database.itemDao().getAllWithCategory().first()
                val allValues = database.itemAttributeValueDao().getAllValues().first()
                val allCatAttrs = database.categoryAttributeDao().getAllWithAttribute().first()

                PrintWriter(outputStream).use { writer ->
                    // ヘッダー (カテゴリ, アイテム名, 在庫数, 属性1名, 属性1値, 属性1単位, ...)
                    writer.println("Category,ItemName,Stock,Attr1_Name,Attr1_Value,Attr1_Unit,Attr2_Name,Attr2_Value,Attr2_Unit,Attr3_Name,Attr3_Value,Attr3_Unit")

                    items.forEach { item ->
                        val details = allValues
                            .filter { it.itemId == item.itemId }
                            .mapNotNull { valEntity ->
                                val catAttr = allCatAttrs.find { it.categoryAttributeId == valEntity.categoryattributeId }
                                catAttr?.let { Triple(it.attributeName, valEntity.value, it.unit ?: "") }
                            }

                        val base = "${item.categoryName},${item.itemName},${item.stock}"
                        val detailPart = details.joinToString(",") { "${it.first},${it.second},${it.third}" }
                        writer.println(if (detailPart.isNotEmpty()) "$base,$detailPart" else base)
                    }
                }
                _event.emit("CSVエクスポートが完了しました")
            } catch (e: Exception) {
                _event.emit("CSVエクスポートに失敗しました: ${e.message}")
            }
        }
    }

    // --- CSV Import (B: Matching & Update) ---
    fun importCsv(inputStream: InputStream) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val reader = BufferedReader(InputStreamReader(inputStream))
                val header = reader.readLine() // ヘッダーを飛ばす
                
                // データ型マスタ（デフォルト用：なければ作成）
                val dataTypes = database.dataTypeDao().getAll().first()
                val stringTypeId = dataTypes.find { it.name.lowercase().contains("string") || it.name.contains("文字列") }?.dataTypeId 
                    ?: dataTypes.firstOrNull()?.dataTypeId ?: 1

                var count = 0
                reader.lineSequence().forEach { line ->
                    val cols = line.split(",").map { it.trim() }
                    if (cols.size >= 3) {
                        val categoryName = cols[0]
                        val itemName = cols[1]
                        val stock = cols[2].toIntOrNull() ?: 0

                        if (categoryName.isNotEmpty() && itemName.isNotEmpty()) {
                            processImportRow(categoryName, itemName, stock, cols.drop(3), stringTypeId)
                            count++
                        }
                    }
                }
                _event.emit("${count}件のデータをインポート・更新しました")
            } catch (e: Exception) {
                _event.emit("CSVインポートに失敗しました: ${e.message}")
            }
        }
    }

    private suspend fun processImportRow(
        categoryName: String,
        itemName: String,
        stock: Int,
        attrCols: List<String>,
        defaultTypeId: Int
    ) {
        // 1. カテゴリの解決
        var category = database.categoryDao().getAll().first().find { it.name == categoryName }
        if (category == null) {
            database.categoryDao().insert(CategoryEntity(name = categoryName))
            category = database.categoryDao().getAll().first().find { it.name == categoryName }!!
        }

        // 2. アイテムの解決
        var item = database.itemDao().getAll().first().find { it.name == itemName && it.categoryId == category.categoryId }
        if (item == null) {
            database.itemDao().insert(ItemEntity(name = itemName, categoryId = category.categoryId, stock = stock))
            item = database.itemDao().getAll().first().find { it.name == itemName && it.categoryId == category.categoryId }!!
        } else {
            database.itemDao().update(item.copy(stock = stock, updatedAt = System.currentTimeMillis()))
        }

        // 3. 属性値の解決 (3列セット: 名, 値, 単位)
        attrCols.chunked(3).forEach { triple ->
            if (triple.size >= 2 && triple[0].isNotEmpty()) {
                val attrName = triple[0]
                val value = triple[1]
                val unit = if (triple.size == 3) triple[2] else null

                // 属性マスタの解決
                var attribute = database.attributeDao().getAll().first().find { it.name == attrName }
                if (attribute == null) {
                    database.attributeDao().insert(AttributeEntity(name = attrName, dataTypeId = defaultTypeId))
                    attribute = database.attributeDao().getAll().first().find { it.name == attrName }!!
                }

                // カテゴリ属性（紐付け）の解決
                var catAttr = database.categoryAttributeDao().getByCategoryAttributeAndUnit(category.categoryId, attribute.attributeId, unit)
                if (catAttr == null) {
                    database.categoryAttributeDao().insert(CategoryAttributeEntity(categoryId = category.categoryId, attributeId = attribute.attributeId, unit = unit))
                    catAttr = database.categoryAttributeDao().getByCategoryAttributeAndUnit(category.categoryId, attribute.attributeId, unit)!!
                } else if (catAttr.deletedAt != null) {
                    database.categoryAttributeDao().update(catAttr.copy(deletedAt = null))
                }

                // アイテム属性値の解決
                val existingVal = database.itemAttributeValueDao().getByItemAndCategoryAttribute(item.itemId, catAttr.categoryattributeId)
                if (existingVal == null) {
                    database.itemAttributeValueDao().insert(ItemAttributeValueEntity(itemId = item.itemId, categoryattributeId = catAttr.categoryattributeId, value = value))
                } else {
                    database.itemAttributeValueDao().update(existingVal.copy(value = value, updatedAt = System.currentTimeMillis()))
                }
            }
        }
    }


    fun exportDatabase(context: Context, outputStream: OutputStream) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // WALモードなどによる未反映データをフラッシュ
                database.query("PRAGMA checkpoint(FULL)", null).close()
                
                val dbFile = context.getDatabasePath("stock_management.db")
                if (dbFile.exists()) {
                    FileInputStream(dbFile).use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    _event.emit("エクスポートが完了しました")
                } else {
                    _event.emit("データベースファイルが見つかりません")
                }
            } catch (e: Exception) {
                _event.emit("エクスポートに失敗しました: ${e.message}")
            }
        }
    }

    fun restoreDatabase(context: Context, inputStream: InputStream) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. 現在のDB接続を閉じる
                database.close()

                val dbFile = context.getDatabasePath("stock_management.db")
                
                // 2. 既存のDBファイルを削除 (関連ファイルも含めて)
                val dbWal = File(dbFile.path + "-wal")
                val dbShm = File(dbFile.path + "-shm")
                if (dbFile.exists()) dbFile.delete()
                if (dbWal.exists()) dbWal.delete()
                if (dbShm.exists()) dbShm.delete()

                // 3. ファイルをコピー（全削除＆リストアに相当）
                FileOutputStream(dbFile).use { output ->
                    inputStream.use { input ->
                        input.copyTo(output)
                    }
                }

                _event.emit("リストアが完了しました。アプリを再起動してください。")
            } catch (e: Exception) {
                _event.emit("リストアに失敗しました: ${e.message}")
            }
        }
    }

    // --- Clear All Data ---
    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                database.clearAllTables()
                _event.emit("すべてのデータを消去しました")
            } catch (e: Exception) {
                _event.emit("消去に失敗しました: ${e.message}")
            }
        }
    }
}
