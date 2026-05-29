package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SaaSScreen(
    viewModel: MainViewModel,
    lang: Language,
    city: String
) {
    val isBypassing = viewModel.billingModalVisible.collectAsState().value
    val selectedPlan = viewModel.selectedPlanName.collectAsState().value
    val selectedMethod = viewModel.selectedPaymentMethod.collectAsState().value

    // Scale pricing and select currency depending on chosen city
    val (currency, exchangeRate) = when (city) {
        "Dhaka", "Chittagong" -> Pair(Lang.get("currency_bdt", lang), 1.0)
        "Mumbai" -> Pair(Lang.get("currency_inr", lang), 0.77)
        "Karachi", "Lahore" -> Pair(Lang.get("currency_pkr", lang), 2.50)
        else -> Pair("$", 1.0)
    }

    // Pricing models
    val starterPrice = (999 * exchangeRate).toInt()
    val growthPrice = (2999 * exchangeRate).toInt()

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
                        text = Lang.get("saas_title", lang),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (lang == Language.EN) "Select a scaling option to unlock more dark stores" else "অতিরিক্ত ডার্ক স্টোর আনলক করতে আপনার প্ল্যান নির্বাচন করুন",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // Starter Plan Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SlateBorder, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateCard)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Lang.get("plan_starter", lang),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SlateBorder)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "1 Store",
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$currency$starterPrice",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = " / " + Lang.get("currency_bdt", lang) + "999 " + Lang.get("tomorrow", lang).replace("আগামীকাল", "পর্যন্ত").replace("Tomorrow", "eqv"),
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = Lang.get("desc_starter", lang),
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        PlanFeatureItem(text = if (lang == Language.EN) "Max 100 Active SKU slots" else "সর্বোচ্চ ১০০ সক্রিয় পণ্যের স্লট")
                        PlanFeatureItem(text = if (lang == Language.EN) "Core AI Demand Forecast" else "কোর এআই চাহিদা পূর্বাভাস")
                        PlanFeatureItem(text = if (lang == Language.EN) "Standard inventory alerts" else "সাধারণ ইনভেন্টরি সতর্কতা")

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.showBillingModal("Starter Core") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SlateBorder)
                        ) {
                            Text(text = Lang.get("btn_subscribe", lang), fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }
            }

            // Growth Plan (Highly Styled Accent Card!)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, IndigoLight, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateCard)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Lang.get("plan_growth", lang),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = IndigoLight
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(IndigoBg)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "POPULAR",
                                    fontSize = 10.sp,
                                    color = IndigoLight,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$currency$growthPrice",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = " / " + Lang.get("currency_bdt", lang) + "2999 eqv",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = Lang.get("desc_growth", lang),
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        PlanFeatureItem(text = if (lang == Language.EN) "Up to 5 Dark Store locations" else "সর্বোচ্চ ৫টি ডার্ক স্টোর লোকেশন")
                        PlanFeatureItem(text = if (lang == Language.EN) "Unlimited SKU database items" else "আনলিমিটেড পণ্যের ডেটাবেস ভুক্তি")
                        PlanFeatureItem(text = if (lang == Language.EN) "Advanced localized festival modeling" else "উন্নত উৎসবকালীন চাহিদা মডেলিং")
                        PlanFeatureItem(text = if (lang == Language.EN) "One-click approval DB sync alerts" else "এক ক্লিকে অটো-রিঅর্ডার সিঙ্ক")

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.showBillingModal("Growth Scale") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent)
                        ) {
                            Text(text = Lang.get("btn_subscribe", lang), fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Enterprise Plan Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SlateBorder, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateCard)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = Lang.get("plan_enterprise", lang),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "CUSTOM",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = Lang.get("desc_enterprise", lang),
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        PlanFeatureItem(text = if (lang == Language.EN) "Custom APIs & Warehouse automated bots" else "কাস্টম এপিআই এবং গুদাম অটোমেশন রিমোট লগ")
                        PlanFeatureItem(text = if (lang == Language.EN) "Dedicated dedicated database servers" else "ডেডিকেটেড এবং এক্সক্লুসিভ ডেটাবেস সার্ভার")

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.showToastMessage(if (lang == Language.EN) "Connecting to Regional Sales Desk..." else "সেলস টিমের সাথে যোগাযোগ করা হচ্ছে...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SlateBg)
                        ) {
                            Text(text = Lang.get("btn_custom_pricing", lang), fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }
            }
        }

        // Checkout & mobile billing simulation bottom window sheet
        if (isBypassing) {
            CheckoutSimulationOverlay(
                viewModel = viewModel,
                lang = lang,
                city = city,
                planName = selectedPlan,
                selectedMethod = selectedMethod,
                onDismiss = { viewModel.hideBillingModal() }
            )
        }
    }
}

@Composable
fun PlanFeatureItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Feature benefit",
            tint = AccentGreen,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 12.sp, color = TextPrimary)
    }
}

// Gorgeous checkout overlay with region-specific payment processors
@Composable
fun CheckoutSimulationOverlay(
    viewModel: MainViewModel,
    lang: Language,
    city: String,
    planName: String,
    selectedMethod: String,
    onDismiss: () -> Unit
) {
    var walletNumber by remember { mutableStateOf("") }
    var otpEntered by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }

    // Region specific billing providers
    val gatewayOptions = when (city) {
        "Dhaka", "Chittagong" -> listOf("bKash", "Nagad")
        "Mumbai" -> listOf("Razorpay UPI", "Card Payment")
        "Karachi", "Lahore" -> listOf("JazzCash", "EasyPaisa")
        else -> listOf("International Card")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f))
            .clickable(enabled = true) { onDismiss() }, // dismiss on backdrop click
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {} // block backdrop click clicks inside overlay
                .border(2.dp, IndigoLight, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            colors = CardDefaults.cardColors(containerColor = SlateCard)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .navigationBarsPadding() // respect bottom notches
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${Lang.get("billing_payment", lang)} ($planName)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = { onDismiss() }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selectable checkout processors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    gatewayOptions.forEach { method ->
                        val isPicked = selectedMethod == method
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isPicked) IndigoBg else SlateBg)
                                .border(
                                    1.dp,
                                    if (isPicked) IndigoAccent else SlateBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.selectPaymentMethod(method) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = method,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPicked) IndigoAccent else TextPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                if (selectedMethod.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(18.dp))

                    if (!isOtpSent) {
                        Column {
                            Text(
                                text = if (lang == Language.EN) "Enter Mobile Wallet/UPI ID" else "মোবাইল ওয়ালেট নম্বর / ইউপিআই আইডি",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = walletNumber,
                                onValueChange = { walletNumber = it },
                                placeholder = { Text(text = "01XXXXXXXXX / id@upibank", color = TextSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = IndigoAccent,
                                    unfocusedBorderColor = SlateBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { isOtpSent = true },
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (lang == Language.EN) "Request OTP Authentication" else "ওটিপি (OTP) ও প্রমাণীকরণ পাঠান",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        Column {
                            Text(
                                text = if (lang == Language.EN) "Enter OTP code sent to your device" else "মোবাইলে পাঠানো ওটিপি (OTP) কোডটি লিখুন",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = otpEntered,
                                onValueChange = { otpEntered = it },
                                placeholder = { Text(text = "6-Digit Secure Pulse Code", color = TextSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = IndigoAccent,
                                    unfocusedBorderColor = SlateBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { viewModel.executePayment() },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (lang == Language.EN) "Authorize & Confirm Payment" else "পেমেন্ট অনুমোদন ও নিশ্চিত করুন",
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
