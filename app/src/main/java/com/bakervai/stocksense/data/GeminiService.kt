package com.bakervai.stocksense.data

import com.bakervai.stocksense.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * GeminiService — calls Google Gemini 1.5 Flash (free tier: 15 req/min, 1M tokens/day)
 * to generate real AI forecasts based on live inventory data.
 *
 * Uses the REST API with BuildConfig.GEMINI_API_KEY injected via secrets-gradle-plugin.
 */
object GeminiService {

    private const val API_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    data class ForecastResponse(
        val recommendedUnits: Int,
        val explanationEn: String,
        val explanationBn: String,
        val confidenceScore: Int,
        val isFromAI: Boolean = true
    )

    /**
     * Generates an AI-powered demand forecast.
     *
     * @param category    Product category (e.g. "Fresh Produce")
     * @param city        Selected city (e.g. "Dhaka")
     * @param period      Forecast period ("Tomorrow", "next_3_days", "next_7_days")
     * @param lowStockSkus List of SKUs with stock <= reorderPoint (context for AI)
     * @param expiringSkus List of SKUs expiring within 3 days (context for AI)
     */
    suspend fun generateForecast(
        category: String,
        city: String,
        period: String,
        lowStockSkus: List<String> = emptyList(),
        expiringSkus: List<String> = emptyList()
    ): ForecastResponse = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Return smart fallback if API key is not configured
            return@withContext generateSmartFallback(category, city, period)
        }

        val periodDays = when (period) {
            "next_3_days" -> "3 days"
            "next_7_days" -> "7 days"
            else -> "1 day (tomorrow)"
        }

        val inventoryContext = buildString {
            if (lowStockSkus.isNotEmpty()) {
                append("Low stock items: ${lowStockSkus.joinToString(", ")}. ")
            }
            if (expiringSkus.isNotEmpty()) {
                append("Expiring soon: ${expiringSkus.joinToString(", ")}. ")
            }
        }

        val prompt = """
You are an AI inventory optimizer for dark stores and quick-commerce warehouses in South Asia.

Context:
- City: $city
- Product category: $category
- Forecast period: $periodDays
- Current inventory status: $inventoryContext

Task: Provide a demand forecast for the "$category" category over the next $periodDays in $city.

Respond with ONLY valid JSON (no markdown, no extra text) in this exact format:
{
  "recommended_units": <integer 50-800>,
  "confidence_score": <integer 70-97>,
  "explanation_en": "<2-3 sentence demand reason in English, mention local factors, weather, festivals>",
  "explanation_bn": "<same explanation in Bengali/Bangla language>"
}
""".trimIndent()

        try {
            val requestBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 512)
                })
            }.toString()

            val request = Request.Builder()
                .url("$API_URL?key=$apiKey")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext generateSmartFallback(category, city, period)
            }

            // Parse Gemini response
            val geminiJson = JSONObject(body)
            val text = geminiJson
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
                .trim()

            // Extract JSON from the response text
            val jsonStart = text.indexOf('{')
            val jsonEnd = text.lastIndexOf('}') + 1
            val cleanJson = if (jsonStart >= 0 && jsonEnd > jsonStart) {
                text.substring(jsonStart, jsonEnd)
            } else text

            val parsed = JSONObject(cleanJson)

            ForecastResponse(
                recommendedUnits = parsed.optInt("recommended_units", 200),
                confidenceScore = parsed.optInt("confidence_score", 85),
                explanationEn = parsed.optString("explanation_en", "Demand forecast based on current market trends."),
                explanationBn = parsed.optString("explanation_bn", "বর্তমান বাজার প্রবণতার ভিত্তিতে চাহিদার পূর্বাভাস।"),
                isFromAI = true
            )

        } catch (e: Exception) {
            // Network or parse error — use smart fallback
            generateSmartFallback(category, city, period)
        }
    }

    /** Smart rule-based fallback when Gemini API is unavailable. */
    private fun generateSmartFallback(
        category: String,
        city: String,
        period: String
    ): ForecastResponse {
        val units = when (category) {
            "Fresh Produce" -> if (city in listOf("Dhaka", "Chittagong")) 280 else 220
            "Grocery"       -> if (city in listOf("Dhaka", "Chittagong")) 450 else 380
            "Beverages"     -> if (city == "Mumbai") 290 else 320
            "Essentials"    -> 640
            else            -> 180
        }
        val multiplier = when (period) {
            "next_3_days" -> 3
            "next_7_days" -> 7
            else          -> 1
        }
        val finalUnits = units * multiplier / (if (period == "Tomorrow") 1 else 1)

        val (en, bn) = when (category) {
            "Fresh Produce" -> Pair(
                "Fresh produce demand in $city is driven by daily household consumption and local wet market supply constraints.",
                "$city-তে তাজা শাকসবজির চাহিদা দৈনন্দিন পারিবারিক ব্যবহার ও স্থানীয় বাজারের সীমিত সরবরাহের কারণে বেশি থাকে।"
            )
            "Grocery" -> Pair(
                "Grocery demand in $city peaks around weekly shopping cycles and upcoming festive occasions.",
                "$city-তে সাপ্তাহিক কেনাকাটার সময় এবং উৎসবের মৌসুমে মুদির পণ্যের চাহিদা উল্লেখযোগ্যভাবে বৃদ্ধি পায়।"
            )
            "Beverages" -> Pair(
                "Beverage demand is elevated due to seasonal temperature patterns and daily consumption habits in $city.",
                "$city-তে মৌসুমী তাপমাত্রার কারণে এবং দৈনন্দিন পানীয়র অভ্যাসের কারণে পানীয় পণ্যের চাহিদা বেশি।"
            )
            else -> Pair(
                "Demand in $city follows steady weekly consumption patterns for the $category segment.",
                "$city-তে $category পণ্যের চাহিদা সাপ্তাহিক ভোক্তা প্যাটার্ন অনুযায়ী স্থিতিশীল।"
            )
        }

        return ForecastResponse(
            recommendedUnits = finalUnits,
            confidenceScore = (78..88).random(),
            explanationEn = en,
            explanationBn = bn,
            isFromAI = false
        )
    }
}
