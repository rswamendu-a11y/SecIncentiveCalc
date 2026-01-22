package com.secincentive.ui

enum class RowType {
    SMARTPHONE,
    WEARABLE,
    TABLET,
    NOTE_PC,
    CARE_PLUS,
    BUNDLE,
    ACCESSORY
}

data class UiRowItem(
    val id: String, // Combination of Type + DB ID
    val label: String,
    val rateInfo: String,
    val unitIncentive: Double = 0.0,
    var quantity: Int = 0,
    val hasCheckbox: Boolean = false,
    var isChecked: Boolean = false,
    val type: RowType,
    // Original IDs for mapping back to DB
    val dbId: Long
)
