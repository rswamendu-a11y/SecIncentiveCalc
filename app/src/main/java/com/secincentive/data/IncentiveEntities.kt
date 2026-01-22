package com.secincentive.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "smartphone_slabs")
data class SmartphoneSlab(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val minPrice: Double,
    val maxPrice: Double,
    val incentiveAmount: Double
)

@Entity(tableName = "global_config")
data class GlobalConfig(
    @PrimaryKey val id: Int = 1,
    val fmSeriesPenaltyPercent: Double = 0.5,
    val targetGateThreshold: Double = 80.0,
    val cat1Cap: Double,
    val tabletCap: Double,
    val pcCap: Double,
    val bundleCap: Double,
    val careGateMinUnits: Int,
    val careVolumeKickerThreshold: Int,
    val careVolumeKickerMultiplier: Double,
    val protectMaxMultiplier: Double
)

@Entity(tableName = "wearable_rules")
data class WearableRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val modelName: String,
    val incentiveAmount: Double
)

@Entity(tableName = "tablet_rules")
data class TabletRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val isFocusModel: Boolean,
    val identifier: String,
    val incentiveAmount: Double,
    val minPrice: Double = 0.0
)

@Entity(tableName = "pc_tiers")
data class PcTier(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tierName: String,
    val incentiveAmount: Double
)

@Entity(tableName = "care_plus_slabs")
data class CarePlusSlab(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val minDevicePrice: Double,
    val maxDevicePrice: Double,
    val baseIncentive: Double
)

@Entity(tableName = "bundle_rules")
data class BundleRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val comboName: String,
    val isExclusiveChannel: Boolean,
    val incentiveAmount: Double
)

@Entity(tableName = "accessory_brackets")
data class AccessoryBracket(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val minAttachPercent: Double,
    val incentiveAmount: Double
)

@Entity(tableName = "additional_boosters")
data class AdditionalBooster(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ruleName: String,
    val conditionType: String,
    val thresholdValue: Double,
    val extraAmount: Double,
    val applyToCategory: String
)
