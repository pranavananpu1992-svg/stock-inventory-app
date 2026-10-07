package com.example.stockinventory.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.stockinventory.data.model.StockItem
import com.example.stockinventory.data.model.StockTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    @Query("SELECT * FROM stock_items ORDER BY name ASC")
    fun getAllItems(): Flow<List<StockItem>>

    @Query("SELECT * FROM stock_items WHERE id = :id")
    fun getItemById(id: Long): Flow<StockItem?>

    @Query("SELECT * FROM stock_items WHERE id = :id")
    suspend fun getItemByIdDirect(id: Long): StockItem?

    @Query("SELECT * FROM stock_items WHERE barcode = :barcode LIMIT 1")
    suspend fun getItemByBarcode(barcode: String): StockItem?

    @Query("SELECT * FROM stock_items WHERE sku = :sku LIMIT 1")
    suspend fun getItemBySku(sku: String): StockItem?

    @Query("SELECT * FROM stock_items WHERE quantity <= minStockThreshold ORDER BY quantity ASC")
    fun getLowStockItems(): Flow<List<StockItem>>

    @Query("SELECT DISTINCT category FROM stock_items ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: StockItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<StockItem>): List<Long>

    @Update
    suspend fun updateItem(item: StockItem)

    @Delete
    suspend fun deleteItem(item: StockItem)

    @Query("SELECT * FROM stock_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<StockTransaction>>

    @Query("SELECT * FROM stock_transactions WHERE itemId = :itemId ORDER BY timestamp DESC")
    fun getTransactionsForItem(itemId: Long): Flow<List<StockTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: StockTransaction): Long

    @Query("SELECT COUNT(*) FROM stock_items")
    suspend fun getItemCount(): Int
}
