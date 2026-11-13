package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ShopLedgerRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createPartyWithOpeningBalance_andAddEntry_updatesBalanceCorrectly() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = ShopLedgerRepository(firestore, auth)

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createParty(
                name = "Zainab Textiles",
                phone = "+1555112233",
                role = Party.ROLE_CUSTOMER,
                notes = "Wholesale cotton fabric buyer",
                openingAmountCents = 25000L, // $250.00 GAVE (they owe us)
                openingType = LedgerEntry.TYPE_GAVE,
                openingDescription = "Initial invoice #101"
            )
        }
        assertTrue("Expected createParty to succeed", createResult.isSuccess)
        val partyId = createResult.getOrThrow()
        assertTrue(partyId.isNotBlank())

        val party = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getPartyById(aliceUid, partyId).getOrThrow()
        }
        assertNotNull(party)
        assertEquals(25000L, party!!.totalGivenCents)
        assertEquals(0L, party.totalGotCents)
        assertEquals(25000L, party.netBalanceCents)
        assertEquals(1L, party.entryCount)

        // Add a GOT entry ($100.00 partial payment received)
        val addEntryResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addLedgerEntry(
                party = party,
                type = LedgerEntry.TYPE_GOT,
                amountCents = 10000L,
                description = "Cash partial payment",
                category = "Cash Received",
                paymentMode = LedgerEntry.MODE_CASH,
                referenceNote = "RCPT-01"
            )
        }
        assertTrue("Expected addLedgerEntry to succeed", addEntryResult.isSuccess)

        // Verify updated party balance via Flow
        val updatedParties = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeParties(aliceUid).first { list ->
                list.any { it.id == partyId && it.netBalanceCents == 15000L }
            }
        }
        val updatedParty = updatedParties.first { it.id == partyId }
        assertEquals(25000L, updatedParty.totalGivenCents)
        assertEquals(10000L, updatedParty.totalGotCents)
        assertEquals(15000L, updatedParty.netBalanceCents)
        assertEquals(2L, updatedParty.entryCount)
    }

    @Test
    fun crossUserAccess_cannotReadAnotherUsersParty() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = ShopLedgerRepository(firestore, auth)
        val partyId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createParty(name = "Alice Private Supplier").getOrThrow()
        }

        // Switch to Bob
        signInTestUser(BOB_EMAIL)
        val bobRepo = ShopLedgerRepository(firestore, auth)
        val crossReadResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getPartyById(aliceUid, partyId)
        }
        assertTrue("Expected cross-user read to fail", crossReadResult.isFailure)
        val exception = crossReadResult.exceptionOrNull() as? FirebaseFirestoreException
        assertNotNull(exception)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, exception?.code)
    }

    @Test
    fun unauthenticatedUser_cannotObserveParties() = runBlocking {
        auth.signOut()
        val repository = ShopLedgerRepository(firestore, auth)
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeParties("unauth_uid_test").first()
            }
            fail("Expected FirebaseFirestoreException for unauthenticated caller")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice.shop@test.com"
        const val BOB_EMAIL = "bob.shop@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
