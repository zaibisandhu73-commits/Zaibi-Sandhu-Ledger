package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.LedgerEntry
import com.example.data.LocalShopLedgerStore
import com.example.data.Party
import com.example.data.ShopLedgerRepository
import com.example.ui.ShopLedgerViewModel
import com.example.ui.ShopTab
import com.example.ui.UiState
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOut
import com.example.ui.components.AddLedgerEntryBottomSheet
import com.example.ui.components.AddOrEditPartyBottomSheet
import com.example.ui.components.PartyStatementDialog
import com.example.ui.screens.AnalyticsTabScreen
import com.example.ui.screens.AppLayoutMode
import com.example.ui.screens.AppSettingsBottomSheet
import com.example.ui.screens.DailyRegisterScreen
import com.example.ui.screens.PartiesTabScreen
import com.example.ui.screens.PartyDetailScreen
import com.example.ui.screens.PersonalPartyDetailScreen
import com.example.ui.screens.PersonalSimpleScreen
import com.example.ui.screens.SimplePersonalAddNameSheet
import com.example.ui.screens.SimplePersonalEntrySheet
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.EmeraldDarkText
import com.example.ui.theme.EmeraldReceivable
import com.example.ui.theme.EmeraldReceivableBorder
import com.example.ui.theme.EmeraldReceivableSoft
import com.example.ui.theme.LedgerThemeState
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OnSaasIndigoContainer
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val PREFS_NAME = "shop_ledger_mode_prefs"
private const val KEY_LOCAL_MODE_ENABLED = "local_mode_enabled"
private const val KEY_LAYOUT_MODE = "app_layout_mode"
private const val KEY_THEME_PRESET = "app_theme_preset"
private const val KEY_CURRENCY_SYMBOL = "app_currency_symbol"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        LedgerThemeState.currentPreset = AppThemePreset.fromKey(
            prefs.getString(KEY_THEME_PRESET, AppThemePreset.SAAS_INDIGO.key)
        )
        enableEdgeToEdge()
        setContent {
            val activeTheme = LedgerThemeState.currentPreset
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as? ComponentActivity)?.window ?: return@SideEffect
                    val controller = WindowCompat.getInsetsController(window, view)
                    controller.isAppearanceLightStatusBars = !activeTheme.isDark
                    controller.isAppearanceLightNavigationBars = !activeTheme.isDark
                }
            }
            MyApplicationTheme(themePreset = activeTheme) {
                AppNavigation()
            }
        }
    }
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = FirebaseAuth.getInstance()) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var isLocalMode by remember {
        mutableStateOf(prefs.getBoolean(KEY_LOCAL_MODE_ENABLED, false))
    }

    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    val modeState = when {
        user != null -> "cloud_${user.uid}"
        isLocalMode -> "local"
        else -> "auth"
    }

    AnimatedContent(
        targetState = modeState,
        transitionSpec = {
            (fadeIn(animationSpec = tween(240)) +
                scaleIn(
                    initialScale = 0.97f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )).togetherWith(
                fadeOut(animationSpec = tween(180))
            )
        },
        label = "AppModeTransition"
    ) { state ->
        when {
            state.startsWith("cloud_") && user != null -> {
                ShopLedgerMainScreen(
                    currentUserId = user.uid,
                    userDisplayName = user.displayName ?: user.email ?: "Cloud Synced Workspace",
                    isLocalMode = false,
                    onExitLocalMode = {}
                )
            }
            state == "local" -> {
                ShopLedgerMainScreen(
                    currentUserId = LocalShopLedgerStore.LOCAL_USER_ID,
                    userDisplayName = "Offline Local Workspace",
                    isLocalMode = true,
                    onExitLocalMode = {
                        prefs.edit().putBoolean(KEY_LOCAL_MODE_ENABLED, false).apply()
                        isLocalMode = false
                    }
                )
            }
            else -> {
                AuthScreen(
                    onAuthSuccess = {
                        prefs.edit().putBoolean(KEY_LOCAL_MODE_ENABLED, false).apply()
                        isLocalMode = false
                    },
                    onSkipToLocalUse = {
                        prefs.edit().putBoolean(KEY_LOCAL_MODE_ENABLED, true).apply()
                        isLocalMode = true
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopLedgerMainScreen(
    currentUserId: String,
    userDisplayName: String,
    isLocalMode: Boolean = false,
    onExitLocalMode: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var layoutMode by remember {
        mutableStateOf(AppLayoutMode.fromKey(prefs.getString(KEY_LAYOUT_MODE, AppLayoutMode.SHOP.key)))
    }
    var savedCurrencySymbol by remember {
        mutableStateOf(prefs.getString(KEY_CURRENCY_SYMBOL, "$") ?: "$")
    }
    val isPersonalLedger = layoutMode == AppLayoutMode.PERSONAL

    val baseSessionKey = if (isLocalMode) "local_session" else "cloud_$currentUserId"
    val sessionKey = "${baseSessionKey}_${layoutMode.key}"
    val viewModel: ShopLedgerViewModel = viewModel(
        key = sessionKey,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val initialCurrency = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .getString(KEY_CURRENCY_SYMBOL, "$") ?: "$"
                val repository = if (isLocalMode) {
                    val fileName = if (isPersonalLedger) {
                        LocalShopLedgerStore.PERSONAL_STORAGE_FILE
                    } else {
                        LocalShopLedgerStore.SHOP_STORAGE_FILE
                    }
                    ShopLedgerRepository(
                        db = null,
                        localStore = LocalShopLedgerStore(app, fileName)
                    )
                } else {
                    val databaseId = app.getString(R.string.firestore_database_id)
                    val db = FirebaseFirestore.getInstance(databaseId)
                    ShopLedgerRepository(db = db)
                }
                ShopLedgerViewModel(
                    repository = repository,
                    currentUserId = currentUserId,
                    isPersonalLedger = isPersonalLedger,
                    initialCurrencySymbol = initialCurrency
                )
            }
        }
    )

    LaunchedEffect(viewModel, savedCurrencySymbol) {
        viewModel.updateCurrencySymbol(savedCurrencySymbol)
    }

    val partiesUiState by viewModel.partiesState.collectAsStateWithLifecycle()
    val entriesUiState by viewModel.allEntriesState.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedPartyId by viewModel.selectedPartyId.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val partyFilter by viewModel.partyFilter.collectAsStateWithLifecycle()
    val partySortOrder by viewModel.partySortOrder.collectAsStateWithLifecycle()
    val registerFilter by viewModel.registerFilter.collectAsStateWithLifecycle()
    val registerSearchQuery by viewModel.registerSearchQuery.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddPartySheet by remember { mutableStateOf(false) }
    var partyBeingEdited by remember { mutableStateOf<Party?>(null) }
    var activeEntrySheetRequest by remember { mutableStateOf<Pair<Party, String>?>(null) }
    var statementParty by remember { mutableStateOf<Party?>(null) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    val parties = (partiesUiState as? UiState.Success)?.data ?: emptyList()
    val allEntries = (entriesUiState as? UiState.Success)?.data ?: emptyList()
    val summaryStats = remember(parties) { viewModel.computeSummaryStats(parties) }
    val filteredParties = remember(parties, searchQuery, partyFilter, partySortOrder) {
        viewModel.filterAndSortParties(parties, searchQuery, partyFilter, partySortOrder)
    }
    val selectedParty = remember(parties, selectedPartyId) {
        parties.firstOrNull { it.id == selectedPartyId }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(SaasBackground)
    ) {
        val isExpandedScreen = maxWidth >= 720.dp

        if (!isExpandedScreen) {
            // Smooth SaaS Drill-Down Transition between Main Register / Personal View and Detail View
            AnimatedContent(
                targetState = selectedParty,
                transitionSpec = {
                    if (targetState != null && initialState == null) {
                        (slideInHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            initialOffsetX = { fullWidth -> fullWidth / 3 }
                        ) + fadeIn(tween(220))).togetherWith(
                            slideOutHorizontally(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                targetOffsetX = { fullWidth -> -fullWidth / 6 }
                            ) + fadeOut(tween(160))
                        )
                    } else if (targetState == null && initialState != null) {
                        (slideInHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            initialOffsetX = { fullWidth -> -fullWidth / 4 }
                        ) + fadeIn(tween(220))).togetherWith(
                            slideOutHorizontally(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                targetOffsetX = { fullWidth -> fullWidth / 3 }
                            ) + fadeOut(tween(160))
                        )
                    } else {
                        fadeIn(tween(180)) togetherWith fadeOut(tween(150))
                    }.using(SizeTransform(clip = false))
                },
                label = "MobileScreenDrillTransition"
            ) { activeParty ->
                if (activeParty != null) {
                    val activePartyEntries = remember(allEntries, activeParty.id) {
                        allEntries.filter { it.partyId == activeParty.id }
                    }
                    Scaffold(
                        containerColor = SaasBackground,
                        snackbarHost = { SnackbarHost(snackbarHostState) }
                    ) { innerPadding ->
                        if (layoutMode == AppLayoutMode.PERSONAL) {
                            PersonalPartyDetailScreen(
                                party = activeParty,
                                partyEntries = activePartyEntries,
                                currencySymbol = currencySymbol,
                                showBackButton = true,
                                onBack = { viewModel.selectParty(null) },
                                onAddEntryClick = { entryType ->
                                    activeEntrySheetRequest = activeParty to entryType
                                },
                                onDeletePartyClick = {
                                    viewModel.deleteParty(activeParty, activePartyEntries)
                                },
                                onDeleteEntryClick = { entry ->
                                    viewModel.deleteEntry(activeParty, entry)
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        } else {
                            PartyDetailScreen(
                                party = activeParty,
                                partyEntries = activePartyEntries,
                                currencySymbol = currencySymbol,
                                showBackButton = true,
                                onBack = { viewModel.selectParty(null) },
                                onAddEntryClick = { entryType ->
                                    activeEntrySheetRequest = activeParty to entryType
                                },
                                onEditPartyClick = {
                                    partyBeingEdited = activeParty
                                },
                                onDeletePartyClick = {
                                    viewModel.deleteParty(activeParty, activePartyEntries)
                                },
                                onSettleBalanceClick = { mode ->
                                    viewModel.settleParty(activeParty, mode)
                                },
                                onDeleteEntryClick = { entry ->
                                    viewModel.deleteEntry(activeParty, entry)
                                },
                                onShareStatementClick = {
                                    statementParty = activeParty
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                } else {
                    SaasMainScaffold(
                        userDisplayName = userDisplayName,
                        isLocalMode = isLocalMode,
                        isExpandedScreen = false,
                        layoutMode = layoutMode,
                        selectedTab = selectedTab,
                        currencySymbol = currencySymbol,
                        onOpenSettings = { showSettingsSheet = true },
                        onSelectTab = viewModel::selectTab,
                        onAddPartyClick = { showAddPartySheet = true },
                        snackbarHostState = snackbarHostState,
                        isBusy = isBusy,
                        partiesUiState = partiesUiState,
                        parties = parties,
                        filteredParties = filteredParties,
                        allEntries = allEntries,
                        summaryStats = summaryStats,
                        searchQuery = searchQuery,
                        partyFilter = partyFilter,
                        partySortOrder = partySortOrder,
                        selectedPartyId = selectedPartyId,
                        selectedParty = selectedParty,
                        registerFilter = registerFilter,
                        registerSearchQuery = registerSearchQuery,
                        onSearchQueryChange = viewModel::updateSearchQuery,
                        onFilterChange = viewModel::updatePartyFilter,
                        onSortChange = viewModel::updatePartySortOrder,
                        onPartyClick = { viewModel.selectParty(it.id) },
                        onQuickEntryClick = { party, type ->
                            activeEntrySheetRequest = party to type
                        },
                        onSeedSampleDataClick = viewModel::seedSampleShopRegister,
                        onEditPartyClick = { partyBeingEdited = it },
                        onDeletePartyClick = { p, entries -> viewModel.deleteParty(p, entries) },
                        onSettleBalanceClick = { p, mode -> viewModel.settleParty(p, mode) },
                        onDeleteEntryClick = { p, e -> viewModel.deleteEntry(p, e) },
                        onShareStatementClick = { statementParty = it },
                        onRegisterFilterChange = viewModel::updateRegisterFilter,
                        onRegisterSearchQueryChange = viewModel::updateRegisterSearchQuery,
                        onEntryPartyClick = { pId ->
                            viewModel.selectParty(pId)
                        }
                    )
                }
            }
        } else {
            SaasMainScaffold(
                userDisplayName = userDisplayName,
                isLocalMode = isLocalMode,
                isExpandedScreen = true,
                layoutMode = layoutMode,
                selectedTab = selectedTab,
                currencySymbol = currencySymbol,
                onOpenSettings = { showSettingsSheet = true },
                onSelectTab = viewModel::selectTab,
                onAddPartyClick = { showAddPartySheet = true },
                snackbarHostState = snackbarHostState,
                isBusy = isBusy,
                partiesUiState = partiesUiState,
                parties = parties,
                filteredParties = filteredParties,
                allEntries = allEntries,
                summaryStats = summaryStats,
                searchQuery = searchQuery,
                partyFilter = partyFilter,
                partySortOrder = partySortOrder,
                selectedPartyId = selectedPartyId,
                selectedParty = selectedParty,
                registerFilter = registerFilter,
                registerSearchQuery = registerSearchQuery,
                onSearchQueryChange = viewModel::updateSearchQuery,
                onFilterChange = viewModel::updatePartyFilter,
                onSortChange = viewModel::updatePartySortOrder,
                onPartyClick = { viewModel.selectParty(it.id) },
                onQuickEntryClick = { party, type ->
                    activeEntrySheetRequest = party to type
                },
                onSeedSampleDataClick = viewModel::seedSampleShopRegister,
                onEditPartyClick = { partyBeingEdited = it },
                onDeletePartyClick = { p, entries -> viewModel.deleteParty(p, entries) },
                onSettleBalanceClick = { p, mode -> viewModel.settleParty(p, mode) },
                onDeleteEntryClick = { p, e -> viewModel.deleteEntry(p, e) },
                onShareStatementClick = { statementParty = it },
                onRegisterFilterChange = viewModel::updateRegisterFilter,
                onRegisterSearchQueryChange = viewModel::updateRegisterSearchQuery,
                onEntryPartyClick = { pId ->
                    viewModel.selectTab(ShopTab.PARTIES)
                    viewModel.selectParty(pId)
                }
            )
        }
    }

    if (showSettingsSheet) {
        AppSettingsBottomSheet(
            currentLayoutMode = layoutMode,
            currentThemePreset = LedgerThemeState.currentPreset,
            currencySymbol = currencySymbol,
            isLocalMode = isLocalMode,
            userDisplayName = userDisplayName,
            onSelectLayoutMode = { newMode ->
                if (layoutMode != newMode) {
                    viewModel.selectParty(null)
                    activeEntrySheetRequest = null
                    partyBeingEdited = null
                    statementParty = null
                    showAddPartySheet = false
                    layoutMode = newMode
                    prefs.edit().putString(KEY_LAYOUT_MODE, newMode.key).apply()
                }
            },
            onSelectThemePreset = { newPreset ->
                LedgerThemeState.currentPreset = newPreset
                prefs.edit().putString(KEY_THEME_PRESET, newPreset.key).apply()
            },
            onSelectCurrency = { symbol ->
                savedCurrencySymbol = symbol
                prefs.edit().putString(KEY_CURRENCY_SYMBOL, symbol).apply()
                viewModel.updateCurrencySymbol(symbol)
            },
            onSwitchToCloudLogin = onExitLocalMode,
            onSignOut = {
                signOut(
                    context = context,
                    credentialManager = credentialManager,
                    onSignOutComplete = {},
                    scope = scope
                )
            },
            onDismiss = { showSettingsSheet = false }
        )
    }

    if (showAddPartySheet || partyBeingEdited != null) {
        if (layoutMode == AppLayoutMode.PERSONAL && partyBeingEdited == null) {
            SimplePersonalAddNameSheet(
                currencySymbol = currencySymbol,
                onDismiss = { showAddPartySheet = false },
                onSave = { name, amountCents, type, reason ->
                    viewModel.createNewParty(
                        name = name,
                        phone = "",
                        role = Party.ROLE_CUSTOMER,
                        notes = "",
                        dueDateMillis = 0L,
                        openingAmountCents = amountCents,
                        openingType = type,
                        openingDescription = reason,
                        onCreated = {}
                    )
                }
            )
        } else {
            AddOrEditPartyBottomSheet(
                existingParty = partyBeingEdited,
                currencySymbol = currencySymbol,
                onDismiss = {
                    showAddPartySheet = false
                    partyBeingEdited = null
                },
                onSaveNew = { name, phone, role, notes, dueMillis, openCents, openType, openDesc ->
                    viewModel.createNewParty(
                        name = name,
                        phone = phone,
                        role = role,
                        notes = notes,
                        dueDateMillis = dueMillis,
                        openingAmountCents = openCents,
                        openingType = openType,
                        openingDescription = openDesc,
                        onCreated = { newPartyId ->
                            viewModel.selectParty(newPartyId)
                        }
                    )
                },
                onSaveEdit = { name, phone, role, notes, dueMillis ->
                    partyBeingEdited?.let { existing ->
                        viewModel.updateParty(existing, name, phone, role, notes, dueMillis)
                    }
                }
            )
        }
    }

    activeEntrySheetRequest?.let { (targetParty, initialType) ->
        if (layoutMode == AppLayoutMode.PERSONAL) {
            SimplePersonalEntrySheet(
                party = targetParty,
                initialType = initialType,
                currencySymbol = currencySymbol,
                onDismiss = { activeEntrySheetRequest = null },
                onSaveEntry = { type, amountCents, reason ->
                    viewModel.recordEntry(
                        party = targetParty,
                        type = type,
                        amountCents = amountCents,
                        description = reason,
                        category = if (type == LedgerEntry.TYPE_GAVE) "Given" else "Taken",
                        paymentMode = LedgerEntry.MODE_CASH,
                        referenceNote = "",
                        entryDateMillis = System.currentTimeMillis()
                    )
                }
            )
        } else {
            AddLedgerEntryBottomSheet(
                party = targetParty,
                initialType = initialType,
                currencySymbol = currencySymbol,
                onDismiss = { activeEntrySheetRequest = null },
                onSaveEntry = { type, amountCents, desc, category, mode, refNote, entryDateMillis ->
                    viewModel.recordEntry(
                        party = targetParty,
                        type = type,
                        amountCents = amountCents,
                        description = desc,
                        category = category,
                        paymentMode = mode,
                        referenceNote = refNote,
                        entryDateMillis = entryDateMillis
                    )
                }
            )
        }
    }

    statementParty?.let { targetParty ->
        val entriesForStatement = allEntries.filter { it.partyId == targetParty.id }
        PartyStatementDialog(
            party = targetParty,
            entries = entriesForStatement,
            currencySymbol = currencySymbol,
            onDismiss = { statementParty = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaasMainScaffold(
    userDisplayName: String,
    isLocalMode: Boolean,
    isExpandedScreen: Boolean,
    layoutMode: AppLayoutMode,
    selectedTab: ShopTab,
    currencySymbol: String,
    onOpenSettings: () -> Unit,
    onSelectTab: (ShopTab) -> Unit,
    onAddPartyClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    isBusy: Boolean,
    partiesUiState: UiState<List<Party>>,
    parties: List<Party>,
    filteredParties: List<Party>,
    allEntries: List<LedgerEntry>,
    summaryStats: com.example.data.ShopSummaryStats,
    searchQuery: String,
    partyFilter: com.example.ui.PartyFilter,
    partySortOrder: com.example.ui.PartySortOrder,
    selectedPartyId: String?,
    selectedParty: Party?,
    registerFilter: com.example.ui.RegisterFilter,
    registerSearchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (com.example.ui.PartyFilter) -> Unit,
    onSortChange: (com.example.ui.PartySortOrder) -> Unit,
    onPartyClick: (Party) -> Unit,
    onQuickEntryClick: (Party, String) -> Unit,
    onSeedSampleDataClick: () -> Unit,
    onEditPartyClick: (Party) -> Unit,
    onDeletePartyClick: (Party, List<LedgerEntry>) -> Unit,
    onSettleBalanceClick: (Party, String) -> Unit,
    onDeleteEntryClick: (Party, LedgerEntry) -> Unit,
    onShareStatementClick: (Party) -> Unit,
    onRegisterFilterChange: (com.example.ui.RegisterFilter) -> Unit,
    onRegisterSearchQueryChange: (String) -> Unit,
    onEntryPartyClick: (String) -> Unit
) {
    Scaffold(
        containerColor = SaasBackground,
        topBar = {
            Column(modifier = Modifier.background(SaasSurface)) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // SaaS Brand Icon Container
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SaasIndigoContainer)
                                    .border(1.dp, SaasIndigoBorder, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = SaasIndigoPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 1.dp)
                                ) {
                                    Text(
                                        text = "Ledger",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SaasInkPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    // Layout Mode Badge (Clickable to open Settings)
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = SaasIndigoContainer,
                                        border = BorderStroke(1.dp, SaasIndigoBorder),
                                        onClick = onOpenSettings,
                                        modifier = Modifier.testTag("layout_mode_badge")
                                    ) {
                                        Text(
                                            text = layoutMode.badgeLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = OnSaasIndigoContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    // SaaS Workspace Status Pill
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isLocalMode) SaasSurfaceSubtle else EmeraldReceivableSoft,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isLocalMode) SaasBorder else EmeraldReceivableBorder
                                        )
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isLocalMode) SaasInkMuted else EmeraldReceivable)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isLocalMode) "Local" else "Cloud",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isLocalMode) SaasInkSecondary else EmeraldDarkText
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = userDisplayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SaasInkMuted
                                )
                            }
                        }
                    },
                    actions = {
                        // Settings Button (Opens Layout, Currency & Account Settings)
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier.testTag("settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Settings",
                                tint = SaasInkSecondary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SaasSurface,
                        titleContentColor = SaasInkPrimary,
                        actionIconContentColor = SaasInkSecondary
                    )
                )
                HorizontalDivider(thickness = 1.dp, color = SaasBorder)
            }
        },
        bottomBar = {
            if (!isExpandedScreen && layoutMode == AppLayoutMode.SHOP) {
                Column(modifier = Modifier.background(SaasSurface)) {
                    HorizontalDivider(thickness = 1.dp, color = SaasBorder)
                    NavigationBar(
                        containerColor = SaasSurface,
                        contentColor = SaasInkSecondary,
                        tonalElevation = 0.dp
                    ) {
                        val navColors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SaasIndigoPrimary,
                            selectedTextColor = SaasIndigoPrimary,
                            indicatorColor = SaasIndigoContainer,
                            unselectedIconColor = SaasInkMuted,
                            unselectedTextColor = SaasInkMuted
                        )
                        NavigationBarItem(
                            selected = selectedTab == ShopTab.PARTIES,
                            onClick = { onSelectTab(ShopTab.PARTIES) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == ShopTab.PARTIES) Icons.Filled.People else Icons.Outlined.People,
                                    contentDescription = "Accounts"
                                )
                            },
                            label = {
                                Text(
                                    text = "Accounts",
                                    fontWeight = if (selectedTab == ShopTab.PARTIES) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = navColors,
                            modifier = Modifier.testTag("nav_tab_parties")
                        )
                        NavigationBarItem(
                            selected = selectedTab == ShopTab.REGISTER,
                            onClick = { onSelectTab(ShopTab.REGISTER) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == ShopTab.REGISTER) Icons.Filled.Book else Icons.Outlined.Book,
                                    contentDescription = "Daily Register"
                                )
                            },
                            label = {
                                Text(
                                    text = "Register",
                                    fontWeight = if (selectedTab == ShopTab.REGISTER) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = navColors,
                            modifier = Modifier.testTag("nav_tab_register")
                        )
                        NavigationBarItem(
                            selected = selectedTab == ShopTab.ANALYTICS,
                            onClick = { onSelectTab(ShopTab.ANALYTICS) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == ShopTab.ANALYTICS) Icons.Filled.Analytics else Icons.Outlined.Analytics,
                                    contentDescription = "Summary & Reports"
                                )
                            },
                            label = {
                                Text(
                                    text = "Summary",
                                    fontWeight = if (selectedTab == ShopTab.ANALYTICS) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = navColors,
                            modifier = Modifier.testTag("nav_tab_analytics")
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = layoutMode == AppLayoutMode.PERSONAL || selectedTab == ShopTab.PARTIES,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(tween(180)),
                exit = scaleOut(tween(150)) + fadeOut(tween(150))
            ) {
                ExtendedFloatingActionButton(
                    onClick = onAddPartyClick,
                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                    text = {
                        Text(
                            text = "Add Name",
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    containerColor = SaasIndigoPrimary,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 3.dp,
                        pressedElevation = 6.dp
                    ),
                    modifier = Modifier.testTag("add_party_fab")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SaasBackground)
                .padding(innerPadding)
        ) {
            AnimatedVisibility(visible = isBusy) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = SaasIndigoPrimary,
                    trackColor = SaasIndigoContainer
                )
            }

            when (val pState = partiesUiState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = SaasIndigoPrimary)
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = pState.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                is UiState.Success -> {
                    AnimatedContent(
                        targetState = layoutMode,
                        transitionSpec = {
                            (fadeIn(tween(220)) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )).togetherWith(fadeOut(tween(160)))
                        },
                        label = "LayoutModeSwitchAnimation"
                    ) { activeLayoutMode ->
                        if (activeLayoutMode == AppLayoutMode.PERSONAL) {
                            if (isExpandedScreen) {
                                Row(modifier = Modifier.fillMaxSize()) {
                                    PersonalSimpleScreen(
                                        parties = parties,
                                        summaryStats = summaryStats,
                                        selectedPartyId = selectedPartyId,
                                        currencySymbol = currencySymbol,
                                        onPartyClick = onPartyClick,
                                        onAddPartyClick = onAddPartyClick,
                                        onSeedSampleDataClick = onSeedSampleDataClick,
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    )

                                    VerticalDivider(color = SaasBorder)

                                    Box(
                                        modifier = Modifier
                                            .weight(1.2f)
                                            .fillMaxHeight()
                                    ) {
                                        val detailParty = selectedParty ?: parties.firstOrNull()
                                        if (detailParty != null) {
                                            val detailEntries = allEntries.filter { it.partyId == detailParty.id }
                                            PersonalPartyDetailScreen(
                                                party = detailParty,
                                                partyEntries = detailEntries,
                                                currencySymbol = currencySymbol,
                                                showBackButton = false,
                                                onBack = {},
                                                onAddEntryClick = { entryType ->
                                                    onQuickEntryClick(detailParty, entryType)
                                                },
                                                onDeletePartyClick = {
                                                    onDeletePartyClick(detailParty, detailEntries)
                                                },
                                                onDeleteEntryClick = { entry ->
                                                    onDeleteEntryClick(detailParty, entry)
                                                }
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "Tap any name box to view entries",
                                                    color = SaasInkMuted
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                PersonalSimpleScreen(
                                    parties = parties,
                                    summaryStats = summaryStats,
                                    selectedPartyId = selectedPartyId,
                                    currencySymbol = currencySymbol,
                                    onPartyClick = onPartyClick,
                                    onAddPartyClick = onAddPartyClick,
                                    onSeedSampleDataClick = onSeedSampleDataClick
                                )
                            }
                        } else {
                            // SHOP LAYOUT
                            if (isExpandedScreen) {
                                Row(modifier = Modifier.fillMaxSize()) {
                                    val railColors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = SaasIndigoPrimary,
                                        selectedTextColor = SaasIndigoPrimary,
                                        indicatorColor = SaasIndigoContainer,
                                        unselectedIconColor = SaasInkMuted,
                                        unselectedTextColor = SaasInkMuted
                                    )
                                    NavigationRail(
                                        containerColor = SaasSurface,
                                        contentColor = SaasInkSecondary
                                    ) {
                                        NavigationRailItem(
                                            selected = selectedTab == ShopTab.PARTIES,
                                            onClick = { onSelectTab(ShopTab.PARTIES) },
                                            icon = { Icon(Icons.Default.People, contentDescription = "Accounts") },
                                            label = { Text("Accounts") },
                                            colors = railColors
                                        )
                                        NavigationRailItem(
                                            selected = selectedTab == ShopTab.REGISTER,
                                            onClick = { onSelectTab(ShopTab.REGISTER) },
                                            icon = { Icon(Icons.Default.Book, contentDescription = "Register") },
                                            label = { Text("Register") },
                                            colors = railColors
                                        )
                                        NavigationRailItem(
                                            selected = selectedTab == ShopTab.ANALYTICS,
                                            onClick = { onSelectTab(ShopTab.ANALYTICS) },
                                            icon = { Icon(Icons.Default.Analytics, contentDescription = "Summary") },
                                            label = { Text("Summary") },
                                            colors = railColors
                                        )
                                    }

                                    VerticalDivider(color = SaasBorder)

                                    AnimatedContent(
                                        targetState = selectedTab,
                                        transitionSpec = {
                                            val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                                            (slideInHorizontally(
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                                    stiffness = Spring.StiffnessMediumLow
                                                ),
                                                initialOffsetX = { fullWidth -> direction * (fullWidth / 8) }
                                            ) + fadeIn(tween(200))).togetherWith(
                                                slideOutHorizontally(
                                                    animationSpec = tween(160),
                                                    targetOffsetX = { fullWidth -> -direction * (fullWidth / 10) }
                                                ) + fadeOut(tween(150))
                                            )
                                        },
                                        label = "ExpandedTabTransition"
                                    ) { tab ->
                                        when (tab) {
                                            ShopTab.PARTIES -> {
                                                Row(modifier = Modifier.fillMaxSize()) {
                                                    PartiesTabScreen(
                                                        parties = filteredParties,
                                                        summaryStats = summaryStats,
                                                        searchQuery = searchQuery,
                                                        selectedFilter = partyFilter,
                                                        selectedSort = partySortOrder,
                                                        selectedPartyId = selectedPartyId,
                                                        currencySymbol = currencySymbol,
                                                        onSearchQueryChange = onSearchQueryChange,
                                                        onFilterChange = onFilterChange,
                                                        onSortChange = onSortChange,
                                                        onPartyClick = onPartyClick,
                                                        onQuickEntryClick = onQuickEntryClick,
                                                        onAddPartyClick = onAddPartyClick,
                                                        onSeedSampleDataClick = onSeedSampleDataClick,
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .fillMaxHeight()
                                                    )

                                                    VerticalDivider(color = SaasBorder)

                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1.2f)
                                                            .fillMaxHeight()
                                                    ) {
                                                        val detailParty = selectedParty ?: filteredParties.firstOrNull()
                                                        if (detailParty != null) {
                                                            val detailEntries = allEntries.filter { it.partyId == detailParty.id }
                                                            PartyDetailScreen(
                                                                party = detailParty,
                                                                partyEntries = detailEntries,
                                                                currencySymbol = currencySymbol,
                                                                showBackButton = false,
                                                                onBack = {},
                                                                onAddEntryClick = { entryType ->
                                                                    onQuickEntryClick(detailParty, entryType)
                                                                },
                                                                onEditPartyClick = { onEditPartyClick(detailParty) },
                                                                onDeletePartyClick = {
                                                                    onDeletePartyClick(detailParty, detailEntries)
                                                                },
                                                                onSettleBalanceClick = { mode ->
                                                                    onSettleBalanceClick(detailParty, mode)
                                                                },
                                                                onDeleteEntryClick = { entry ->
                                                                    onDeleteEntryClick(detailParty, entry)
                                                                },
                                                                onShareStatementClick = {
                                                                    onShareStatementClick(detailParty)
                                                                }
                                                            )
                                                        } else {
                                                            Box(
                                                                modifier = Modifier.fillMaxSize(),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(
                                                                    text = "Select an account to view detailed ledger entries",
                                                                    color = SaasInkMuted
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            ShopTab.REGISTER -> {
                                                DailyRegisterScreen(
                                                    allEntries = allEntries,
                                                    selectedFilter = registerFilter,
                                                    searchQuery = registerSearchQuery,
                                                    currencySymbol = currencySymbol,
                                                    onFilterChange = onRegisterFilterChange,
                                                    onSearchQueryChange = onRegisterSearchQueryChange,
                                                    onEntryPartyClick = onEntryPartyClick
                                                )
                                            }
                                            ShopTab.ANALYTICS -> {
                                                AnalyticsTabScreen(
                                                    parties = parties,
                                                    allEntries = allEntries,
                                                    summaryStats = summaryStats,
                                                    currencySymbol = currencySymbol,
                                                    onPartyClick = { party ->
                                                        onEntryPartyClick(party.id)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Compact Screen SaaS Tab Transition
                                AnimatedContent(
                                    targetState = selectedTab,
                                    transitionSpec = {
                                        val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                                        (slideInHorizontally(
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioNoBouncy,
                                                stiffness = Spring.StiffnessMediumLow
                                            ),
                                            initialOffsetX = { fullWidth -> direction * (fullWidth / 6) }
                                        ) + fadeIn(tween(210))).togetherWith(
                                            slideOutHorizontally(
                                                animationSpec = tween(160),
                                                targetOffsetX = { fullWidth -> -direction * (fullWidth / 8) }
                                            ) + fadeOut(tween(150))
                                        )
                                    },
                                    label = "MobileTabTransition"
                                ) { tab ->
                                    when (tab) {
                                        ShopTab.PARTIES -> {
                                            PartiesTabScreen(
                                                parties = filteredParties,
                                                summaryStats = summaryStats,
                                                searchQuery = searchQuery,
                                                selectedFilter = partyFilter,
                                                selectedSort = partySortOrder,
                                                selectedPartyId = selectedPartyId,
                                                currencySymbol = currencySymbol,
                                                onSearchQueryChange = onSearchQueryChange,
                                                onFilterChange = onFilterChange,
                                                onSortChange = onSortChange,
                                                onPartyClick = onPartyClick,
                                                onQuickEntryClick = onQuickEntryClick,
                                                onAddPartyClick = onAddPartyClick,
                                                onSeedSampleDataClick = onSeedSampleDataClick
                                            )
                                        }
                                        ShopTab.REGISTER -> {
                                            DailyRegisterScreen(
                                                allEntries = allEntries,
                                                selectedFilter = registerFilter,
                                                searchQuery = registerSearchQuery,
                                                currencySymbol = currencySymbol,
                                                onFilterChange = onRegisterFilterChange,
                                                onSearchQueryChange = onRegisterSearchQueryChange,
                                                onEntryPartyClick = onEntryPartyClick
                                            )
                                        }
                                        ShopTab.ANALYTICS -> {
                                            AnalyticsTabScreen(
                                                parties = parties,
                                                allEntries = allEntries,
                                                summaryStats = summaryStats,
                                                currencySymbol = currencySymbol,
                                                onPartyClick = { party ->
                                                    onPartyClick(party)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
