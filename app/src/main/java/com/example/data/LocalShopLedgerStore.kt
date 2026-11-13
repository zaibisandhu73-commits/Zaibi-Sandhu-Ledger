package com.example.data

import android.content.Context
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Date
import java.util.Locale
import java.util.UUID

class LocalShopLedgerStore(
    context: Context,
    private val storageFileName: String = SHOP_STORAGE_FILE
) {

    private val appContext = context.applicationContext
    private val storageFile = File(appContext.filesDir, storageFileName)
    private val mutex = Mutex()
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _partiesFlow = MutableStateFlow<List<Party>>(emptyList())
    private val _entriesFlow = MutableStateFlow<List<LedgerEntry>>(emptyList())

    init {
        ioScope.launch {
            loadFromDisk()
        }
    }

    fun observeParties(): Flow<List<Party>> = _partiesFlow.asStateFlow()

    fun observeAllEntries(): Flow<List<LedgerEntry>> = _entriesFlow.asStateFlow()

    fun observePartyEntries(partyId: String): Flow<List<LedgerEntry>> =
        _entriesFlow.map { list -> list.filter { it.partyId == partyId } }

    suspend fun getPartyById(partyId: String): Result<Party?> = mutex.withLock {
        Result.success(_partiesFlow.value.firstOrNull { it.id == partyId })
    }

    suspend fun createParty(
        userId: String,
        name: String,
        phone: String,
        role: String,
        notes: String,
        dueDateMillis: Long,
        openingAmountCents: Long,
        openingType: String,
        openingDescription: String
    ): Result<String> = mutex.withLock {
        val partyId = "loc_party_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val now = Timestamp(Date())
        val hasOpening = openingAmountCents > 0L
        val givenCents = if (hasOpening && openingType == LedgerEntry.TYPE_GAVE) openingAmountCents else 0L
        val gotCents = if (hasOpening && openingType == LedgerEntry.TYPE_GOT) openingAmountCents else 0L
        val netCents = givenCents - gotCents
        val count = if (hasOpening) 1L else 0L
        val summary = if (hasOpening) {
            val prefix = if (openingType == LedgerEntry.TYPE_GAVE) "Gave" else "Got"
            val formatted = String.format(Locale.US, "%.2f", openingAmountCents / 100.0)
            "$prefix $formatted • ${openingDescription.take(60)}"
        } else {
            "Account created"
        }

        val newParty = Party(
            id = partyId,
            userId = userId,
            name = name.trim().take(100),
            phone = phone.trim().take(30),
            role = role,
            notes = notes.trim().take(300),
            totalGivenCents = givenCents,
            totalGotCents = gotCents,
            netBalanceCents = netCents,
            entryCount = count,
            lastActivitySummary = summary.take(150),
            dueDateMillis = dueDateMillis.coerceAtLeast(0L),
            createdAt = now,
            updatedAt = now
        )

        val isPersonalParty = notes.trim().startsWith(PERSONAL_LEDGER_MARKER)
        val updatedParties = _partiesFlow.value + newParty
        val updatedEntries = if (hasOpening) {
            val entryId = "loc_entry_${UUID.randomUUID().toString().replace("-", "").take(16)}"
            val openingEntry = LedgerEntry(
                id = entryId,
                userId = userId,
                partyId = partyId,
                partyName = newParty.name,
                type = openingType,
                amountCents = openingAmountCents,
                description = openingDescription.trim().ifBlank { "Opening Balance" }.take(300),
                category = "Opening Balance",
                paymentMode = LedgerEntry.MODE_CREDIT,
                referenceNote = if (isPersonalParty) PERSONAL_LEDGER_MARKER else "OPEN-BAL",
                entryDateMillis = System.currentTimeMillis(),
                createdAt = now,
                updatedAt = now
            )
            _entriesFlow.value + openingEntry
        } else {
            _entriesFlow.value
        }

        _partiesFlow.value = updatedParties
        _entriesFlow.value = updatedEntries
        saveToDisk(updatedParties, updatedEntries)
        Result.success(partyId)
    }

    suspend fun updatePartyDetails(
        party: Party,
        newName: String,
        newPhone: String,
        newRole: String,
        newNotes: String,
        newDueDateMillis: Long
    ): Result<Unit> = mutex.withLock {
        val now = Timestamp(Date())
        val cleanName = newName.trim().take(100)
        val updatedParties = _partiesFlow.value.map { existing ->
            if (existing.id == party.id) {
                existing.copy(
                    name = cleanName,
                    phone = newPhone.trim().take(30),
                    role = newRole,
                    notes = newNotes.trim().take(300),
                    dueDateMillis = newDueDateMillis.coerceAtLeast(0L),
                    updatedAt = now
                )
            } else {
                existing
            }
        }
        val updatedEntries = _entriesFlow.value.map { entry ->
            if (entry.partyId == party.id) {
                entry.copy(partyName = cleanName, updatedAt = now)
            } else {
                entry
            }
        }
        _partiesFlow.value = updatedParties
        _entriesFlow.value = updatedEntries
        saveToDisk(updatedParties, updatedEntries)
        Result.success(Unit)
    }

    suspend fun addLedgerEntry(
        userId: String,
        party: Party,
        type: String,
        amountCents: Long,
        description: String,
        category: String,
        paymentMode: String,
        referenceNote: String,
        entryDateMillis: Long
    ): Result<String> = mutex.withLock {
        val now = Timestamp(Date())
        val entryId = "loc_entry_${UUID.randomUUID().toString().replace("-", "").take(16)}"

        val currentParty = _partiesFlow.value.firstOrNull { it.id == party.id } ?: party
        val newGiven = if (type == LedgerEntry.TYPE_GAVE) {
            currentParty.totalGivenCents + amountCents
        } else {
            currentParty.totalGivenCents
        }
        val newGot = if (type == LedgerEntry.TYPE_GOT) {
            currentParty.totalGotCents + amountCents
        } else {
            currentParty.totalGotCents
        }
        val newNet = newGiven - newGot
        val newCount = currentParty.entryCount + 1L

        val cleanDesc = description.trim().ifBlank {
            if (type == LedgerEntry.TYPE_GAVE) "Credit Given / Goods Sold" else "Payment Received / Goods Got"
        }.take(300)

        val prefix = if (type == LedgerEntry.TYPE_GAVE) "Gave" else "Got"
        val formattedAmt = String.format(Locale.US, "%.2f", amountCents / 100.0)
        val summary = "$prefix $formattedAmt • ${cleanDesc.take(60)}".take(150)

        val newEntry = LedgerEntry(
            id = entryId,
            userId = userId,
            partyId = currentParty.id,
            partyName = currentParty.name.take(100),
            type = type,
            amountCents = amountCents,
            description = cleanDesc,
            category = category.trim().ifBlank { "General" }.take(50),
            paymentMode = paymentMode,
            referenceNote = referenceNote.trim().take(100),
            entryDateMillis = entryDateMillis.coerceAtLeast(1L),
            createdAt = now,
            updatedAt = now
        )

        val updatedParties = _partiesFlow.value.map { existing ->
            if (existing.id == currentParty.id) {
                existing.copy(
                    totalGivenCents = newGiven,
                    totalGotCents = newGot,
                    netBalanceCents = newNet,
                    entryCount = newCount,
                    lastActivitySummary = summary,
                    updatedAt = now
                )
            } else {
                existing
            }
        }
        val updatedEntries = _entriesFlow.value + newEntry

        _partiesFlow.value = updatedParties
        _entriesFlow.value = updatedEntries
        saveToDisk(updatedParties, updatedEntries)
        Result.success(entryId)
    }

    suspend fun deleteLedgerEntry(
        party: Party,
        entry: LedgerEntry
    ): Result<Unit> = mutex.withLock {
        val now = Timestamp(Date())
        val currentParty = _partiesFlow.value.firstOrNull { it.id == party.id } ?: party

        val newGiven = if (entry.type == LedgerEntry.TYPE_GAVE) {
            (currentParty.totalGivenCents - entry.amountCents).coerceAtLeast(0L)
        } else {
            currentParty.totalGivenCents
        }
        val newGot = if (entry.type == LedgerEntry.TYPE_GOT) {
            (currentParty.totalGotCents - entry.amountCents).coerceAtLeast(0L)
        } else {
            currentParty.totalGotCents
        }
        val newNet = newGiven - newGot
        val newCount = (currentParty.entryCount - 1L).coerceAtLeast(0L)

        val updatedEntries = _entriesFlow.value.filterNot { it.id == entry.id }
        val updatedParties = _partiesFlow.value.map { existing ->
            if (existing.id == currentParty.id) {
                existing.copy(
                    totalGivenCents = newGiven,
                    totalGotCents = newGot,
                    netBalanceCents = newNet,
                    entryCount = newCount,
                    lastActivitySummary = "Entry removed • Balance adjusted",
                    updatedAt = now
                )
            } else {
                existing
            }
        }

        _partiesFlow.value = updatedParties
        _entriesFlow.value = updatedEntries
        saveToDisk(updatedParties, updatedEntries)
        Result.success(Unit)
    }

    suspend fun deletePartyAndEntries(partyId: String): Result<Unit> = mutex.withLock {
        val updatedParties = _partiesFlow.value.filterNot { it.id == partyId }
        val updatedEntries = _entriesFlow.value.filterNot { it.partyId == partyId }
        _partiesFlow.value = updatedParties
        _entriesFlow.value = updatedEntries
        saveToDisk(updatedParties, updatedEntries)
        Result.success(Unit)
    }

    private suspend fun loadFromDisk() = mutex.withLock {
        withContext(Dispatchers.IO) {
            if (!storageFile.exists()) return@withContext
            runCatching {
                val root = JSONObject(storageFile.readText())
                val partiesArr = root.optJSONArray("parties") ?: JSONArray()
                val entriesArr = root.optJSONArray("entries") ?: JSONArray()

                val loadedParties = buildList {
                    for (i in 0 until partiesArr.length()) {
                        val obj = partiesArr.getJSONObject(i)
                        val createdMs = obj.optLong("createdAtMs", System.currentTimeMillis())
                        val updatedMs = obj.optLong("updatedAtMs", createdMs)
                        add(
                            Party(
                                id = obj.getString("id"),
                                userId = obj.optString("userId", LOCAL_USER_ID),
                                name = obj.optString("name", ""),
                                phone = obj.optString("phone", ""),
                                role = obj.optString("role", Party.ROLE_CUSTOMER),
                                notes = obj.optString("notes", ""),
                                totalGivenCents = obj.optLong("totalGivenCents", 0L),
                                totalGotCents = obj.optLong("totalGotCents", 0L),
                                netBalanceCents = obj.optLong("netBalanceCents", 0L),
                                entryCount = obj.optLong("entryCount", 0L),
                                lastActivitySummary = obj.optString("lastActivitySummary", ""),
                                dueDateMillis = obj.optLong("dueDateMillis", 0L),
                                createdAt = Timestamp(Date(createdMs)),
                                updatedAt = Timestamp(Date(updatedMs))
                            )
                        )
                    }
                }

                val loadedEntries = buildList {
                    for (i in 0 until entriesArr.length()) {
                        val obj = entriesArr.getJSONObject(i)
                        val createdMs = obj.optLong("createdAtMs", System.currentTimeMillis())
                        val updatedMs = obj.optLong("updatedAtMs", createdMs)
                        add(
                            LedgerEntry(
                                id = obj.getString("id"),
                                userId = obj.optString("userId", LOCAL_USER_ID),
                                partyId = obj.optString("partyId", ""),
                                partyName = obj.optString("partyName", ""),
                                type = obj.optString("type", LedgerEntry.TYPE_GAVE),
                                amountCents = obj.optLong("amountCents", 0L),
                                description = obj.optString("description", ""),
                                category = obj.optString("category", "General"),
                                paymentMode = obj.optString("paymentMode", LedgerEntry.MODE_CASH),
                                referenceNote = obj.optString("referenceNote", ""),
                                entryDateMillis = obj.optLong("entryDateMillis", createdMs),
                                createdAt = Timestamp(Date(createdMs)),
                                updatedAt = Timestamp(Date(updatedMs))
                            )
                        )
                    }
                }

                _partiesFlow.value = loadedParties
                _entriesFlow.value = loadedEntries
            }
        }
    }

    private suspend fun saveToDisk(parties: List<Party>, entries: List<LedgerEntry>) {
        withContext(Dispatchers.IO) {
            runCatching {
                val partiesArr = JSONArray().apply {
                    parties.forEach { p ->
                        put(
                            JSONObject().apply {
                                put("id", p.id)
                                put("userId", p.userId)
                                put("name", p.name)
                                put("phone", p.phone)
                                put("role", p.role)
                                put("notes", p.notes)
                                put("totalGivenCents", p.totalGivenCents)
                                put("totalGotCents", p.totalGotCents)
                                put("netBalanceCents", p.netBalanceCents)
                                put("entryCount", p.entryCount)
                                put("lastActivitySummary", p.lastActivitySummary)
                                put("dueDateMillis", p.dueDateMillis)
                                put("createdAtMs", p.createdAt?.toDate()?.time ?: System.currentTimeMillis())
                                put("updatedAtMs", p.updatedAt?.toDate()?.time ?: System.currentTimeMillis())
                            }
                        )
                    }
                }
                val entriesArr = JSONArray().apply {
                    entries.forEach { e ->
                        put(
                            JSONObject().apply {
                                put("id", e.id)
                                put("userId", e.userId)
                                put("partyId", e.partyId)
                                put("partyName", e.partyName)
                                put("type", e.type)
                                put("amountCents", e.amountCents)
                                put("description", e.description)
                                put("category", e.category)
                                put("paymentMode", e.paymentMode)
                                put("referenceNote", e.referenceNote)
                                put("entryDateMillis", e.entryDateMillis)
                                put("createdAtMs", e.createdAt?.toDate()?.time ?: System.currentTimeMillis())
                                put("updatedAtMs", e.updatedAt?.toDate()?.time ?: System.currentTimeMillis())
                            }
                        )
                    }
                }
                val root = JSONObject().apply {
                    put("parties", partiesArr)
                    put("entries", entriesArr)
                }
                storageFile.writeText(root.toString())
            }
        }
    }

    companion object {
        const val LOCAL_USER_ID = "local_device_user"
        const val SHOP_STORAGE_FILE = "local_shop_ledger_v1.json"
        const val PERSONAL_STORAGE_FILE = "local_personal_ledger_v1.json"
        const val PERSONAL_LEDGER_MARKER = "[PERSONAL_LEDGER]"
    }
}
