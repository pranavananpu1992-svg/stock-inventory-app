package com.example.stockinventory.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.stockinventory.data.model.StockItem
import com.example.stockinventory.data.model.TransactionType
import com.example.stockinventory.ui.StockViewModel
import com.example.stockinventory.ui.components.ExportCsvDialog
import com.example.stockinventory.ui.components.MetricCard
import com.example.stockinventory.ui.components.StockAdjustDialog
import com.example.stockinventory.ui.components.StockStatusBadge
import com.example.stockinventory.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: StockViewModel,
    onNavigateToInventory: () -> Unit,
    onNavigateToItemDetail: (Long) -> Unit,
    onNavigateToAddItem: () -> Unit,
    onNavigateToScanner: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics by viewModel.dashboardMetrics.collectAsState()
    val categoryStats by viewModel.categoryStats.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val items by viewModel.rawItems.collectAsState()

    var showExportDialog by remember { mutableStateOf(false) }
    var adjustTargetItem by remember { mutableStateOf<StockItem?>(null) }

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val timeFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }

    val lowStockItems = remember(items) {
        items.filter { it.quantity <= it.minStockThreshold }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Top Welcome & Quick Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Warehouse Overview",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Real-time stock monitoring & metrics",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalIconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.testTag("export_csv_button")
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_download), contentDescription = "Export CSV")
                    }
                    FilledIconButton(
                        onClick = onNavigateToScanner,
                        modifier = Modifier.testTag("quick_scan_button")
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_scanner), contentDescription = "Scan")
                    }
                }
            }
        }

        // Metrics Grid (2x2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Cost Valuation",
                        value = currencyFormat.format(metrics.totalValuationCost),
                        subtitle = "Retail: ${currencyFormat.format(metrics.totalValuationRetail)}",
                        iconRes = R.drawable.ic_dollar,
                        iconTint = NavyPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Total Units",
                        value = "${metrics.totalUnitsCount}",
                        subtitle = "${metrics.totalItemsCount} unique SKUs",
                        iconRes = R.drawable.ic_inventory,
                        iconTint = TealSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Low Stock Alert",
                        value = "${metrics.lowStockCount}",
                        subtitle = "Needs replenishment",
                        iconRes = R.drawable.ic_inventory,
                        iconTint = AmberWarning,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Out of Stock",
                        value = "${metrics.outOfStockCount}",
                        subtitle = "Depleted items",
                        iconRes = R.drawable.ic_inventory,
                        iconTint = RoseError,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Low Stock Urgent Carousel
        if (lowStockItems.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AmberLight.copy(alpha = 0.5f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AmberWarning.copy(alpha = 0.4f))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = AmberWarning)
                                Text(
                                    text = "Requires Reorder (${lowStockItems.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberWarning
                                )
                            }
                            TextButton(onClick = {
                                viewModel.toggleLowStockFilter()
                                onNavigateToInventory()
                            }) {
                                Text("View All", color = AmberWarning, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(lowStockItems) { item ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(12.dp),
                                    tonalElevation = 2.dp,
                                    modifier = Modifier
                                        .width(220.dp)
                                        .clickable { onNavigateToItemDetail(item.id) }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = item.sku,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            StockStatusBadge(
                                                quantity = item.quantity,
                                                minThreshold = item.minStockThreshold,
                                                unit = item.unit
                                            )
                                            FilledTonalButton(
                                                onClick = { adjustTargetItem = item },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("+ In", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Category Breakdown
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Inventory by Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (categoryStats.isEmpty()) {
                        Text(
                            text = "No category data available.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        categoryStats.forEach { stat ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stat.category,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${stat.itemCount} items (${currencyFormat.format(stat.totalValue)})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { stat.percentage },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = NavyPrimary,
                                    trackColor = Slate200
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Movements / Activity
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Stock Movements",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = onNavigateToTransactions) {
                            Text("Full Audit Log")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val recent = transactions.take(4)
                    if (recent.isEmpty()) {
                        Text(
                            text = "No transactions logged yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        recent.forEach { tx ->
                            val iconRes = when (tx.type) {
                                TransactionType.STOCK_IN -> R.drawable.ic_arrow_down
                                TransactionType.STOCK_OUT -> R.drawable.ic_arrow_up
                                TransactionType.ADJUSTMENT -> R.drawable.ic_sync
                                TransactionType.DAMAGE_LOSS -> R.drawable.ic_remove
                                TransactionType.RETURN -> R.drawable.ic_sync
                            }
                            val tint = when (tx.type) {
                                TransactionType.STOCK_IN -> EmeraldSuccess
                                TransactionType.STOCK_OUT -> RoseError
                                TransactionType.ADJUSTMENT -> NavyPrimary
                                TransactionType.DAMAGE_LOSS -> AmberWarning
                                TransactionType.RETURN -> TealSecondary
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(tint.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = iconRes),
                                            contentDescription = null,
                                            tint = tint,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = tx.itemName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${tx.itemSku} • ${timeFormat.format(Date(tx.timestamp))}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Text(
                                    text = if (tx.quantityChanged > 0) "+${tx.quantityChanged}" else "${tx.quantityChanged}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tx.quantityChanged >= 0) EmeraldSuccess else RoseError
                                )
                            }
                            HorizontalDivider(color = Slate100)
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showExportDialog) {
        ExportCsvDialog(
            csvContent = viewModel.generateCsvExport(),
            onDismiss = { showExportDialog = false }
        )
    }

    adjustTargetItem?.let { item ->
        StockAdjustDialog(
            item = item,
            onDismiss = { adjustTargetItem = null },
            onConfirm = { type, qty, notes, operator ->
                if (type == TransactionType.STOCK_IN) {
                    viewModel.performStockIn(item.id, qty, notes, operator)
                } else {
                    viewModel.performStockOut(item.id, qty, notes, operator)
                }
                adjustTargetItem = null
            }
        )
    }
}
