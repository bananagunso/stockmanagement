package com.example.stockmanagement.data.database

import android.content.Context
import androidx.room.Room
import com.example.stockmanagement.data.entity.DatabaseInfoEntity
import kotlinx.coroutines.runBlocking

object DatabaseProvider {

    @Volatile
    private var MASTER_INSTANCE: MasterDatabase? = null

    @Volatile
    private var APP_INSTANCE: AppDatabase? = null
    
    private var CURRENT_DB_NAME: String? = null

    fun getMasterDatabase(context: Context): MasterDatabase {
        return MASTER_INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                MasterDatabase::class.java,
                "master_config.db"
            )
                .allowMainThreadQueries() // 同期的なDB切り替えのためにメインスレッドでのクエリを許可
                .build()
            MASTER_INSTANCE = instance
            instance
        }
    }

    fun getDatabase(context: Context): AppDatabase {
        val master = getMasterDatabase(context)
        
        // 現在アクティブなDB情報を取得
        val activeInfo = runBlocking { master.databaseInfoDao().getActive() }
        val dbName = activeInfo?.fileName ?: "stock_management.db"

        // デフォルトのDB情報がない場合は作成（初回起動用）
        if (activeInfo == null && dbName == "stock_management.db") {
            runBlocking {
                master.databaseInfoDao().insert(
                    DatabaseInfoEntity(displayName = "デフォルト", fileName = dbName, isActive = true)
                )
            }
        }

        return synchronized(this) {
            if (APP_INSTANCE == null || CURRENT_DB_NAME != dbName) {
                APP_INSTANCE?.close()
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    dbName
                )
                    .fallbackToDestructiveMigration()
                    .build()
                APP_INSTANCE = instance
                CURRENT_DB_NAME = dbName
            }
            APP_INSTANCE!!
        }
    }

    // データベースを強制的に切り替える（インスタンスを破棄する）
    fun switchDatabase() {
        synchronized(this) {
            APP_INSTANCE?.close()
            APP_INSTANCE = null
            CURRENT_DB_NAME = null
        }
    }
}
