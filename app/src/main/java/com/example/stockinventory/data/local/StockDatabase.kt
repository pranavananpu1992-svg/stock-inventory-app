package com.example.stockinventory.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.stockinventory.data.model.StockItem
import com.example.stockinventory.data.model.StockTransaction

@Database(
    entities = [StockItem::class, StockTransaction::class],
    version = 1,
    exportSchema = false
)
abstract class StockDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao

    companion object {
        @Volatile
        private var INSTANCE: StockDatabase? = null

        fun getDatabase(context: Context): StockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StockDatabase::class.java,
                    "stock_inventory.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
