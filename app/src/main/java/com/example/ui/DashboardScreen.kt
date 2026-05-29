package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
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
import com.example.data.Product
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    products: List<Product>,
    lang: Language,
    city: String
) {
    // Determine the currency prefix based on active city
    val currency = when (city) {
        "Dhaka", "Chittagong" -> Lang.get("currency_bdt", lang)
        "Mumbai" -> Lang.get("currency_inr", lang)
        "Karachi", "Lahore" -> Lang.get("currency_pkr", lang)
        else -> "$"
    }

    // Multiply values by currency factor for regional realism
    val scaleFactor = when (city) {
        "Mumbai" -> 0.85
        "Karachi", "Lahore" -> 2.45
        else -> 1.0
    }

    val baseRevenue = 15800.0 * scaleFactor
    val revenueText = "$currency${String.format("%,.0f", baseRevenue)}"

    // Calculate low stock count reactively from DB!
    val lowStockCount = products.filter { it.currentStock <= it.reorderPoint }.size
    val alerts = viewModel.smartAlerts.collectAsState().value

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
    ) {
        // Welcome Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (lang == Language.EN) "Welcome back, Operator" else "স্বাগতম, অপারেটর",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = if (lang == Language.EN) "Monitoring Dark Hub - $city Point" else "ডার্ক হাব পর্যবেক্ষণ করা হচ্ছে - $city পয়েন্ট",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        // Overview metrics card grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = Lang.get("card_predicted_demand", lang),
                        value = "1,840",
                        subtitle = Lang.get("unit_text", lang),
                        color = IndigoAccent,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = Lang.get("card_stock_alerts", lang),
                        value = lowStockCount.toString(),
                        subtitle = Lang.get("alert_text", lang),
                        color = if (lowStockCount > 2) AccentRed else AccentYellow,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = Lang.get("card_waste_score", lang),
                        value = "94.2%",
                        subtitle = if (lang == Language.EN) "Optimal conservation" else "সর্বোত্তম সংরক্ষণ",
                        color = AccentGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = Lang.get("card_revenue_saved", lang),
                        value = revenueText,
                        subtitle = if (lang == Language.EN) "Prevented shrinkage" else "ক্ষতি প্রতিরোধ করা হয়েছে",
                        color = AccentGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Live stock health meter
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SlateCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (lang == Language.EN) "Real-time Hub Health Index" else "হাব লাইভ হেলথ সূচক",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val healthProgress = when {
                        lowStockCount > 3 -> 0.45f
                        lowStockCount > 1 -> 0.76f
                        else -> 0.94f
                    }
                    val healthLabel = when {
                        lowStockCount > 3 -> if (lang == Language.EN) "CRITICAL OUTAGES" else "মারাত্মক ঘাটতি"
                        lowStockCount > 1 -> if (lang == Language.EN) "MODERATE OUTAGES" else "মাঝারি ঘাটতি"
                        else -> if (lang == Language.EN) "ALL SYSTEMS GREEN" else "সবকিছু স্বাভাবিক"
                    }
                    val healthColor = when {
                        lowStockCount > 3 -> AccentRed
                        lowStockCount > 1 -> AccentYellow
                        else -> AccentGreen
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = healthLabel,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = healthColor
                        )
                        Text(
                            text = "${(healthProgress * 100).toInt()}%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Stylized progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(50))
                            .background(SlateBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(healthProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(healthColor.mix(Color.Black, 0.2f), healthColor)
                                    )
                                )
                        )
                    }
                }
            }
        }

        // Smart Alert Header
        item {
            Text(
                text = Lang.get("smart_alerts", lang),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Active Alert notification list
        if (alerts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (lang == Language.EN) "No active alerts. Operational levels healthy!" else "কোনো সতর্কবার্তা নেই। সবকিছু স্বাভাবিক!",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(alerts, key = { it.id }) { alert ->
                AnimatedVisibility(visible = true) {
                    val alertColor = when (alert.type) {
                        "WET" -> IndigoAccent
                        "FESTIVAL" -> AccentYellow
                        else -> AccentRed
                    }
                    val cardBgColor = when (alert.type) {
                        "WET" -> Color(0x1F6366F1)
                        "FESTIVAL" -> Color(0x1FF59E0B)
                        else -> Color(0x1FEF4444)
                    }
                    val cardBorderColor = alertColor.copy(alpha = 0.35f)
                    val confText = when (alert.type) {
                        "WET" -> "92% Conf."
                        "FESTIVAL" -> "95% Conf."
                        else -> "88% Conf."
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, cardBorderColor, RoundedCornerShape(24.dp))
                            .padding(bottom = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBgColor)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            // Header: tag + confidence badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(RoundedCornerShape(50))
                                                .background(alertColor)
                                        )
                                        Text(
                                            text = if (alert.type == "WET") "AI ALERT: WEATHER CONTEXT" else "AI ALERT: SEASONAL CAPACITY",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.2.sp,
                                            color = alertColor.mix(Color.White, 0.4f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (lang == Language.EN) alert.titleEn else alert.titleBn,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        lineHeight = 21.sp
                                    )
                                }

                                // Confidence badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(alertColor)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = confText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Highlighted Internal detail box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.4f))
                                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = if (lang == Language.EN) alert.descEn else alert.descBn,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (alert.isApplied) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Success",
                                        tint = AccentGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (lang == Language.EN) "✓ Implemented locally" else "✓ স্থানীয়ভাবে সমন্বিত",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentGreen
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Secondary Button (Dismiss)
                                    Box(
                                        modifier = Modifier
                                            .weight(0.35f)
                                            .height(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                            .clickable { viewModel.dismissSmartAlert(alert.id) }
                                            .padding(horizontal = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = Lang.get("btn_dismiss", lang),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextSecondary
                                        )
                                    }

                                    // Primary Button (Approve Restock)
                                    Button(
                                        onClick = { viewModel.approveSmartAlert(alert.id) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = alertColor,
                                            contentColor = Color.White
                                        ),
                                        contentPadding = PaddingValues(horizontal = 14.dp),
                                        modifier = Modifier
                                            .weight(0.65f)
                                            .height(38.dp)
                                    ) {
                                        Text(
                                            text = if (alert.type == "EXPIRY") Lang.get("btn_apply_discount", lang) else Lang.get("btn_approve", lang),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
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
}
