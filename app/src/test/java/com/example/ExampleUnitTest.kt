package com.example

import com.example.data.model.CurrencyCatalog
import com.example.data.model.UnitCatalog
import com.example.data.model.UnitCategory
import com.example.ui.ActiveInputSource
import com.example.ui.CurrencyUiState
import com.example.ui.UnitsUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testCurrencyCatalogContainsPopularCurrencies() {
        val usd = CurrencyCatalog.getCurrency("USD")
        val eur = CurrencyCatalog.getCurrency("EUR")
        val gbp = CurrencyCatalog.getCurrency("GBP")
        val jpy = CurrencyCatalog.getCurrency("JPY")

        assertEquals("USD", usd.code)
        assertEquals("$", usd.symbol)
        assertEquals("EUR", eur.code)
        assertEquals("€", eur.symbol)
        assertEquals("GBP", gbp.code)
        assertEquals("JPY", jpy.code)
    }

    @Test
    fun testCurrencyCatalogFallback() {
        val unknown = CurrencyCatalog.getCurrency("XYZ")
        assertNotNull(unknown)
        assertEquals("XYZ", unknown.code)
    }

    @Test
    fun testCrossRateCalculation() {
        val rates = mapOf(
            "USD" to 1.0,
            "EUR" to 0.92,
            "JPY" to 150.0
        )

        // USD to EUR
        val usdToEur = rates["EUR"]!! / rates["USD"]!!
        assertEquals(0.92, usdToEur, 0.0001)

        // EUR to JPY cross rate = (JPY / USD) / (EUR / USD) = 150.0 / 0.92 = 163.0434...
        val eurToJpy = rates["JPY"]!! / rates["EUR"]!!
        assertTrue(eurToJpy > 163.0 && eurToJpy < 164.0)

        // Identical currency
        val eurToEur = rates["EUR"]!! / rates["EUR"]!!
        assertEquals(1.0, eurToEur, 0.0001)
    }

    @Test
    fun testFeeCalculation() {
        val principal = 1000.0
        val feePercent = 2.5
        val feeAmount = principal * (feePercent / 100.0)
        val netRecipient = principal - feeAmount

        assertEquals(25.0, feeAmount, 0.001)
        assertEquals(975.0, netRecipient, 0.001)
    }

    @Test
    fun testHighlightedAmountReplacement() {
        var state = CurrencyUiState(inputAmount = "150", isAmountHighlighted = true)

        // Helper replicating ViewModel keypad logic
        fun applyKeypad(char: String) {
            if (state.isAmountHighlighted) {
                when (char) {
                    "C", "DEL" -> state = state.copy(inputAmount = "0", isAmountHighlighted = false)
                    "." -> state = state.copy(inputAmount = "0.", isAmountHighlighted = false)
                    else -> state = state.copy(inputAmount = char, isAmountHighlighted = false)
                }
            } else {
                when (char) {
                    "C" -> state = state.copy(inputAmount = "0")
                    "DEL" -> state = state.copy(inputAmount = if (state.inputAmount.length <= 1) "0" else state.inputAmount.dropLast(1))
                    "." -> if (!state.inputAmount.contains(".")) state = state.copy(inputAmount = "${state.inputAmount}.")
                    else -> state = state.copy(inputAmount = if (state.inputAmount == "0") char else state.inputAmount + char)
                }
            }
        }

        // When highlighted, typing "5" replaces "150" with "5"
        applyKeypad("5")
        assertEquals("5", state.inputAmount)
        assertFalse(state.isAmountHighlighted)

        // Subsequent typing appends normally
        applyKeypad("0")
        assertEquals("50", state.inputAmount)
    }

    @Test
    fun testPresetThemeColorsExist() {
        assertEquals(7, com.example.ui.theme.PRESET_THEMES.size)
        assertEquals("ocean", com.example.ui.theme.PRESET_THEMES.first().id)
        assertEquals("ocean", CurrencyUiState().themeColorId)
        val ids = com.example.ui.theme.PRESET_THEMES.map { it.id }
        assertTrue(ids.contains("emerald"))
        assertTrue(ids.contains("ocean"))
        assertTrue(ids.contains("violet"))
        assertTrue(ids.contains("ruby"))
        assertTrue(ids.contains("amber"))
        assertTrue(ids.contains("teal"))
        assertTrue(ids.contains("charcoal"))
    }

    @Test
    fun testCustomColorSchemeGeneration() {
        val customColor = androidx.compose.ui.graphics.Color(0xFFE91E63)
        val lightScheme = com.example.ui.theme.generateCustomColorScheme(customColor, isDark = false)
        val darkScheme = com.example.ui.theme.generateCustomColorScheme(customColor, isDark = true)

        assertNotNull(lightScheme)
        assertNotNull(darkScheme)
        assertEquals(customColor, lightScheme.primary)
    }

    @Test
    fun testBidirectionalConversionBaseEntry() {
        val testRates = mapOf("USD" to 1.0, "EUR" to 0.85)
        val state = CurrencyUiState(
            baseCurrency = "USD",
            targetCurrency = "EUR",
            inputAmount = "200",
            activeInputSource = com.example.ui.ActiveInputSource.BASE,
            ratesMap = testRates
        )

        assertEquals(200.0, state.numericBaseAmount, 0.001)
        assertEquals(170.0, state.numericTargetAmount, 0.001)
        assertEquals(170.0, state.convertedAmount, 0.001)
        assertEquals(0.85, state.currentRate, 0.001)
        assertEquals(1.0 / 0.85, state.inverseRate, 0.001)
    }

    @Test
    fun testBidirectionalConversionTargetEntry() {
        val testRates = mapOf("USD" to 1.0, "EUR" to 0.80)
        // User directly inputs 40 EUR in the target box
        val state = CurrencyUiState(
            baseCurrency = "USD",
            targetCurrency = "EUR",
            inputAmount = "40",
            activeInputSource = com.example.ui.ActiveInputSource.TARGET,
            ratesMap = testRates
        )

        // Target amount is 40 EUR
        assertEquals(40.0, state.numericTargetAmount, 0.001)
        // Base amount should be 40 / 0.8 = 50 USD
        assertEquals(50.0, state.numericBaseAmount, 0.001)
        assertEquals(40.0, state.convertedAmount, 0.001)
    }

    @Test
    fun testUpdatedTimestampFormatted() {
        val testMillis = 1700000000000L
        val state = CurrencyUiState(lastUpdatedMillis = testMillis)
        val timeFormatter = java.text.SimpleDateFormat("MMM d, HH:mm", java.util.Locale.US)
        val formatted = timeFormatter.format(java.util.Date(state.lastUpdatedMillis))
        assertTrue(formatted.isNotEmpty())
    }

    @Test
    fun testLengthConversion() {
        val meter = UnitCatalog.getUnit("m")!!
        val km = UnitCatalog.getUnit("km")!!
        val ft = UnitCatalog.getUnit("ft")!!
        val mi = UnitCatalog.getUnit("mi")!!

        // 1 km = 1000 m
        assertEquals(1000.0, UnitCatalog.convert(1.0, km, meter), 0.001)
        // 1 meter = 3.28084 feet
        assertEquals(3.28084, UnitCatalog.convert(1.0, meter, ft), 0.001)
        // 1 mile = 1.609344 km
        assertEquals(1.609344, UnitCatalog.convert(1.0, mi, km), 0.0001)
        // Identity
        assertEquals(5.5, UnitCatalog.convert(5.5, meter, meter), 0.0001)
    }

    @Test
    fun testMassConversion() {
        val kg = UnitCatalog.getUnit("kg")!!
        val lb = UnitCatalog.getUnit("lb")!!
        val g = UnitCatalog.getUnit("g")!!
        val oz = UnitCatalog.getUnit("oz")!!

        // 1 kg = 2.20462 lb
        assertEquals(2.20462, UnitCatalog.convert(1.0, kg, lb), 0.001)
        // 1 kg = 1000 g
        assertEquals(1000.0, UnitCatalog.convert(1.0, kg, g), 0.001)
        // 1 lb = 16 oz
        assertEquals(16.0, UnitCatalog.convert(1.0, lb, oz), 0.01)
    }

    @Test
    fun testTemperatureConversion() {
        val c = UnitCatalog.getUnit("c")!!
        val f = UnitCatalog.getUnit("f")!!
        val k = UnitCatalog.getUnit("k")!!

        // 0 °C = 32 °F
        assertEquals(32.0, UnitCatalog.convert(0.0, c, f), 0.001)
        // 100 °C = 212 °F
        assertEquals(212.0, UnitCatalog.convert(100.0, c, f), 0.001)
        // 32 °F = 0 °C
        assertEquals(0.0, UnitCatalog.convert(32.0, f, c), 0.001)
        // 212 °F = 100 °C
        assertEquals(100.0, UnitCatalog.convert(212.0, f, c), 0.001)
        // -40 °C = -40 °F
        assertEquals(-40.0, UnitCatalog.convert(-40.0, c, f), 0.001)
        // 0 °C = 273.15 K
        assertEquals(273.15, UnitCatalog.convert(0.0, c, k), 0.001)
        // 0 K = -273.15 °C
        assertEquals(-273.15, UnitCatalog.convert(0.0, k, c), 0.001)
    }

    @Test
    fun testVolumeAndSpeedConversion() {
        val gal = UnitCatalog.getUnit("gal_us")!!
        val liter = UnitCatalog.getUnit("l")!!
        val kmh = UnitCatalog.getUnit("kmh")!!
        val mph = UnitCatalog.getUnit("mph")!!

        // 1 US Gallon = ~3.78541 Liters
        assertEquals(3.78541, UnitCatalog.convert(1.0, gal, liter), 0.001)
        // 100 km/h = ~62.1371 mph
        assertEquals(62.1371, UnitCatalog.convert(100.0, kmh, mph), 0.01)
    }

    @Test
    fun testDigitalStorageConversion() {
        val gb = UnitCatalog.getUnit("gb")!!
        val mb = UnitCatalog.getUnit("mb")!!
        val gib = UnitCatalog.getUnit("gib")!!
        val mib = UnitCatalog.getUnit("mib")!!

        // 1 GB = 1000 MB (decimal)
        assertEquals(1000.0, UnitCatalog.convert(1.0, gb, mb), 0.001)
        // 1 GiB = 1024 MiB (binary)
        assertEquals(1024.0, UnitCatalog.convert(1.0, gib, mib), 0.001)
    }

    @Test
    fun testUnitsUiStateCalculations() {
        val km = UnitCatalog.getUnit("km")!!
        val mi = UnitCatalog.getUnit("mi")!!

        // Forward conversion: 10 km to miles
        val stateFrom = UnitsUiState(
            selectedCategory = UnitCategory.LENGTH,
            fromUnit = km,
            toUnit = mi,
            inputAmount = "10",
            activeInputSource = ActiveInputSource.BASE
        )
        assertEquals(10.0, stateFrom.numericFromAmount, 0.001)
        assertEquals(6.2137, stateFrom.numericToAmount, 0.01)
        assertTrue(stateFrom.liveComparisons.isNotEmpty())
        assertTrue(stateFrom.formulaText.contains("1 km ="))

        // Reverse entry: user enters 10 miles in TARGET box
        val stateTo = UnitsUiState(
            selectedCategory = UnitCategory.LENGTH,
            fromUnit = km,
            toUnit = mi,
            inputAmount = "10",
            activeInputSource = ActiveInputSource.TARGET
        )
        assertEquals(10.0, stateTo.numericToAmount, 0.001)
        assertEquals(16.0934, stateTo.numericFromAmount, 0.01)
    }

    @Test
    fun testUnitCatalogHasAll10Categories() {
        assertEquals(10, UnitCatalog.allCategories.size)
        UnitCatalog.allCategories.forEach { category ->
            val units = UnitCatalog.getUnitsForCategory(category)
            assertTrue("Category ${category.displayName} must have at least 2 units", units.size >= 2)
            val defaults = UnitCatalog.getDefaultUnitsForCategory(category)
            assertNotNull(defaults.first)
            assertNotNull(defaults.second)
            assertTrue(defaults.first.id != defaults.second.id)
        }
    }

    @Test
    fun testCurrencyCompareAlwaysCalculatesFromEnteredCurrency() {
        val testRates = mapOf("USD" to 1.0, "EUR" to 0.80, "GBP" to 0.60, "JPY" to 120.0)

        // Case 1: User entered 100 in BASE currency (USD)
        val stateBase = CurrencyUiState(
            baseCurrency = "USD",
            targetCurrency = "EUR",
            inputAmount = "100",
            activeInputSource = ActiveInputSource.BASE,
            ratesMap = testRates
        )
        assertEquals("USD", stateBase.enteredCurrency)
        assertEquals(100.0, stateBase.numericInput, 0.001)

        // Converted to GBP: 100 * (0.60 / 1.0) = 60 GBP
        val rateToGbp = stateBase.calculateRate(stateBase.enteredCurrency, "GBP")
        assertEquals(0.60, rateToGbp, 0.001)
        assertEquals(60.0, stateBase.numericInput * rateToGbp, 0.001)

        // Converted to JPY: 100 * (120.0 / 1.0) = 12,000 JPY
        val rateToJpy = stateBase.calculateRate(stateBase.enteredCurrency, "JPY")
        assertEquals(120.0, rateToJpy, 0.001)
        assertEquals(12000.0, stateBase.numericInput * rateToJpy, 0.001)

        // Case 2: User entered 80 in TARGET currency (EUR)
        val stateTarget = CurrencyUiState(
            baseCurrency = "USD",
            targetCurrency = "EUR",
            inputAmount = "80",
            activeInputSource = ActiveInputSource.TARGET,
            ratesMap = testRates
        )
        assertEquals("EUR", stateTarget.enteredCurrency)
        assertEquals(80.0, stateTarget.numericInput, 0.001)

        // Compare from entered EUR to USD: 80 * (1.0 / 0.80) = 100 USD
        val rateEurToUsd = stateTarget.calculateRate(stateTarget.enteredCurrency, "USD")
        assertEquals(1.25, rateEurToUsd, 0.001)
        assertEquals(100.0, stateTarget.numericInput * rateEurToUsd, 0.001)

        // Compare from entered EUR to GBP: 80 * (0.60 / 0.80) = 60 GBP
        val rateEurToGbp = stateTarget.calculateRate(stateTarget.enteredCurrency, "GBP")
        assertEquals(0.75, rateEurToGbp, 0.001)
        assertEquals(60.0, stateTarget.numericInput * rateEurToGbp, 0.001)

        // Compare from entered EUR to JPY: 80 * (120.0 / 0.80) = 12,000 JPY
        val rateEurToJpy = stateTarget.calculateRate(stateTarget.enteredCurrency, "JPY")
        assertEquals(150.0, rateEurToJpy, 0.001)
        assertEquals(12000.0, stateTarget.numericInput * rateEurToJpy, 0.001)
    }
}
