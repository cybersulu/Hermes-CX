package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ConversionHistoryEntity
import com.example.data.model.ConversionUnit
import com.example.data.model.CurrencyCatalog
import com.example.data.model.SavedCurrencyPairEntity
import com.example.data.model.UnitCatalog
import com.example.data.model.UnitCategory
import com.example.data.repository.CurrencyRepository
import com.example.receiver.CadenceScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

enum class ActiveInputSource {
    BASE,
    TARGET
}

data class CurrencyUiState(
    val baseCurrency: String = "USD",
    val targetCurrency: String = "EUR",
    val inputAmount: String = "100",
    val activeInputSource: ActiveInputSource = ActiveInputSource.TARGET,
    val ratesMap: Map<String, Double> = CurrencyCatalog.defaultUsdRates,
    val savedPairs: List<SavedCurrencyPairEntity> = emptyList(),
    val history: List<ConversionHistoryEntity> = emptyList(),
    val lastUpdatedMillis: Long = System.currentTimeMillis(),
    val isSyncing: Boolean = false,
    val syncStatusMessage: String? = null,
    val isOffline: Boolean = false,
    val themeMode: Int = 0,
    val themeColorId: String = "ocean",
    val customThemeColor: Int = 0xFF7C3AED.toInt(),
    val cadenceMinutes: Int = 60,
    val decimalPlaces: Int = 2,
    val feeEnabled: Boolean = false,
    val feePercent: Double = 1.5,
    val searchQuery: String = "",
    val selectedRegion: String = "All",
    val isAmountHighlighted: Boolean = false,
    val pinnedCurrencies: Set<String> = emptySet()
) {
    val enteredCurrency: String
        get() = if (activeInputSource == ActiveInputSource.BASE) baseCurrency else targetCurrency

    fun calculateRate(fromCurrency: String, toCurrency: String): Double {
        if (fromCurrency.equals(toCurrency, ignoreCase = true)) return 1.0
        val fromToUsd = if (fromCurrency == "USD") 1.0 else ratesMap[fromCurrency]
        val toToUsd = if (toCurrency == "USD") 1.0 else ratesMap[toCurrency]
        if (fromToUsd == null || toToUsd == null || fromToUsd == 0.0) {
            val fallbackFrom = CurrencyCatalog.defaultUsdRates[fromCurrency] ?: 1.0
            val fallbackTo = CurrencyCatalog.defaultUsdRates[toCurrency] ?: 1.0
            return if (fallbackFrom != 0.0) fallbackTo / fallbackFrom else 1.0
        }
        return toToUsd / fromToUsd
    }

    val currentRate: Double
        get() = calculateRate(baseCurrency, targetCurrency)

    val numericInput: Double
        get() = inputAmount.toDoubleOrNull() ?: 0.0

    val numericBaseAmount: Double
        get() = if (activeInputSource == ActiveInputSource.BASE) {
            numericInput
        } else {
            numericInput * inverseRate
        }

    val numericTargetAmount: Double
        get() = if (activeInputSource == ActiveInputSource.TARGET) {
            numericInput
        } else {
            numericInput * currentRate
        }

    val convertedAmount: Double
        get() = numericTargetAmount

    val feeAmount: Double
        get() = if (feeEnabled) numericBaseAmount * (feePercent / 100.0) else 0.0

    val recipientGets: Double
        get() {
            val netBase = if (feeEnabled) (numericBaseAmount - feeAmount).coerceAtLeast(0.0) else numericBaseAmount
            return netBase * currentRate
        }

    val inverseRate: Double
        get() = if (currentRate > 0.0) 1.0 / currentRate else 0.0

    val isCurrentPairSaved: Boolean
        get() = savedPairs.any { it.baseCurrency == baseCurrency && it.targetCurrency == targetCurrency }
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CurrencyRepository(application)
    private val preferences = repository.preferences

    private val _uiState = MutableStateFlow(
        CurrencyUiState(
            baseCurrency = preferences.getDefaultBase(),
            targetCurrency = preferences.getDefaultTarget(),
            themeMode = preferences.getThemeMode(),
            themeColorId = preferences.getThemeColorId(),
            customThemeColor = preferences.getCustomThemeColor(),
            cadenceMinutes = preferences.getCadenceMinutes(),
            decimalPlaces = preferences.getDecimalPlaces(),
            feeEnabled = preferences.isFeeEnabled(),
            feePercent = preferences.getFeePercent(),
            pinnedCurrencies = preferences.getPinnedCurrencies()
        )
    )
    val uiState: StateFlow<CurrencyUiState> = _uiState.asStateFlow()

    private val _unitsState = MutableStateFlow(UnitsUiState())
    val unitsState: StateFlow<UnitsUiState> = _unitsState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureDefaultPairsExist()
            syncRates(force = false)
            CadenceScheduler.scheduleRefresh(getApplication())
        }

        // Collect rates from Room
        viewModelScope.launch {
            repository.allRatesFlow.collect { entities ->
                if (entities.isNotEmpty()) {
                    val map = entities.associate { it.currencyCode to it.rateToUsd }
                    val maxTimestamp = entities.maxOfOrNull { it.lastUpdatedMillis } ?: System.currentTimeMillis()
                    _uiState.value = _uiState.value.copy(
                        ratesMap = map,
                        lastUpdatedMillis = maxTimestamp
                    )
                }
            }
        }

        // Collect saved pairs
        viewModelScope.launch {
            repository.savedPairsFlow.collect { pairs ->
                _uiState.value = _uiState.value.copy(savedPairs = pairs)
            }
        }

        // Collect conversion history
        viewModelScope.launch {
            repository.recentHistoryFlow.collect { history ->
                _uiState.value = _uiState.value.copy(history = history)
            }
        }

        // Collect pinned currencies
        viewModelScope.launch {
            preferences.pinnedCurrenciesFlow.collect { pinned ->
                _uiState.value = _uiState.value.copy(pinnedCurrencies = pinned)
            }
        }
    }

    fun togglePinCurrency(code: String) {
        preferences.togglePinCurrency(code)
    }

    fun syncRates(force: Boolean = true) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, syncStatusMessage = null)
            val result = repository.syncRates(force = force)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    isOffline = false,
                    syncStatusMessage = null
                )
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Unable to sync."
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    isOffline = true,
                    syncStatusMessage = "Offline mode. Using cached exchange rates."
                )
            }
        }
    }

    fun setBaseCurrency(code: String) {
        if (_uiState.value.baseCurrency == code) return
        if (_uiState.value.targetCurrency == code) {
            // Swap if user picked same as target
            swapCurrencies()
            return
        }
        _uiState.value = _uiState.value.copy(baseCurrency = code)
        preferences.setDefaultBase(code)
    }

    fun setTargetCurrency(code: String) {
        if (_uiState.value.targetCurrency == code) return
        if (_uiState.value.baseCurrency == code) {
            swapCurrencies()
            return
        }
        _uiState.value = _uiState.value.copy(targetCurrency = code)
        preferences.setDefaultTarget(code)
    }

    fun swapCurrencies() {
        val oldBase = _uiState.value.baseCurrency
        val oldTarget = _uiState.value.targetCurrency
        _uiState.value = _uiState.value.copy(
            baseCurrency = oldTarget,
            targetCurrency = oldBase
        )
        preferences.setDefaultBase(oldTarget)
        preferences.setDefaultTarget(oldBase)
    }

    fun selectPair(base: String, target: String) {
        _uiState.value = _uiState.value.copy(
            baseCurrency = base,
            targetCurrency = target
        )
        preferences.setDefaultBase(base)
        preferences.setDefaultTarget(target)
    }

    fun setInputAmount(amount: String) {
        val cleaned = amount.filter { it.isDigit() || it == '.' }
        // Prevent multiple decimal points
        if (cleaned.count { it == '.' } > 1) return
        // Max 10 digits
        if (cleaned.length > 12) return
        _uiState.value = _uiState.value.copy(inputAmount = cleaned, isAmountHighlighted = false)
    }

    fun highlightInputAmount() {
        selectTargetInput()
    }

    fun selectBaseInput() {
        if (_uiState.value.activeInputSource == ActiveInputSource.BASE) {
            _uiState.value = _uiState.value.copy(isAmountHighlighted = true)
            return
        }
        val currentBase = _uiState.value.numericBaseAmount
        val formatted = formatNumberForInput(currentBase, _uiState.value.decimalPlaces)
        _uiState.value = _uiState.value.copy(
            activeInputSource = ActiveInputSource.BASE,
            inputAmount = formatted,
            isAmountHighlighted = true
        )
    }

    fun selectTargetInput() {
        if (_uiState.value.activeInputSource == ActiveInputSource.TARGET) {
            _uiState.value = _uiState.value.copy(isAmountHighlighted = true)
            return
        }
        val currentTarget = _uiState.value.numericTargetAmount
        val formatted = formatNumberForInput(currentTarget, _uiState.value.decimalPlaces)
        _uiState.value = _uiState.value.copy(
            activeInputSource = ActiveInputSource.TARGET,
            inputAmount = formatted,
            isAmountHighlighted = true
        )
    }

    private fun formatNumberForInput(value: Double, decimals: Int): String {
        if (value == 0.0 || value.isNaN() || value.isInfinite()) return "0"
        val df = java.text.DecimalFormat("#.####", java.text.DecimalFormatSymbols(Locale.US))
        df.maximumFractionDigits = decimals.coerceIn(2, 4)
        df.isGroupingUsed = false
        return df.format(value)
    }

    fun clearAmountHighlight() {
        _uiState.value = _uiState.value.copy(isAmountHighlighted = false)
    }

    fun appendKeypad(char: String) {
        val isHighlighted = _uiState.value.isAmountHighlighted
        val current = _uiState.value.inputAmount

        if (isHighlighted) {
            when (char) {
                "C", "DEL" -> {
                    _uiState.value = _uiState.value.copy(inputAmount = "0", isAmountHighlighted = false)
                }
                "." -> {
                    _uiState.value = _uiState.value.copy(inputAmount = "0.", isAmountHighlighted = false)
                }
                else -> {
                    // Directly replace the highlighted old entry with the new digit!
                    _uiState.value = _uiState.value.copy(inputAmount = char, isAmountHighlighted = false)
                }
            }
            return
        }

        when (char) {
            "C" -> _uiState.value = _uiState.value.copy(inputAmount = "0")
            "DEL" -> {
                val next = if (current.length <= 1) "0" else current.dropLast(1)
                _uiState.value = _uiState.value.copy(inputAmount = next)
            }
            "." -> {
                if (!current.contains(".")) {
                    _uiState.value = _uiState.value.copy(inputAmount = "$current.")
                }
            }
            else -> {
                val next = if (current == "0") char else current + char
                if (next.length <= 10) {
                    _uiState.value = _uiState.value.copy(inputAmount = next)
                }
            }
        }
    }

    fun quickSetAmount(amount: Double) {
        val str = if (amount % 1.0 == 0.0) {
            amount.toLong().toString()
        } else {
            String.format(Locale.US, "%.2f", amount)
        }
        _uiState.value = _uiState.value.copy(inputAmount = str, isAmountHighlighted = false)
    }

    fun toggleSaveCurrentPair() {
        val base = _uiState.value.baseCurrency
        val target = _uiState.value.targetCurrency
        val existing = _uiState.value.savedPairs.find { it.baseCurrency == base && it.targetCurrency == target }

        viewModelScope.launch {
            if (existing != null) {
                repository.deleteSavedPair(existing.id)
            } else {
                repository.saveCurrencyPair(base, target)
            }
        }
    }

    fun deleteSavedPair(id: Long) {
        viewModelScope.launch {
            repository.deleteSavedPair(id)
        }
    }

    fun togglePinSavedPair(pair: SavedCurrencyPairEntity) {
        viewModelScope.launch {
            repository.togglePinSavedPair(pair)
        }
    }

    fun addCustomPair(base: String, target: String) {
        viewModelScope.launch {
            repository.saveCurrencyPair(base, target)
        }
    }

    fun recordConversion() {
        val state = _uiState.value
        val numInput = state.numericInput
        if (numInput <= 0) return
        viewModelScope.launch {
            repository.recordConversion(
                from = state.baseCurrency,
                to = state.targetCurrency,
                fromAmount = numInput,
                toAmount = state.convertedAmount,
                rate = state.currentRate
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun setThemeMode(mode: Int) {
        _uiState.value = _uiState.value.copy(themeMode = mode)
        preferences.setThemeMode(mode)
    }

    fun setThemeColor(colorId: String) {
        _uiState.value = _uiState.value.copy(themeColorId = colorId)
        preferences.setThemeColorId(colorId)
    }

    fun setCustomThemeColor(colorInt: Int) {
        _uiState.value = _uiState.value.copy(customThemeColor = colorInt, themeColorId = "custom")
        preferences.setCustomThemeColor(colorInt)
        preferences.setThemeColorId("custom")
    }

    fun setCadenceMinutes(minutes: Int) {
        _uiState.value = _uiState.value.copy(cadenceMinutes = minutes)
        preferences.setCadenceMinutes(minutes)
        CadenceScheduler.scheduleRefresh(getApplication())
    }

    fun setDecimalPlaces(places: Int) {
        _uiState.value = _uiState.value.copy(decimalPlaces = places)
        preferences.setDecimalPlaces(places)
    }

    fun setFeeEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(feeEnabled = enabled)
        preferences.setFeeEnabled(enabled)
    }

    fun setFeePercent(percent: Double) {
        _uiState.value = _uiState.value.copy(feePercent = percent)
        preferences.setFeePercent(percent)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setSelectedRegion(region: String) {
        _uiState.value = _uiState.value.copy(selectedRegion = region)
    }

    fun dismissSyncMessage() {
        _uiState.value = _uiState.value.copy(syncStatusMessage = null)
    }

    // --- UNIT CONVERSION ACTIONS ---

    fun setUnitCategory(category: UnitCategory) {
        val (defFrom, defTo) = UnitCatalog.getDefaultUnitsForCategory(category)
        _unitsState.value = _unitsState.value.copy(
            selectedCategory = category,
            fromUnit = defFrom,
            toUnit = defTo,
            inputAmount = "1",
            activeInputSource = ActiveInputSource.BASE,
            isAmountHighlighted = false
        )
    }

    fun setFromUnit(unit: ConversionUnit) {
        if (_unitsState.value.fromUnit.id == unit.id) return
        if (_unitsState.value.toUnit.id == unit.id) {
            swapUnits()
            return
        }
        _unitsState.value = _unitsState.value.copy(fromUnit = unit)
    }

    fun setToUnit(unit: ConversionUnit) {
        if (_unitsState.value.toUnit.id == unit.id) return
        if (_unitsState.value.fromUnit.id == unit.id) {
            swapUnits()
            return
        }
        _unitsState.value = _unitsState.value.copy(toUnit = unit)
    }

    fun swapUnits() {
        val curFrom = _unitsState.value.fromUnit
        val curTo = _unitsState.value.toUnit
        _unitsState.value = _unitsState.value.copy(
            fromUnit = curTo,
            toUnit = curFrom
        )
    }

    fun selectUnitFromInput() {
        if (_unitsState.value.activeInputSource == ActiveInputSource.BASE) {
            _unitsState.value = _unitsState.value.copy(isAmountHighlighted = true)
            return
        }
        val curFrom = _unitsState.value.numericFromAmount
        val formatted = formatNumberForInput(curFrom, _uiState.value.decimalPlaces)
        _unitsState.value = _unitsState.value.copy(
            activeInputSource = ActiveInputSource.BASE,
            inputAmount = formatted,
            isAmountHighlighted = true
        )
    }

    fun selectUnitToInput() {
        if (_unitsState.value.activeInputSource == ActiveInputSource.TARGET) {
            _unitsState.value = _unitsState.value.copy(isAmountHighlighted = true)
            return
        }
        val curTo = _unitsState.value.numericToAmount
        val formatted = formatNumberForInput(curTo, _uiState.value.decimalPlaces)
        _unitsState.value = _unitsState.value.copy(
            activeInputSource = ActiveInputSource.TARGET,
            inputAmount = formatted,
            isAmountHighlighted = true
        )
    }

    fun appendUnitKeypad(char: String) {
        val isHighlighted = _unitsState.value.isAmountHighlighted
        val current = _unitsState.value.inputAmount

        if (isHighlighted) {
            when (char) {
                "C", "DEL" -> {
                    _unitsState.value = _unitsState.value.copy(inputAmount = "0", isAmountHighlighted = false)
                }
                "." -> {
                    _unitsState.value = _unitsState.value.copy(inputAmount = "0.", isAmountHighlighted = false)
                }
                else -> {
                    _unitsState.value = _unitsState.value.copy(inputAmount = char, isAmountHighlighted = false)
                }
            }
            return
        }

        when (char) {
            "C" -> _unitsState.value = _unitsState.value.copy(inputAmount = "0")
            "DEL" -> {
                val next = if (current.length <= 1) "0" else current.dropLast(1)
                _unitsState.value = _unitsState.value.copy(inputAmount = next)
            }
            "." -> {
                if (!current.contains(".")) {
                    _unitsState.value = _unitsState.value.copy(inputAmount = "$current.")
                }
            }
            else -> {
                val next = if (current == "0") char else current + char
                if (next.length <= 10) {
                    _unitsState.value = _unitsState.value.copy(inputAmount = next)
                }
            }
        }
    }

    fun quickSetUnitAmount(amount: Double) {
        val str = if (amount % 1.0 == 0.0) {
            amount.toLong().toString()
        } else {
            String.format(Locale.US, "%.2f", amount)
        }
        _unitsState.value = _unitsState.value.copy(inputAmount = str, isAmountHighlighted = false)
    }
}
