package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.LedgerEntry
import com.example.data.Party
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
import com.example.ui.theme.SaasBackground
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

data class LedgerEntryWithRunningBalance(
    val entry: LedgerEntry,
    val runningBalanceCents: Long
)

@Composable
fun PartyDetailScreen(
    party: Party,
    partyEntries: List<LedgerEntry>,
    currencySymbol: String,
    showBackButton: Boolean,
    onBack: () -> Unit,
    onAddEntryClick: (String) -> Unit,
    onEditPartyClick: () -> Unit,
    onDeletePartyClick: () -> Unit,
    onSettleBalanceClick: (String) -> Unit,
    onDeleteEntryClick: (LedgerEntry) -> Unit,
    onShareStatementClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (showBackButton) {
        BackHandler(onBack = onBack)
    }

    val context = LocalContext.current
    var entryPendingDelete by remember { mutableStateOf<LedgerEntry?>(null) }
    var showDeletePartyConfirm by remember { mutableStateOf(false) }
    var showSettleConfirm by remember { mutableStateOf(false) }

    val entriesWithRunningBalance = remember(partyEntries) {
        val chronological = partyEntries.sortedWith(
            compareBy<LedgerEntry> { it.entryDateMillis }
                .thenBy { (it.createdAt?.toDate()?.time) ?: 0L }
        )
        var running = 0L
        val computed = chronological.map { entry ->
            if (entry.type == LedgerEntry.TYPE_GAVE) {
                running += entry.amountCents
            } else {
                running -= entry.amountCents
            }
            LedgerEntryWithRunningBalance(entry = entry, runningBalanceCents = running)
        }
        computed.reversed()
    }

    val net = party.netBalanceCents
    val isReceivable = net > 0L
    val isPayable = net < 0L
    val animatedNetColor by animateColorAsState(
        targetValue = when {
            isReceivable -> EmeraldReceivable
            isPayable -> RubyPayable
            else -> SaasInkPrimary
        },
        animationSpec = tween(250),
        label = "DetailNetBalanceColor"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SaasBackground)
            .testTag("party_detail_screen")
    ) {
        // Light SaaS Navigation & Summary Header
        Surface(
            color = SaasSurface,
            contentColor = SaasInkPrimary,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showBackButton) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("party_detail_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to accounts",
                                tint = SaasInkPrimary
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = party.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = SaasInkPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SaasIndigoContainer,
                                border = BorderStroke(1.dp, SaasIndigoBorder)
                            ) {
                                Text(
                                    text = party.role,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OnSaasIndigoContainer,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (party.phone.isNotBlank() || party.notes.isNotBlank()) {
                            Text(
                                text = listOf(party.phone, party.notes).filter { it.isNotBlank() }.joinToString(" • "),
                                style = MaterialTheme.typography.bodySmall,
                                color = SaasInkSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (party.phone.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${party.phone}"))
                                runCatching { context.startActivity(dialIntent) }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call ${party.name}",
                                tint = SaasIndigoPrimary
                            )
                        }
                    }

                    IconButton(
                        onClick = onShareStatementClick,
                        modifier = Modifier.testTag("share_statement_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share statement",
                            tint = SaasInkSecondary
                        )
                    }

                    IconButton(
                        onClick = onEditPartyClick,
                        modifier = Modifier.testTag("edit_party_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit account",
                            tint = SaasInkSecondary
                        )
                    }

                    IconButton(
                        onClick = { showDeletePartyConfirm = true },
                        modifier = Modifier.testTag("delete_party_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete account",
                            tint = RubyPayable
                        )
                    }
                }

                // 3-Column Light SaaS Balance Card inside Header
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SaasBackground,
                    border = BorderStroke(1.dp, SaasBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "YOU GAVE (OUT)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = EmeraldDarkText,
                                    maxLines = 1
                                )
                                Text(
                                    text = ShopLedgerViewModel.formatMoney(party.totalGivenCents, currencySymbol),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = EmeraldReceivable,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "YOU GOT (IN)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RubyDarkText,
                                    maxLines = 1
                                )
                                Text(
                                    text = ShopLedgerViewModel.formatMoney(party.totalGotCents, currencySymbol),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = RubyPayable,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1.2f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = when {
                                        isReceivable -> "THEY OWE YOU"
                                        isPayable -> "YOU OWE THEM"
                                        else -> "SETTLED"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    color = when {
                                        isReceivable -> EmeraldDarkText
                                        isPayable -> RubyDarkText
                                        else -> SaasInkMuted
                                    }
                                )
                                Text(
                                    text = ShopLedgerViewModel.formatMoney(abs(net), currencySymbol),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = animatedNetColor
                                )
                            }
                        }

                        if (net != 0L) {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = SaasBorder)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isReceivable) {
                                        "${party.name} owes you ${ShopLedgerViewModel.formatMoney(net, currencySymbol)}"
                                    } else {
                                        "You owe ${party.name} ${ShopLedgerViewModel.formatMoney(abs(net), currencySymbol)}"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SaasInkSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                )

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SaasIndigoContainer,
                                    border = BorderStroke(1.dp, SaasIndigoBorder),
                                    onClick = { showSettleConfirm = true },
                                    modifier = Modifier.testTag("settle_up_button")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DoneAll,
                                            contentDescription = null,
                                            tint = SaasIndigoPrimary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "Settle Balance",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = OnSaasIndigoContainer,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = SaasBorder)
            }
        }

        // Column Headers for SaaS Ledger Table
        Surface(
            color = SaasSurfaceSubtle,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ENTRIES (${entriesWithRunningBalance.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaasInkMuted,
                        modifier = Modifier.weight(1.75f)
                    )
                    Text(
                        text = "YOU GAVE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDarkText,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "YOU GOT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = RubyDarkText,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }
                HorizontalDivider(color = SaasBorder)
            }
        }

        // Ledger Entries List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("party_entries_list"),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (entriesWithRunningBalance.isEmpty()) {
                item(key = "empty_entries") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp)
                            .border(1.dp, SaasBorder, RoundedCornerShape(18.dp))
                            .animateItem(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SaasSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
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
                                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                    contentDescription = null,
                                    tint = SaasIndigoPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No Entries Recorded for ${party.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SaasInkPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap 'You Gave' below when you give credit or goods, or 'You Got' when you receive payment or goods.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SaasInkSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(entriesWithRunningBalance, key = { it.entry.id }) { item ->
                    PartyEntryRowCard(
                        item = item,
                        currencySymbol = currencySymbol,
                        onDeleteClick = { entryPendingDelete = item.entry },
                        modifier = Modifier.animateItem(
                            fadeInSpec = tween(200),
                            fadeOutSpec = tween(150),
                            placementSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                    )
                }
            }
        }

        // Sticky Dual Action Bar at Bottom: YOU GAVE vs YOU GOT
        Surface(
            color = SaasSurface,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                HorizontalDivider(color = SaasBorder)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onAddEntryClick(LedgerEntry.TYPE_GAVE) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldReceivable,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("detail_you_gave_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CallMade,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "YOU GAVE",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "Credit / Paid Out",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.88f),
                                maxLines = 1
                            )
                        }
                    }

                    Button(
                        onClick = { onAddEntryClick(LedgerEntry.TYPE_GOT) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RubyPayable,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("detail_you_got_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CallReceived,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "YOU GOT",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "Received / Bought",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.88f),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }

    if (entryPendingDelete != null) {
        val target = entryPendingDelete!!
        AlertDialog(
            onDismissRequest = { entryPendingDelete = null },
            title = { Text("Delete Ledger Entry?") },
            text = {
                Text(
                    "Remove this ${ShopLedgerViewModel.formatMoney(target.amountCents, currencySymbol)} (${target.description}) entry? ${party.name}'s net balance will be automatically recalculated."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteEntryClick(target)
                        entryPendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Entry")
                }
            },
            dismissButton = {
                TextButton(onClick = { entryPendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeletePartyConfirm) {
        AlertDialog(
            onDismissRequest = { showDeletePartyConfirm = false },
            title = { Text("Delete ${party.name}?") },
            text = {
                Text("This will permanently remove ${party.name} and all ${partyEntries.size} ledger entries from your shop register.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeletePartyConfirm = false
                        onDeletePartyClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeletePartyConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSettleConfirm) {
        val absBal = ShopLedgerViewModel.formatMoney(abs(party.netBalanceCents), currencySymbol)
        AlertDialog(
            onDismissRequest = { showSettleConfirm = false },
            title = { Text("Settle Account Balance") },
            text = {
                Text(
                    if (party.netBalanceCents > 0L) {
                        "Record a full payment received of $absBal from ${party.name} to mark this account as Settled?"
                    } else {
                        "Record a full payment given of $absBal to ${party.name} to mark this account as Settled?"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSettleConfirm = false
                        onSettleBalanceClick(LedgerEntry.MODE_CASH)
                    }
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Settle in Cash")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showSettleConfirm = false
                        onSettleBalanceClick(LedgerEntry.MODE_BANK_TRANSFER)
                    }
                ) {
                    Text("Settle via Bank/UPI")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PartyEntryRowCard(
    item: LedgerEntryWithRunningBalance,
    currencySymbol: String,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entry = item.entry
    val isGave = entry.type == LedgerEntry.TYPE_GAVE
    val runBal = item.runningBalanceCents

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, SaasBorder, RoundedCornerShape(14.dp))
            .testTag("entry_row_${entry.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SaasSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Details Column (aligned with ENTRIES header weight 1.75f)
            Column(
                modifier = Modifier
                    .weight(1.75f)
                    .padding(end = 6.dp)
            ) {
                Text(
                    text = entry.description,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = SaasInkPrimary
                )

                Spacer(modifier = Modifier.height(5.dp))

                // Use FlowRow so tags wrap as complete pills and never squeeze character-by-character
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SaasSurfaceSubtle,
                        border = BorderStroke(1.dp, SaasBorder)
                    ) {
                        Text(
                            text = entry.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = SaasInkSecondary,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SaasIndigoContainer,
                        border = BorderStroke(1.dp, SaasIndigoBorder)
                    ) {
                        Text(
                            text = entry.paymentMode.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSaasIndigoContainer,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (entry.referenceNote.isNotBlank()) {
                        Text(
                            text = "#${entry.referenceNote}",
                            style = MaterialTheme.typography.labelSmall,
                            color = SaasIndigoPrimary,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Date on its own horizontal line
                Text(
                    text = ShopLedgerViewModel.formatDateTime(entry.entryDateMillis),
                    style = MaterialTheme.typography.bodySmall,
                    color = SaasInkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                // Running balance + compact delete action on its own dedicated line
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Bal: ${ShopLedgerViewModel.formatMoney(abs(runBal), currencySymbol)} ${
                            when {
                                runBal > 0L -> "(Owes You)"
                                runBal < 0L -> "(You Owe)"
                                else -> "(Settled)"
                            }
                        }",
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            runBal > 0L -> EmeraldReceivable
                            runBal < 0L -> RubyPayable
                            else -> SaasInkMuted
                        },
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onDeleteClick)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete entry",
                            tint = SaasInkMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // YOU GAVE Column (aligned with header weight 1f)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (isGave) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldReceivableSoft)
                            .border(1.dp, EmeraldReceivableBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 7.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = ShopLedgerViewModel.formatMoney(entry.amountCents, currencySymbol),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDarkText,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                } else {
                    Text(
                        text = "—",
                        style = MaterialTheme.typography.bodySmall,
                        color = SaasInkMuted
                    )
                }
            }

            // YOU GOT Column (aligned with header weight 1f)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 2.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (!isGave) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(RubyPayableSoft)
                            .border(1.dp, RubyPayableBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 7.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = ShopLedgerViewModel.formatMoney(entry.amountCents, currencySymbol),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = RubyDarkText,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                } else {
                    Text(
                        text = "—",
                        style = MaterialTheme.typography.bodySmall,
                        color = SaasInkMuted
                    )
                }
            }
        }
    }
}
