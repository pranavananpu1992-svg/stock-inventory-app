package com.example.stockinventory.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.stockinventory.data.model.StockItem
import com.example.stockinventory.data.model.TransactionType
import com.example.stockinventory.ui.StockViewModel
import com.example.stockinventory.ui.components.StockAdjustDialog
import com.example.stockinventory.ui.components.StockStatusBadge
import com.example.stockinventory.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: Long,
    viewModel: StockViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val itemFlow = remember(itemId) { viewModel.getItem(itemId) }
    val item by itemFlow.collectAsState(initial = null)

    val itemTxFlow = remember(itemId) { viewModel.getItemTransactions(itemId) }
    val itemTransactions by itemTxFlow.collectAsState(initial = emptyList())

    var showAdjustDialog by remember { mutableStateOf(false) }
    var adjustType by remember { mutableStateOf(TransactionType.STOCK_IN) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item?.name ?: "Item Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    item?.let { currentItem ->
                        IconButton(
                            onClick = { onNavigateToEdit(currentItem.id) },
                            modifier = Modifier.testTag("edit_item_action")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.testTag("delete_item_action")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RoseError)
                        }
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        if (item == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val currentItem = item!!

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp)
            ) {
                // Main Stock Status & Quantity Hero Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = currentItem.category,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                StockStatusBadge(
                                    quantity = currentItem.quantity,
                                    minThreshold = currentItem.minStockThreshold,
                                    unit = currentItem.unit
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "${currentItem.quantity} ${currentItem.unit}",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Current In-Stock Quantity (Min Threshold: ${currentItem.minStockThreshold} ${currentItem.unit})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Quick Adjust Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        adjustType = TransactionType.STOCK_IN
                                        showAdjustDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Stock In (+)")
                                }

                                FilledTonalButton(
                                    onClick = {
                                        adjustType = TransactionType.STOCK_OUT
                                        showAdjustDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(painter = painterResource(id = R.drawable.ic_remove), contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Stock Out (-)")
                                }
                            }
                        }
                    }
                }

                // Identifiers & Barcode Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Identification & Tracking",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("SKU Number", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(currentItem.sku, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Slate100)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Barcode / EAN", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    if (currentItem.barcode.isNotBlank()) currentItem.barcode else "N/A",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Slate100)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Storage Location", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(currentItem.location, fontWeight = FontWeight.Medium)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Slate100)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Supplier", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(currentItem.supplier.ifBlank { "Not specified" }, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                // Valuation & Financials Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Valuation & Financials",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Unit Cost Price", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(currencyFormat.format(currentItem.costPrice), fontWeight = FontWeight.SemiBold)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Slate100)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Unit Selling Price", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(currencyFormat.format(currentItem.sellingPrice), fontWeight = FontWeight.SemiBold)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Slate100)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Profit Margin", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    String.format("%.1f%%", currentItem.profitMarginPercent),
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Slate100)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Inventory Value (Cost)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    currencyFormat.format(currentItem.totalCostValue),
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                            }
                        }
                    }
                }

                // Description Card
                if (currentItem.description.isNotBlank()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Description",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = currentItem.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Audit History for this Item
                item {
                    Text(
                        text = "Stock Movement History (${itemTransactions.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (itemTransactions.isEmpty()) {
                    item {
                        Text(
                            text = "No recorded transactions for this SKU yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(itemTransactions) { tx ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = tx.type.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${dateFormat.format(Date(tx.timestamp))} • By ${tx.operator}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (tx.notes.isNotBlank()) {
                                        Text(
                                            text = tx.notes,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = if (tx.quantityChanged > 0) "+${tx.quantityChanged}" else "${tx.quantityChanged}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (tx.quantityChanged >= 0) EmeraldSuccess else RoseError
                                    )
                                    Text(
                                        text = "Bal: ${tx.resultingQuantity}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Stock Adjust Dialog
            if (showAdjustDialog) {
                StockAdjustDialog(
                    item = currentItem,
                    initialType = adjustType,
                    onDismiss = { showAdjustDialog = false },
                    onConfirm = { type, qty, notes, operator ->
                        if (type == TransactionType.STOCK_IN) {
                            viewModel.performStockIn(currentItem.id, qty, notes, operator)
                        } else {
                            viewModel.performStockOut(currentItem.id, qty, notes, operator)
                        }
                        showAdjustDialog = false
                    }
                )
            }

            // Delete Confirmation Dialog
            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    title = { Text("Delete Stock Item?") },
                    text = {
                        Text("Are you sure you want to permanently delete \"${currentItem.name}\" (${currentItem.sku})? This cannot be undone.")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteItem(currentItem) {
                                    showDeleteConfirm = false
                                    onNavigateBack()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoseError)
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirm = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
