package com.example.data.model

enum class UnitSystem(val label: String, val abbreviation: String, val mlToUnitFactor: Float) {
    METRIC("Milliliters", "ml", 1.0f),
    IMPERIAL("Fluid Ounces", "fl oz", 0.033814f);

    fun format(amountMl: Int): String {
        return when (this) {
            METRIC -> "$amountMl ml"
            IMPERIAL -> {
                val oz = (amountMl * mlToUnitFactor)
                String.format(java.util.Locale.US, "%.1f fl oz", oz)
            }
        }
    }

    fun toDisplayValue(amountMl: Int): Int {
        return when (this) {
            METRIC -> amountMl
            IMPERIAL -> (amountMl * mlToUnitFactor).toInt()
        }
    }
}

enum class ClimateProfile(
    val title: String,
    val emoji: String,
    val tempEstimateC: Int,
    val waterMultiplier: Float,
    val frequencyMultiplier: Float,
    val description: String
) {
    MILD("Mild / Normal", "🌤️", 20, 1.0f, 1.0f, "Standard baseline hydration"),
    WARM("Warm Weather", "☀️", 28, 1.15f, 1.25f, "+15% water needed, reminders 25% closer"),
    HOT_DRY("Hot & Arid", "🌵", 36, 1.30f, 1.50f, "+30% water needed, hourly reminders recommended"),
    TROPICAL_HUMID("Tropical Humid", "🌴", 32, 1.25f, 1.40f, "High perspiration rate, frequent intake"),
    COLD("Cold / Winter", "❄️", 8, 0.95f, 1.0f, "Dry indoor air, maintain regular sips");

    companion object {
        fun fromName(name: String): ClimateProfile {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: MILD
        }
    }
}

enum class ActivityLevel(
    val title: String,
    val emoji: String,
    val minSteps: Int,
    val waterBoostMl: Int,
    val description: String
) {
    SEDENTARY("Resting / Light", "🪑", 0, 0, "Normal daily activities"),
    MODERATE("Moderate Movement", "🚶", 4500, 300, "+300ml water bonus for active steps"),
    ACTIVE("High Activity", "🏃", 8500, 600, "+600ml water bonus & extra reminder intervals"),
    INTENSE("Intense Training", "🔥", 12000, 950, "+950ml bonus for heavy workout / exertion");

    companion object {
        fun fromSteps(steps: Int): ActivityLevel {
            return when {
                steps >= 12000 -> INTENSE
                steps >= 8500 -> ACTIVE
                steps >= 4500 -> MODERATE
                else -> SEDENTARY
            }
        }
    }
}

data class UserSettings(
    val dailyGoalMl: Int = 2500,
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val lockScreenProgressEnabled: Boolean = true,
    val smartClimateAdaptiveEnabled: Boolean = true,
    val smartSensorActivityEnabled: Boolean = true,
    val climateProfile: ClimateProfile = ClimateProfile.WARM,
    val reminderFrequencyPerDay: Int = 4, // at least 4 times a day
    val reminderStartHour: Int = 8,       // 08:00 AM
    val reminderEndHour: Int = 22,        // 10:00 PM
    val soundVibrationEnabled: Boolean = true,
    val userWeightKg: Float = 70.0f,
    val isAutoGoalCalculated: Boolean = true,
    val lastSyncTimestamp: Long = 0L,
    val userEmail: String = ""
) {
    /**
     * Calculates the dynamically recommended water intake goal (in ml)
     * combining user baseline + climate multiplier + live step sensor activity.
     */
    fun calculateAdaptiveGoal(detectedStepsToday: Int): Int {
        val baseGoal = if (isAutoGoalCalculated) {
            (userWeightKg * 35f).toInt().coerceIn(1500, 4500)
        } else {
            dailyGoalMl
        }

        val climateBonus = if (smartClimateAdaptiveEnabled) {
            (baseGoal * (climateProfile.waterMultiplier - 1.0f)).toInt()
        } else 0

        val activityBonus = if (smartSensorActivityEnabled) {
            ActivityLevel.fromSteps(detectedStepsToday).waterBoostMl
        } else 0

        return baseGoal + climateBonus + activityBonus
    }

    /**
     * Calculates the dynamically adapted reminder frequency per day
     * (Always at least 4 times a day, up to 8 times during hot climate / heavy workout).
     */
    fun calculateAdaptiveReminderCount(detectedStepsToday: Int): Int {
        val baseFreq = reminderFrequencyPerDay.coerceAtLeast(4)
        var adapted = baseFreq.toFloat()

        if (smartClimateAdaptiveEnabled) {
            adapted *= climateProfile.frequencyMultiplier
        }
        if (smartSensorActivityEnabled && detectedStepsToday > 7500) {
            adapted += 1.5f
        }

        return adapted.toInt().coerceIn(4, 10)
    }
}
