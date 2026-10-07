package com.example.stockinventory.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.stockinventory.data.model.StockItem
import com.example.stockinventory.ui.StockViewModel
import com.example.stockinventory.ui.components.StockStatusBadge
import com.example.stockinventory.ui.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    viewModel: StockViewModel,
    onNavigateToItemDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val allItems by viewModel.rawItems.collectAsState()

    var barcodeInput by remember { mutableStateOf("") }
    var scannedItem by remember { mutableStateOf<StockItem?>(null) }
    var lookupError by remember { mutableStateOf<String?>(null) }
    var isScanning by remember { mutableStateOf(true) }

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    // Scanning laser animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser_transition")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_offset"
    )

    fun searchBarcode(code: String) {
        val trimmed = code.trim()
        if (trimmed.isBlank()) return
        barcodeInput = trimmed
        coroutineScope.launch {
            val byBarcode = viewModel.findItemByBarcode(trimmed)
            val item = byBarcode ?: viewModel.findItemBySku(trimmed) ?: allItems.find {
                it.barcode.equals(trimmed, ignoreCase = true) || it.sku.equals(trimmed, ignoreCase = true)
            }

            if (item != null) {
                scannedItem = item
                lookupError = null
            } else {
                scannedItem = null
                lookupError = "No item found with barcode/SKU: \"$trimmed\""
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Barcode Scanner",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Scan or enter barcode/SKU for quick actions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scanner Viewfinder Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Viewfinder frame
                Box(
                    modifier = Modifier
                        .size(width = 240.dp, height = 130.dp)
                        .border(
                            width = 2.dp,
                            color = if (scannedItem != null) EmeraldSuccess else NavyLight,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    // Laser Beam
                    if (isScanning) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .offset(y = (130 * laserOffset).dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color.Transparent,
                                            if (scannedItem != null) EmeraldSuccess else RoseError,
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }
                }

                // Center Icon / Status
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (scannedItem != null) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_scanner),
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (scannedItem != null) "Product Identified!" else "Ready to Scan",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Barcode input field
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = barcodeInput,
                onValueChange = {
                    barcodeInput = it
                    if (it.length >= 6) {
                        searchBarcode(it)
                    }
                },
                placeholder = { Text("Enter Barcode or SKU…") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_scanner),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("scanner_input_field")
            )

            Button(
                onClick = { searchBarcode(barcodeInput) },
                modifier = Modifier.testTag("scanner_lookup_button")
            ) {
                Text("Lookup")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Barcode Test Chips
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Quick Sample Barcodes:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(allItems.take(5)) { item ->
                    AssistChip(
                        onClick = { searchBarcode(item.barcode.ifBlank { item.sku }) },
                        label = { Text(item.name.take(16) + "…") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_scanner),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Error message
        lookupError?.let { err ->
            Surface(
                color = RoseLight,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = RoseError)
                    Text(text = err, color = RoseError, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Scanned Item Result Card
        scannedItem?.let { item ->
            // Re-fetch reactive item state
            val liveItem = allItems.find { it.id == item.id } ?: item

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("scanned_result_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = liveItem.category,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        StockStatusBadge(
                            quantity = liveItem.quantity,
                            minThreshold = liveItem.minStockThreshold,
                            unit = liveItem.unit
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = liveItem.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "SKU: ${liveItem.sku}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "• Loc: ${liveItem.location}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Current Stock",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${liveItem.quantity} ${liveItem.unit}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Retail Price",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = currencyFormat.format(liveItem.sellingPrice),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Slate100)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Instant Quick +/- buttons for scanner
                    Text(
                        text = "Instant Stock Action:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.performStockIn(liveItem.id, 1, "Quick scan restock (+1)", "Scanner")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+1 In")
                        }

                        Button(
                            onClick = {
                                viewModel.performStockIn(liveItem.id, 5, "Quick scan restock (+5)", "Scanner")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+5 In")
                        }

                        FilledTonalButton(
                            onClick = {
                                viewModel.performStockOut(liveItem.id, 1, "Quick scan dispatch (-1)", "Scanner")
                            },
                            enabled = liveItem.quantity > 0,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("-1 Out")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { onNavigateToItemDetail(liveItem.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Full Item Details")
                    }
                }
            }
        }
    }
}
