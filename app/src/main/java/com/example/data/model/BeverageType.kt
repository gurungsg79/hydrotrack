package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.HydroAmber
import com.example.ui.theme.HydroBlue
import com.example.ui.theme.HydroCyan
import com.example.ui.theme.HydroMint
import com.example.ui.theme.HydroTeal

enum class BeverageType(
    val displayName: String,
    val iconEmoji: String,
    val hydrationFactor: Float, // Hydration effectiveness factor
    val colorHex: Long
) {
    WATER("Pure Water", "💧", 1.0f, 0xFF88CEFF),
    SPARKLING("Sparkling Water", "🫧", 1.0f, 0xFF67E8F9),
    ELECTROLYTE("Electrolytes", "⚡", 1.08f, 0xFF7DD0B6),
    HERBAL_TEA("Herbal Tea", "🍵", 0.98f, 0xFFA8D7E0),
    GREEN_TEA("Green Tea", "🌿", 0.92f, 0xFF86EFAC),
    COFFEE("Coffee", "☕", 0.82f, 0xFFD4A373),
    COCONUT_WATER("Coconut Water", "🥥", 1.00f, 0xFFE0E7FF),
    JUICE_SMOOTHIE("Juice / Smoothie", "🥤", 0.85f, 0xFFFFB74D),
    MILK("Milk / Oat Milk", "🥛", 0.90f, 0xFFF5F5F4);

    fun calculateEffectiveHydration(volumeMl: Int): Int {
        return (volumeMl * hydrationFactor).toInt()
    }

    companion object {
        fun fromName(name: String): BeverageType {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: WATER
        }
    }
}
