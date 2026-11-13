package com.example.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.LedgerEntry
import com.example.data.Party
import com.example.data.ShopSummaryStats
import com.example.ui.RegisterFilter
import com.example.ui.ShopLedgerViewModel
import com.example.ui.theme.EmeraldDarkText
import com.example.ui.theme.EmeraldReceivable
import com.example.ui.theme.EmeraldReceivableBorder
import com.example.ui.theme.EmeraldReceivableSoft
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OnSaasIndigoContainer
import com.example.ui.theme.RubyDarkText
import com.example.ui.theme.RubyPayable
import com.example.ui.theme.RubyPayableBorder
import com.example.ui.theme.RubyPayableSoft
import com.example.ui.theme.SaasBorder
import com.example.ui.theme.SaasIndigoBorder
import com.example.ui.theme.SaasIndigoContainer
import com.example.ui.theme.SaasIndigoPrimary
import com.example.ui.theme.SaasInkMuted
import com.example.ui.theme.SaasInkPrimary
import com.example.ui.theme.SaasInkSecondary
import com.example.ui.theme.SaasSurface
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

@Composable
fun DailyRegisterScreen(
    allEntries: List<LedgerEntry>,
    selectedFilter: RegisterFilter,
    searchQuery: String,
    currencySymbol: String,
    onFilterChange: (RegisterFilter) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onEntryPartyClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val startOfTodayMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val sevenDaysAgoMillis = remember {
        System.currentTimeMillis() - 7 * 86_400_000L
    }

    val filteredEntries = remember(allEntries, selectedFilter, searchQuery) {
        val q = searchQuery.trim().lowercase(Locale.getDefault())
        allEntries.filter { entry ->
            val matchesSearch = q.isEmpty() ||
                entry.partyName.lowercase(Locale.getDefault()).contains(q) ||
                entry.description.lowercase(Locale.getDefault()).contains(q) ||
                entry.category.lowercase(Locale.getDefault()).contains(q) ||
                entry.referenceNote.lowercase(Locale.getDefault()).contains(q)

            val matchesFilter = when (selectedFilter) {
                RegisterFilter.ALL -> true
                RegisterFilter.GAVE_ONLY -> entry.type == LedgerEntry.TYPE_GAVE
                RegisterFilter.GOT_ONLY -> entry.type == LedgerEntry.TYPE_GOT
                RegisterFilter.TODAY -> entry.entryDateMillis >= startOfTodayMillis
                RegisterFilter.LAST_7_DAYS -> entry.entryDateMillis >= sevenDaysAgoMillis
            }
            matchesSearch && matchesFilter
        }
    }

    val periodGaveCents = remember(filteredEntries) {
        filteredEntries.filter { it.type == LedgerEntry.TYPE_GAVE }.sumOf { it.amountCents }
    }
    val periodGotCents = remember(filteredEntries) {
        filteredEntries.filter { it.type == LedgerEntry.TYPE_GOT }.sumOf { it.amountCents }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("daily_register_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "cashbook_header") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SaasBorder, RoundedCornerShape(18.dp))
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SaasSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SHOP REGISTER CASHBOOK",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SaasInkMuted
                        )
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = SaasIndigoContainer,
                            border = BorderStroke(1.dp, SaasIndigoBorder)
                        ) {
                            Text(
                                text = "${filteredEntries.size} entries",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = OnSaasIndigoContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(EmeraldReceivableSoft)
                                .border(1.dp, EmeraldReceivableBorder, RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL YOU GAVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldDarkText,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ShopLedgerViewModel.formatMoney(periodGaveCents, currencySymbol),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = EmeraldReceivable,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(RubyPayableSoft)
                                .border(1.dp, RubyPayableBorder, RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL YOU GOT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RubyDarkText,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ShopLedgerViewModel.formatMoney(periodGotCents, currencySymbol),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = RubyPayable,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        item(key = "register_search") {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = "Search entries by party, item, or bill #...",
                        color = SaasInkMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = SaasInkMuted
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SaasSurface,
                    unfocusedContainerColor = SaasSurface,
                    focusedBorderColor = SaasIndigoPrimary,
                    unfocusedBorderColor = SaasBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_search_input")
            )
        }

        item(key = "register_filters") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RegisterFilter.entries.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterChange(filter) },
                        label = {
                            Text(
                                text = filter.label,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = SaasBorder,
                            selectedBorderColor = SaasIndigoBorder
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SaasSurface,
                            labelColor = SaasInkSecondary,
                            selectedContainerColor = SaasIndigoContainer,
                            selectedLabelColor = OnSaasIndigoContainer
                        ),
                        modifier = Modifier.testTag("register_filter_${filter.name.lowercase()}")
                    )
                }
            }
        }

        if (filteredEntries.isEmpty()) {
            item(key = "empty_register") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .border(1.dp, SaasBorder, RoundedCornerShape(18.dp))
                        .animateItem(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SaasSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SaasIndigoContainer)
                                .border(1.dp, SaasIndigoBorder, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HistoryEdu,
                                contentDescription = null,
                                tint = SaasIndigoPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Register Entries Found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SaasInkPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Every 'You Gave' or 'You Got' entry recorded under any person or supplier appears here chronologically.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SaasInkSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredEntries, key = { it.id }) { entry ->
                val isGave = entry.type == LedgerEntry.TYPE_GAVE
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SaasBorder, RoundedCornerShape(16.dp))
                        .clickable { onEntryPartyClick(entry.partyId) }
                        .animateItem(
                            fadeInSpec = tween(200),
                            fadeOutSpec = tween(150),
                            placementSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SaasSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isGave) EmeraldReceivableSoft else RubyPayableSoft)
                                .border(
                                    1.dp,
                                    if (isGave) EmeraldReceivableBorder else RubyPayableBorder,
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isGave) Icons.AutoMirrored.Filled.CallMade else Icons.AutoMirrored.Filled.CallReceived,
                                contentDescription = null,
                                tint = if (isGave) EmeraldReceivable else RubyPayable,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.partyName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SaasInkPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = entry.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = SaasInkSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "${ShopLedgerViewModel.formatDateTime(entry.entryDateMillis)} • ${entry.category} • ${entry.paymentMode.replace("_", " ")}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SaasInkMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = ShopLedgerViewModel.formatMoney(entry.amountCents, currencySymbol),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (isGave) EmeraldReceivable else RubyPayable,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = if (isGave) "YOU GAVE" else "YOU GOT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isGave) EmeraldDarkText else RubyDarkText,
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

