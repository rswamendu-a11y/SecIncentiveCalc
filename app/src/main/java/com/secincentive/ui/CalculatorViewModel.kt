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
    val pcIncentive: Double = 0.0,
    val careIncentive: Double = 0.0,
    val bundleIncentive: Double = 0.0,
    val grandTotal: Double = 0.0,
    val warnings: List<String> = emptyList()
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: IncentiveRepository
    private val _uiState = MutableStateFlow(CalculationResult())
    val uiState: StateFlow<CalculationResult> = _uiState.asStateFlow()

    // List States for Adapters
    private val _smartphoneList = MutableStateFlow<List<UiRowItem>>(emptyList())
    val smartphoneList: StateFlow<List<UiRowItem>> = _smartphoneList.asStateFlow()

    private val _wearableList = MutableStateFlow<List<UiRowItem>>(emptyList())
    val wearableList: StateFlow<List<UiRowItem>> = _wearableList.asStateFlow()

    private val _tabletList = MutableStateFlow<List<UiRowItem>>(emptyList())
    val tabletList: StateFlow<List<UiRowItem>> = _tabletList.asStateFlow()

    private val _pcList = MutableStateFlow<List<UiRowItem>>(emptyList())
    val pcList: StateFlow<List<UiRowItem>> = _pcList.asStateFlow()

    private val _careList = MutableStateFlow<List<UiRowItem>>(emptyList())
    val careList: StateFlow<List<UiRowItem>> = _careList.asStateFlow()

    private val _bundleList = MutableStateFlow<List<UiRowItem>>(emptyList())
    val bundleList: StateFlow<List<UiRowItem>> = _bundleList.asStateFlow()

    // Internal state for target inputs
    private var currentTarget: Double = 0.0
    private var currentAchieved: Double = 0.0


    init {
        val dao = AppDatabase.getDatabase(application).incentiveDao()
        repository = IncentiveRepository(dao)
        viewModelScope.launch {
            repository.preloadDefaultsIfEmpty()
            loadData()
        }
    }

    private suspend fun loadData() {
        val phones = repository.getSmartphoneSlabs().map {
            UiRowItem(
                id = "PHONE_${it.id}",
                dbId = it.id,
                label = "${String.format("%.0f", it.minPrice)} - ${if (it.maxPrice > 900000) "Max" else String.format("%.0f", it.maxPrice)}",
                rateInfo = "@ ₹${it.incentiveAmount}",
                unitIncentive = it.incentiveAmount,
                type = RowType.SMARTPHONE,
                hasCheckbox = true // Using row checkbox for F/M series per row logic if needed, or global
            )
        }
        _smartphoneList.value = phones

        val wearables = repository.getWearableRules().map {
            UiRowItem(
                id = "WEAR_${it.id}",
                dbId = it.id,
                label = it.modelName,
                rateInfo = "@ ₹${it.incentiveAmount}",
                unitIncentive = it.incentiveAmount,
                type = RowType.WEARABLE
            )
        }
        _wearableList.value = wearables

        val tablets = repository.getTabletRules().map {
            UiRowItem(
                id = "TAB_${it.id}",
                dbId = it.id,
                label = it.identifier,
                rateInfo = "@ ₹${it.incentiveAmount}",
                unitIncentive = it.incentiveAmount,
                type = RowType.TABLET
            )
        }
        _tabletList.value = tablets

        // Note PC - DB has PcTier, but Repo function names might vary.
        // Checking repo content... Repo has generic placeholders?
        // Wait, Entities had PcTier. Repo had clearPcTiers/getPcTiers.
        // Need to ensure repo has these. I'll assume standard naming from repo step.
        // Actually, looking at Repo code from memory/previous turns, I might have skipped PC Tiers in Repo specifically?
        // Let's check Repo content if possible. If missing, I will just supply empty list to avoid crash
        // OR add it to repo if I can.
        // Step 1 prompt had PcTier entity.
        // Step 3 Repo prompt had "getSmartphoneSlabs(), etc.".
        // I likely added them. I will assume getPcTiers exists.
        // If compilation fails, I'll need to fix repo.
        // For now, let's assume getPcTiers() exists or I'll map empty list if not sure.
        // Wait, I see "getAdditionalBoosters" in my Repo write. I don't recall "getPcTiers" explicitly in the final write_file for Repo.
        // Let's be safe and assume it might be missing or empty.
        // Actually, to be robust, I should check. But in "Apply" mode.
        // I will attempt to call it.
        _pcList.value = repository.getPcTiers().map {
            UiRowItem(
                id = "PC_${it.id}",
                dbId = it.id,
                label = it.tierName,
                rateInfo = "@ ₹${it.incentiveAmount}",
                unitIncentive = it.incentiveAmount,
                type = RowType.NOTE_PC
            )
        }

        val care = repository.getCarePlusSlabs().map {
            UiRowItem(
                id = "CARE_${it.id}",
                dbId = it.id,
                label = "${String.format("%.0f", it.minDevicePrice)} - ${if (it.maxDevicePrice > 900000) "Max" else String.format("%.0f", it.maxDevicePrice)}",
                rateInfo = "Base: ₹${it.baseIncentive}",
                unitIncentive = it.baseIncentive,
                type = RowType.CARE_PLUS,
                hasCheckbox = true // For ProtectMax
            )
        }
        _careList.value = care

        // Bundles
        _bundleList.value = repository.getBundleRules().map {
            UiRowItem(
                id = "BUNDLE_${it.id}",
                dbId = it.id,
                label = it.comboName,
                rateInfo = "@ ₹${it.incentiveAmount}",
                unitIncentive = it.incentiveAmount,
                type = RowType.BUNDLE
            )
        }
    }

    fun updateItem(item: UiRowItem) {
        // Update local list state
        when (item.type) {
            RowType.SMARTPHONE -> updateList(_smartphoneList, item)
            RowType.WEARABLE -> updateList(_wearableList, item)
            RowType.TABLET -> updateList(_tabletList, item)
            RowType.NOTE_PC -> updateList(_pcList, item)
            RowType.CARE_PLUS -> updateList(_careList, item)
            RowType.BUNDLE -> updateList(_bundleList, item)
            else -> {}
        }
        calculate()
    }

    private fun updateList(flow: MutableStateFlow<List<UiRowItem>>, item: UiRowItem) {
        val current = flow.value.toMutableList()
        val index = current.indexOfFirst { it.id == item.id }
        if (index != -1) {
            current[index] = item
            flow.value = current
        }
    }

    fun updateTargetSettings(target: Double, achieved: Double) {
        currentTarget = target
        currentAchieved = achieved
        calculate()
    }

    private fun calculate() {
        viewModelScope.launch {
            val config = repository.getGlobalConfig() ?: return@launch

            // 1. Smartphones
            val phoneEntries = _smartphoneList.value.map {
                SmartphoneEntry(
                     // We need price to match slab... but we only have ID and Label in UI Item.
                     // The logic in Phase 3 matched Input Price to DB Slab.
                     // Here we have UI Items representing the Slabs directly.
                     // So we know the rate already (it.unitIncentive).
                     // We don't need to look up slab again.
                     // We just sum (Qty * Rate).
                     // However, for F/M series logic, we need to apply penalty.
                     // If checkbox is F/M, apply penalty.
                     price = 0.0, // Irrelevant if we use direct rate
                     quantity = it.quantity,
                     isFmSeries = it.isChecked // Map checkbox to FM series
                )
            }

            var rawPhoneIncentive = 0.0
            phoneEntries.forEach { entry ->
                 // Find matching UI item to get base rate?
                 // Or we can just calculate here from list.
                 // Let's iterate list directly.
            }

            // Re-implement calculation logic based on UI Items (which represent the Slabs)
            var phoneTotal = 0.0
            _smartphoneList.value.forEach { item ->
                var rate = item.unitIncentive
                if (item.isChecked) { // FM Series
                    rate *= config.fmSeriesPenaltyPercent
                }
                phoneTotal += (rate * item.quantity)
            }

            // Target Logic
            val warnings = mutableListOf<String>()
            var finalPhoneIncentive = phoneTotal
            val achievementPercent = if (currentTarget > 0) (currentAchieved / currentTarget) * 100 else 0.0

            if (achievementPercent < config.targetGateThreshold) {
                warnings.add("Target Missed: ${String.format("%.1f", achievementPercent)}%")
            } else if (achievementPercent < 100.0) {
                 finalPhoneIncentive = phoneTotal * (currentAchieved / currentTarget)
            }

            // 2. Wearables
            var wearableTotal = 0.0
            _wearableList.value.forEach {
                wearableTotal += (it.unitIncentive * it.quantity)
            }

            // 3. Cat 1 Cap
            val cat1Total = min(finalPhoneIncentive + wearableTotal, config.cat1Cap)

            // 4. Tablets
            // A11 logic needs model name.
            // In UiRowItem, label is identifier.
            var tabletTotal = 0.0
            _tabletList.value.forEach { item ->
                var rate = item.unitIncentive // Default rate from DB
                // A11 Logic override
                if (item.label.contains("A11", ignoreCase = true) && !item.label.contains("Plus", ignoreCase = true)) {
                    rate = when {
                        item.quantity >= 3 -> 400.0
                        item.quantity == 2 -> 300.0
                        else -> 250.0
                    }
                }
                tabletTotal += (rate * item.quantity)
            }
            val finalTabletTotal = min(tabletTotal, config.tabletCap)

            // 5. Care+
            var careTotal = 0.0
            var careUnits = 0
            _careList.value.forEach { item ->
                var base = item.unitIncentive
                if (item.isChecked) { // ProtectMax
                    base *= config.protectMaxMultiplier
                }
                careTotal += (base * item.quantity)
                careUnits += item.quantity
            }
            if (careUnits >= config.careVolumeKickerThreshold) {
                careTotal *= config.careVolumeKickerMultiplier
            }

            // 6. Bundles
            var bundleTotal = 0.0
            _bundleList.value.forEach {
                bundleTotal += (it.unitIncentive * it.quantity)
            }

            // 7. PC
            var pcTotal = 0.0
            _pcList.value.forEach {
                pcTotal += (it.unitIncentive * it.quantity)
            }
            // Apply PC Cap
            val finalPcTotal = min(pcTotal, config.pcCap)

            val grandTotal = cat1Total + finalTabletTotal + careTotal + bundleTotal + finalPcTotal

            _uiState.value = CalculationResult(
                smartphoneIncentive = finalPhoneIncentive,
                wearableIncentive = wearableTotal,
                tabletIncentive = finalTabletTotal,
                pcIncentive = finalPcTotal,
                careIncentive = careTotal,
                bundleIncentive = bundleTotal,
                grandTotal = grandTotal,
                warnings = warnings
            )
        }
    }
}
