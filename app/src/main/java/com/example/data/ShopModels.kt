package com.example.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Party(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val phone: String = "",
    val role: String = ROLE_CUSTOMER,
    val notes: String = "",
    val totalGivenCents: Long = 0L,
    val totalGotCents: Long = 0L,
    val netBalanceCents: Long = 0L,
    val entryCount: Long = 0L,
    val lastActivitySummary: String = "",
    val dueDateMillis: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    companion object {
        const val ROLE_CUSTOMER = "CUSTOMER"
        const val ROLE_SUPPLIER = "SUPPLIER"
        const val ROLE_BOTH = "BOTH"
    }
}

@IgnoreExtraProperties
data class LedgerEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val partyId: String = "",
    val partyName: String = "",
    val type: String = TYPE_GAVE,
    val amountCents: Long = 0L,
    val description: String = "",
    val category: String = "Goods Sold",
    val paymentMode: String = MODE_CASH,
    val referenceNote: String = "",
    val entryDateMillis: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    companion object {
        const val TYPE_GAVE = "GAVE" // Shop gave money/goods on credit (Party owes us)
        const val TYPE_GOT = "GOT"   // Shop got money/purchased on credit (We owe party)

        const val MODE_CASH = "CASH"
        const val MODE_BANK_TRANSFER = "BANK_TRANSFER"
        const val MODE_UPI_WALLET = "UPI_WALLET"
        const val MODE_CREDIT = "CREDIT"
        const val MODE_CHEQUE = "CHEQUE"
    }
}

data class ShopSummaryStats(
    val totalReceivableCents: Long = 0L, // Sum of positive netBalanceCents (They owe us)
    val totalPayableCents: Long = 0L,    // Sum of abs(negative netBalanceCents) (We owe them)
    val netPositionCents: Long = 0L,     // totalReceivableCents - totalPayableCents
    val receivableCount: Int = 0,
    val payableCount: Int = 0,
    val settledCount: Int = 0,
    val overdueCount: Int = 0
)
