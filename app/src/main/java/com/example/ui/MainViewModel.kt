package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Product
import com.example.data.ProductRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class AppTab {
    DASHBOARD, FORECAST, INVENTORY, ANALYTICS, SAAS
}

class MainViewModel(private val repository: ProductRepository) : ViewModel() {

    // Language state
    private val _language = MutableStateFlow(Language.EN)
    val language: StateFlow<Language> = _language.asStateFlow()

    // Tab state
    private val _activeTab = MutableStateFlow(AppTab.DASHBOARD)
    val activeTab: StateFlow<AppTab> = _activeTab.asStateFlow()

    // City state: Dhaka, Chittagong, Mumbai, Karachi, Lahore
    private val _selectedCity = MutableStateFlow("Dhaka")
    val selectedCity: StateFlow<String> = _selectedCity.asStateFlow()

    // Search query for Inventory
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Product form dialog states
    private val _isAddDialogVisible = MutableStateFlow(false)
    val isAddDialogVisible: StateFlow<Boolean> = _isAddDialogVisible.asStateFlow()

    private val _editingProduct = MutableStateFlow<Product?>(null)
    val editingProduct: StateFlow<Product?> = _editingProduct.asStateFlow()

    // AI Forecast inputs and state
    private val _forecastCategory = MutableStateFlow("Fresh Produce")
    val forecastCategory: StateFlow<String> = _forecastCategory.asStateFlow()

    private val _forecastZone = MutableStateFlow("Hub Zone A")
    val forecastZone: StateFlow<String> = _forecastZone.asStateFlow()

    private val _forecastRange = MutableStateFlow("Tomorrow")
    val forecastRange: StateFlow<String> = _forecastRange.asStateFlow()

    private val _isForecastLoading = MutableStateFlow(false)
    val isForecastLoading: StateFlow<Boolean> = _isForecastLoading.asStateFlow()

    // Active Forecast response
    data class ForecastResult(
        val recommendedUnits: Int,
        val explanationEn: String,
        val explanationBn: String,
        val confidenceScore: Int
    )

    private val _activeForecast = MutableStateFlow<ForecastResult?>(null)
    val activeForecast: StateFlow<ForecastResult?> = _activeForecast.asStateFlow()

    // Smart alerts list (can be custom models, editable in ViewModel so users can "Approve" or "Dismiss" them)
    data class SmartAlert(
        val id: Int,
        val type: String, // "WET", "FESTIVAL", "EXPIRY"
        val titleEn: String,
        val titleBn: String,
        val descEn: String,
        val descBn: String,
        val targetSku: String,
        val actionValue: Int, // reorder boost percentage or discount
        val isApplied: Boolean = false
    )

    private val _smartAlerts = MutableStateFlow<List<SmartAlert>>(emptyList())
    val smartAlerts: StateFlow<List<SmartAlert>> = _smartAlerts.asStateFlow()

    // Subscription Billing selection
    private val _billingModalVisible = MutableStateFlow(false)
    val billingModalVisible: StateFlow<Boolean> = _billingModalVisible.asStateFlow()

    private val _selectedPlanName = MutableStateFlow("")
    val selectedPlanName: StateFlow<String> = _selectedPlanName.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow("")
    val selectedPaymentMethod: StateFlow<String> = _selectedPaymentMethod.asStateFlow()

    // PDF generation trigger
    private val _isGeneratingPdf = MutableStateFlow(false)
    val isGeneratingPdf: StateFlow<Boolean> = _isGeneratingPdf.asStateFlow()

