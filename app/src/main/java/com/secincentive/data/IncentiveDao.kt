package com.secincentive.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface IncentiveDao {

    // --- Global Config ---
    @Query("SELECT * FROM global_config WHERE id = 1 LIMIT 1")
    suspend fun getGlobalConfig(): GlobalConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGlobalConfig(config: GlobalConfig)

    @Update
    suspend fun updateGlobalConfig(config: GlobalConfig)


    // --- Smartphone Slabs ---
    @Query("SELECT * FROM smartphone_slabs ORDER BY minPrice ASC")
    suspend fun getSmartphoneSlabs(): List<SmartphoneSlab>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmartphoneSlab(slab: SmartphoneSlab)

    @Update
    suspend fun updateSmartphoneSlab(slab: SmartphoneSlab)

    @Query("DELETE FROM smartphone_slabs")
    suspend fun clearSmartphoneSlabs()


    // --- Wearable Rules ---
    @Query("SELECT * FROM wearable_rules")
    suspend fun getWearableRules(): List<WearableRule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWearableRule(rule: WearableRule)

    @Update
    suspend fun updateWearableRule(rule: WearableRule)

    @Query("DELETE FROM wearable_rules")
    suspend fun clearWearableRules()


    // --- Tablet Rules ---
    @Query("SELECT * FROM tablet_rules")
    suspend fun getTabletRules(): List<TabletRule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTabletRule(rule: TabletRule)

    @Update
    suspend fun updateTabletRule(rule: TabletRule)

    @Query("DELETE FROM tablet_rules")
    suspend fun clearTabletRules()


    // --- PC Tiers ---
    @Query("SELECT * FROM pc_tiers")
    suspend fun getPcTiers(): List<PcTier>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPcTier(tier: PcTier)

    @Update
    suspend fun updatePcTier(tier: PcTier)

    @Query("DELETE FROM pc_tiers")
    suspend fun clearPcTiers()


    // --- Care Plus Slabs ---
    @Query("SELECT * FROM care_plus_slabs ORDER BY minDevicePrice ASC")
    suspend fun getCarePlusSlabs(): List<CarePlusSlab>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCarePlusSlab(slab: CarePlusSlab)

    @Update
    suspend fun updateCarePlusSlab(slab: CarePlusSlab)

    @Query("DELETE FROM care_plus_slabs")
    suspend fun clearCarePlusSlabs()


    // --- Bundle Rules ---
    @Query("SELECT * FROM bundle_rules")
    suspend fun getBundleRules(): List<BundleRule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBundleRule(rule: BundleRule)

    @Update
    suspend fun updateBundleRule(rule: BundleRule)

    @Query("DELETE FROM bundle_rules")
    suspend fun clearBundleRules()


    // --- Accessory Brackets ---
    @Query("SELECT * FROM accessory_brackets ORDER BY minAttachPercent ASC")
    suspend fun getAccessoryBrackets(): List<AccessoryBracket>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccessoryBracket(bracket: AccessoryBracket)

    @Update
    suspend fun updateAccessoryBracket(bracket: AccessoryBracket)

    @Query("DELETE FROM accessory_brackets")
    suspend fun clearAccessoryBrackets()


    // --- Additional Boosters ---
    @Query("SELECT * FROM additional_boosters")
    suspend fun getAdditionalBoosters(): List<AdditionalBooster>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdditionalBooster(booster: AdditionalBooster)

    @Update
    suspend fun updateAdditionalBooster(booster: AdditionalBooster)

    @Query("DELETE FROM additional_boosters")
    suspend fun clearAdditionalBoosters()
}
