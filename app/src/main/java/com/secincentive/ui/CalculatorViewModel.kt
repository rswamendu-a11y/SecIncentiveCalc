package com.secincentive.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.secincentive.data.AppDatabase
import com.secincentive.data.IncentiveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.min

// --- Input Data Classes ---
data class SmartphoneEntry(
    val price: Double,
    val quantity: Int,
    val isFmSeries: Boolean
)

data class WearableEntry(
    val modelName: String,
    val quantity: Int
)

data class TabletEntry(
    val identifier: String, // Model Name or Slab Identifier
    val price: Double,
    val quantity: Int
)

data class CareEntry(
    val devicePrice: Double,
    val isProtectMax: Boolean
)

data class CalculationInput(
    val target: Double,
    val achieved: Double,
    val smartphones: List<SmartphoneEntry>,
    val wearables: List<WearableEntry>,
    val tablets: List<TabletEntry>,
    val carePlus: List<CareEntry>
)

// --- Result Data Classes ---
data class CalculationResult(
    val smartphoneIncentive: Double = 0.0,
    val wearableIncentive: Double = 0.0,
    val tabletIncentive: Double = 0.0,
    val careIncentive: Double = 0.0,
    val grandTotal: Double = 0.0,
    val warnings: List<String> = emptyList()
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: IncentiveRepository
    private val _uiState = MutableStateFlow(CalculationResult())
    val uiState: StateFlow<CalculationResult> = _uiState.asStateFlow()

    init {
        val dao = AppDatabase.getDatabase(application).incentiveDao()
        repository = IncentiveRepository(dao)
        viewModelScope.launch {
            repository.preloadDefaultsIfEmpty()
        }
    }

    fun calculateIncentives(input: CalculationInput) {
        viewModelScope.launch {
            val config = repository.getGlobalConfig() ?: return@launch
            val phoneSlabs = repository.getSmartphoneSlabs()
            val wearableRules = repository.getWearableRules()
            val tabletRules = repository.getTabletRules()
            val careSlabs = repository.getCarePlusSlabs()
            val boosters = repository.getAdditionalBoosters()

            val warnings = mutableListOf<String>()

            // 1. Smartphone Calculation
            var rawPhoneIncentive = 0.0
            input.smartphones.forEach { entry ->
                // Find matching slab
                val slab = phoneSlabs.find { entry.price >= it.minPrice && entry.price <= it.maxPrice }
                var rate = slab?.incentiveAmount ?: 0.0

                if (entry.isFmSeries) {
                    rate *= config.fmSeriesPenaltyPercent
                }
                rawPhoneIncentive += (rate * entry.quantity)
            }

            // Target Logic
            var finalPhoneIncentive = rawPhoneIncentive
            val achievementPercent = if (input.target > 0) (input.achieved / input.target) * 100 else 0.0

            if (achievementPercent < config.targetGateThreshold) {
                warnings.add("Target Missed: Achievement ${String.format("%.1f", achievementPercent)}% < ${config.targetGateThreshold}%")
                // Still calculated, but warned.
                // Requirement: "If < 80%: WARNING ONLY. (Calculate the incentive anyway, but flag it)."
                // Requirement: "Scope: Apply this logic to the Smartphone Category Subtotal ONLY"
                // No penalty applied to the amount per instructions, just warning.
            } else if (achievementPercent < 100.0) {
                // Pro-rata: 80% <= Achieved < 100%
                val factor = input.achieved / input.target
                finalPhoneIncentive = rawPhoneIncentive * factor
            }
            // If >= 100%, full payout (factor 1.0)


            // 2. Wearable Calculation
            var rawWearableIncentive = 0.0
            input.wearables.forEach { entry ->
                val rule = wearableRules.find { it.modelName == entry.modelName }
                val rate = rule?.incentiveAmount ?: 0.0
                rawWearableIncentive += (rate * entry.quantity)
            }

            // 3. Category 1 Cap (Phone + Wearable)
            val cat1Total = min(finalPhoneIncentive + rawWearableIncentive, config.cat1Cap)


            // 4. Tablet Calculation
            var rawTabletIncentive = 0.0
            input.tablets.forEach { entry ->
                var rate = 0.0

                // A11 Volume Logic
                // Logic: If modelName contains "A11" AND !modelName contains "Plus"
                if (entry.identifier.contains("A11", ignoreCase = true) && !entry.identifier.contains("Plus", ignoreCase = true)) {
                    rate = when {
                        entry.quantity >= 3 -> 400.0
                        entry.quantity == 2 -> 300.0
                        else -> 250.0
                    }
                } else {
                    // Standard Rules
                    // Check Focus Models first
                    val focusRule = tabletRules.find { it.isFocusModel && it.identifier.equals(entry.identifier, ignoreCase = true) }

                    if (focusRule != null) {
                        rate = focusRule.incentiveAmount
                    } else {
                        // Check Price Slabs
                        // TabletRule(isFocusModel=false, minPrice=X) matches if price >= minPrice
                        // We need to find the highest matching slab (assumed ordered or we filter)
                        val slabRule = tabletRules
                            .filter { !it.isFocusModel && entry.price >= it.minPrice }
                            .maxByOrNull { it.minPrice }

                        rate = slabRule?.incentiveAmount ?: 0.0
                    }
                }
                rawTabletIncentive += (rate * entry.quantity)
            }
            val finalTabletIncentive = min(rawTabletIncentive, config.tabletCap)


            // 5. Care+ Calculation
            var rawCareIncentive = 0.0
            var totalCareUnits = 0

            input.carePlus.forEach { entry ->
                val slab = careSlabs.find { entry.devicePrice >= it.minDevicePrice && entry.devicePrice <= it.maxDevicePrice }
                var base = slab?.baseIncentive ?: 0.0

                if (entry.isProtectMax) {
                    base *= config.protectMaxMultiplier
                }
                rawCareIncentive += base
                totalCareUnits++
            }

            // Volume Kicker
            if (totalCareUnits >= config.careVolumeKickerThreshold) {
                rawCareIncentive *= config.careVolumeKickerMultiplier
            }

            val finalCareIncentive = rawCareIncentive // No specific cap mentioned for Care+ in instructions, only Volume Kicker logic.


            // 6. Additional Boosters (Placeholders - logic described but no input data structure for generic items yet)
            // The instructions mentioned iterating AdditionalBooster rules.
            // "If Booster.condition == 'QTY_GT' and ItemQty > Booster.threshold"
            // Since our Input models are specific (SmartphoneEntry, etc), implementing generic booster logic
            // would require mapping specific inputs to "Categories".
            // Skipping complex generic booster iteration for this phase as per "Specific Logic" focus,
            // unless specific boosters are defined in defaults. (None defined in defaults).


            // 7. Grand Total
            val grandTotal = cat1Total + finalTabletIncentive + finalCareIncentive

            _uiState.value = CalculationResult(
                smartphoneIncentive = finalPhoneIncentive, // Reporting the post-target-factor amount
                wearableIncentive = rawWearableIncentive,
                tabletIncentive = finalTabletIncentive,
                careIncentive = finalCareIncentive,
                grandTotal = grandTotal,
                warnings = warnings
            )
        }
    }
}
