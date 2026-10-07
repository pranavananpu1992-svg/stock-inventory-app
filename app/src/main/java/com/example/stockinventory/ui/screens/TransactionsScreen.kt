package com.example.stockinventory.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.stockinventory.data.model.StockTransaction
import com.example.stockinventory.data.model.TransactionType
import com.example.stockinventory.ui.StockViewModel
import com.example.stockinventory.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: StockViewModel,
    onNavigateToItemDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val allTransactions by viewModel.transactions.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<TransactionType?>(null) }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.getDefault()) }

    val filteredTransactions = remember(allTransactions, searchQuery, selectedTypeFilter) {
        allTransactions.filter { tx ->
            val matchesQuery = searchQuery.isBlank() ||
                    tx.itemName.contains(searchQuery, ignoreCase = true) ||
                    tx.itemSku.contains(searchQuery, ignoreCase = true) ||
                    tx.notes.contains(searchQuery, ignoreCase = true) ||
                    tx.operator.contains(searchQuery, ignoreCase = true)

            val matchesType = selectedTypeFilter == null || tx.type == selectedTypeFilter

            matchesQuery && matchesType
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Transaction History",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Warehouse audit trail & movements log",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter by item, SKU, notes, operator…") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate500) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Type Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedTypeFilter == null,
                    onClick = { selectedTypeFilter = null },
                    label = { Text("All Activities") }
                )
            }
            items(TransactionType.values()) { type ->
                FilterChip(
                    selected = selectedTypeFilter == type,
                    onClick = { selectedTypeFilter = type },
                    label = { Text(type.displayName) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${filteredTransactions.size} transactions logged",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_receipt),
                        contentDescription = null,
                        tint = Slate300,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "No transactions found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTransactions, key = { it.id }) { tx ->
                    TransactionItemCard(
                        transaction = tx,
                        dateFormat = dateFormat,
                        onClick = { onNavigateToItemDetail(tx.itemId) }
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItemCard(
    transaction: StockTransaction,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconRes = when (transaction.type) {
        TransactionType.STOCK_IN -> R.drawable.ic_arrow_down
        TransactionType.STOCK_OUT -> R.drawable.ic_arrow_up
        TransactionType.ADJUSTMENT -> R.drawable.ic_sync
        TransactionType.DAMAGE_LOSS -> R.drawable.ic_remove
        TransactionType.RETURN -> R.drawable.ic_sync
    }
    val tint = when (transaction.type) {
        TransactionType.STOCK_IN -> EmeraldSuccess
        TransactionType.STOCK_OUT -> RoseError
        TransactionType.ADJUSTMENT -> NavyPrimary
        TransactionType.DAMAGE_LOSS -> AmberWarning
        TransactionType.RETURN -> TealSecondary
    }
    val bgColor = when (transaction.type) {
        TransactionType.STOCK_IN -> EmeraldLight
        TransactionType.STOCK_OUT -> RoseLight
        TransactionType.ADJUSTMENT -> Slate200
        TransactionType.DAMAGE_LOSS -> AmberLight
        TransactionType.RETURN -> Color(0xFFCCFBF1)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = transaction.itemName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "${transaction.itemSku} • ${transaction.type.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${dateFormat.format(Date(transaction.timestamp))} • ${transaction.operator}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500
                    )
                    if (transaction.notes.isNotBlank()) {
                        Text(
                            text = "Ref: ${transaction.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = if (transaction.quantityChanged > 0) "+${transaction.quantityChanged}" else "${transaction.quantityChanged}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (transaction.quantityChanged >= 0) EmeraldSuccess else RoseError
                )
                Text(
                    text = "Bal: ${transaction.resultingQuantity}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
