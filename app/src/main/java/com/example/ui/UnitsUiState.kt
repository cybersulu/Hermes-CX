package com.example.ui

import com.example.data.model.ConversionUnit
import com.example.data.model.UnitCatalog
import com.example.data.model.UnitCategory

data class UnitsUiState(
    val selectedCategory: UnitCategory = UnitCategory.LENGTH,
    val fromUnit: ConversionUnit = UnitCatalog.getDefaultUnitsForCategory(UnitCategory.LENGTH).first,
    val toUnit: ConversionUnit = UnitCatalog.getDefaultUnitsForCategory(UnitCategory.LENGTH).second,
    val inputAmount: String = "1",
    val activeInputSource: ActiveInputSource = ActiveInputSource.BASE,
    val isAmountHighlighted: Boolean = false,
    val decimalPlaces: Int = 4,
    val searchQuery: String = ""
) {
    val numericInput: Double
        get() = inputAmount.toDoubleOrNull() ?: 0.0

    val numericFromAmount: Double
        get() = if (activeInputSource == ActiveInputSource.BASE) {
            numericInput
        } else {
            UnitCatalog.convert(numericInput, toUnit, fromUnit)
        }

    val numericToAmount: Double
        get() = if (activeInputSource == ActiveInputSource.TARGET) {
            numericInput
        } else {
            UnitCatalog.convert(numericInput, fromUnit, toUnit)
        }

    val formulaText: String
        get() = UnitCatalog.getFormula(fromUnit, toUnit)

    val allCategoryUnits: List<ConversionUnit>
        get() = UnitCatalog.getUnitsForCategory(selectedCategory)

    val liveComparisons: List<Pair<ConversionUnit, Double>>
        get() {
            val baseVal = numericFromAmount
            return allCategoryUnits.map { unit ->
                Pair(unit, UnitCatalog.convert(baseVal, fromUnit, unit))
            }
        }
}
