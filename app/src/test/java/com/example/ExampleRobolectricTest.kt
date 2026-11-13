package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.LedgerEntry
import com.example.data.LocalShopLedgerStore
import com.example.data.Party
import com.example.data.ShopLedgerRepository
import com.example.data.ShopSummaryStats
import com.example.ui.screens.AppLayoutMode
import com.example.ui.screens.PersonalPartyDetailScreen
import com.example.ui.screens.PersonalSimpleScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `read string from context`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Ledger", appName)
    }

    @Test
    fun `local shop ledger store tracks receivables and payables accurately`() = runBlocking {
        val store = LocalShopLedgerStore(context)
        val repo = ShopLedgerRepository(db = null, localStore = store)
        val userId = LocalShopLedgerStore.LOCAL_USER_ID

        val createRes = repo.createParty(
            name = "Apex Traders",
            phone = "555-0199",
            role = "Customer",
            notes = "SaaS Account Test",
            dueDateMillis = 0L,
            openingAmountCents = 100_00L,
            openingType = LedgerEntry.TYPE_GAVE,
            openingDescription = "Opening Credit Invoice"
        )
        assertTrue(createRes.isSuccess)
        val partyId = createRes.getOrThrow()

        val partiesAfterCreate = repo.observeParties(userId).first()
        val created = partiesAfterCreate.first { it.id == partyId }
        assertEquals(100_00L, created.netBalanceCents)

        val addRes = repo.addLedgerEntry(
            party = created,
            type = LedgerEntry.TYPE_GOT,
            amountCents = 40_00L,
            description = "Partial UPI Settlement",
            category = "Part Settlement",
            paymentMode = LedgerEntry.MODE_UPI_WALLET,
            referenceNote = "UPI-882",
            entryDateMillis = System.currentTimeMillis()
        )
        assertTrue(addRes.isSuccess)

        val updated = repo.observeParties(userId).first().first { it.id == partyId }
        assertEquals(60_00L, updated.netBalanceCents)
        assertEquals(2L, updated.entryCount)
    }

    @Test
    fun `personal simple layout displays amount-only upper boxes and SaaS glass party boxes`() {
        var clickedPartyId: String? = null
        val parties = listOf(
            Party(
                id = "p_green",
                name = "Ali Khan",
                netBalanceCents = 250_00L
            ),
            Party(
                id = "p_red",
                name = "Bilal Traders",
                netBalanceCents = -120_00L
            )
        )
        val stats = ShopSummaryStats(
            totalReceivableCents = 250_00L,
            totalPayableCents = 120_00L,
            netPositionCents = 130_00L,
            receivableCount = 1,
            payableCount = 1,
            settledCount = 0
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                PersonalSimpleScreen(
                    parties = parties,
                    summaryStats = stats,
                    selectedPartyId = null,
                    currencySymbol = "$",
                    onPartyClick = { clickedPartyId = it.id },
                    onAddPartyClick = {},
                    onSeedSampleDataClick = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("personal_given_total_box").assertExists()
        composeTestRule.onNodeWithTag("personal_taken_total_box").assertExists()
        composeTestRule.onNodeWithText("Given Amount").assertDoesNotExist()
        composeTestRule.onNodeWithText("Taken Money").assertDoesNotExist()
        composeTestRule.onNodeWithTag("personal_party_box_p_green").assertExists()
        composeTestRule.onNodeWithTag("personal_party_box_p_red").performClick()

        assertEquals("p_red", clickedPartyId)
        assertEquals(AppLayoutMode.PERSONAL, AppLayoutMode.fromKey("PERSONAL"))
    }

    @Test
    fun `personal party detail screen displays simple entries with reason and amount`() {
        val party = Party(id = "p1", name = "Hamza", netBalanceCents = 80_00L)
        val entries = listOf(
            LedgerEntry(
                id = "e1",
                partyId = "p1",
                partyName = "Hamza",
                type = LedgerEntry.TYPE_GAVE,
                amountCents = 80_00L,
                description = "Dinner bill split"
            )
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                PersonalPartyDetailScreen(
                    party = party,
                    partyEntries = entries,
                    currencySymbol = "$",
                    showBackButton = true,
                    onBack = {},
                    onAddEntryClick = {},
                    onDeletePartyClick = {},
                    onDeleteEntryClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Hamza").assertExists()
        composeTestRule.onNodeWithText("Dinner bill split").assertExists()
        composeTestRule.onNodeWithTag("entry_row_e1").assertExists()
    }

    @Test
    fun `shop ledger and personal ledger remain completely separate`() = runBlocking {
        val shopStore = LocalShopLedgerStore(context, LocalShopLedgerStore.SHOP_STORAGE_FILE)
        val personalStore = LocalShopLedgerStore(context, LocalShopLedgerStore.PERSONAL_STORAGE_FILE)
        val shopRepo = ShopLedgerRepository(db = null, localStore = shopStore)
        val personalRepo = ShopLedgerRepository(db = null, localStore = personalStore)
        val userId = LocalShopLedgerStore.LOCAL_USER_ID

        val shopCreate = shopRepo.createParty(
            name = "Wholesale Mart",
            phone = "555-1000",
            role = Party.ROLE_SUPPLIER,
            notes = "Shop supplier account",
            dueDateMillis = 0L,
            openingAmountCents = 500_00L,
            openingType = LedgerEntry.TYPE_GOT,
            openingDescription = "Stock purchase"
        )
        assertTrue(shopCreate.isSuccess)

        val personalCreate = personalRepo.createParty(
            name = "Zain Friend",
            phone = "",
            role = Party.ROLE_CUSTOMER,
            notes = LocalShopLedgerStore.PERSONAL_LEDGER_MARKER,
            dueDateMillis = 0L,
            openingAmountCents = 35_00L,
            openingType = LedgerEntry.TYPE_GAVE,
            openingDescription = "Lunch split"
        )
        assertTrue(personalCreate.isSuccess)

        val shopParties = shopRepo.observeParties(userId).first()
        val personalParties = personalRepo.observeParties(userId).first()

        assertTrue(shopParties.any { it.name == "Wholesale Mart" })
        assertTrue(shopParties.none { it.name == "Zain Friend" })

        assertTrue(personalParties.any { it.name == "Zain Friend" })
        assertTrue(personalParties.none { it.name == "Wholesale Mart" })
    }

    @Test
    fun `settings supports 5 popular themes and updates reactive theme state`() {
        assertEquals(5, com.example.ui.theme.AppThemePreset.entries.size)
        com.example.ui.theme.LedgerThemeState.currentPreset = com.example.ui.theme.AppThemePreset.MIDNIGHT_DARK
        assertTrue(com.example.ui.theme.LedgerThemeState.currentPreset.isDark)
        com.example.ui.theme.LedgerThemeState.currentPreset = com.example.ui.theme.AppThemePreset.SAAS_INDIGO
        assertEquals(
            com.example.ui.theme.AppThemePreset.EMERALD_MINT,
            com.example.ui.theme.AppThemePreset.fromKey("EMERALD_MINT")
        )
    }
}
