package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
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
import com.example.ui.theme.RubyDarkText
import com.example.ui.theme.RubyPayable
import com.example.ui.theme.RubyPayableBorder
import com.example.ui.theme.RubyPayableSoft
import com.example.ui.theme.SaasBackground
import com.example.ui.theme.SaasBorder
import com.example.ui.theme.SaasIndigoPrimary
import com.example.ui.theme.SaasInkMuted
import com.example.ui.theme.SaasInkPrimary
import com.example.ui.theme.SaasInkSecondary
import com.example.ui.theme.SaasSurface
import com.example.ui.theme.SaasSurfaceSubtle
import kotlin.math.abs
import kotlin.math.roundToLong

@Composable
fun PersonalPartyDetailScreen(
    party: Party,
    partyEntries: List<LedgerEntry>,
    currencySymbol: String,
    showBackButton: Boolean,
    onBack: () -> Unit,
    onAddEntryClick: (String) -> Unit,
    onDeletePartyClick: () -> Unit,
    onDeleteEntryClick: (LedgerEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    if (showBackButton) {
        BackHandler(onBack = onBack)
    }

    var entryPendingDelete by remember { mutableStateOf<LedgerEntry?>(null) }
    var showDeletePartyConfirm by remember { mutableStateOf(false) }

    val sortedEntries = remember(partyEntries) {
        partyEntries.sortedWith(
            compareByDescending<LedgerEntry> { it.entryDateMillis }
                .thenByDescending { (it.createdAt?.toDate()?.time) ?: 0L }
        )
    }

    val net = party.netBalanceCents
    val someoneOwesYou = net > 0L
    val youOweSomeone = net < 0L

    val headerCardBg = when {
        someoneOwesYou -> EmeraldReceivableSoft
        youOweSomeone -> RubyPayableSoft
        else -> SaasSurfaceSubtle
    }
    val headerCardBorder = when {
        someoneOwesYou -> EmeraldReceivableBorder
        youOweSomeone -> RubyPayableBorder
        else -> SaasBorder
    }
    val headerAmountColor = when {
        someoneOwesYou -> EmeraldReceivable
        youOweSomeone -> RubyPayable
        else -> SaasInkPrimary
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SaasBackground)
            .testTag("personal_party_detail_screen")
    ) {
        // Light SaaS Glass Header
        Surface(
            color = SaasSurface,
            contentColor = SaasInkPrimary,
            tonalElevation = 0.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
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
                                contentDescription = "Back",
                                tint = SaasInkPrimary
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = party.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SaasInkPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { showDeletePartyConfirm = true },
                        modifier = Modifier.testTag("delete_party_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete name",
                            tint = RubyPayable
                        )
                    }
                }

                // SaaS Glass Balance Box for this Person
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = headerCardBg,
                    border = BorderStroke(1.5.dp, headerCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        SaasSurface.copy(alpha = 0.65f),
                                        headerCardBg
                                    )
                                )
                            )
                            .padding(vertical = 14.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ShopLedgerViewModel.formatMoney(abs(net), currencySymbol),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = headerAmountColor
                        )
                    }
                }

                HorizontalDivider(color = SaasBorder)
            }
        }

        // Simple Entries List (Only Reason + Amount)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("party_entries_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (sortedEntries.isEmpty()) {
                item(key = "empty_personal_entries") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp)
                            .border(1.dp, SaasBorder, RoundedCornerShape(18.dp)),
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
                            Text(
                                text = "No Entries Yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SaasInkPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Use the buttons below to add an amount and reason.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SaasInkSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(sortedEntries, key = { it.id }) { entry ->
                    PersonalSimpleEntryRow(
                        entry = entry,
                        currencySymbol = currencySymbol,
                        onDeleteClick = { entryPendingDelete = entry },
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

        // Bottom Bar: Simple Gave (Green) & Took (Red) Buttons
        Surface(
            color = SaasSurface,
            tonalElevation = 0.dp,
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
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("detail_you_gave_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CallMade,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Gave",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { onAddEntryClick(LedgerEntry.TYPE_GOT) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RubyPayable,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("detail_you_got_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CallReceived,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Took",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (entryPendingDelete != null) {
        val target = entryPendingDelete!!
        AlertDialog(
            onDismissRequest = { entryPendingDelete = null },
            title = { Text("Delete Entry?") },
            text = {
                Text(
                    "Remove ${ShopLedgerViewModel.formatMoney(target.amountCents, currencySymbol)} (${target.description})?"
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
                    Text("Delete")
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
                Text("Remove ${party.name} and all entries?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeletePartyConfirm = false
                        onDeletePartyClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeletePartyConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PersonalSimpleEntryRow(
    entry: LedgerEntry,
    currencySymbol: String,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isGave = entry.type == LedgerEntry.TYPE_GAVE
    val cardBg = if (isGave) EmeraldReceivableSoft else RubyPayableSoft
    val cardBorder = if (isGave) EmeraldReceivableBorder else RubyPayableBorder
    val reasonColor = if (isGave) EmeraldDarkText else RubyDarkText
    val amountColor = if (isGave) EmeraldReceivable else RubyPayable

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("entry_row_${entry.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SaasSurface.copy(alpha = 0.65f),
                            cardBg
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = entry.description.ifBlank { "No reason added" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = reasonColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = ShopLedgerViewModel.formatDateShort(entry.entryDateMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = reasonColor.copy(alpha = 0.7f)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = ShopLedgerViewModel.formatMoney(entry.amountCents, currencySymbol),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = amountColor,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete entry",
                        tint = reasonColor.copy(alpha = 0.65f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimplePersonalAddNameSheet(
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        amountCents: Long,
        type: String,
        reason: String
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var entryType by remember { mutableStateOf(LedgerEntry.TYPE_GAVE) }
    var nameError by remember { mutableStateOf(false) }

    val isGave = entryType == LedgerEntry.TYPE_GAVE

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SaasSurface
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
                    text = "New Entry",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = SaasInkPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = SaasInkSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Simple 2-Button Toggle: Gave vs Took
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SimpleDirectionPill(
                    selected = isGave,
                    label = "Gave Money",
                    accentColor = EmeraldReceivable,
                    softBg = EmeraldReceivableSoft,
                    borderColor = EmeraldReceivableBorder,
                    onClick = { entryType = LedgerEntry.TYPE_GAVE },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("opening_type_gave")
                )
                SimpleDirectionPill(
                    selected = !isGave,
                    label = "Took Money",
                    accentColor = RubyPayable,
                    softBg = RubyPayableSoft,
                    borderColor = RubyPayableBorder,
                    onClick = { entryType = LedgerEntry.TYPE_GOT },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("opening_type_got")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                label = { Text("Name") },
                placeholder = { Text("Person's name") },
                isError = nameError,
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SaasSurface,
                    unfocusedContainerColor = SaasSurface,
                    focusedBorderColor = SaasIndigoPrimary,
                    unfocusedBorderColor = SaasBorder
                ),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("party_name_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    if (input.isEmpty() || input.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) {
                        amountText = input
                    }
                },
                label = { Text("Amount ($currencySymbol)") },
                placeholder = { Text("0.00") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SaasSurface,
                    unfocusedContainerColor = SaasSurface,
                    focusedBorderColor = if (isGave) EmeraldReceivable else RubyPayable,
                    unfocusedBorderColor = SaasBorder
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("opening_amount_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Reason") },
                placeholder = { Text("Reason for giving or taking money") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SaasSurface,
                    unfocusedContainerColor = SaasSurface,
                    focusedBorderColor = SaasIndigoPrimary,
                    unfocusedBorderColor = SaasBorder
                ),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("opening_description_input")
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    val parsedCents = ((amountText.toDoubleOrNull() ?: 0.0) * 100.0).roundToLong()
                    val finalReason = reason.trim().ifBlank {
                        if (isGave) "Money given" else "Money taken"
                    }
                    onSave(name.trim(), parsedCents, entryType, finalReason)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGave) EmeraldReceivable else RubyPayable,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_party_button")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimplePersonalEntrySheet(
    party: Party,
    initialType: String,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSaveEntry: (
        type: String,
        amountCents: Long,
        reason: String
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var entryType by remember { mutableStateOf(initialType) }
    var amountText by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf(false) }

    val isGave = entryType == LedgerEntry.TYPE_GAVE
    val accentColor = if (isGave) EmeraldReceivable else RubyPayable

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SaasSurface
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
                        text = party.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = SaasInkPrimary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = SaasInkSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SimpleDirectionPill(
                    selected = isGave,
                    label = "Gave Money",
                    accentColor = EmeraldReceivable,
                    softBg = EmeraldReceivableSoft,
                    borderColor = EmeraldReceivableBorder,
                    onClick = { entryType = LedgerEntry.TYPE_GAVE },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sheet_entry_type_gave")
                )
                SimpleDirectionPill(
                    selected = !isGave,
                    label = "Took Money",
                    accentColor = RubyPayable,
                    softBg = RubyPayableSoft,
                    borderColor = RubyPayableBorder,
                    onClick = { entryType = LedgerEntry.TYPE_GOT },
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
                label = { Text("Amount ($currencySymbol)") },
                placeholder = { Text("0.00") },
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                isError = amountError,
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SaasSurface,
                    unfocusedContainerColor = SaasSurface,
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = SaasBorder
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("entry_amount_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Reason") },
                placeholder = { Text("Reason for giving or taking money") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SaasSurface,
                    unfocusedContainerColor = SaasSurface,
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = SaasBorder
                ),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("entry_description_input")
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val cents = ((amountText.toDoubleOrNull() ?: 0.0) * 100.0).roundToLong()
                    if (cents <= 0L) {
                        amountError = true
                        return@Button
                    }
                    val finalReason = reason.trim().ifBlank {
                        if (isGave) "Money given" else "Money taken"
                    }
                    onSaveEntry(entryType, cents, finalReason)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_entry_button")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SimpleDirectionPill(
    selected: Boolean,
    label: String,
    accentColor: Color,
    softBg: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        targetValue = if (selected) softBg else SaasSurfaceSubtle,
        animationSpec = tween(180),
        label = "SimplePillBg"
    )
    val border by animateColorAsState(
        targetValue = if (selected) borderColor else SaasBorder,
        animationSpec = tween(180),
        label = "SimplePillBorder"
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bg,
        border = BorderStroke(if (selected) 2.dp else 1.dp, border),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (selected) accentColor else SaasInkSecondary
            )
        }
    }
}
