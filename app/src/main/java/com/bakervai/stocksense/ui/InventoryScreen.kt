package com.bakervai.stocksense.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bakervai.stocksense.data.Product
import com.bakervai.stocksense.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: MainViewModel,
    products: List<Product>,
    lang: Language,
    city: String
) {
    val searchQuery = viewModel.searchQuery.collectAsState().value
    val isAddVisible = viewModel.isAddDialogVisible.collectAsState().value
    val editingProduct = viewModel.editingProduct.collectAsState().value

    // Determine local currency prefix based on active city
    val currency = when (city) {
        "Dhaka", "Chittagong" -> Lang.get("currency_bdt", lang)
        "Mumbai" -> Lang.get("currency_inr", lang)
        "Karachi", "Lahore" -> Lang.get("currency_pkr", lang)
        else -> "$"
    }

    // Filter items reactively based on clean search tags
    val filteredProducts = products.filter {
        it.sku.contains(searchQuery, ignoreCase = true) ||
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.category.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddProductDialog(true) },
                containerColor = IndigoAccent,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = Lang.get("inv_title", lang),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (lang == Language.EN) "Database Sync: Live connected" else "ডেটাবেস সিঙ্ক: লাইভ সংযুক্ত",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text(text = Lang.get("search_placeholder", lang), color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IndigoAccent,
                    unfocusedBorderColor = SlateBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SlateCard,
                    unfocusedContainerColor = SlateCard
                ),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lazy Products Catalog List
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (lang == Language.EN) "No items match your search." else "পণ্য পাওয়া যায়নি।",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SlateBorder, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = SlateCard)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Product Header SKU + Action buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = product.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = product.sku,
                                                fontSize = 11.sp,
                                                color = IndigoLight,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(IndigoBg)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = product.category,
                                                    fontSize = 9.sp,
                                                    color = IndigoLight,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }

                                    // Action Buttons Edit / Delete
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = { viewModel.setEditingProduct(product) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit SKU",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteProduct(product) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete SKU",
                                                tint = AccentRed,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Divider(
                                    color = SlateBorder.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )

                                // Current stock vs reorder limits & price info
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Stock widget
                                    Column {
                                        Text(
                                            text = Lang.get("inv_stock", lang),
                                            fontSize = 10.sp,
                                            color = TextSecondary
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "${product.currentStock}",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (product.currentStock <= product.reorderPoint) AccentYellow else AccentGreen
                                            )
                                            Text(
                                                text = "/ min ${product.reorderPoint}",
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }

                                    // Expiry tracking with color badges
                                    val daysToExpiry = getDaysToExpiry(product.expiryDate)
                                    val (expiryColor, expiryText) = when {
                                        daysToExpiry <= 3 -> Pair(
                                            AccentRed,
                                            if (lang == Language.EN) "Expiling in $daysToExpiry d" else "$daysToExpiry দিনে মেয়াদ শেষ"
                                        )
                                        daysToExpiry <= 14 -> Pair(
                                            AccentYellow,
                                            if (lang == Language.EN) "Expiring in $daysToExpiry d" else "$daysToExpiry দিনে মেয়াদ শেষ"
                                        )
                                        else -> Pair(
                                            AccentGreen,
                                            if (lang == Language.EN) "Safe Expiry" else "নিরাপদ মেয়াদ"
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = Lang.get("inv_price", lang),
                                            fontSize = 10.sp,
                                            color = TextSecondary
                                        )
                                        Text(
                                            text = "$currency${product.price}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 4.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(expiryColor.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = expiryText,
                                                fontSize = 9.sp,
                                                color = expiryColor,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Quick Auto Reorder Suggestion Action on low stock items!
                                if (product.currentStock <= product.reorderPoint) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(YellowBg)
                                            .border(1.dp, AccentYellow.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .clickable {
                                                val refilled = product.copy(currentStock = product.reorderPoint + 150)
                                                viewModel.updateProduct(refilled)
                                                viewModel.showToastMessage(Lang.get("notification_action_applied", lang))
                                            }
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Reorder",
                                                tint = AccentYellow,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (lang == Language.EN) "Auto-Reorder Level low. Top-up +150 units?" else "স্টক কম। বাড়তি ১৫০ ইউনিট রিঅর্ডার দিন?",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentYellow
                                            )
                                        }
                                        Text(
                                            text = Lang.get("btn_approve", lang),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentYellow
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet Add dialog
    if (isAddVisible) {
        ProductFormDialog(
            lang = lang,
            city = city,
            onDismiss = { viewModel.showAddProductDialog(false) },
            onSave = { sku, name, cat, stock, reorder, price, expiry ->
                viewModel.addProduct(sku, name, cat, stock, reorder, price, expiry)
                viewModel.showAddProductDialog(false)
            }
        )
    }

    // Modal Sheet Edit dialog
    if (editingProduct != null) {
        ProductFormDialog(
            lang = lang,
            city = city,
            product = editingProduct,
            onDismiss = { viewModel.setEditingProduct(null) },
            onSave = { sku, name, cat, stock, reorder, price, expiry ->
                viewModel.updateProduct(
                    editingProduct.copy(
                        sku = sku,
                        name = name,
                        category = cat,
                        currentStock = stock,
                        reorderPoint = reorder,
                        price = price,
                        expiryDate = expiry
                    )
                )
                viewModel.setEditingProduct(null)
            }
        )
    }
}

private fun getDaysToExpiry(expiryDateStr: String): Int {
    return try {
        val today = LocalDate.now()
        val expiry = LocalDate.parse(expiryDateStr, DateTimeFormatter.ISO_LOCAL_DATE)
        ChronoUnit.DAYS.between(today, expiry).toInt()
    } catch (e: Exception) {
        15 // Default safe falloff
    }
}

// Reusable custom Form Dialog
@Composable
fun ProductFormDialog(
    lang: Language,
    city: String,
    product: Product? = null,
    onDismiss: () -> Unit,
    onSave: (sku: String, name: String, category: String, stock: Int, reorder: Int, price: Double, expiryDate: String) -> Unit
) {
    var sku by remember { mutableStateOf(product?.sku ?: "PRD-${(10000..99999).random()}") }
    var name by remember { mutableStateOf(product?.name ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "Fresh Produce") }
    var stock by remember { mutableStateOf(product?.currentStock?.toString() ?: "100") }
    var reorder by remember { mutableStateOf(product?.reorderPoint?.toString() ?: "40") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "99.00") }
    var expiry by remember { mutableStateOf(product?.expiryDate ?: LocalDate.now().plusDays(30).toString()) }

    val categories = listOf("Fresh Produce", "Grocery", "Beverages", "Essentials", "Spices & Traditional")
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = if (product == null) Lang.get("inv_add", lang) else Lang.get("inv_edit", lang),
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        containerColor = SlateCard,
        confirmButton = {
            Button(
                onClick = {
                    val finalStock = stock.toIntOrNull() ?: 0
                    val finalReorder = reorder.toIntOrNull() ?: 0
                    val finalPrice = price.toDoubleOrNull() ?: 0.0
                    onSave(sku, name, category, finalStock, finalReorder, finalPrice, expiry)
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent)
            ) {
                Text(text = Lang.get("btn_save", lang), color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = Lang.get("btn_cancel", lang), color = TextSecondary)
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                item {
                    // SKU
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text(text = "SKU", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = IndigoAccent,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    // Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(text = Lang.get("inv_name", lang), color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = IndigoAccent,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    // Category Selection dropdown
                    Column {
                        Text(text = Lang.get("lbl_category", lang), color = TextSecondary, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(SlateBg)
                                .border(1.dp, SlateBorder, RoundedCornerShape(4.dp))
                                .clickable { categoryDropdownExpanded = true }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(text = category, color = TextPrimary, fontSize = 14.sp)
                            DropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false },
                                modifier = Modifier.background(SlateCard)
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(text = cat, color = TextPrimary) },
                                        onClick = {
                                            category = cat
                                            categoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    // Current Stock Level
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it },
                        label = { Text(text = Lang.get("inv_stock", lang), color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = IndigoAccent,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    // Reorder point
                    OutlinedTextField(
                        value = reorder,
                        onValueChange = { reorder = it },
                        label = { Text(text = Lang.get("inv_reorder", lang), color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = IndigoAccent,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    // Price
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text(text = Lang.get("inv_price", lang), color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = IndigoAccent,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    // Expiry string
                    OutlinedTextField(
                        value = expiry,
                        onValueChange = { expiry = it },
                        label = { Text(text = Lang.get("inv_expiry", lang), color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = IndigoAccent,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    )
}