@Composable
fun AnalyticsTabScreen(
    parties: List<Party>,
    allEntries: List<LedgerEntry>,
    summaryStats: ShopSummaryStats,
    currencySymbol: String,
    onPartyClick: (Party) -> Unit,
    modifier: Modifier = Modifier
) {
    val topDebtors = remember(parties) {
        parties.filter { it.netBalanceCents > 0L }
            .sortedByDescending { it.netBalanceCents }
            .take(5)
    }
    val topCreditors = remember(parties) {
        parties.filter { it.netBalanceCents < 0L }
            .sortedBy { it.netBalanceCents }
            .take(5)
    }
    val paymentBreakdown = remember(allEntries) {
        allEntries.groupBy { it.paymentMode }
            .mapValues { (_, list) -> list.sumOf { it.amountCents } }
            .entries
            .sortedByDescending { it.value }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("analytics_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Overview Card
        item(key = "balance_sheet") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SaasBorder, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SaasSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LEDGER BALANCE SHEET",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaasInkMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Total They Owe Us",
                                style = MaterialTheme.typography.bodySmall,
                                color = SaasInkSecondary
                            )
                            Text(
                                text = ShopLedgerViewModel.formatMoney(summaryStats.totalReceivableCents, currencySymbol),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = EmeraldReceivable
                            )
                            Text(
                                text = "${summaryStats.receivableCount} accounts",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldDarkText
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Total We Owe Them",
                                style = MaterialTheme.typography.bodySmall,
                                color = SaasInkSecondary
                            )
                            Text(
                                text = ShopLedgerViewModel.formatMoney(summaryStats.totalPayableCents, currencySymbol),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = RubyPayable
                            )
                            Text(
                                text = "${summaryStats.payableCount} accounts",
                                style = MaterialTheme.typography.labelSmall,
                                color = RubyDarkText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = SaasBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Settled Accounts: ${summaryStats.settledCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SaasInkSecondary
                        )
                        Text(
                            text = "Total Entries Logged: ${allEntries.size}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = SaasInkPrimary
                        )
                    }
                }
            }
        }

        // Top Debtors (Who Owes Us Most)
        item(key = "top_receivables") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SaasBorder, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SaasSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TOP RECEIVABLES (WHO OWES US MOST)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDarkText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (topDebtors.isEmpty()) {
                        Text(
                            text = "No pending receivables right now.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SaasInkMuted
                        )
                    } else {
                        val maxReceivable = topDebtors.first().netBalanceCents.coerceAtLeast(1L)
                        topDebtors.forEach { party ->
                            val targetRatio = (party.netBalanceCents.toFloat() / maxReceivable.toFloat()).coerceIn(0.08f, 1f)
                            val animatedRatio by animateFloatAsState(
                                targetValue = targetRatio,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "ReceivableBar_${party.id}"
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onPartyClick(party) }
                                    .padding(vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = party.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SaasInkPrimary
                                    )
                                    Text(
                                        text = ShopLedgerViewModel.formatMoney(party.netBalanceCents, currencySymbol),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldReceivable
                                    )
                                }
                                Spacer(modifier = Modifier.height(5.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(EmeraldReceivableSoft)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(animatedRatio)
                                            .fillMaxHeight()
                                            .background(EmeraldReceivable)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Top Creditors (Who We Owe Most)
        item(key = "top_payables") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SaasBorder, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SaasSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TOP PAYABLES (WHO WE OWE MOST)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = RubyDarkText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (topCreditors.isEmpty()) {
                        Text(
                            text = "No pending payables right now.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SaasInkMuted
                        )
                    } else {
                        val maxPayable = abs(topCreditors.first().netBalanceCents).coerceAtLeast(1L)
                        topCreditors.forEach { party ->
                            val absAmt = abs(party.netBalanceCents)
                            val targetRatio = (absAmt.toFloat() / maxPayable.toFloat()).coerceIn(0.08f, 1f)
                            val animatedRatio by animateFloatAsState(
                                targetValue = targetRatio,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "PayableBar_${party.id}"
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onPartyClick(party) }
                                    .padding(vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = party.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SaasInkPrimary
                                    )
                                    Text(
                                        text = ShopLedgerViewModel.formatMoney(absAmt, currencySymbol),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = RubyPayable
                                    )
                                }
                                Spacer(modifier = Modifier.height(5.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(RubyPayableSoft)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(animatedRatio)
                                            .fillMaxHeight()
                                            .background(RubyPayable)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Payment Mode Volume Breakdown
        item(key = "payment_breakdown") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SaasBorder, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SaasSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "VOLUME BY PAYMENT / CREDIT INSTRUMENT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaasInkMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (paymentBreakdown.isEmpty()) {
                        Text(
                            text = "No transactions recorded yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SaasInkMuted
                        )
                    } else {
                        paymentBreakdown.forEach { (mode, totalCents) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = mode.replace("_", " "),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = SaasInkPrimary
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SaasIndigoContainer,
                                    border = BorderStroke(1.dp, SaasIndigoBorder)
                                ) {
                                    Text(
                                        text = ShopLedgerViewModel.formatMoney(totalCents, currencySymbol),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = OnSaasIndigoContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
