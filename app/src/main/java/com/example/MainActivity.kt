package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppDatabase
import com.example.data.ProductRepository
import com.example.ui.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize Room Database, DAO and Repository
    val database = AppDatabase.getDatabase(applicationContext)
    val repository = ProductRepository(database.productDao())
    val viewModel = MainViewModel(repository)

    setContent {
      MyApplicationTheme {
        StockSenseDashboard(viewModel)
      }
    }
  }
}

@Composable
fun StockSenseDashboard(viewModel: MainViewModel) {
    val context = LocalContext.current
    val products by viewModel.productsList.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val lang by viewModel.language.collectAsState()
    val city by viewModel.selectedCity.collectAsState()
    val toastMsg by viewModel.toastMessage.collectAsState()

    var cityDropdownExpanded by remember { mutableStateOf(false) }
    val citiesList = listOf("Dhaka", "Chittagong", "Mumbai", "Karachi", "Lahore")

    // Show native secure prompt notices when viewmodel triggers feedback
    LaunchedEffect(toastMsg) {
        if (toastMsg != null) {
            Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBg),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlateBg)
                    .statusBarsPadding() // avoid status notches
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
            ) {
                // Header brand name row + language change button + Avatar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "STOCKSENSE AI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = IndigoLight
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Pulsing Live Beacon Indicator
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(AccentGreen)
                            )
                        }

                        // Region location dropdown selector
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { cityDropdownExpanded = true }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = "Location Pin",
                                    tint = IndigoAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${Lang.get(city.lowercase(), lang)}, BD",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "City Dropdown",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = cityDropdownExpanded,
                                onDismissRequest = { cityDropdownExpanded = false },
                                modifier = Modifier.background(SlateCard)
                            ) {
                                citiesList.forEach { cName ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = Lang.get(cName.lowercase(), lang),
                                                color = TextPrimary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        },
                                        onClick = {
                                            viewModel.setSelectedCity(cName)
                                            cityDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Top Right Action Group (Language Pill + Initials Avatar)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Bilingual Toggle Slider (EN vs বাংলা) - Perfect Circle Pill shape
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(SlateCard)
                                .border(1.dp, SlateBorder, RoundedCornerShape(50))
                                .padding(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "EN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (lang == Language.EN) Color.White else TextSecondary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (lang == Language.EN) IndigoAccent else Color.Transparent)
                                    .clickable { viewModel.setLanguage(Language.EN) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                            Text(
                                text = "বাংলা",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (lang == Language.BN) Color.White else TextSecondary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (lang == Language.BN) IndigoAccent else Color.Transparent)
                                    .clickable { viewModel.setLanguage(Language.BN) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        // Circular Avatar Badge "DS" (Operator initials)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(50))
                                .background(IndigoAccent)
                                .border(1.dp, IndigoLight.copy(alpha = 0.3f), RoundedCornerShape(50)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "DS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                HorizontalDivider(
                    color = SlateBorder.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        },
        bottomBar = {
            // Raised bottoms navigation container with full gesture padding overrides
            NavigationBar(
                containerColor = SlateCard,
                contentColor = TextSecondary,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .background(SlateCard)
                    .windowInsetsPadding(WindowInsets.navigationBars) // avoid bottom pill overrides!
                    .border(1.dp, SlateBorder, RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
            ) {
                // Tab 1: Dashboard
                NavigationBarItem(
                    selected = activeTab == AppTab.DASHBOARD,
                    onClick = { viewModel.setActiveTab(AppTab.DASHBOARD) },
                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
                    label = { Text(text = Lang.get("tab_dashboard", lang), fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = IndigoAccent,
                        selectedTextColor = IndigoAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = IndigoBg
                    )
                )

                // Tab 2: AI Forecasting
                NavigationBarItem(
                    selected = activeTab == AppTab.FORECAST,
                    onClick = { viewModel.setActiveTab(AppTab.FORECAST) },
                    icon = { Icon(imageVector = Icons.Default.Refresh, contentDescription = "Forecast") },
                    label = { Text(text = Lang.get("tab_forecast", lang), fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = IndigoAccent,
                        selectedTextColor = IndigoAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = IndigoBg
                    )
                )

                // Tab 3: Inventory Database
                NavigationBarItem(
                    selected = activeTab == AppTab.INVENTORY,
                    onClick = { viewModel.setActiveTab(AppTab.INVENTORY) },
                    icon = { Icon(imageVector = Icons.Default.List, contentDescription = "List") },
                    label = { Text(text = Lang.get("tab_inventory", lang), fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = IndigoAccent,
                        selectedTextColor = IndigoAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = IndigoBg
                    )
                )

                // Tab 4: Analytics Line & Bar Metrics
                NavigationBarItem(
                    selected = activeTab == AppTab.ANALYTICS,
                    onClick = { viewModel.setActiveTab(AppTab.ANALYTICS) },
                    icon = { Icon(imageVector = Icons.Default.Share, contentDescription = "Analytics") },
                    label = { Text(text = Lang.get("tab_analytics", lang), fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = IndigoAccent,
                        selectedTextColor = IndigoAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = IndigoBg
                    )
                )

                // Tab 5: Subscription Pricing model checkout
                NavigationBarItem(
                    selected = activeTab == AppTab.SAAS,
                    onClick = { viewModel.setActiveTab(AppTab.SAAS) },
                    icon = { Icon(imageVector = Icons.Default.Check, contentDescription = "Pricing") },
                    label = { Text(text = Lang.get("tab_saas", lang), fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = IndigoAccent,
                        selectedTextColor = IndigoAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = IndigoBg
                    )
                )
            }
        }
    ) { paddingValues ->
        // Render content page dynamically in a Box layout
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SlateBg)
                .padding(paddingValues)
        ) {
            when (activeTab) {
                AppTab.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    products = products,
                    lang = lang,
                    city = city
                )
                AppTab.FORECAST -> ForecastScreen(
                    viewModel = viewModel,
                    lang = lang,
                    city = city
                )
                AppTab.INVENTORY -> InventoryScreen(
                    viewModel = viewModel,
                    products = products,
                    lang = lang,
                    city = city
                )
                AppTab.ANALYTICS -> AnalyticsScreen(
                    viewModel = viewModel,
                    lang = lang
                )
                AppTab.SAAS -> SaaSScreen(
                    viewModel = viewModel,
                    lang = lang,
                    city = city
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

