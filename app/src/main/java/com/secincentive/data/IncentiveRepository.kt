package com.secincentive.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IncentiveRepository(private val incentiveDao: IncentiveDao) {

    // --- Global Config ---
    suspend fun getGlobalConfig(): GlobalConfig? = incentiveDao.getGlobalConfig()
    suspend fun updateGlobalConfig(config: GlobalConfig) = incentiveDao.updateGlobalConfig(config)

    // --- Smartphone Slabs ---
    suspend fun getSmartphoneSlabs(): List<SmartphoneSlab> = incentiveDao.getSmartphoneSlabs()
    suspend fun insertSmartphoneSlab(slab: SmartphoneSlab) = incentiveDao.insertSmartphoneSlab(slab)
    suspend fun clearSmartphoneSlabs() = incentiveDao.clearSmartphoneSlabs()

    // --- Wearable Rules ---
    suspend fun getWearableRules(): List<WearableRule> = incentiveDao.getWearableRules()
    suspend fun insertWearableRule(rule: WearableRule) = incentiveDao.insertWearableRule(rule)
    suspend fun clearWearableRules() = incentiveDao.clearWearableRules()

    // --- Tablet Rules ---
    suspend fun getTabletRules(): List<TabletRule> = incentiveDao.getTabletRules()
    suspend fun insertTabletRule(rule: TabletRule) = incentiveDao.insertTabletRule(rule)
    suspend fun clearTabletRules() = incentiveDao.clearTabletRules()

    // --- Care+ Slabs ---
    suspend fun getCarePlusSlabs(): List<CarePlusSlab> = incentiveDao.getCarePlusSlabs()
    suspend fun insertCarePlusSlab(slab: CarePlusSlab) = incentiveDao.insertCarePlusSlab(slab)
    suspend fun clearCarePlusSlabs() = incentiveDao.clearCarePlusSlabs()

    // --- Additional Boosters ---
    suspend fun getAdditionalBoosters(): List<AdditionalBooster> = incentiveDao.getAdditionalBoosters()


    suspend fun preloadDefaultsIfEmpty() {
        withContext(Dispatchers.IO) {
            // 1. Global Config
            if (incentiveDao.getGlobalConfig() == null) {
                incentiveDao.insertGlobalConfig(
                    GlobalConfig(
                        fmSeriesPenaltyPercent = 0.50,
                        targetGateThreshold = 80.0,
                        careGateMinUnits = 3,
                        careVolumeKickerThreshold = 8,
                        careVolumeKickerMultiplier = 1.20,
                        protectMaxMultiplier = 1.25,
                        cat1Cap = 75000.0,
                        tabletCap = 20000.0,
                        pcCap = 50000.0,
                        bundleCap = 10000.0
                    )
                )
            }

            // 2. Smartphone Slabs
            if (incentiveDao.getSmartphoneSlabs().isEmpty()) {
                val slabs = listOf(
                    SmartphoneSlab(minPrice = 100000.0, maxPrice = Double.MAX_VALUE, incentiveAmount = 700.0),
                    SmartphoneSlab(minPrice = 70000.0, maxPrice = 99999.0, incentiveAmount = 600.0),
                    SmartphoneSlab(minPrice = 40000.0, maxPrice = 69999.0, incentiveAmount = 500.0),
                    SmartphoneSlab(minPrice = 30000.0, maxPrice = 39999.0, incentiveAmount = 300.0),
                    SmartphoneSlab(minPrice = 20000.0, maxPrice = 29999.0, incentiveAmount = 200.0),
                    SmartphoneSlab(minPrice = 15000.0, maxPrice = 19999.0, incentiveAmount = 100.0),
                    SmartphoneSlab(minPrice = 10000.0, maxPrice = 14999.0, incentiveAmount = 75.0),
                    SmartphoneSlab(minPrice = 0.0, maxPrice = 9999.0, incentiveAmount = 0.0)
                )
                slabs.forEach { incentiveDao.insertSmartphoneSlab(it) }
            }

            // 3. Wearable Rules
            if (incentiveDao.getWearableRules().isEmpty()) {
                val rules = listOf(
                    WearableRule(modelName = "Watch Ultra / Watch 8 Classic", incentiveAmount = 1200.0),
                    WearableRule(modelName = "Watch 8 / Other", incentiveAmount = 500.0),
                    WearableRule(modelName = "Buds Pro", incentiveAmount = 600.0),
                    WearableRule(modelName = "Buds 3 / FE", incentiveAmount = 500.0),
                    WearableRule(modelName = "Buds Core", incentiveAmount = 200.0)
                )
                rules.forEach { incentiveDao.insertWearableRule(it) }
            }

            // 4. Tablet Rules
            if (incentiveDao.getTabletRules().isEmpty()) {
                val rules = listOf(
                    TabletRule(isFocusModel = true, identifier = "S11 Ultra", incentiveAmount = 1500.0),
                    TabletRule(isFocusModel = true, identifier = "S11 / S10+", incentiveAmount = 1000.0),
                    TabletRule(isFocusModel = false, identifier = "> 70k", minPrice = 70000.0, incentiveAmount = 800.0),
                    TabletRule(isFocusModel = false, identifier = "40k - 70k", minPrice = 40000.0, incentiveAmount = 600.0)
                )
                rules.forEach { incentiveDao.insertTabletRule(it) }
            }

            // 5. Care+ Slabs
            if (incentiveDao.getCarePlusSlabs().isEmpty()) {
                val slabs = listOf(
                    CarePlusSlab(minDevicePrice = 100000.0, maxDevicePrice = Double.MAX_VALUE, baseIncentive = 400.0),
                    CarePlusSlab(minDevicePrice = 70000.0, maxDevicePrice = 99999.0, baseIncentive = 350.0),
                    CarePlusSlab(minDevicePrice = 40000.0, maxDevicePrice = 69999.0, baseIncentive = 300.0)
                )
                slabs.forEach { incentiveDao.insertCarePlusSlab(it) }
            }
        }
    }
}
