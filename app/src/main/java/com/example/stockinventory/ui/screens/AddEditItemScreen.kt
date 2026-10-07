package com.example.stockinventory.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.stockinventory.data.model.StockItem
import com.example.stockinventory.ui.StockViewModel
import com.example.stockinventory.ui.theme.NavyPrimary
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditItemScreen(
    itemId: Long?,
    viewModel: StockViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val isEditing = itemId != null && itemId > 0
    val existingItemFlow = remember(itemId) {
        if (isEditing) viewModel.getItem(itemId!!) else null
    }
    val existingItem by existingItemFlow?.collectAsState(initial = null) ?: remember { mutableStateOf(null) }

    var sku by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var quantityText by remember { mutableStateOf("0") }
    var minThresholdText by remember { mutableStateOf("5") }
    var unit by remember { mutableStateOf("pcs") }
    var costPriceText by remember { mutableStateOf("0.00") }
    var sellingPriceText by remember { mutableStateOf("0.00") }
    var barcode by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Warehouse 1 - Aisle A") }
    var supplier by remember { mutableStateOf("") }
    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(existingItem) {
        existingItem?.let { item ->
            if (!isInitialized) {
                sku = item.sku
                name = item.name
                description = item.description
                category = item.category
                quantityText = item.quantity.toString()
                minThresholdText = item.minStockThreshold.toString()
                unit = item.unit
                costPriceText = String.format("%.2f", item.costPrice)
                sellingPriceText = String.format("%.2f", item.sellingPrice)
                barcode = item.barcode
                location = item.location
                supplier = item.supplier
                isInitialized = true
            }
        }
    }

    val presetCategories = listOf(
        "Electronics", "Packaging", "Hardware", "Office Supplies",
        "Raw Materials", "Finished Goods", "Perishables", "Apparel"
    )

    val presetUnits = listOf("pcs", "boxes", "packs", "kg", "rolls", "liters", "units")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Stock Item" else "New Stock Item") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val newItem = StockItem(
                            id = itemId ?: 0L,
                            sku = sku.trim(),
                            name = name.trim(),
                            description = description.trim(),
                            category = category.trim(),
                            quantity = quantityText.toIntOrNull() ?: 0,
                            minStockThreshold = minThresholdText.toIntOrNull() ?: 5,
                            unit = unit.trim(),
                            costPrice = costPriceText.toDoubleOrNull() ?: 0.0,
                            sellingPrice = sellingPriceText.toDoubleOrNull() ?: 0.0,
                            barcode = barcode.trim(),
                            location = location.trim(),
                            supplier = supplier.trim(),
                            lastUpdated = System.currentTimeMillis()
                        )
                        viewModel.saveItem(newItem) {
                            onNavigateBack()
                        }
                    },
                    enabled = name.isNotBlank() && sku.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("save_item_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isEditing) "Save Changes" else "Add Item to Inventory")
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
        ) {
            // General Details
            item {
                Text(
                    text = "Basic Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product / Item Name *") },
                    placeholder = { Text("e.g. Wireless Barcode Scanner") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("item_name_input")
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it.uppercase() },
                        label = { Text("SKU Number *") },
                        placeholder = { Text("e.g. SKU-1049") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("item_sku_input")
                    )

                    OutlinedButton(
                        onClick = {
                            val code = "SKU-${(1000..9999).random()}"
                            sku = code
                        },
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Text("Auto SKU")
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Specifications, dimensions, color, etc.") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Category Selection
            item {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(presetCategories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Stock & Units
            item {
                Text(
                    text = "Stock & Quantities",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it.filter { c -> c.isDigit() } },
                        label = { Text("Initial Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("item_quantity_input")
                    )

                    OutlinedTextField(
                        value = minThresholdText,
                        onValueChange = { minThresholdText = it.filter { c -> c.isDigit() } },
                        label = { Text("Low Stock Alert (Min)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text(
                    text = "Unit of Measurement",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(presetUnits) { u ->
                        FilterChip(
                            selected = unit == u,
                            onClick = { unit = u },
                            label = { Text(u) }
                        )
                    }
                }
            }

            // Pricing
            item {
                Text(
                    text = "Pricing ($)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = costPriceText,
                        onValueChange = { costPriceText = it },
                        label = { Text("Unit Cost Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = sellingPriceText,
                        onValueChange = { sellingPriceText = it },
                        label = { Text("Unit Selling Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Location & Barcode
            item {
                Text(
                    text = "Storage & Logistics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("Barcode / EAN") },
                        placeholder = { Text("e.g. 789123456001") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedButton(
                        onClick = {
                            barcode = "789${Random.nextInt(100000000, 999999999)}"
                        },
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Text("Generate")
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Warehouse Location") },
                    placeholder = { Text("Aisle 3 - Shelf B") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = supplier,
                    onValueChange = { supplier = it },
                    label = { Text("Supplier / Vendor") },
                    placeholder = { Text("Acme Logistics Corp") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
