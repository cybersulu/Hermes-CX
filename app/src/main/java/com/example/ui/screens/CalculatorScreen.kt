package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import com.example.data.model.CurrencyCatalog
import com.example.data.model.SavedCurrencyPairEntity
import com.example.ui.ActiveInputSource
import com.example.ui.CurrencyUiState
import com.example.ui.components.CurrencySelectSheet
import com.example.ui.components.KeypadView
import com.example.ui.components.MultiCompareSection
import com.example.ui.components.RateDisplayCard
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    state: CurrencyUiState,
    onBaseCurrencyChange: (String) -> Unit,
    onTargetCurrencyChange: (String) -> Unit,
    onSwap: () -> Unit,
    onKeypadPress: (String) -> Unit,
    onQuickAmountSelected: (Double) -> Unit = {},
    onToggleSavePair: () -> Unit,
    onSelectPair: (String, String) -> Unit = { base, target ->
        onBaseCurrencyChange(base)
        onTargetCurrencyChange(target)
    },
    onUpdateSavedPair: (Long, String, String) -> Unit = { _, _, _ -> },
    onSyncRates: () -> Unit,
    onDismissSyncMessage: () -> Unit = {},
    onAmountClick: () -> Unit = {},
    onBaseAmountClick: () -> Unit = onAmountClick,
    onTargetAmountClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var isSelectingBase by remember { mutableStateOf(false) }
    var isSelectingTarget by remember { mutableStateOf(false) }
    var calculatorSubTab by remember { mutableIntStateOf(0) } // 0 = Keypad, 1 = Multi-Compare

    // Edit favorite pair state
    var pairToEdit by remember { mutableStateOf<SavedCurrencyPairEntity?>(null) }
    var editBase by remember { mutableStateOf("") }
    var editTarget by remember { mutableStateOf("") }
    var isEditingPickBase by remember { mutableStateOf(false) }
    var isEditingPickTarget by remember { mutableStateOf(false) }

    val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val timeFormatter = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }
    val formattedLastUpdate = remember(state.lastUpdatedMillis) {
        timeFormatter.format(Date(state.lastUpdatedMillis))
    }

    PullToRefreshBox(
        isRefreshing = state.isSyncing,
        onRefresh = onSyncRates,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp)
        ) {
            // Hero Rate Card - Non-scrolling
            RateDisplayCard(
                state = state,
                onBaseClick = {
                    isSelectingBase = true
                    coroutineScope.launch { sheetState.show() }
                },
                onTargetClick = {
                    isSelectingTarget = true
                    coroutineScope.launch { sheetState.show() }
                },
                onSwap = onSwap,
                onToggleSave = onToggleSavePair,
                onBaseAmountClick = {
                    calculatorSubTab = 0
                    onBaseAmountClick()
                },
                onTargetAmountClick = {
                    calculatorSubTab = 0
                    onTargetAmountClick()
                },
                onSyncRates = onSyncRates
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Sub Tab Row: Keypad vs Compare - Non-scrolling
            PrimaryTabRow(
                selectedTabIndex = calculatorSubTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
            ) {
                Tab(
                    selected = calculatorSubTab == 0,
                    onClick = { calculatorSubTab = 0 },
                    text = { Text("Keypad", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("tab_keypad")
                )
                Tab(
                    selected = calculatorSubTab == 1,
                    onClick = { calculatorSubTab = 1 },
                    text = { Text("Compare", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("tab_compare")
                )
            }

            // Only the Compare sub-tab is scrollable
            if (calculatorSubTab == 1) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    MultiCompareSection(
                        state = state,
                        onSelectTarget = { code ->
                            if (state.activeInputSource == ActiveInputSource.BASE) {
                                onTargetCurrencyChange(code)
                            } else {
                                onBaseCurrencyChange(code)
                            }
                            calculatorSubTab = 0
                        }
                    )
                }
            } else {
                // Keypad sub-tab: anchored to bottom without scrolling
                Spacer(modifier = Modifier.weight(1f))
                // Favorite currency pairs selector strip just above the number keypad (limited to 5 pairs)
                FavoritePairsSelectorStrip(
                    savedPairs = state.savedPairs,
                    currentBase = state.baseCurrency,
                    currentTarget = state.targetCurrency,
                    onSelectPair = onSelectPair,
                    onLongClickPair = { pair ->
                        pairToEdit = pair
                        editBase = pair.baseCurrency
                        editTarget = pair.targetCurrency
                    }
                )
                Spacer(modifier = Modifier.height(6.dp))
                KeypadView(
                    onKeyPress = onKeypadPress,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Base Currency Selector Sheet
        if (isSelectingBase) {
            CurrencySelectSheet(
                sheetState = sheetState,
                title = "Select Base Currency (You Send)",
                selectedCode = state.baseCurrency,
                pinnedCurrencies = state.pinnedCurrencies,
                onDismiss = {
                    isSelectingBase = false
                    coroutineScope.launch { sheetState.hide() }
                },
                onCurrencySelected = { code ->
                    onBaseCurrencyChange(code)
                    isSelectingBase = false
                    coroutineScope.launch { sheetState.hide() }
                }
            )
        }

        // Target Currency Selector Sheet
        if (isSelectingTarget) {
            CurrencySelectSheet(
                sheetState = sheetState,
                title = "Select Target Currency (You Receive)",
                selectedCode = state.targetCurrency,
                pinnedCurrencies = state.pinnedCurrencies,
                onDismiss = {
                    isSelectingTarget = false
                    coroutineScope.launch { sheetState.hide() }
                },
                onCurrencySelected = { code ->
                    onTargetCurrencyChange(code)
                    isSelectingTarget = false
                    coroutineScope.launch { sheetState.hide() }
                }
            )
        }

        // Edit Favorite Pair Dialog
        pairToEdit?.let { targetPair ->
            AlertDialog(
                onDismissRequest = { pairToEdit = null },
                title = { Text("Edit Favorite Pair", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Choose the currencies for this favorite pair slot:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // From Currency picker button
                        val baseCurr = CurrencyCatalog.getCurrency(editBase)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    isEditingPickBase = true
                                    coroutineScope.launch { editSheetState.show() }
                                }
                                .testTag("edit_pair_from_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("From: ", fontWeight = FontWeight.SemiBold)
                                Text("${baseCurr.flag} ${baseCurr.code} - ${baseCurr.name}")
                            }
                        }

                        // To Currency picker button
                        val targetCurr = CurrencyCatalog.getCurrency(editTarget)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    isEditingPickTarget = true
                                    coroutineScope.launch { editSheetState.show() }
                                }
                                .testTag("edit_pair_to_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("To: ", fontWeight = FontWeight.SemiBold)
                                Text("${targetCurr.flag} ${targetCurr.code} - ${targetCurr.name}")
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (editBase.isNotEmpty() && editTarget.isNotEmpty() && editBase != editTarget) {
                                onUpdateSavedPair(targetPair.id, editBase, editTarget)
                            }
                            pairToEdit = null
                        },
                        modifier = Modifier.testTag("edit_pair_save_btn")
                    ) {
                        Text("Save Changes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pairToEdit = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Sheet to pick Base Currency while editing pair
        if (isEditingPickBase) {
            CurrencySelectSheet(
                sheetState = editSheetState,
                title = "Select From Currency",
                selectedCode = editBase,
                pinnedCurrencies = state.pinnedCurrencies,
                onDismiss = {
                    isEditingPickBase = false
                    coroutineScope.launch { editSheetState.hide() }
                },
                onCurrencySelected = { code ->
                    editBase = code
                    isEditingPickBase = false
                    coroutineScope.launch { editSheetState.hide() }
                }
            )
        }

        // Sheet to pick Target Currency while editing pair
        if (isEditingPickTarget) {
            CurrencySelectSheet(
                sheetState = editSheetState,
                title = "Select To Currency",
                selectedCode = editTarget,
                pinnedCurrencies = state.pinnedCurrencies,
                onDismiss = {
                    isEditingPickTarget = false
                    coroutineScope.launch { editSheetState.hide() }
                },
                onCurrencySelected = { code ->
                    editTarget = code
                    isEditingPickTarget = false
                    coroutineScope.launch { editSheetState.hide() }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FavoritePairsSelectorStrip(
    savedPairs: List<SavedCurrencyPairEntity>,
    currentBase: String,
    currentTarget: String,
    onSelectPair: (String, String) -> Unit,
    onLongClickPair: (SavedCurrencyPairEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (savedPairs.isEmpty()) return

    // Limit to 5 favorite currency pairs
    val displayPairs = remember(savedPairs) { savedPairs.take(5) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        displayPairs.forEach { pair ->
            val isSelected = pair.baseCurrency == currentBase && pair.targetCurrency == currentTarget
            val baseCurrency = CurrencyCatalog.getCurrency(pair.baseCurrency)
            val targetCurrency = CurrencyCatalog.getCurrency(pair.targetCurrency)

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .combinedClickable(
                        onClick = { onSelectPair(pair.baseCurrency, pair.targetCurrency) },
                        onLongClick = { onLongClickPair(pair) }
                    )
                    .testTag("chip_favorite_pair_${pair.baseCurrency}_${pair.targetCurrency}"),
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(
                    1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${baseCurrency.flag}${targetCurrency.flag}",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${pair.baseCurrency} / ${pair.targetCurrency}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
