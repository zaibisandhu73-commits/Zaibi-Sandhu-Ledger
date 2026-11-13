package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.LedgerEntry
import com.example.data.Party
import com.example.data.ShopSummaryStats
import com.example.ui.PartyFilter
import com.example.ui.PartySortOrder
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
import com.example.ui.theme.SaasSurfaceSubtle
import kotlin.math.abs

@Composable
fun PartiesTabScreen(
    parties: List<Party>,
    summaryStats: ShopSummaryStats,
    searchQuery: String,
    selectedFilter: PartyFilter,
    selectedSort: PartySortOrder,
    selectedPartyId: String?,
    currencySymbol: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (PartyFilter) -> Unit,
    onSortChange: (PartySortOrder) -> Unit,
    onPartyClick: (Party) -> Unit,
    onQuickEntryClick: (Party, String) -> Unit,
    onAddPartyClick: () -> Unit,
    onSeedSampleDataClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("parties_list"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item(key = "summary_header") {
            ShopBalanceSummaryCard(
                stats = summaryStats,
                currencySymbol = currencySymbol,
                onReceivableClick = {
                    onFilterChange(
                        if (selectedFilter == PartyFilter.THEY_OWE_US) PartyFilter.ALL
                        else PartyFilter.THEY_OWE_US
                    )
                },
                onPayableClick = {
                    onFilterChange(
                        if (selectedFilter == PartyFilter.WE_OWE_THEM) PartyFilter.ALL
                        else PartyFilter.WE_OWE_THEM
                    )
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        item(key = "search_and_filters") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                text = "Search name, phone, or note...",
                                color = SaasInkMuted
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = SaasInkMuted
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
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
                            .weight(1f)
                            .testTag("search_parties_input")
                    )

                    Box {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SaasSurface,
                            border = BorderStroke(1.dp, SaasBorder),
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showSortMenu = true }
                                .testTag("sort_parties_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Sort accounts",
                                    tint = SaasInkSecondary
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(SaasSurface)
                        ) {
                            PartySortOrder.entries.forEach { order ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = order.label,
                                            fontWeight = if (selectedSort == order) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedSort == order) SaasIndigoPrimary else SaasInkPrimary
                                        )
                                    },
                                    onClick = {
                                        onSortChange(order)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PartyFilter.entries.forEach { filter ->
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
                                selectedBorderColor = when (filter) {
                                    PartyFilter.THEY_OWE_US -> EmeraldReceivableBorder
                                    PartyFilter.WE_OWE_THEM -> RubyPayableBorder
                                    else -> SaasIndigoBorder
                                }
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SaasSurface,
                                labelColor = SaasInkSecondary,
                                selectedContainerColor = when (filter) {
                                    PartyFilter.THEY_OWE_US -> EmeraldReceivableSoft
                                    PartyFilter.WE_OWE_THEM -> RubyPayableSoft
                                    else -> SaasIndigoContainer
                                },
                                selectedLabelColor = when (filter) {
                                    PartyFilter.THEY_OWE_US -> EmeraldDarkText
                                    PartyFilter.WE_OWE_THEM -> RubyDarkText
                                    else -> OnSaasIndigoContainer
                                }
                            ),
                            modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (parties.isEmpty()) {
            item(key = "empty_parties") {
                EmptyPartiesCard(
                    hasActiveFilter = searchQuery.isNotBlank() || selectedFilter != PartyFilter.ALL,
                    onResetFilter = {
                        onSearchQueryChange("")
                        onFilterChange(PartyFilter.ALL)
                    },
                    onAddPartyClick = onAddPartyClick,
                    onSeedSampleDataClick = onSeedSampleDataClick,
                    modifier = Modifier
                        .padding(16.dp)
                        .animateItem()
                )
            }
        } else {
            items(parties, key = { it.id }) { party ->
                PartyLedgerCard(
                    party = party,
                    isSelected = party.id == selectedPartyId,
                    currencySymbol = currencySymbol,
                    onClick = { onPartyClick(party) },
                    onQuickGave = { onQuickEntryClick(party, LedgerEntry.TYPE_GAVE) },
                    onQuickGot = { onQuickEntryClick(party, LedgerEntry.TYPE_GOT) },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                        .animateItem(
                            fadeInSpec = tween(220),
                            fadeOutSpec = tween(160),
                            placementSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun ShopBalanceSummaryCard(
    stats: ShopSummaryStats,
    currencySymbol: String,
    onReceivableClick: () -> Unit,
    onPayableClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalVolume = (stats.totalReceivableCents + stats.totalPayableCents).coerceAtLeast(1L)
    val targetFraction = if (stats.totalReceivableCents == 0L && stats.totalPayableCents == 0L) {
        0.5f
    } else {
        (stats.totalReceivableCents.toFloat() / totalVolume.toFloat()).coerceIn(0.08f, 0.92f)
    }
    val receivableFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ReceivableProportionSpring"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, SaasBorder, RoundedCornerShape(20.dp))
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SaasSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Subtle SaaS Top Accent Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(SaasIndigoPrimary, EmeraldReceivable, RubyPayable)
                        )
                    )
            )

            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SHOP NET POSITION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SaasInkMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val netText = when {
                            stats.netPositionCents > 0L ->
                                "+${ShopLedgerViewModel.formatMoney(stats.netPositionCents, currencySymbol)} (In Favor)"
                            stats.netPositionCents < 0L ->
                                "-${ShopLedgerViewModel.formatMoney(abs(stats.netPositionCents), currencySymbol)} (Net Payable)"
                            else -> "${currencySymbol}0.00 (Balanced)"
                        }
                        val netColor = when {
                            stats.netPositionCents > 0L -> EmeraldReceivable
                            stats.netPositionCents < 0L -> RubyPayable
                            else -> SaasInkPrimary
                        }
                        Text(
                            text = netText,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = netColor
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = SaasIndigoContainer,
                        border = BorderStroke(1.dp, SaasIndigoBorder)
                    ) {
                        Text(
                            text = "${stats.receivableCount + stats.payableCount + stats.settledCount} Accounts",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSaasIndigoContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // THEY OWE US (You Will Get) — Light SaaS KPI Card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = EmeraldReceivableSoft,
                        border = BorderStroke(1.dp, EmeraldReceivableBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onReceivableClick)
                            .testTag("summary_they_owe_us_card")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(EmeraldReceivable.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = EmeraldReceivable,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "THEY OWE US",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldDarkText,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = ShopLedgerViewModel.formatMoney(stats.totalReceivableCents, currencySymbol),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = EmeraldReceivable,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "You will get • ${stats.receivableCount} ${if (stats.receivableCount == 1) "name" else "names"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldDarkText.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // WE OWE THEM (You Will Give) — Light SaaS KPI Card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = RubyPayableSoft,
                        border = BorderStroke(1.dp, RubyPayableBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onPayableClick)
                            .testTag("summary_we_owe_them_card")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(RubyPayable.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        tint = RubyPayable,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "WE OWE THEM",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RubyDarkText,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = ShopLedgerViewModel.formatMoney(stats.totalPayableCents, currencySymbol),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = RubyPayable,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "You will pay • ${stats.payableCount} ${if (stats.payableCount == 1) "name" else "names"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = RubyDarkText.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Animated SaaS Proportion Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(SaasSurfaceSubtle)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(receivableFraction)
                            .fillMaxHeight()
                            .background(EmeraldReceivable)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Box(
                        modifier = Modifier
                            .weight((1f - receivableFraction).coerceAtLeast(0.05f))
                            .fillMaxHeight()
                            .background(RubyPayable)
                    )
                }
            }
        }
    }
}

@Composable
fun PartyLedgerCard(
    party: Party,
    isSelected: Boolean,
    currencySymbol: String,
    onClick: () -> Unit,
    onQuickGave: () -> Unit,
    onQuickGot: () -> Unit,
    modifier: Modifier = Modifier
) {
    val net = party.netBalanceCents
    val isReceivable = net > 0L
    val isPayable = net < 0L

    val balanceColor by animateColorAsState(
        targetValue = when {
            isReceivable -> EmeraldReceivable
            isPayable -> RubyPayable
            else -> SaasInkMuted
        },
        animationSpec = tween(250),
        label = "BalanceColorAnim"
    )
    val statusBadgeBg = when {
        isReceivable -> EmeraldReceivableSoft
        isPayable -> RubyPayableSoft
        else -> SaasSurfaceSubtle
    }
    val statusBadgeBorder = when {
        isReceivable -> EmeraldReceivableBorder
        isPayable -> RubyPayableBorder
        else -> SaasBorder
    }
    val statusLabel = when {
        isReceivable -> "OWES YOU"
        isPayable -> "YOU OWE"
        else -> "SETTLED"
    }

    val initials = party.name.trim()
        .split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }

    val isOverdue = net != 0L && party.dueDateMillis in 1 until System.currentTimeMillis()
    val cardBorderColor by animateColorAsState(
        targetValue = if (isSelected) SaasIndigoPrimary else SaasBorder,
        animationSpec = tween(200),
        label = "PartyCardBorder"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = cardBorderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .testTag("party_card_${party.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SaasIndigoContainer.copy(alpha = 0.35f) else SaasSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SaasIndigoContainer)
                        .border(1.dp, SaasIndigoBorder, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSaasIndigoContainer
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = party.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SaasInkPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SaasSurfaceSubtle,
                            border = BorderStroke(1.dp, SaasBorder)
                        ) {
                            Text(
                                text = party.role,
                                style = MaterialTheme.typography.labelSmall,
                                color = SaasInkSecondary,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = party.lastActivitySummary.ifBlank { "No entries yet" },
                        style = MaterialTheme.typography.bodySmall,
                        color = SaasInkSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "${party.entryCount} ${if (party.entryCount == 1L) "entry" else "entries"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = SaasInkMuted,
                            maxLines = 1,
                            softWrap = false
                        )
                        if (party.dueDateMillis > 0L && net != 0L) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = if (isOverdue) RubyPayable else SaasIndigoPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isOverdue) "Overdue (${ShopLedgerViewModel.formatDateShort(party.dueDateMillis)})"
                                    else "Due ${ShopLedgerViewModel.formatDateShort(party.dueDateMillis)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isOverdue) RubyPayable else SaasIndigoPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Net Balance Column
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = ShopLedgerViewModel.formatMoney(abs(net), currencySymbol),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = balanceColor,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusBadgeBg)
                            .border(1.dp, statusBadgeBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            color = when {
                                isReceivable -> EmeraldDarkText
                                isPayable -> RubyDarkText
                                else -> SaasInkMuted
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick inline action buttons (+ Gave / + Got) and Given/Got mini-breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gave: ${ShopLedgerViewModel.formatMoney(party.totalGivenCents, currencySymbol)}  •  Got: ${ShopLedgerViewModel.formatMoney(party.totalGotCents, currencySymbol)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SaasInkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldReceivableSoft,
                        border = BorderStroke(1.dp, EmeraldReceivableBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onQuickGave)
                            .testTag("quick_gave_${party.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = EmeraldDarkText,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Gave",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDarkText
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = RubyPayableSoft,
                        border = BorderStroke(1.dp, RubyPayableBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onQuickGot)
                            .testTag("quick_got_${party.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = RubyDarkText,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Got",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = RubyDarkText
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyPartiesCard(
    hasActiveFilter: Boolean,
    onResetFilter: () -> Unit,
    onAddPartyClick: () -> Unit,
    onSeedSampleDataClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, SaasBorder, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SaasSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_empty_ledger_illustration),
                contentDescription = "Empty shop register",
                modifier = Modifier.size(130.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (hasActiveFilter) "No Matching Accounts Found" else "Your Shop Register is Empty",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = SaasInkPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (hasActiveFilter) {
                    "Try clearing your search query or switching the account filter."
                } else {
                    "Add customers and suppliers to start tracking how much they owe you, how much you owe them, and itemized ledger entries."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = SaasInkSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))

            if (hasActiveFilter) {
                OutlinedButton(
                    onClick = onResetFilter,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SaasBorder)
                ) {
                    Text("Reset Search & Filters")
                }
            } else {
                Button(
                    onClick = onAddPartyClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SaasIndigoPrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("empty_add_party_button")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add First Customer or Supplier", fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onSeedSampleDataClick,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SaasBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("seed_sample_data_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Dataset,
                        contentDescription = null,
                        tint = SaasIndigoPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Load Sample Shop Accounts", color = SaasInkPrimary)
                }
            }
        }
    }
}
