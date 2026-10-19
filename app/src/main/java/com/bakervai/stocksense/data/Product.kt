package com.bakervai.stocksense.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val sku: String,
    val name: String,
    val category: String,
    val currentStock: Int,
    val reorderPoint: Int,
    val price: Double,
    val expiryDate: String // Format: YYYY-MM-DD
)