    // UI feedback toast/snack message state
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Room database inventory list flow (reactive!)
    val productsList: StateFlow<List<Product>> = repository.allProducts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Prepopulate database if empty and initialize standard alerts
        viewModelScope.launch {
            repository.allProducts.collect { list ->
                if (list.isEmpty()) {
                    seedDatabase()
                }
            }
        }
        resetAlerts()
    }

    private suspend fun seedDatabase() {
        val today = LocalDate.now()
        val seedItems = listOf(
            Product(
                sku = "PRD-TOMATO-101",
                name = "Deshi Fresh Tomato",
                category = "Fresh Produce",
                currentStock = 240,
                reorderPoint = 100,
                price = 85.0,
                expiryDate = today.plusDays(2).toString() // Expiring in 2 days (Trigger warning!)
            ),
            Product(
                sku = "PRD-SEMAI-202",
                name = "Lacha Semai Classic",
                category = "Grocery",
                currentStock = 120,
                reorderPoint = 350, // Low stock for upcoming Eid spike!
                price = 145.0,
                expiryDate = today.plusDays(45).toString()
            ),
            Product(
                sku = "PRD-TEA-303",
                name = "Ispahani Premium Blend Tea",
                category = "Beverages",
                currentStock = 85,
                reorderPoint = 150, // Below threshold
                price = 220.0,
                expiryDate = today.plusDays(200).toString()
            ),
            Product(
                sku = "PRD-NOODLES-404",
                name = "Maggi Instant Noodles 8p",
                category = "Essentials",
                currentStock = 450,
                reorderPoint = 120, // Healthy stock
                price = 195.0,
                expiryDate = today.plusDays(90).toString()
            ),
            Product(
                sku = "PRD-ATTAR-505",
                name = "Musk Al-Ghazal Attar 6ml",
                category = "Spices & Traditional",
                currentStock = 15,
                reorderPoint = 40, // Below threshold
                price = 450.0,
                expiryDate = today.plusDays(365).toString()
            )
        )
        repository.insertAll(seedItems)
    }

    private fun resetAlerts() {
        _smartAlerts.value = listOf(
            SmartAlert(
                id = 1,
                type = "WET",
                titleEn = "Heavy Rain expected tomorrow!",
                titleBn = "আগামীকাল ভারী বৃষ্টির সম্ভাবনা!",
                descEn = "A rain system will hit your area. Increase Tea, Noodles, and Umbrella stock to meet delivery surges.",
                descBn = "আপনার এলাকায় বৃষ্টির সম্ভাবনা রয়েছে। ডেলিভারি বৃদ্ধির সাথে চা, নুডলস এবং ছাতা স্টক বাড়িয়ে রাখুন।",
                targetSku = "PRD-TEA-303",
                actionValue = 40
            ),
            SmartAlert(
                id = 2,
                type = "FESTIVAL",
                titleEn = "Major Festive Event in 3 days!",
                titleBn = "৩ দিন পর বড় উৎসবের দিন!",
                descEn = "Eid Shopping peaks soon. Double up Semai, Vermicelli, Sugars, and Attar items to maximize quick commerce trade.",
                descBn = "ঈদের কেনাকাটা তুঙ্গে উঠছে। কুইক কমার্স বেচাকেনা বাড়াতে সেমাই, চিনি এবং আতর স্টক দ্বিগুণ করুন।",
                targetSku = "PRD-SEMAI-202",
                actionValue = 300
            ),
            SmartAlert(
                id = 3,
                type = "EXPIRY",
                titleEn = "Critical Expiry Alert!",
                titleBn = "মেয়াদ উত্তীর্ণের জরুরি সতর্কীকরণ!",
                descEn = "Deshi Fresh Tomato stock (120 extra units) is expiring in 2 days. Launch a 30% discount to avoid total waste.",
                descBn = "দেশি ফ্রেশ টমেটো স্টক (১২০ অতিরিক্ত ইউনিট) ২ দিনে মেয়াদ শেষ হবে। অপচয় রোধে এখনই ৩০% ডিসকাউন্ট চালু করুন।",
                targetSku = "PRD-TOMATO-101",
                actionValue = 30
            )
        )
    }

    fun setLanguage(lang: Language) {
        _language.value = lang
    }

    fun setActiveTab(tab: AppTab) {
        _activeTab.value = tab
    }

    fun setSelectedCity(city: String) {
        _selectedCity.value = city
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Toggle dialog visibility
    fun showAddProductDialog(show: Boolean) {
        _isAddDialogVisible.value = show
    }

    fun setEditingProduct(product: Product?) {
        _editingProduct.value = product
    }

    // AI Forecast inputs
    fun setForecastCategory(cat: String) {
        _forecastCategory.value = cat
    }

    fun setForecastZone(zone: String) {
        _forecastZone.value = zone
    }

    fun setForecastRange(range: String) {
        _forecastRange.value = range
    }

    // Run dynamic forecast with delay
    fun generateForecast() {
        viewModelScope.launch {
            _isForecastLoading.value = true
            _activeForecast.value = null
            // Simulate AI crunching latency of 2.5 seconds
            delay(2500)

            val category = _forecastCategory.value
            val city = _selectedCity.value
            val range = _forecastRange.value

            val units = when (category) {
                "Fresh Produce" -> 280
                "Grocery" -> 420
                "Beverages" -> 310
                "Essentials" -> 640
                else -> 150
            }

            val confidence = (82..97).random()

            // Construct smart localized explanations based on input configurations
            val (expEn, expBn) = when {
                category == "Fresh Produce" -> {
                    Pair(
                        "Fresh Greens and Tomatoes have a high demand spike in $city because local supply lanes are bottlenecked by heavy morning forecasts.",
                        "সকালের ভারী বৃষ্টিপাতের কারণে স্থানীয় সরবরাহ বিঘ্নিত হওয়ায় $city-তে শাকসবজি ও টমেটোর উচ্চ চাহিদা দেখা দেবে।"
                    )
                }
                category == "Beverages" -> {
                    Pair(
                        "Cold storms or tea cycles predicted for mid-week in $city are pushing daily hot-tea demand of premium blends up by 55%.",
                        "সপ্তাহের মাঝামাঝি সময়ে $city-তে বৃষ্টিপাতের কারণে প্রিমিয়াম চা ও গরম পানীয়র চাহিদা ৫৫% বেশি থাকবে।"
                    )
                }
                category == "Grocery" -> {
                    Pair(
                        "Upcoming festive calendar days are shifting grocery baking components demand by 300% across central delivery zones in $city.",
                        "আসন্ন উৎসবের দিনগুলোর কারণে $city-এর কেন্দ্রীয় হাবগুলোতে প্রসাধন সামগ্রী ও গুড়ো সেমাইয়ের বেচাকেনা ৩০০% বৃদ্ধি পাবে।"
                    )
                }
                else -> {
                    Pair(
                        "Weekly cyclical stocking is peaking, and day-of-week demand models for $category indicate healthy consumer traction in $city hubs.",
                        "সাপ্তাহিক স্টক চক্রটি তুঙ্গে রয়েছে, এবং $category এর সাপ্তাহিক চাহিদা বিশ্লেষণ অনুযায়ী $city পয়েন্টে দারুণ অগ্রগতি দেখা যাচ্ছে।"
                    )
                }
            }

            _activeForecast.value = ForecastResult(
                recommendedUnits = units,
                explanationEn = expEn,
                explanationBn = expBn,
                confidenceScore = confidence
            )
            _isForecastLoading.value = false
        }
    }

    // Add normal product
    fun addProduct(sku: String, name: String, category: String, stock: Int, reorder: Int, price: Double, expiry: String) {
        viewModelScope.launch {
            val prod = Product(sku = sku, name = name, category = category, currentStock = stock, reorderPoint = reorder, price = price, expiryDate = expiry)
            repository.insert(prod)
            showToastMessage("Product $sku successfully added!")
        }
    }

    // Update normal product
    fun updateProduct(prod: Product) {
        viewModelScope.launch {
            repository.update(prod)
            showToastMessage("Product ${prod.sku} successfully updated!")
        }
    }

    // Delete product
    fun deleteProduct(prod: Product) {
        viewModelScope.launch {
            repository.delete(prod)
            showToastMessage("Product ${prod.sku} permanently removed.")
        }
    }

    // One-click alert approvals (affects the Database!)
    fun approveSmartAlert(alertId: Int) {
        viewModelScope.launch {
            val alertList = _smartAlerts.value.map { alert ->
                if (alert.id == alertId) {
                    // Execute stock increments on database reactively!
                    val matchingSku = alert.targetSku
                    val originalProduct = productsList.value.find { it.sku == matchingSku }
                    if (originalProduct != null) {
                        val increasePercentage = alert.actionValue
                        val addUnits = when (alert.type) {
                            "WET" -> 50 // rain boost tea: +50 units
                            "FESTIVAL" -> 250 // Eid boost semai: +250 units
                            else -> 0
                        }

                        if (addUnits > 0) {
                            val updatedProduct = originalProduct.copy(
                                currentStock = originalProduct.currentStock + addUnits
                            )
                            repository.update(updatedProduct)
                        }
                    } else if (alert.type == "EXPIRY") {
                        // Apply tomatoes 30% discount directly in Room database!
                        val tomatoProd = productsList.value.find { it.sku == "PRD-TOMATO-101" }
                        if (tomatoProd != null) {
                            val updatedTomato = tomatoProd.copy(
                                price = (tomatoProd.price * 0.70)
                            )
                            repository.update(updatedTomato)
                        }
                    }
                    alert.copy(isApplied = true)
                } else alert
            }
            _smartAlerts.value = alertList
            showToastMessage("Action applied on database!")
        }
    }

    fun dismissSmartAlert(alertId: Int) {
        _smartAlerts.value = _smartAlerts.value.filter { it.id != alertId }
    }

    fun showBillingModal(planName: String) {
        _selectedPlanName.value = planName
        _selectedPaymentMethod.value = ""
        _billingModalVisible.value = true
    }

    fun hideBillingModal() {
        _billingModalVisible.value = false
    }

    fun selectPaymentMethod(method: String) {
        _selectedPaymentMethod.value = method
    }

    fun executePayment() {
        viewModelScope.launch {
            _billingModalVisible.value = false
            showToastMessage("Subscription to ${_selectedPlanName.value} successful! Gateway authorized via ${_selectedPaymentMethod.value}.")
        }
    }

    fun triggerPdfExport() {
        viewModelScope.launch {
            _isGeneratingPdf.value = true
            delay(2000) // Simulate PDF compiling
            _isGeneratingPdf.value = false
            showToastMessage("Audit Report (PDF) successfully generated and shared!")
        }
    }

    fun showToastMessage(msg: String) {
        viewModelScope.launch {
            _toastMessage.value = msg
            delay(3000)
            if (_toastMessage.value == msg) {
                _toastMessage.value = null
            }
        }
    }

    class Factory(private val repository: ProductRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
