package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConversionUnit
import com.example.data.model.UnitCatalog
import com.example.data.model.UnitCategory
import com.example.ui.ActiveInputSource
import com.example.ui.UnitsUiState
import com.example.ui.components.KeypadView
import com.example.ui.components.UnitSelectSheet
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitsScreen(
    state: UnitsUiState,
    onCategoryChange: (UnitCategory) -> Unit,
    onFromUnitChange: (ConversionUnit) -> Unit,
    onToUnitChange: (ConversionUnit) -> Unit,
    onSwapUnits: () -> Unit,
    onKeypadPress: (String) -> Unit,
    onFromAmountClick: () -> Unit,
    onToAmountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isSelectingFrom by remember { mutableStateOf(false) }
    var isSelectingTo by remember { mutableStateOf(false) }
    var unitsSubTab by remember { mutableIntStateOf(0) } // 0 = Keypad, 1 = All Units

    var rotationAngle by remember { mutableFloatStateOf(0f) }
    val animatedRotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "unit_swap_rotation"
    )

    val formatter = remember {
        DecimalFormat("#,##0.####", DecimalFormatSymbols(Locale.US)).apply {
            maximumFractionDigits = 6
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp)
            .testTag("units_screen")
    ) {
        // HERO UNIT CONVERSION CARD - Non-scrolling
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("unit_conversion_card"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // FORMULA BANNER
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("unit_formula_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "Formula / Rate",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = state.formulaText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("unit_formula_text")
                            )
                        }

                        FilledTonalButton(
                            onClick = {
                                val resultText = "${formatter.format(state.numericFromAmount)} ${state.fromUnit.symbol} = ${formatter.format(state.numericToAmount)} ${state.toUnit.symbol}"
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Unit Conversion", resultText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied: $resultText", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("btn_copy_unit_result")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy result",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Copy",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // FROM UNIT ROW (Top Position)
                val isFromActive = state.activeInputSource == ActiveInputSource.BASE
                val fromDisplayValue = if (isFromActive) {
                    state.inputAmount.ifEmpty { "0" }
                } else {
                    formatter.format(state.numericFromAmount)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 68.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // From Unit Selector Capsule
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        tonalElevation = 1.dp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(22.dp))
                            .clickable {
                                isSelectingFrom = true
                                coroutineScope.launch { sheetState.show() }
                            }
                            .testTag("from_unit_selector")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = state.fromUnit.symbol.take(3),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = state.fromUnit.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = state.fromUnit.symbol,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // From Amount Box
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (isFromActive) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (state.isAmountHighlighted) 0.85f else 0.45f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        },
                        border = if (isFromActive) {
                            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        } else {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                unitsSubTab = 0
                                onFromAmountClick()
                            }
                            .testTag("from_unit_amount_box")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            AnimatedContent(
                                targetState = fromDisplayValue,
                                transitionSpec = {
                                    slideInVertically { it / 2 } togetherWith slideOutVertically { -it / 2 }
                                },
                                label = "from_amount_text"
                            ) { text ->
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFromActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.End,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = if (isFromActive) "Active • ${state.fromUnit.symbol}" else "Tap to edit • ${state.fromUnit.symbol}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isFromActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                // SWAP BUTTON
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable {
                                rotationAngle += 180f
                                onSwapUnits()
                            }
                            .testTag("btn_swap_units"),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        tonalElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Swap Units",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .size(26.dp)
                                    .rotate(animatedRotation)
                            )
                        }
                    }
                }

                // TO UNIT ROW (Bottom Position)
                val isToActive = state.activeInputSource == ActiveInputSource.TARGET
                val toDisplayValue = if (isToActive) {
                    state.inputAmount.ifEmpty { "0" }
                } else {
                    formatter.format(state.numericToAmount)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 68.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // To Unit Selector Capsule
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        tonalElevation = 1.dp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(22.dp))
                            .clickable {
                                isSelectingTo = true
                                coroutineScope.launch { sheetState.show() }
                            }
                            .testTag("to_unit_selector")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = state.toUnit.symbol.take(3),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = state.toUnit.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = state.toUnit.symbol,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // To Amount Box
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (isToActive) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (state.isAmountHighlighted) 0.85f else 0.45f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        },
                        border = if (isToActive) {
                            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        } else {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                unitsSubTab = 0
                                onToAmountClick()
                            }
                            .testTag("to_unit_amount_box")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            AnimatedContent(
                                targetState = toDisplayValue,
                                transitionSpec = {
                                    slideInVertically { it / 2 } togetherWith slideOutVertically { -it / 2 }
                                },
                                label = "to_amount_text"
                            ) { text ->
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isToActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.End,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = if (isToActive) "Active • ${state.toUnit.symbol}" else "Tap to edit • ${state.toUnit.symbol}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isToActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // SUB TABS: KEYPAD VS ALL UNITS COMPARISON
        PrimaryTabRow(
            selectedTabIndex = unitsSubTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
        ) {
            Tab(
                selected = unitsSubTab == 0,
                onClick = { unitsSubTab = 0 },
                text = { Text("Keypad", fontWeight = FontWeight.SemiBold) },
                modifier = Modifier.testTag("tab_unit_keypad")
            )
            Tab(
                selected = unitsSubTab == 1,
                onClick = { unitsSubTab = 1 },
                text = { Text("Compare", fontWeight = FontWeight.SemiBold) },
                modifier = Modifier.testTag("tab_unit_compare")
            )
        }

        if (unitsSubTab == 1) {
            Spacer(modifier = Modifier.height(10.dp))
            // ALL UNITS LIST - only this sub-tab is scrollable
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .testTag("all_units_comparison_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                    state.liveComparisons.forEach { (unit, convertedVal) ->
                        val isCurrentTarget = unit.id == state.toUnit.id
                        val isCurrentFrom = unit.id == state.fromUnit.id

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isCurrentTarget) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            },
                            border = BorderStroke(
                                1.dp,
                                if (isCurrentTarget) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    if (!isCurrentFrom) {
                                        onToUnitChange(unit)
                                        unitsSubTab = 0
                                    }
                                }
                                .testTag("compare_item_${unit.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = unit.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isCurrentFrom) "Input Unit" else if (isCurrentTarget) "Target Unit" else "Tap to set as target",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isCurrentTarget || isCurrentFrom) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = formatter.format(convertedVal),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = unit.symbol,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
        }

        // Keypad area anchored consistently at the bottom
        if (unitsSubTab == 0) {
            Spacer(modifier = Modifier.weight(1f))
            // Unit category selector strip just above the number keypad
            UnitCategorySelectorStrip(
                selectedCategory = state.selectedCategory,
                onCategoryChange = onCategoryChange
            )
            Spacer(modifier = Modifier.height(6.dp))
            KeypadView(
                onKeyPress = onKeypadPress,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // FROM UNIT SELECT SHEET
    if (isSelectingFrom) {
        UnitSelectSheet(
            sheetState = sheetState,
            title = "Select Source Unit",
            category = state.selectedCategory,
            selectedUnitId = state.fromUnit.id,
            onDismiss = {
                isSelectingFrom = false
                coroutineScope.launch { sheetState.hide() }
            },
            onUnitSelected = { unit ->
                onFromUnitChange(unit)
                isSelectingFrom = false
                coroutineScope.launch { sheetState.hide() }
            }
        )
    }

    // TO UNIT SELECT SHEET
    if (isSelectingTo) {
        UnitSelectSheet(
            sheetState = sheetState,
            title = "Select Target Unit",
            category = state.selectedCategory,
            selectedUnitId = state.toUnit.id,
            onDismiss = {
                isSelectingTo = false
                coroutineScope.launch { sheetState.hide() }
            },
            onUnitSelected = { unit ->
                onToUnitChange(unit)
                isSelectingTo = false
                coroutineScope.launch { sheetState.hide() }
            }
        )
    }
}

@Composable
private fun UnitCategorySelectorStrip(
    selectedCategory: UnitCategory,
    onCategoryChange: (UnitCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UnitCatalog.allCategories.forEach { category ->
            val isSelected = category == selectedCategory
            FilterChip(
                selected = isSelected,
                onClick = { onCategoryChange(category) },
                label = {
                    Text(
                        text = category.displayName,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = getCategoryIcon(category),
                        contentDescription = category.displayName,
                        modifier = Modifier.size(18.dp)
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.testTag("chip_category_${category.id}")
            )
        }
    }
}

private fun getCategoryIcon(category: UnitCategory): ImageVector {
    return when (category) {
        UnitCategory.LENGTH -> Icons.Default.Straighten
        UnitCategory.MASS -> Icons.Default.Scale
        UnitCategory.TEMPERATURE -> Icons.Default.Thermostat
        UnitCategory.VOLUME -> Icons.Default.WaterDrop
        UnitCategory.AREA -> Icons.Default.SquareFoot
        UnitCategory.SPEED -> Icons.Default.Speed
        UnitCategory.TIME -> Icons.Default.Schedule
        UnitCategory.DIGITAL -> Icons.Default.Storage
        UnitCategory.PRESSURE -> Icons.Default.Compress
        UnitCategory.ENERGY -> Icons.Default.Bolt
    }
}
