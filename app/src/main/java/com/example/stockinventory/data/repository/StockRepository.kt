package com.example.stockinventory.data.repository

import com.example.stockinventory.data.local.StockDao
import com.example.stockinventory.data.model.StockItem
import com.example.stockinventory.data.model.StockTransaction
import com.example.stockinventory.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

class StockRepository(private val stockDao: StockDao) {

    val allItems: Flow<List<StockItem>> = stockDao.getAllItems()
    val lowStockItems: Flow<List<StockItem>> = stockDao.getLowStockItems()
    val allTransactions: Flow<List<StockTransaction>> = stockDao.getAllTransactions()
    val categories: Flow<List<String>> = stockDao.getAllCategories()

    fun getItemById(id: Long): Flow<StockItem?> = stockDao.getItemById(id)

    fun getTransactionsForItem(itemId: Long): Flow<List<StockTransaction>> =
        stockDao.getTransactionsForItem(itemId)

    suspend fun findByBarcode(barcode: String): StockItem? =
        stockDao.getItemByBarcode(barcode)

    suspend fun findBySku(sku: String): StockItem? =
        stockDao.getItemBySku(sku)

    suspend fun insertItem(item: StockItem): Long =
        stockDao.insertItem(item)

    suspend fun updateItem(item: StockItem) =
        stockDao.updateItem(item)

    suspend fun deleteItem(item: StockItem) =
        stockDao.deleteItem(item)

    suspend fun adjustStock(
        itemId: Long,
        type: TransactionType,
        quantityDelta: Int,
        notes: String,
        operator: String = "Operator"
    ): Boolean {
        val currentItem = stockDao.getItemByIdDirect(itemId) ?: return false
        val newQuantity = (currentItem.quantity + quantityDelta).coerceAtLeast(0)

        val updatedItem = currentItem.copy(
            quantity = newQuantity,
            lastUpdated = System.currentTimeMillis()
        )
        stockDao.updateItem(updatedItem)

        val transaction = StockTransaction(
            itemId = currentItem.id,
            itemSku = currentItem.sku,
            itemName = currentItem.name,
            type = type,
            quantityChanged = quantityDelta,
            resultingQuantity = newQuantity,
            unitPrice = currentItem.sellingPrice,
            notes = notes,
            operator = operator,
            timestamp = System.currentTimeMillis()
        )
        stockDao.insertTransaction(transaction)
        return true
    }

