package com.example.stockinventory.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.stockinventory.data.model.StockItem
import com.example.stockinventory.data.model.StockTransaction
import com.example.stockinventory.data.model.TransactionType
import com.example.stockinventory.data.repository.StockRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardMetrics(
    val totalItemsCount: Int = 0,
    val totalUnitsCount: Int = 0,
    val totalValuationCost: Double = 0.0,
    val totalValuationRetail: Double = 0.0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val healthyStockCount: Int = 0
)

data class CategoryStat(
    val category: String,
    val itemCount: Int,
    val totalUnits: Int,
    val totalValue: Double,
    val percentage: Float
)

class StockViewModel(private val repository: StockRepository) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
        }
    }

    val rawItems: StateFlow<List<StockItem>> = repository.allItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val transactions: StateFlow<List<StockTransaction>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val categories: StateFlow<List<String>> = repository.categories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _filterLowStockOnly = MutableStateFlow(false)
    val filterLowStockOnly: StateFlow<Boolean> = _filterLowStockOnly.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    val filteredItems: StateFlow<List<StockItem>> = combine(
        rawItems,
        _searchQuery,
        _selectedCategory,
        _filterLowStockOnly
    ) { items, query, category, lowStockOnly ->
        items.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.name.contains(query, ignoreCase = true) ||
                    item.sku.contains(query, ignoreCase = true) ||
                    item.barcode.contains(query, ignoreCase = true) ||
                    item.location.contains(query, ignoreCase = true)

            val matchesCategory = category == null || item.category.equals(category, ignoreCase = true)

            val matchesLowStock = !lowStockOnly || (item.quantity <= item.minStockThreshold)

            matchesQuery && matchesCategory && matchesLowStock
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val dashboardMetrics: StateFlow<DashboardMetrics> = rawItems.combine(rawItems) { items, _ ->
        val totalItems = items.size
        val totalUnits = items.sumOf { it.quantity }
        val costVal = items.sumOf { it.totalCostValue }
        val retailVal = items.sumOf { it.totalSellingValue }
        val lowStock = items.count { it.isLowStock }
        val outOfStock = items.count { it.isOutOfStock }
        val healthy = items.count { !it.isLowStock && !it.isOutOfStock }

        DashboardMetrics(
            totalItemsCount = totalItems,
            totalUnitsCount = totalUnits,
            totalValuationCost = costVal,
            totalValuationRetail = retailVal,
            lowStockCount = lowStock,
            outOfStockCount = outOfStock,
            healthyStockCount = healthy
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardMetrics()
    )

    val categoryStats: StateFlow<List<CategoryStat>> = rawItems.combine(rawItems) { items, _ ->
        val totalVal = items.sumOf { it.totalCostValue }.coerceAtLeast(1.0)
        items.groupBy { it.category }
            .map { (cat, catItems) ->
                val count = catItems.size
                val units = catItems.sumOf { it.quantity }
                val value = catItems.sumOf { it.totalCostValue }
                CategoryStat(
                    category = cat,
                    itemCount = count,
                    totalUnits = units,
                    totalValue = value,
                    percentage = (value / totalVal).toFloat()
                )
            }.sortedByDescending { it.totalValue }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun toggleLowStockFilter() {
        _filterLowStockOnly.value = !_filterLowStockOnly.value
    }

    fun getItem(id: Long) = repository.getItemById(id)

    fun getItemTransactions(itemId: Long) = repository.getTransactionsForItem(itemId)

    suspend fun findItemByBarcode(barcode: String): StockItem? = repository.findByBarcode(barcode)

    suspend fun findItemBySku(sku: String): StockItem? = repository.findBySku(sku)

    fun saveItem(item: StockItem, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (item.id == 0L) {
                val newId = repository.insertItem(item)
                if (item.quantity > 0) {
                    repository.adjustStock(
                        itemId = newId,
                        type = TransactionType.STOCK_IN,
                        quantityDelta = item.quantity,
                        notes = "Initial inventory registration",
                        operator = "Admin"
                    )
                }
                _userMessage.emit("Added ${item.name} ($item.sku)")
            } else {
                repository.updateItem(item)
                _userMessage.emit("Updated ${item.name}")
            }
            onComplete()
        }
    }

    fun deleteItem(item: StockItem, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteItem(item)
            _userMessage.emit("Deleted ${item.name}")
            onComplete()
        }
    }

    fun performStockIn(itemId: Long, quantity: Int, notes: String, operator: String = "Staff") {
        if (quantity <= 0) return
        viewModelScope.launch {
            val success = repository.adjustStock(
                itemId = itemId,
                type = TransactionType.STOCK_IN,
                quantityDelta = quantity,
                notes = notes.ifBlank { "Stock received into warehouse" },
                operator = operator
            )
            if (success) {
                _userMessage.emit("Stocked in +$quantity units")
            }
        }
    }

    fun performStockOut(itemId: Long, quantity: Int, notes: String, operator: String = "Staff") {
        if (quantity <= 0) return
        viewModelScope.launch {
            val success = repository.adjustStock(
                itemId = itemId,
                type = TransactionType.STOCK_OUT,
                quantityDelta = -quantity,
                notes = notes.ifBlank { "Dispatched / Sold" },
                operator = operator
            )
            if (success) {
                _userMessage.emit("Dispatched -$quantity units")
            }
        }
    }

    fun performAudit(itemId: Long, targetQuantity: Int, notes: String, operator: String = "Auditor") {
        viewModelScope.launch {
            val item = repository.findBySku(targetQuantity.toString()) // or get direct
            // Get item from items list
            val current = rawItems.value.find { it.id == itemId } ?: return@launch
            val delta = targetQuantity - current.quantity
            val success = repository.adjustStock(
                itemId = itemId,
                type = TransactionType.ADJUSTMENT,
                quantityDelta = delta,
                notes = notes.ifBlank { "Physical count audit adjustment" },
                operator = operator
            )
            if (success) {
                _userMessage.emit("Stock adjusted to $targetQuantity units")
            }
        }
    }

    fun generateCsvExport(): String {
        val items = rawItems.value
        val sb = StringBuilder()
        sb.append("SKU,Name,Category,Quantity,Unit,MinThreshold,CostPrice,SellingPrice,TotalCostValue,Location,Barcode,Supplier\n")
        for (item in items) {
            sb.append("\"${item.sku}\",")
            sb.append("\"${item.name.replace("\"", "\"\"")}\",")
            sb.append("\"${item.category}\",")
            sb.append("${item.quantity},")
            sb.append("\"${item.unit}\",")
            sb.append("${item.minStockThreshold},")
            sb.append("${String.format("%.2f", item.costPrice)},")
            sb.append("${String.format("%.2f", item.sellingPrice)},")
            sb.append("${String.format("%.2f", item.totalCostValue)},")
            sb.append("\"${item.location}\",")
            sb.append("\"${item.barcode}\",")
            sb.append("\"${item.supplier}\"\n")
        }
        return sb.toString()
    }
}

class StockViewModelFactory(private val repository: StockRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StockViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StockViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
