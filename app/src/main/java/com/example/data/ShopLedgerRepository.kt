package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Locale

class ShopLedgerRepository(
    private val db: FirebaseFirestore? = null,
    private val auth: FirebaseAuth? = null,
    private val localStore: LocalShopLedgerStore? = null
) {
    constructor(context: Context) : this(
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireFirestore(): FirebaseFirestore {
        return checkNotNull(db) { "Firestore instance is not configured in local-only mode." }
    }

    private fun requireUserId(): String {
        if (localStore != null) {
            return LocalShopLedgerStore.LOCAL_USER_ID
        }
        val activeAuth = auth ?: FirebaseAuth.getInstance()
        return activeAuth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeParties(userId: String): Flow<List<Party>> {
        if (localStore != null) {
            return localStore.observeParties()
        }
        val firestore = requireFirestore()
        val path = "users/$userId/parties"
        return callbackFlow {
            val registration = firestore.collection("users").document(userId).collection("parties")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, OperationType.LIST, path)
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        trySend(
                            snapshot.toObjects(Party::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        )
                    }
                }
            awaitClose { registration.remove() }
        }
    }

    fun observeAllEntries(userId: String): Flow<List<LedgerEntry>> {
        if (localStore != null) {
            return localStore.observeAllEntries()
        }
        val firestore = requireFirestore()
        val path = "users/$userId/entries"
        return callbackFlow {
            val registration = firestore.collection("users").document(userId).collection("entries")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, OperationType.LIST, path)
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        trySend(
                            snapshot.toObjects(LedgerEntry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        )
                    }
                }
            awaitClose { registration.remove() }
        }
    }

    fun observePartyEntries(userId: String, partyId: String): Flow<List<LedgerEntry>> {
        if (localStore != null) {
            return localStore.observePartyEntries(partyId)
        }
        val firestore = requireFirestore()
        val path = "users/$userId/entries"
        return callbackFlow {
            val registration = firestore.collection("users").document(userId).collection("entries")
                .whereEqualTo("partyId", partyId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        handleFirestoreError(error, OperationType.LIST, path)
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        trySend(
                            snapshot.toObjects(LedgerEntry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        )
                    }
                }
            awaitClose { registration.remove() }
        }
    }

    suspend fun getPartyById(userId: String, partyId: String): Result<Party?> {
        if (localStore != null) {
            return localStore.getPartyById(partyId)
        }
        val firestore = requireFirestore()
        val docRef = firestore.collection("users").document(userId).collection("parties").document(partyId)
        return try {
            val snap = docRef.get().await()
            Result.success(snap.toObject(Party::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun createParty(
        name: String,
        phone: String = "",
        role: String = Party.ROLE_CUSTOMER,
        notes: String = "",
        dueDateMillis: Long = 0L,
        openingAmountCents: Long = 0L,
        openingType: String = LedgerEntry.TYPE_GAVE,
        openingDescription: String = "Opening Balance"
    ): Result<String> {
        val uid = requireUserId()
        if (localStore != null) {
            return localStore.createParty(
                userId = uid,
                name = name,
                phone = phone,
                role = role,
                notes = notes,
                dueDateMillis = dueDateMillis,
                openingAmountCents = openingAmountCents,
                openingType = openingType,
                openingDescription = openingDescription
            )
        }
        val firestore = requireFirestore()
        val partiesRef = firestore.collection("users").document(uid).collection("parties")
        val newPartyDoc = partiesRef.document()
        val partyId = newPartyDoc.id

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

        val partyPayload = mapOf(
            "userId" to uid,
            "name" to name.trim().take(100),
            "phone" to phone.trim().take(30),
            "role" to role,
            "notes" to notes.trim().take(300),
            "totalGivenCents" to givenCents,
            "totalGotCents" to gotCents,
            "netBalanceCents" to netCents,
            "entryCount" to count,
            "lastActivitySummary" to summary.take(150),
            "dueDateMillis" to dueDateMillis.coerceAtLeast(0L),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        val isPersonalParty = notes.trim().startsWith(LocalShopLedgerStore.PERSONAL_LEDGER_MARKER)
        return try {
            val batch = firestore.batch()
            batch.set(newPartyDoc, partyPayload)
            if (hasOpening) {
                val entryDoc = firestore.collection("users").document(uid).collection("entries").document()
                val entryPayload = mapOf(
                    "userId" to uid,
                    "partyId" to partyId,
                    "partyName" to name.trim().take(100),
                    "type" to openingType,
                    "amountCents" to openingAmountCents,
                    "description" to openingDescription.trim().ifBlank { "Opening Balance" }.take(300),
                    "category" to "Opening Balance",
                    "paymentMode" to LedgerEntry.MODE_CREDIT,
                    "referenceNote" to if (isPersonalParty) LocalShopLedgerStore.PERSONAL_LEDGER_MARKER else "OPEN-BAL",
                    "entryDateMillis" to System.currentTimeMillis(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                batch.set(entryDoc, entryPayload)
            }
            batch.commit().await()
            Result.success(partyId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, newPartyDoc.path)
            Result.failure(e)
        }
    }

    suspend fun updatePartyDetails(
        party: Party,
        newName: String,
        newPhone: String,
        newRole: String,
        newNotes: String,
        newDueDateMillis: Long
    ): Result<Unit> {
        val uid = requireUserId()
        require(party.id.isNotBlank()) { "Party ID must not be blank" }
        if (localStore != null) {
            return localStore.updatePartyDetails(
                party = party,
                newName = newName,
                newPhone = newPhone,
                newRole = newRole,
                newNotes = newNotes,
                newDueDateMillis = newDueDateMillis
            )
        }
        val firestore = requireFirestore()
        val docRef = firestore.collection("users").document(uid).collection("parties").document(party.id)
        val updates = mapOf(
            "name" to newName.trim().take(100),
            "phone" to newPhone.trim().take(30),
            "role" to newRole,
            "notes" to newNotes.trim().take(300),
            "dueDateMillis" to newDueDateMillis.coerceAtLeast(0L),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun addLedgerEntry(
        party: Party,
        type: String,
        amountCents: Long,
        description: String,
        category: String,
        paymentMode: String,
        referenceNote: String = "",
        entryDateMillis: Long = System.currentTimeMillis()
    ): Result<String> {
        val uid = requireUserId()
        require(party.id.isNotBlank()) { "Party ID must not be blank" }
        require(amountCents > 0L) { "Amount must be greater than zero" }

        if (localStore != null) {
            return localStore.addLedgerEntry(
                userId = uid,
                party = party,
                type = type,
                amountCents = amountCents,
                description = description,
                category = category,
                paymentMode = paymentMode,
                referenceNote = referenceNote,
                entryDateMillis = entryDateMillis
            )
        }

        val firestore = requireFirestore()
        val partyDocRef = firestore.collection("users").document(uid).collection("parties").document(party.id)
        val entryDocRef = firestore.collection("users").document(uid).collection("entries").document()

        val newGiven = if (type == LedgerEntry.TYPE_GAVE) {
            party.totalGivenCents + amountCents
        } else {
            party.totalGivenCents
        }
        val newGot = if (type == LedgerEntry.TYPE_GOT) {
            party.totalGotCents + amountCents
        } else {
            party.totalGotCents
        }
        val newNet = newGiven - newGot
        val newCount = party.entryCount + 1L

        val cleanDesc = description.trim().ifBlank {
            if (type == LedgerEntry.TYPE_GAVE) "Credit Given / Goods Sold" else "Payment Received / Goods Got"
        }.take(300)

        val prefix = if (type == LedgerEntry.TYPE_GAVE) "Gave" else "Got"
        val formattedAmt = String.format(Locale.US, "%.2f", amountCents / 100.0)
        val summary = "$prefix $formattedAmt • ${cleanDesc.take(60)}".take(150)

        val entryPayload = mapOf(
            "userId" to uid,
            "partyId" to party.id,
            "partyName" to party.name.take(100),
            "type" to type,
            "amountCents" to amountCents,
            "description" to cleanDesc,
            "category" to category.trim().ifBlank { "General" }.take(50),
            "paymentMode" to paymentMode,
            "referenceNote" to referenceNote.trim().take(100),
            "entryDateMillis" to entryDateMillis.coerceAtLeast(1L),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        val partyUpdates = mapOf(
            "totalGivenCents" to newGiven,
            "totalGotCents" to newGot,
            "netBalanceCents" to newNet,
            "entryCount" to newCount,
            "lastActivitySummary" to summary,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return try {
            val batch = firestore.batch()
            batch.set(entryDocRef, entryPayload)
            batch.update(partyDocRef, partyUpdates)
            batch.commit().await()
            Result.success(entryDocRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, entryDocRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteLedgerEntry(
        party: Party,
        entry: LedgerEntry
    ): Result<Unit> {
        val uid = requireUserId()
        require(party.id.isNotBlank()) { "Party ID must not be blank" }
        require(entry.id.isNotBlank()) { "Entry ID must not be blank" }

        if (localStore != null) {
            return localStore.deleteLedgerEntry(party, entry)
        }

        val firestore = requireFirestore()
        val partyDocRef = firestore.collection("users").document(uid).collection("parties").document(party.id)
        val entryDocRef = firestore.collection("users").document(uid).collection("entries").document(entry.id)

        val newGiven = if (entry.type == LedgerEntry.TYPE_GAVE) {
            (party.totalGivenCents - entry.amountCents).coerceAtLeast(0L)
        } else {
            party.totalGivenCents
        }
        val newGot = if (entry.type == LedgerEntry.TYPE_GOT) {
            (party.totalGotCents - entry.amountCents).coerceAtLeast(0L)
        } else {
            party.totalGotCents
        }
        val newNet = newGiven - newGot
        val newCount = (party.entryCount - 1L).coerceAtLeast(0L)

        val partyUpdates = mapOf(
            "totalGivenCents" to newGiven,
            "totalGotCents" to newGot,
            "netBalanceCents" to newNet,
            "entryCount" to newCount,
            "lastActivitySummary" to "Entry removed • Balance adjusted",
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return try {
            val batch = firestore.batch()
            batch.delete(entryDocRef)
            batch.update(partyDocRef, partyUpdates)
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, entryDocRef.path)
            Result.failure(e)
        }
    }

    suspend fun settlePartyBalance(
        party: Party,
        paymentMode: String = LedgerEntry.MODE_CASH,
        note: String = "Full balance settlement"
    ): Result<String> {
        if (party.netBalanceCents == 0L) {
            return Result.failure(IllegalArgumentException("Account is already settled"))
        }
        val settleType = if (party.netBalanceCents > 0L) LedgerEntry.TYPE_GOT else LedgerEntry.TYPE_GAVE
        val settleAmount = kotlin.math.abs(party.netBalanceCents)
        return addLedgerEntry(
            party = party,
            type = settleType,
            amountCents = settleAmount,
            description = note.ifBlank { "Full balance settlement" },
            category = "Settlement",
            paymentMode = paymentMode,
            referenceNote = "SETTLED"
        )
    }

    suspend fun deletePartyAndEntries(
        partyId: String,
        entries: List<LedgerEntry>
    ): Result<Unit> {
        val uid = requireUserId()
        if (localStore != null) {
            return localStore.deletePartyAndEntries(partyId)
        }
        val firestore = requireFirestore()
        val partyDocRef = firestore.collection("users").document(uid).collection("parties").document(partyId)
        return try {
            val batch = firestore.batch()
            entries.forEach { entry ->
                if (entry.id.isNotBlank()) {
                    val entryRef = firestore.collection("users").document(uid).collection("entries").document(entry.id)
                    batch.delete(entryRef)
                }
            }
            batch.delete(partyDocRef)
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, partyDocRef.path)
            Result.failure(e)
        }
    }
}