    suspend fun seedInitialDataIfNeeded() {
        if (stockDao.getItemCount() == 0) {
            val sampleItems = listOf(
                StockItem(
                    sku = "SKU-ELEC-1001",
                    name = "Wireless Barcode Scanner",
                    description = "High-precision 2.4GHz handheld laser scanner with USB dock",
                    category = "Electronics",
                    quantity = 24,
                    minStockThreshold = 5,
                    unit = "pcs",
                    costPrice = 32.50,
                    sellingPrice = 59.99,
                    barcode = "789123456001",
                    location = "Aisle 1 - Shelf A2",
                    supplier = "OptiScan Solutions"
                ),
                StockItem(
                    sku = "SKU-ELEC-1002",
                    name = "Thermal Receipt Printer Paper",
                    description = "80mm x 80m thermal roll pack of 10 rolls",
                    category = "Packaging",
                    quantity = 85,
                    minStockThreshold = 20,
                    unit = "boxes",
                    costPrice = 14.00,
                    sellingPrice = 24.50,
                    barcode = "789123456002",
                    location = "Aisle 3 - Pallet 1",
                    supplier = "PaperTech Media"
                ),
                StockItem(
                    sku = "SKU-HARD-2001",
                    name = "Heavy Duty Packing Tape (6pk)",
                    description = "Clear 48mm x 66m industrial adhesive packaging tape",
                    category = "Packaging",
                    quantity = 4,
                    minStockThreshold = 10,
                    unit = "packs",
                    costPrice = 8.20,
                    sellingPrice = 15.00,
                    barcode = "789123456003",
                    location = "Aisle 3 - Shelf C4",
                    supplier = "PackSecure Supplies"
                ),
                StockItem(
                    sku = "SKU-ELEC-1003",
                    name = "Industrial USB-C Cable (3m)",
                    description = "Braided nylon heavy-duty high-speed data & charging cable",
                    category = "Electronics",
                    quantity = 42,
                    minStockThreshold = 15,
                    unit = "pcs",
                    costPrice = 4.10,
                    sellingPrice = 11.99,
                    barcode = "789123456004",
                    location = "Aisle 1 - Bin 14",
                    supplier = "Ampere Wire Co"
                ),
                StockItem(
                    sku = "SKU-OFFC-3001",
                    name = "Warehouse Inventory Clipboard",
                    description = "A4 aluminum storage clipboard with document clip",
                    category = "Office Supplies",
                    quantity = 0,
                    minStockThreshold = 6,
                    unit = "pcs",
                    costPrice = 5.50,
                    sellingPrice = 12.00,
                    barcode = "789123456005",
                    location = "Office Bay - Rack 2",
                    supplier = "Stationery Pro"
                ),
                StockItem(
                    sku = "SKU-LOGI-4001",
                    name = "Cardboard Shipping Boxes (Medium)",
                    description = "Corrugated 400x300x200mm carton shipping boxes (Bundle of 25)",
                    category = "Packaging",
                    quantity = 150,
                    minStockThreshold = 40,
                    unit = "bundles",
                    costPrice = 18.00,
                    sellingPrice = 32.00,
                    barcode = "789123456006",
                    location = "Aisle 4 - Bay 1",
                    supplier = "BoxCraft Logistics"
                ),
                StockItem(
                    sku = "SKU-HARD-2002",
                    name = "Precision Digital Caliper",
                    description = "Stainless steel 150mm/6 inch vernier measuring caliper",
                    category = "Hardware",
                    quantity = 3,
                    minStockThreshold = 5,
                    unit = "pcs",
                    costPrice = 19.50,
                    sellingPrice = 38.00,
                    barcode = "789123456007",
                    location = "Aisle 2 - Cabinet B",
                    supplier = "Precision Tools Ltd"
                ),
                StockItem(
                    sku = "SKU-RAW-5001",
                    name = "Industrial Stretch Wrap Film",
                    description = "500mm x 300m 23-micron pallet shrink wrap roll",
                    category = "Raw Materials",
                    quantity = 18,
                    minStockThreshold = 8,
                    unit = "rolls",
                    costPrice = 11.20,
                    sellingPrice = 19.99,
                    barcode = "789123456008",
                    location = "Aisle 4 - Pallet 3",
                    supplier = "Polymer Wraps Inc"
                )
            )

            stockDao.insertItems(sampleItems)

            // Seed initial transactions
            val initialTx = listOf(
                StockTransaction(
                    itemId = 1,
                    itemSku = "SKU-ELEC-1001",
                    itemName = "Wireless Barcode Scanner",
                    type = TransactionType.STOCK_IN,
                    quantityChanged = 24,
                    resultingQuantity = 24,
                    unitPrice = 32.50,
                    notes = "Initial warehouse procurement",
                    operator = "John M."
                ),
                StockTransaction(
                    itemId = 3,
                    itemSku = "SKU-HARD-2001",
                    itemName = "Heavy Duty Packing Tape (6pk)",
                    type = TransactionType.STOCK_OUT,
                    quantityChanged = -6,
                    resultingQuantity = 4,
                    unitPrice = 15.00,
                    notes = "Dispatched for shipping dept batch #104",
                    operator = "Sarah K."
                ),
                StockTransaction(
                    itemId = 5,
                    itemSku = "SKU-OFFC-3001",
                    itemName = "Warehouse Inventory Clipboard",
                    type = TransactionType.STOCK_OUT,
                    quantityChanged = -6,
                    resultingQuantity = 0,
                    unitPrice = 12.00,
                    notes = "Stockout - Allocated to field staff",
                    operator = "Sarah K."
                )
            )
            for (tx in initialTx) {
                stockDao.insertTransaction(tx)
            }
        }
    }
}
