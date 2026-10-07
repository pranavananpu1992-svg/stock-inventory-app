package com.example.stockinventory.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_items")
data class StockItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sku: String,
    val name: String,
    val description: String = "",
    val category: String = "General",
    val quantity: Int = 0,
    val minStockThreshold: Int = 5,
    val unit: String = "pcs",
    val costPrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val barcode: String = "",
    val location: String = "Warehouse A",
    val supplier: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val isOutOfStock: Boolean
        get() = quantity <= 0

    val isLowStock: Boolean
        get() = quantity in 1..minStockThreshold

    val totalCostValue: Double
        get() = quantity * costPrice

    val totalSellingValue: Double
        get() = quantity * sellingPrice

    val profitMarginPercent: Double
        get() = if (costPrice > 0) ((sellingPrice - costPrice) / costPrice) * 100 else 0.0
}
