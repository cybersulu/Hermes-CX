package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrencyCatalog
import com.example.ui.CurrencyUiState
import com.example.ui.components.CurrencySelectSheet
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketRatesScreen(
    state: CurrencyUiState,
    onSelectBase: (String) -> Unit,
    onSelectTargetAndOpen: (String) -> Unit,
    onTogglePin: (String) -> Unit = {},
    onSyncRates: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf("All") }
    var isChangingBase by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    val regions = listOf("All", "Popular", "Europe", "Americas", "Asia", "Middle East", "Africa", "Oceania")

    val filteredList = remember(searchQuery, selectedRegion, state.marketBaseCurrency, state.pinnedCurrencies) {
        val baseList = if (selectedRegion == "Popular") {
            CurrencyCatalog.popularCurrencies.map { CurrencyCatalog.getCurrency(it) }
        } else if (selectedRegion != "All") {
            CurrencyCatalog.allCurrencies.filter { it.region.equals(selectedRegion, ignoreCase = true) }
        } else {
            CurrencyCatalog.allCurrencies
        }

        val searched = if (searchQuery.isBlank()) {
            baseList
        } else {
            val q = searchQuery.trim().lowercase()
            baseList.filter {
                it.code.lowercase().contains(q) ||
                it.name.lowercase().contains(q) ||
                it.country.lowercase().contains(q) ||
                it.symbol.lowercase().contains(q)
            }
        }

        // Pinned currencies at the top arranged alphabetically by country, followed by remaining currencies arranged alphabetically by country
        CurrencyCatalog.orderCurrencies(searched, state.pinnedCurrencies)
    }

    val baseCurrency = CurrencyCatalog.getCurrency(state.marketBaseCurrency)
    val rateFormatter = DecimalFormat("#,##0.0000", DecimalFormatSymbols(Locale.US))
    val timeFormatter = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }
    val formattedLastUpdate = remember(state.lastUpdatedMillis) {
        if (state.lastUpdatedMillis > 0) {
            timeFormatter.format(Date(state.lastUpdatedMillis))
        } else {
            timeFormatter.format(Date())
        }
    }

    PullToRefreshBox(
        isRefreshing = state.isSyncing,
        onRefresh = onSyncRates,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
        // Top Title & Base Currency Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Market Rates",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (state.isOffline) "Offline • Updated $formattedLastUpdate" else "Updated $formattedLastUpdate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("market_rates_updated_timestamp")
                )
            }

            // Clickable Base Currency Badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        isChangingBase = true
                        coroutineScope.launch { sheetState.show() }
                    }
                    .testTag("market_rates_base_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = baseCurrency.flag, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Base: ${baseCurrency.code}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("market_search_input"),
            placeholder = { Text("Search 160+ world currencies...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Region filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(regions) { region ->
                FilterChip(
                    selected = selectedRegion == region,
                    onClick = { selectedRegion = region },
                    label = { Text(region, fontSize = 13.sp) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Currency list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredList, key = { it.code }) { currency ->
                val fromToUsd = if (state.marketBaseCurrency == "USD") 1.0 else state.ratesMap[state.marketBaseCurrency] ?: 1.0
                val toToUsd = if (currency.code == "USD") 1.0 else state.ratesMap[currency.code] ?: 1.0
                val rate = if (fromToUsd != 0.0) toToUsd / fromToUsd else 1.0

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (currency.code == state.marketBaseCurrency) {
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelectTargetAndOpen(currency.code) }
                        .testTag("rate_item_${currency.code}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 80.dp)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = currency.flag, fontSize = 26.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currency.code,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(${currency.symbol})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                Text(
                                    text = if (currency.country.isNotBlank() && currency.country != currency.name) {
                                        "${currency.country} • ${currency.name}"
                                    } else {
                                        currency.name
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    softWrap = true,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        val isPinned = state.pinnedCurrencies.contains(currency.code)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = rateFormatter.format(rate),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.End,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Text(
                                    text = "1 ${state.marketBaseCurrency}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    textAlign = TextAlign.End,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            IconButton(
                                onClick = { onTogglePin(currency.code) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("pin_currency_${currency.code}")
                            ) {
                                Icon(
                                    imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                    contentDescription = if (isPinned) "Unpin currency" else "Pin currency",
                                    tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    }

    if (isChangingBase) {
        CurrencySelectSheet(
            sheetState = sheetState,
            title = "Select Base Currency for Market Rates",
            selectedCode = state.marketBaseCurrency,
            pinnedCurrencies = state.pinnedCurrencies,
            onDismiss = {
                isChangingBase = false
                coroutineScope.launch { sheetState.hide() }
            },
            onCurrencySelected = { code ->
                onSelectBase(code)
                isChangingBase = false
                coroutineScope.launch { sheetState.hide() }
            }
        )
    }
}
