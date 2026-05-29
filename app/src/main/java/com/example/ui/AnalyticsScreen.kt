package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
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
import com.example.ui.theme.*

@Composable
fun AnalyticsScreen(
    viewModel: MainViewModel,
    lang: Language
) {
    val isGenerating = viewModel.isGeneratingPdf.collectAsState().value

    // Seed mock coordinate values for charts
    val accuracyData = listOf(0.79f, 0.86f, 0.84f, 0.91f, 0.95f)
    val accuracyWeeks = listOf("Wk 19", "Wk 20", "Wk 21", "Wk 22", "Wk 23")

    val wasteData = listOf(0.18f, 0.28f, 0.42f, 0.55f, 0.62f, 0.74f)
    val wasteMonths = listOf("Dec", "Jan", "Feb", "Mar", "Apr", "May")

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
        ) {
            // Header
            item {
                Column {
                    Text(
                        text = Lang.get("analytics_title", lang),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (lang == Language.EN) "Operational Efficiency Reports" else "কার্যকরী দক্ষতা অডিট রিপোর্ট",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // Line Chart Accuracy
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SlateBorder, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = Lang.get("chart_demand_flow", lang),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        InteractiveLineChart(
                            weeklyData = accuracyData,
                            weeks = accuracyWeeks,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            accuracyWeeks.forEach { week ->
                                Text(text = week, fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }

            // Bar Chart Waste
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SlateBorder, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = Lang.get("chart_waste_flow", lang),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        InteractiveBarChart(
                            monthlyData = wasteData,
                            months = wasteMonths,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            wasteMonths.forEach { m ->
                                Text(text = m, fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }

            // Seasonal Leaders
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SlateBorder, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = Lang.get("seasonal_performers", lang),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Category Leader Item 1
                        SeasonalItem(
                            season = if (lang == Language.EN) "Festival (Eid/Puja)" else "উৎসবকালীন (ঈদ/পূজা)",
                            category = if (lang == Language.EN) "Spices & Traditional / Sweet Meats" else "মশলা ও সেমাই জাত",
                            efficiency = "+320% spike demand"
                        )
                        Divider(color = SlateBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))

                        // Category Leader Item 2
                        SeasonalItem(
                            season = if (lang == Language.EN) "Monsoon Rain Cycle" else "বর্ষাকালীন চক্র",
                            category = if (lang == Language.EN) "Hot Beverages & Instant Food" else "চা-কফি ও নুডলস",
                            efficiency = "94% forecast accuracy"
                        )
                        Divider(color = SlateBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))

                        // Category Leader Item 3
                        SeasonalItem(
                            season = if (lang == Language.EN) "Extreme Summer Peak" else "গ্রীষ্মকালীন তাপদাহ",
                            category = if (lang == Language.EN) "Fresh Produce & Electrolyte Beverages" else "শাকসবজি ও ড্রিংকস",
                            efficiency = "12% waste reduction saved"
                        )
                    }
                }
            }

            // Export to PDF button
            item {
                Button(
                    onClick = { viewModel.triggerPdfExport() },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Export Report", tint = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = Lang.get("btn_export_pdf", lang), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Circular Loader overlay when constructing PDF file
        if (isGenerating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable(enabled = false) {}, // blocking click throughs
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    modifier = Modifier
                        .padding(32.dp)
                        .border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = IndigoAccent)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (lang == Language.EN) "Generating PDF Audit Log..." else "পিডিএফ অডিট লগ তৈরি হচ্ছে...",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (lang == Language.EN) "Injecting stock health vectors & accuracy margins." else "মজুদ স্বাস্থ্য ভেক্টর এবং নির্ভুলতা মার্জিন ইনজেক্ট করা হচ্ছে।",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SeasonalItem(
    season: String,
    category: String,
    efficiency: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = season, fontSize = 10.sp, color = IndigoLight, fontWeight = FontWeight.Bold)
            Text(text = category, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(GreenBg)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(text = efficiency, fontSize = 10.sp, color = AccentGreen, fontWeight = FontWeight.SemiBold)
        }
    }
}
