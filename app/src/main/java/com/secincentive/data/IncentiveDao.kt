package com.secincentive.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface IncentiveDao {

    // GlobalConfig
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGlobalConfig(config: GlobalConfig)

    @Query("SELECT * FROM global_config WHERE id = 1")
    suspend fun getGlobalConfig(): GlobalConfig?

    // SmartphoneSlab
    @Query("SELECT * FROM smartphone_slabs ORDER BY minPrice ASC")
    suspend fun getAllSmartphoneSlabs(): List<SmartphoneSlab>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmartphoneSlab(slab: SmartphoneSlab)

    @Update
    suspend fun updateSmartphoneSlab(slab: SmartphoneSlab)

    @Delete
    suspend fun deleteSmartphoneSlab(slab: SmartphoneSlab)

    // WearableRule
    @Query("SELECT * FROM wearable_rules")
    suspend fun getAllWearableRules(): List<WearableRule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWearableRule(rule: WearableRule)

    @Update
    suspend fun updateWearableRule(rule: WearableRule)

    @Delete
    suspend fun deleteWearableRule(rule: WearableRule)

    // TabletRule
    @Query("SELECT * FROM tablet_rules")
    suspend fun getAllTabletRules(): List<TabletRule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTabletRule(rule: TabletRule)

    @Update
    suspend fun updateTabletRule(rule: TabletRule)

    @Delete
    suspend fun deleteTabletRule(rule: TabletRule)

    // PcTier
    @Query("SELECT * FROM pc_tiers")
    suspend fun getAllPcTiers(): List<PcTier>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPcTier(tier: PcTier)

    @Update
    suspend fun updatePcTier(tier: PcTier)

    @Delete
    suspend fun deletePcTier(tier: PcTier)

    // CarePlusSlab
    @Query("SELECT * FROM care_plus_slabs ORDER BY minDevicePrice ASC")
    suspend fun getAllCarePlusSlabs(): List<CarePlusSlab>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCarePlusSlab(slab: CarePlusSlab)

    @Update
    suspend fun updateCarePlusSlab(slab: CarePlusSlab)

    @Delete
    suspend fun deleteCarePlusSlab(slab: CarePlusSlab)

    // BundleRule
    @Query("SELECT * FROM bundle_rules")
    suspend fun getAllBundleRules(): List<BundleRule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBundleRule(rule: BundleRule)

    @Update
    suspend fun updateBundleRule(rule: BundleRule)

    @Delete
    suspend fun deleteBundleRule(rule: BundleRule)

    // AccessoryBracket
    @Query("SELECT * FROM accessory_brackets ORDER BY minAttachPercent ASC")
    suspend fun getAllAccessoryBrackets(): List<AccessoryBracket>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccessoryBracket(bracket: AccessoryBracket)

    @Update
    suspend fun updateAccessoryBracket(bracket: AccessoryBracket)

    @Delete
    suspend fun deleteAccessoryBracket(bracket: AccessoryBracket)

    // AdditionalBooster
    @Query("SELECT * FROM additional_boosters")
    suspend fun getAllAdditionalBoosters(): List<AdditionalBooster>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdditionalBooster(booster: AdditionalBooster)

    @Update
    suspend fun updateAdditionalBooster(booster: AdditionalBooster)

    @Delete
    suspend fun deleteAdditionalBooster(booster: AdditionalBooster)
}
