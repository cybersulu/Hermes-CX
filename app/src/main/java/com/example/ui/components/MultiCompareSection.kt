package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.ActiveInputSource
import com.example.ui.CurrencyUiState
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun MultiCompareSection(
    state: CurrencyUiState,
    onSelectTarget: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val enteredCurrencyCode = state.enteredCurrency
    val otherSelectedCurrency = if (state.activeInputSource == ActiveInputSource.BASE) {
        state.targetCurrency
    } else {
        state.baseCurrency
    }

    // Only show pinned currencies on the Currency Compare sub-tab (excluding entered and other selected currency), ordered alphabetically by country
    val comparisonCodes = state.pinnedCurrencies
        .filter { it != enteredCurrencyCode && it != otherSelectedCurrency }
        .sortedWith(Comparator { a, b ->
            val c1 = CurrencyCatalog.getCurrency(a).country
            val c2 = CurrencyCatalog.getCurrency(b).country
            val comp = c1.compareTo(c2, ignoreCase = true)
            if (comp != 0) comp else a.compareTo(b, ignoreCase = true)
        })

    val formatter = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
    val enteredAmount = state.numericInput
    val enteredAmountStr = state.inputAmount.ifEmpty { "0" }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Compare ($enteredAmountStr $enteredCurrencyCode)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )

        if (comparisonCodes.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = "No pinned currencies to compare.\nPin currencies in the Market tab to see them here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
            return
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            for (code in comparisonCodes) {
                val currency = CurrencyCatalog.getCurrency(code)
                val rate = state.calculateRate(enteredCurrencyCode, code)
                val converted = enteredAmount * rate

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    shadowElevation = 1.dp,
                    tonalElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 76.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelectTarget(code) }
                        .testTag("compare_item_$code")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 76.dp)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = currency.flag, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = currency.code,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
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

                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                text = "${currency.symbol} ${formatter.format(converted)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = "1 $enteredCurrencyCode = ${String.format(Locale.US, "%.4f", rate)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }
}
