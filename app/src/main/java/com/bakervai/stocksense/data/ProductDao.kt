package com.bakervai.stocksense.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProducts(): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(product: Product)

    @Update
    suspend fun updateItem(product: Product)

    @Delete
    suspend fun deleteItem(product: Product)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteItemById(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>)
}
