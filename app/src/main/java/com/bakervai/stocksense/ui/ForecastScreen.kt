package com.bakervai.stocksense.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bakervai.stocksense.ui.theme.*

@Composable
fun ForecastScreen(
    viewModel: MainViewModel,
    lang: Language,
    city: String
) {
    val categories = listOf("Fresh Produce", "Grocery", "Beverages", "Essentials")
    val zones = listOf("Hub Zone A", "Hub Zone B", "Hub Zone C")
    val periods = listOf("Tomorrow", "next_3_days", "next_7_days")

    val selectedCat = viewModel.forecastCategory.collectAsState().value
    val selectedZone = viewModel.forecastZone.collectAsState().value
    val selectedPeriod = viewModel.forecastRange.collectAsState().value

    val isLoading = viewModel.isForecastLoading.collectAsState().value
    val forecast = viewModel.activeForecast.collectAsState().value

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var zoneDropdownExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
    ) {
        // Module Headline
        item {
            Column {
                Text(
                    text = if (lang == Language.EN) "AI Predictive Forecaster" else "এআই চাহিদার পূর্বাভাস",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = if (lang == Language.EN) "Pre-calculate dark-store stock peaks using seasonal trends" else "ঋতুভিত্তিক প্যাটার্ন বিশ্লেষণ করে স্টকের পূর্বাভাস হিসাব করুন",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        // Form parameters selection block
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorder, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = SlateCard)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Category drop list
                    Column {
                        Text(
                            text = Lang.get("lbl_category", lang),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SlateBg)
                                .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                                .clickable { categoryDropdownExpanded = true }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = selectedCat, color = TextPrimary, fontSize = 14.sp)
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Drop list",
                                    tint = TextSecondary
                                )
                            }
                            DropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false },
                                modifier = Modifier.background(SlateCard)
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(text = cat, color = TextPrimary) },
                                        onClick = {
                                            viewModel.setForecastCategory(cat)
                                            categoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Zone selection drop list
                    Column {
                        Text(
                            text = Lang.get("lbl_location", lang),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SlateBg)
                                .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                                .clickable { zoneDropdownExpanded = true }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = selectedZone, color = TextPrimary, fontSize = 14.sp)
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Drop list",
                                    tint = TextSecondary
                                )
                            }
                            DropdownMenu(
                                expanded = zoneDropdownExpanded,
                                onDismissRequest = { zoneDropdownExpanded = false },
                                modifier = Modifier.background(SlateCard)
                            ) {
                                zones.forEach { zone ->
                                    DropdownMenuItem(
                                        text = { Text(text = zone, color = TextPrimary) },
                                        onClick = {
                                            viewModel.setForecastZone(zone)
                                            zoneDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Forecasting Period Chips Row
                    Column {
                        Text(
                            text = Lang.get("lbl_date_range", lang),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            periods.forEach { periodKey ->
                                val periodValue = if (periodKey == "Tomorrow") "Tomorrow" else periodKey
                                val isSelected = selectedPeriod == periodValue
                                val labelText = Lang.get(periodKey, lang)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) IndigoBg else SlateBg)
                                        .border(
                                            1.dp,
                                            if (isSelected) IndigoAccent else SlateBorder,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { viewModel.setForecastRange(periodValue) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = labelText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) IndigoAccent else TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Core Trigger Button
                    Button(
                        onClick = { viewModel.generateForecast() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent)
                    ) {
                        Text(
                            text = Lang.get("btn_forecast_now", lang),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Loading skeletons for AI processing states
        if (isLoading) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LoadingSkeleton(modifier = Modifier.fillMaxWidth().height(90.dp))
                    LoadingSkeleton(modifier = Modifier.fillMaxWidth().height(150.dp))
                }
            }
        }

        // AI Results output
        item {
            AnimatedVisibility(
                visible = forecast != null && !isLoading,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (forecast != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SlateBorder, RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = SlateCard)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Centered Units Recommended Banner
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(IndigoBg)
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // AI source badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(if (forecast.isFromAI) AccentGreen.copy(alpha = 0.2f) else AccentYellow.copy(alpha = 0.2f))
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (forecast.isFromAI) "✦ GEMINI AI" else "◈ SMART ESTIMATE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (forecast.isFromAI) AccentGreen else AccentYellow
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (lang == Language.EN) "RECOMMENDED OPTIMAL STOCK" else "সুপারিশকৃত সর্বোত্তম মজুদ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IndigoLight
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${forecast.recommendedUnits} UNITS",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = IndigoLight
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Localized Reason explaining weather patterns & holidays
                            Text(
                                text = Lang.get("lbl_reason", lang),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (lang == Language.EN) forecast.explanationEn else forecast.explanationBn,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Confidence score progress bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = Lang.get("confidence", lang),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "${forecast.confidenceScore}%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(SlateBg)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(forecast.confidenceScore / 100f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(50))
                                        .background(AccentGreen)
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Sync Action Button
                            Button(
                                onClick = {
                                    viewModel.showToastMessage(Lang.get("notification_action_applied", lang))
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoDark)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Sync",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = Lang.get("btn_apply_stock", lang),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
