package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.LedgerEntry
import com.example.data.Party
import com.example.ui.ShopLedgerViewModel
import com.example.ui.theme.EmeraldReceivable
import com.example.ui.theme.EmeraldReceivableSoft
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RubyPayable
import com.example.ui.theme.RubyPayableSoft
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddOrEditPartyBottomSheet(
    existingParty: Party? = null,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSaveNew: (
        name: String,
        phone: String,
        role: String,
        notes: String,
        dueDateMillis: Long,
        openingAmountCents: Long,
        openingType: String,
        openingDescription: String
    ) -> Unit,
    onSaveEdit: (
        name: String,
        phone: String,
        role: String,
        notes: String,
        dueDateMillis: Long
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(existingParty?.name ?: "") }
    var phone by remember { mutableStateOf(existingParty?.phone ?: "") }
    var role by remember { mutableStateOf(existingParty?.role ?: Party.ROLE_CUSTOMER) }
    var notes by remember { mutableStateOf(existingParty?.notes ?: "") }
    var dueInDays by remember {
        mutableStateOf(
            if ((existingParty?.dueDateMillis ?: 0L) > System.currentTimeMillis()) {
                val days = ((existingParty!!.dueDateMillis - System.currentTimeMillis()) / 86_400_000L).toInt()
                days.coerceAtLeast(1)
            } else {
                0
            }
        )
    }

    var openingAmountText by remember { mutableStateOf("") }
    var openingType by remember { mutableStateOf(LedgerEntry.TYPE_GAVE) }
    var openingDesc by remember { mutableStateOf("Opening Balance") }
    var nameError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingParty == null) "Add Account to Register" else "Edit Account Details",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close dialog")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Account Type",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Party.ROLE_CUSTOMER to "Customer",
                    Party.ROLE_SUPPLIER to "Supplier",
                    Party.ROLE_BOTH to "Both"
                ).forEach { (roleKey, label) ->
                    FilterChip(
                        selected = role == roleKey,
                        onClick = {
                            role = roleKey
                            if (existingParty == null) {
                                openingType = if (roleKey == Party.ROLE_SUPPLIER) {
                                    LedgerEntry.TYPE_GOT
                                } else {
                                    LedgerEntry.TYPE_GAVE
                                }
                            }
                        },
                        label = { Text(label) },
                        modifier = Modifier.testTag("party_role_chip_${roleKey.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                label = { Text("Person or Business Name *") },
                placeholder = { Text("e.g., Malik General Store") },
                isError = nameError,
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("party_name_input")
            )
            if (nameError) {
                Text(
                    text = "Please enter a name for this account",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone / WhatsApp Number") },
                placeholder = { Text("e.g., +1 555-234-5678") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("party_phone_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes / Address / Credit Terms") },
                placeholder = { Text("e.g., Shop #14 Main Bazaar • Net 15 days") },
                maxLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("party_notes_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Payment Due / Reminder Target",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    0 to "No Due Date",
                    3 to "In 3 Days",
                    7 to "In 7 Days",
                    15 to "In 15 Days",
                    30 to "In 30 Days"
                ).forEach { (days, label) ->
                    FilterChip(
                        selected = dueInDays == days,
                        onClick = { dueInDays = days },
                        label = { Text(label) }
                    )
                }
            }

            if (existingParty == null) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Opening Balance (Optional)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Set an existing balance if they already owe you or you owe them.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val isGave = openingType == LedgerEntry.TYPE_GAVE
                    DirectionSelectCard(
                        selected = isGave,
                        title = "You Gave (Credit Out)",
                        subtitle = "They owe us",
                        accentColor = EmeraldReceivable,
                        softBg = EmeraldReceivableSoft,
                        onClick = { openingType = LedgerEntry.TYPE_GAVE },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("opening_type_gave")
                    )
                    DirectionSelectCard(
                        selected = !isGave,
                        title = "You Got (Credit In)",
                        subtitle = "We owe them",
                        accentColor = RubyPayable,
                        softBg = RubyPayableSoft,
                        onClick = { openingType = LedgerEntry.TYPE_GOT },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("opening_type_got")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = openingAmountText,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) {
                                openingAmountText = input
                            }
                        },
                        label = { Text("Opening Amount ($currencySymbol)") },
                        placeholder = { Text("0.00") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("opening_amount_input")
                    )
                    OutlinedTextField(
                        value = openingDesc,
                        onValueChange = { openingDesc = it },
                        label = { Text("Item / Particulars") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("opening_description_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    val dueMillis = if (dueInDays > 0) {
                        System.currentTimeMillis() + dueInDays * 86_400_000L
                    } else {
                        0L
                    }
                    if (existingParty == null) {
                        val parsedCents = ((openingAmountText.toDoubleOrNull() ?: 0.0) * 100.0).roundToLong()
                        onSaveNew(
                            name,
                            phone,
                            role,
                            notes,
                            dueMillis,
                            parsedCents,
                            openingType,
                            openingDesc
                        )
                    } else {
                        onSaveEdit(name, phone, role, notes, dueMillis)
                    }
                    onDismiss()
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_party_button")
            ) {
                Text(
                    text = if (existingParty == null) "Save Account to Register" else "Update Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DirectionSelectCard(
    selected: Boolean,
    title: String,
    subtitle: String,
    accentColor: Color,
    softBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val bgColor = if (selected) softBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(width = if (selected) 2.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (accentColor == EmeraldReceivable) Icons.Default.CallMade else Icons.Default.CallReceived,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) accentColor else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddLedgerEntryBottomSheet(
    party: Party,
    initialType: String,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSaveEntry: (
        type: String,
        amountCents: Long,
        description: String,
        category: String,
        paymentMode: String,
        referenceNote: String,
        entryDateMillis: Long
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var entryType by remember { mutableStateOf(initialType) }
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember {
        mutableStateOf(
            if (initialType == LedgerEntry.TYPE_GAVE) "Goods Sold" else "Cash Received"
        )
    }
    var paymentMode by remember {
        mutableStateOf(
            if (initialType == LedgerEntry.TYPE_GAVE) LedgerEntry.MODE_CREDIT else LedgerEntry.MODE_CASH
        )
    }
    var referenceNote by remember { mutableStateOf("") }
    var dateOffsetDays by remember { mutableLongStateOf(0L) }
    var amountError by remember { mutableStateOf(false) }

    val isGave = entryType == LedgerEntry.TYPE_GAVE
    val accentColor = if (isGave) EmeraldReceivable else RubyPayable

    val categories = if (isGave) {
        listOf("Goods Sold", "Credit Issued", "Bill Payment", "Supplier Advance", "Cash Given")
    } else {
        listOf("Cash Received", "Stock Purchase", "Part Settlement", "Customer Advance", "Goods Return")
    }

    val paymentModes = listOf(
        LedgerEntry.MODE_CASH to "Cash",
        LedgerEntry.MODE_CREDIT to "On Credit",
        LedgerEntry.MODE_UPI_WALLET to "UPI / Wallet",
        LedgerEntry.MODE_BANK_TRANSFER to "Bank Transfer",
        LedgerEntry.MODE_CHEQUE to "Cheque"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isGave) "Record You Gave (Out)" else "Record You Got (In)",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Text(
                        text = "Account: ${party.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close entry sheet")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Toggle between You Gave and You Got
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DirectionSelectCard(
                    selected = isGave,
                    title = "You Gave",
                    subtitle = "Sold on credit / Paid out",
                    accentColor = EmeraldReceivable,
                    softBg = EmeraldReceivableSoft,
                    onClick = {
                        entryType = LedgerEntry.TYPE_GAVE
                        category = "Goods Sold"
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sheet_entry_type_gave")
                )
                DirectionSelectCard(
                    selected = !isGave,
                    title = "You Got",
                    subtitle = "Received cash / Bought stock",
                    accentColor = RubyPayable,
                    softBg = RubyPayableSoft,
                    onClick = {
                        entryType = LedgerEntry.TYPE_GOT
                        category = "Cash Received"
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sheet_entry_type_got")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    if (input.isEmpty() || input.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) {
                        amountText = input
                        amountError = false
                    }
                },
                label = { Text("Amount ($currencySymbol) *") },
                placeholder = { Text("0.00") },
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                isError = amountError,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("entry_amount_input")
            )

            // Quick amount chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(50, 100, 250, 500, 1000).forEach { quickAmt ->
                    SuggestionChip(
                        onClick = {
                            val current = amountText.toDoubleOrNull() ?: 0.0
                            amountText = String.format(Locale.US, "%.2f", current + quickAmt)
                            amountError = false
                        },
                        label = { Text("+$currencySymbol$quickAmt") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Itemized Details / Particulars *") },
                placeholder = {
                    Text(
                        if (isGave) "e.g., 5 bags Flour, 10L Cooking Oil, Sugar"
                        else "e.g., Cash payment received / Wholesale Invoice #42"
                    )
                },
                maxLines = 3,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("entry_description_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Category",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Payment / Credit Mode",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                paymentModes.forEach { (modeKey, modeLabel) ->
                    FilterChip(
                        selected = paymentMode == modeKey,
                        onClick = { paymentMode = modeKey },
                        label = { Text(modeLabel) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = referenceNote,
                    onValueChange = { referenceNote = it },
                    label = { Text("Bill / Receipt No. (Optional)") },
                    placeholder = { Text("e.g., INV-108") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("entry_reference_input")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Entry Date",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    0L to "Today",
                    1L to "Yesterday",
                    2L to "2 Days Ago"
                ).forEach { (daysAgo, label) ->
                    FilterChip(
                        selected = dateOffsetDays == daysAgo,
                        onClick = { dateOffsetDays = daysAgo },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val cents = ((amountText.toDoubleOrNull() ?: 0.0) * 100.0).roundToLong()
                    if (cents <= 0L) {
                        amountError = true
                        return@Button
                    }
                    val finalDesc = description.trim().ifBlank {
                        "$category (${paymentModes.firstOrNull { it.first == paymentMode }?.second ?: paymentMode})"
                    }
                    val entryMillis = System.currentTimeMillis() - (dateOffsetDays * 86_400_000L)
                    onSaveEntry(
                        entryType,
                        cents,
                        finalDesc,
                        category,
                        paymentMode,
                        referenceNote,
                        entryMillis
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_entry_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isGave) "Save You Gave Entry" else "Save You Got Entry",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PartyStatementDialog(
    party: Party,
    entries: List<LedgerEntry>,
    currencySymbol: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val statementText = remember(party, entries, currencySymbol) {
        buildString {
            appendLine("=== SHOP LEDGER STATEMENT ===")
            appendLine("Account: ${party.name} (${party.role})")
            if (party.phone.isNotBlank()) appendLine("Phone: ${party.phone}")
            appendLine("Date: ${ShopLedgerViewModel.formatDateShort(System.currentTimeMillis())}")
            appendLine("-----------------------------")
            appendLine("Total You Gave (Credit Out): ${ShopLedgerViewModel.formatMoney(party.totalGivenCents, currencySymbol)}")
            appendLine("Total You Got (Payments/In): ${ShopLedgerViewModel.formatMoney(party.totalGotCents, currencySymbol)}")
            val netLabel = when {
                party.netBalanceCents > 0L -> "NET RECEIVABLE (${party.name} Owes Us): ${ShopLedgerViewModel.formatMoney(party.netBalanceCents, currencySymbol)}"
                party.netBalanceCents < 0L -> "NET PAYABLE (We Owe ${party.name}): ${ShopLedgerViewModel.formatMoney(abs(party.netBalanceCents), currencySymbol)}"
                else -> "NET BALANCE: SETTLED (${currencySymbol}0.00)"
            }
            appendLine(netLabel)
            appendLine("-----------------------------")
            appendLine("Recent Entries (${entries.size} total):")
            entries.sortedByDescending { it.entryDateMillis }.take(15).forEach { e ->
                val sign = if (e.type == LedgerEntry.TYPE_GAVE) "[GAVE]" else "[GOT]"
                val dateStr = ShopLedgerViewModel.formatDateShort(e.entryDateMillis)
                val amtStr = ShopLedgerViewModel.formatMoney(e.amountCents, currencySymbol)
                appendLine("• $dateStr $sign $amtStr - ${e.description} (${e.paymentMode})")
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Account Statement & Reminder",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = statementText,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (party.phone.isNotBlank()) {
                        OutlinedButton(
                            onClick = {
                                val smsBody = when {
                                    party.netBalanceCents > 0L ->
                                        "Hello ${party.name}, a friendly reminder from our Shop Register: your pending balance is ${ShopLedgerViewModel.formatMoney(party.netBalanceCents, currencySymbol)}. Thank you!"
                                    party.netBalanceCents < 0L ->
                                        "Hello ${party.name}, our Shop Register balance payable to you is ${ShopLedgerViewModel.formatMoney(abs(party.netBalanceCents), currencySymbol)}."
                                    else ->
                                        "Hello ${party.name}, your account balance with us is settled (${currencySymbol}0.00)."
                                }
                                val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("smsto:${party.phone}")
                                    putExtra("sms_body", smsBody)
                                }
                                runCatching { context.startActivity(smsIntent) }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SMS")
                        }
                    }

                    Button(
                        onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Statement for ${party.name}")
                                putExtra(Intent.EXTRA_TEXT, statementText)
                            }
                            runCatching {
                                context.startActivity(Intent.createChooser(sendIntent, "Share Ledger Statement"))
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Statement")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
