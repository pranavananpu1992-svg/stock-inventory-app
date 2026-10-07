package com.example.stockinventory.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType(val displayName: String) {
    STOCK_IN("Stock In / Purchase"),
    STOCK_OUT("Stock Out / Sale"),
    ADJUSTMENT("Inventory Audit"),
    DAMAGE_LOSS("Damage / Spoilage"),
    RETURN("Customer Return")
}

@Entity(tableName = "stock_transactions")
data class StockTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemId: Long,
    val itemSku: String,
    val itemName: String,
    val type: TransactionType,
    val quantityChanged: Int,
    val resultingQuantity: Int,
    val unitPrice: Double = 0.0,
    val notes: String = "",
    val operator: String = "Operator",
    val timestamp: Long = System.currentTimeMillis()
)
