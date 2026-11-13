package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LedgerEntry
import com.example.data.LocalShopLedgerStore
import com.example.data.Party
import com.example.data.ShopLedgerRepository
import com.example.data.ShopSummaryStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class ShopTab {
    PARTIES,
    REGISTER,
    ANALYTICS
}

enum class PartyFilter(val label: String) {
    ALL("All Accounts"),
    THEY_OWE_US("They Owe Us"),
    WE_OWE_THEM("We Owe Them"),
    CUSTOMERS("Customers"),
    SUPPLIERS("Suppliers"),
    SETTLED("Settled")
}

enum class PartySortOrder(val label: String) {
    RECENT_ACTIVITY("Recent Activity"),
    HIGHEST_RECEIVABLE("Highest Receivable"),
    HIGHEST_PAYABLE("Highest Payable"),
    NAME_ASC("Name (A–Z)")
}

enum class RegisterFilter(val label: String) {
    ALL("All Entries"),
    GAVE_ONLY("You Gave (Out)"),
    GOT_ONLY("You Got (In)"),
    TODAY("Today"),
    LAST_7_DAYS("Last 7 Days")
}

class ShopLedgerViewModel(
    private val repository: ShopLedgerRepository,
    private val currentUserId: String,
    private val isPersonalLedger: Boolean = false,
    initialCurrencySymbol: String = "$"
) : ViewModel() {

    val partiesState: StateFlow<UiState<List<Party>>> = repository.observeParties(currentUserId)
        .map<List<Party>, UiState<List<Party>>> { list ->
            val scoped = list.filter { party ->
                val isPersonal = party.notes.startsWith(LocalShopLedgerStore.PERSONAL_LEDGER_MARKER)
                isPersonal == isPersonalLedger
            }
            UiState.Success(scoped)
        }
        .catch { error ->
            Log.w(TAG, "Error observing parties", error)
            emit(UiState.Error(error.localizedMessage ?: "Failed to load accounts"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val allEntriesState: StateFlow<UiState<List<LedgerEntry>>> = repository.observeAllEntries(currentUserId)
        .map<List<LedgerEntry>, UiState<List<LedgerEntry>>> { list ->
            val scoped = list.filter { entry ->
                val isPersonal = entry.referenceNote.startsWith(LocalShopLedgerStore.PERSONAL_LEDGER_MARKER)
                isPersonal == isPersonalLedger
            }
            val sorted = scoped.sortedByDescending { it.entryDateMillis }
            UiState.Success(sorted)
        }
        .catch { error ->
            Log.w(TAG, "Error observing entries", error)
            emit(UiState.Error(error.localizedMessage ?: "Failed to load register entries"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    private val _selectedTab = MutableStateFlow(ShopTab.PARTIES)
    val selectedTab: StateFlow<ShopTab> = _selectedTab.asStateFlow()

    private val _selectedPartyId = MutableStateFlow<String?>(null)
    val selectedPartyId: StateFlow<String?> = _selectedPartyId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _partyFilter = MutableStateFlow(PartyFilter.ALL)
    val partyFilter: StateFlow<PartyFilter> = _partyFilter.asStateFlow()

    private val _partySortOrder = MutableStateFlow(PartySortOrder.RECENT_ACTIVITY)
    val partySortOrder: StateFlow<PartySortOrder> = _partySortOrder.asStateFlow()

    private val _registerFilter = MutableStateFlow(RegisterFilter.ALL)
    val registerFilter: StateFlow<RegisterFilter> = _registerFilter.asStateFlow()

    private val _registerSearchQuery = MutableStateFlow("")
    val registerSearchQuery: StateFlow<String> = _registerSearchQuery.asStateFlow()

    private val _currencySymbol = MutableStateFlow(initialCurrencySymbol)
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    fun selectTab(tab: ShopTab) {
        _selectedTab.value = tab
    }

    fun selectParty(partyId: String?) {
        _selectedPartyId.value = partyId
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updatePartyFilter(filter: PartyFilter) {
        _partyFilter.value = filter
    }

    fun updatePartySortOrder(order: PartySortOrder) {
        _partySortOrder.value = order
    }

    fun updateRegisterFilter(filter: RegisterFilter) {
        _registerFilter.value = filter
    }

    fun updateRegisterSearchQuery(query: String) {
        _registerSearchQuery.value = query
    }

    fun updateCurrencySymbol(symbol: String) {
        _currencySymbol.value = symbol
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun computeSummaryStats(parties: List<Party>): ShopSummaryStats {
        var totalReceivable = 0L
        var totalPayable = 0L
        var receivableCount = 0
        var payableCount = 0
        var settledCount = 0
        var overdueCount = 0
        val now = System.currentTimeMillis()

        for (p in parties) {
            when {
                p.netBalanceCents > 0L -> {
                    totalReceivable += p.netBalanceCents
                    receivableCount++
                }
                p.netBalanceCents < 0L -> {
                    totalPayable += abs(p.netBalanceCents)
                    payableCount++
                }
                else -> {
                    settledCount++
                }
            }
            if (p.netBalanceCents != 0L && p.dueDateMillis in 1 until now) {
                overdueCount++
            }
        }
        return ShopSummaryStats(
            totalReceivableCents = totalReceivable,
            totalPayableCents = totalPayable,
            netPositionCents = totalReceivable - totalPayable,
            receivableCount = receivableCount,
            payableCount = payableCount,
            settledCount = settledCount,
            overdueCount = overdueCount
        )
    }

    fun filterAndSortParties(
        parties: List<Party>,
        query: String,
        filter: PartyFilter,
        sortOrder: PartySortOrder
    ): List<Party> {
        val trimmed = query.trim().lowercase(Locale.getDefault())
        val filtered = parties.filter { party ->
            val matchesQuery = trimmed.isEmpty() ||
                party.name.lowercase(Locale.getDefault()).contains(trimmed) ||
                party.phone.lowercase(Locale.getDefault()).contains(trimmed) ||
                party.notes.lowercase(Locale.getDefault()).contains(trimmed)

            val matchesFilter = when (filter) {
                PartyFilter.ALL -> true
                PartyFilter.THEY_OWE_US -> party.netBalanceCents > 0L
                PartyFilter.WE_OWE_THEM -> party.netBalanceCents < 0L
                PartyFilter.CUSTOMERS -> party.role == Party.ROLE_CUSTOMER || party.role == Party.ROLE_BOTH
                PartyFilter.SUPPLIERS -> party.role == Party.ROLE_SUPPLIER || party.role == Party.ROLE_BOTH
                PartyFilter.SETTLED -> party.netBalanceCents == 0L
            }
            matchesQuery && matchesFilter
        }

        return when (sortOrder) {
            PartySortOrder.RECENT_ACTIVITY -> filtered.sortedByDescending {
                (it.updatedAt ?: it.createdAt)?.toDate()?.time ?: 0L
            }
            PartySortOrder.HIGHEST_RECEIVABLE -> filtered.sortedByDescending { it.netBalanceCents }
            PartySortOrder.HIGHEST_PAYABLE -> filtered.sortedBy { it.netBalanceCents }
            PartySortOrder.NAME_ASC -> filtered.sortedBy { it.name.lowercase(Locale.getDefault()) }
        }
    }

    fun createNewParty(
        name: String,
        phone: String,
        role: String,
        notes: String,
        dueDateMillis: Long,
        openingAmountCents: Long,
        openingType: String,
        openingDescription: String,
        onCreated: (String) -> Unit = {}
    ) {
        if (name.isBlank()) {
            _statusMessage.value = "Please enter a name"
            return
        }
        val effectiveNotes = if (isPersonalLedger) {
            LocalShopLedgerStore.PERSONAL_LEDGER_MARKER
        } else {
            notes
        }
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.createParty(
                name = name,
                phone = phone,
                role = role,
                notes = effectiveNotes,
                dueDateMillis = dueDateMillis,
                openingAmountCents = openingAmountCents,
                openingType = openingType,
                openingDescription = openingDescription
            )
            _isBusy.value = false
            result.onSuccess { newId ->
                val ledgerLabel = if (isPersonalLedger) "personal ledger" else "shop register"
                _statusMessage.value = "Added ${name.trim()} to $ledgerLabel"
                onCreated(newId)
            }.onFailure { err ->
                _statusMessage.value = err.localizedMessage ?: "Failed to create party"
            }
        }
    }

    fun updateParty(
        party: Party,
        name: String,
        phone: String,
        role: String,
        notes: String,
        dueDateMillis: Long
    ) {
        if (name.isBlank()) {
            _statusMessage.value = "Name cannot be empty"
            return
        }
        val effectiveNotes = if (isPersonalLedger) {
            LocalShopLedgerStore.PERSONAL_LEDGER_MARKER
        } else {
            notes
        }
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.updatePartyDetails(
                party = party,
                newName = name,
                newPhone = phone,
                newRole = role,
                newNotes = effectiveNotes,
                newDueDateMillis = dueDateMillis
            )
            _isBusy.value = false
            result.onSuccess {
                _statusMessage.value = "Updated ${name.trim()}"
            }.onFailure { err ->
                _statusMessage.value = err.localizedMessage ?: "Failed to update account"
            }
        }
    }

    fun recordEntry(
        party: Party,
        type: String,
        amountCents: Long,
        description: String,
        category: String,
        paymentMode: String,
        referenceNote: String,
        entryDateMillis: Long = System.currentTimeMillis()
    ) {
        if (amountCents <= 0L) {
            _statusMessage.value = "Please enter an amount greater than zero"
            return
        }
        val effectiveRefNote = if (isPersonalLedger) {
            LocalShopLedgerStore.PERSONAL_LEDGER_MARKER
        } else {
            referenceNote
        }
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.addLedgerEntry(
                party = party,
                type = type,
                amountCents = amountCents,
                description = description,
                category = category,
                paymentMode = paymentMode,
                referenceNote = effectiveRefNote,
                entryDateMillis = entryDateMillis
            )
            _isBusy.value = false
            result.onSuccess {
                val actionLabel = if (type == LedgerEntry.TYPE_GAVE) "Recorded You Gave" else "Recorded You Got"
                _statusMessage.value = "$actionLabel ${formatMoney(amountCents, _currencySymbol.value)} for ${party.name}"
            }.onFailure { err ->
                _statusMessage.value = err.localizedMessage ?: "Failed to save entry"
            }
        }
    }

    fun settleParty(party: Party, paymentMode: String) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.settlePartyBalance(
                party = party,
                paymentMode = paymentMode,
                note = "Full balance settlement"
            )
            _isBusy.value = false
            result.onSuccess {
                _statusMessage.value = "${party.name}'s account settled to 0.00"
            }.onFailure { err ->
                _statusMessage.value = err.localizedMessage ?: "Failed to settle balance"
            }
        }
    }

    fun deleteEntry(party: Party, entry: LedgerEntry) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.deleteLedgerEntry(party, entry)
            _isBusy.value = false
            result.onSuccess {
                _statusMessage.value = "Entry deleted and balance recalculated"
            }.onFailure { err ->
                _statusMessage.value = err.localizedMessage ?: "Failed to delete entry"
            }
        }
    }

    fun deleteParty(party: Party, partyEntries: List<LedgerEntry>) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.deletePartyAndEntries(party.id, partyEntries)
            _isBusy.value = false
            result.onSuccess {
                if (_selectedPartyId.value == party.id) {
                    _selectedPartyId.value = null
                }
                _statusMessage.value = "Deleted ${party.name} and ${partyEntries.size} entries"
            }.onFailure { err ->
                _statusMessage.value = err.localizedMessage ?: "Failed to delete account"
            }
        }
    }

    fun seedSampleShopRegister() {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                if (isPersonalLedger) {
                    // Personal Simple Ledger sample names and reasons
                    val p1 = repository.createParty(
                        name = "Ali Khan",
                        phone = "",
                        role = Party.ROLE_CUSTOMER,
                        notes = LocalShopLedgerStore.PERSONAL_LEDGER_MARKER,
                        dueDateMillis = 0L,
                        openingAmountCents = 15000L, // $150.00 GAVE
                        openingType = LedgerEntry.TYPE_GAVE,
                        openingDescription = "Lent cash for car repair"
                    )
                    p1.getOrNull()?.let { partyId ->
                        repository.getPartyById(currentUserId, partyId).getOrNull()?.let { party ->
                            repository.addLedgerEntry(
                                party = party,
                                type = LedgerEntry.TYPE_GOT,
                                amountCents = 5000L,
                                description = "Returned partial cash",
                                category = "Taken",
                                paymentMode = LedgerEntry.MODE_CASH,
                                referenceNote = LocalShopLedgerStore.PERSONAL_LEDGER_MARKER
                            )
                        }
                    }

                    repository.createParty(
                        name = "Bilal Ahmed",
                        phone = "",
                        role = Party.ROLE_CUSTOMER,
                        notes = LocalShopLedgerStore.PERSONAL_LEDGER_MARKER,
                        dueDateMillis = 0L,
                        openingAmountCents = 6500L, // $65.00 GOT (we owe him)
                        openingType = LedgerEntry.TYPE_GOT,
                        openingDescription = "Dinner & coffee bill split"
                    )

                    repository.createParty(
                        name = "Hamza Tariq",
                        phone = "",
                        role = Party.ROLE_CUSTOMER,
                        notes = LocalShopLedgerStore.PERSONAL_LEDGER_MARKER,
                        dueDateMillis = 0L,
                        openingAmountCents = 4200L, // $42.00 GAVE (he owes us)
                        openingType = LedgerEntry.TYPE_GAVE,
                        openingDescription = "Movie & snacks tickets"
                    )

                    _statusMessage.value = "Sample personal ledger entries loaded!"
                } else {
                    // 1. Customer who owes us (Receivable)
                    val r1 = repository.createParty(
                        name = "Malik General Store",
                        phone = "+1-555-234-8890",
                        role = Party.ROLE_CUSTOMER,
                        notes = "Weekly grocery wholesale buyer • Net 15 days",
                        dueDateMillis = System.currentTimeMillis() + 5 * 86_400_000L,
                        openingAmountCents = 85000L, // $850.00 GAVE
                        openingType = LedgerEntry.TYPE_GAVE,
                        openingDescription = "10 sacks Basmati Rice & 24x Sunflower Oil"
                    )
                    r1.getOrNull()?.let { partyId ->
                        repository.getPartyById(currentUserId, partyId).getOrNull()?.let { party ->
                            repository.addLedgerEntry(
                                party = party,
                                type = LedgerEntry.TYPE_GOT,
                                amountCents = 30000L, // $300.00 received
                                description = "Part payment received in cash",
                                category = "Cash Received",
                                paymentMode = LedgerEntry.MODE_CASH,
                                referenceNote = "RCPT-104"
                            )
                        }
                    }

                    // 2. Supplier whom we owe (Payable)
                    val r2 = repository.createParty(
                        name = "Golden Harvest Distributors",
                        phone = "+1-555-901-4421",
                        role = Party.ROLE_SUPPLIER,
                        notes = "Primary flour, sugar & pulses supplier",
                        dueDateMillis = System.currentTimeMillis() + 3 * 86_400_000L,
                        openingAmountCents = 140000L, // $1,400.00 GOT on credit (we owe them)
                        openingType = LedgerEntry.TYPE_GOT,
                        openingDescription = "Wholesale stock delivery Invoice #GH-882"
                    )
                    r2.getOrNull()?.let { partyId ->
                        repository.getPartyById(currentUserId, partyId).getOrNull()?.let { party ->
                            repository.addLedgerEntry(
                                party = party,
                                type = LedgerEntry.TYPE_GAVE,
                                amountCents = 50000L, // $500.00 paid via bank transfer
                                description = "Bank transfer installment towards #GH-882",
                                category = "Bill Payment",
                                paymentMode = LedgerEntry.MODE_BANK_TRANSFER,
                                referenceNote = "TXN-9941"
                            )
                        }
                    }

                    // 3. Another Customer who owes us
                    repository.createParty(
                        name = "Sara Bakery & Cafe",
                        phone = "+1-555-672-3310",
                        role = Party.ROLE_CUSTOMER,
                        notes = "Daily dairy, butter & cocoa supplies",
                        dueDateMillis = System.currentTimeMillis() + 7 * 86_400_000L,
                        openingAmountCents = 32550L, // $325.50 GAVE
                        openingType = LedgerEntry.TYPE_GAVE,
                        openingDescription = "Butter cartons, icing sugar & vanilla extract"
                    )

                    _statusMessage.value = "Sample shop accounts and ledger entries loaded!"
                }
            } catch (e: Exception) {
                _statusMessage.value = e.localizedMessage ?: "Failed to seed sample accounts"
            } finally {
                _isBusy.value = false
            }
        }
    }

    companion object {
        private const val TAG = "ShopLedgerVM"
        private const val STOP_TIMEOUT_MILLIS = 5000L

        fun formatMoney(amountCents: Long, currencySymbol: String = "$"): String {
            val absCents = abs(amountCents)
            val major = absCents / 100.0
            val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
                minimumFractionDigits = 2
                maximumFractionDigits = 2
            }
            return "$currencySymbol${numberFormat.format(major)}"
        }

        fun formatDateShort(epochMillis: Long): String {
            if (epochMillis <= 0L) return "No date"
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            return sdf.format(Date(epochMillis))
        }

        fun formatDateTime(epochMillis: Long): String {
            if (epochMillis <= 0L) return ""
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            return sdf.format(Date(epochMillis))
        }
    }
}
