package com.example.data

import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()

    suspend fun insert(product: Product) {
        productDao.insertItem(product)
    }

    suspend fun update(product: Product) {
        productDao.updateItem(product)
    }

    suspend fun delete(product: Product) {
        productDao.deleteItem(product)
    }

    suspend fun deleteById(id: Int) {
        productDao.deleteItemById(id)
    }

    suspend fun insertAll(products: List<Product>) {
        productDao.insertAll(products)
    }
}
