package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Storefront
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.Party
import com.example.data.ShopSummaryStats
import com.example.ui.ShopLedgerViewModel
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.EmeraldDarkText
import com.example.ui.theme.EmeraldReceivable
import com.example.ui.theme.EmeraldReceivableBorder
import com.example.ui.theme.EmeraldReceivableSoft
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LedgerThemeState
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

enum class AppLayoutMode(
    val key: String,
    val title: String,
    val badgeLabel: String,
    val description: String
) {
    SHOP(
        key = "SHOP",
        title = "Shop Layout",
        badgeLabel = "Shop",
        description = "Full business register with accounts, filters, daily cashbook, and summary reports."
    ),
    PERSONAL(
        key = "PERSONAL",
        title = "Personal Simple Layout",
        badgeLabel = "Personal",
        description = "Simple SaaS glass boxes for totals and names with quick name, amount & reason entries."
    );

    companion object {
        fun fromKey(key: String?): AppLayoutMode {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: SHOP
        }
    }
}

@Composable
fun PersonalSimpleScreen(
    parties: List<Party>,
    summaryStats: ShopSummaryStats,
    selectedPartyId: String?,
    currencySymbol: String,
    onPartyClick: (Party) -> Unit,
    onAddPartyClick: () -> Unit,
    onSeedSampleDataClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SaasBackground)
            .testTag("personal_simple_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Upper Two SaaS Glass Rectangles: ONLY Total Amount fills each box (Green = Given, Red = Taken)
        item(key = "personal_top_totals") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Left Rectangle: Green SaaS Glass Box (Only Total Amount inside)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = EmeraldReceivableSoft,
                    border = BorderStroke(1.5.dp, EmeraldReceivableBorder),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(104.dp)
                        .testTag("personal_given_total_box")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        SaasSurface.copy(alpha = 0.65f),
                                        EmeraldReceivableSoft,
                                        EmeraldReceivableBorder.copy(alpha = 0.35f)
                                    )
                                )
                            )
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ShopLedgerViewModel.formatMoney(
                                summaryStats.totalReceivableCents,
                                currencySymbol
                            ),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = EmeraldReceivable,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Right Rectangle: Red SaaS Glass Box (Only Total Amount inside)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = RubyPayableSoft,
                    border = BorderStroke(1.5.dp, RubyPayableBorder),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(104.dp)
                        .testTag("personal_taken_total_box")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        SaasSurface.copy(alpha = 0.65f),
                                        RubyPayableSoft,
                                        RubyPayableBorder.copy(alpha = 0.35f)
                                    )
                                )
                            )
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ShopLedgerViewModel.formatMoney(
                                summaryStats.totalPayableCents,
                                currencySymbol
                            ),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = RubyPayable,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        if (parties.isEmpty()) {
            item(key = "personal_empty_state") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .border(1.dp, SaasBorder, RoundedCornerShape(20.dp))
                        .animateItem(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SaasSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_empty_ledger_illustration),
                            contentDescription = "Empty personal ledger",
                            modifier = Modifier.size(120.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Names Added Yet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SaasInkPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Add a name, amount, and reason. Names who owe you appear in green; names you owe appear in red.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SaasInkSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onAddPartyClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SaasIndigoPrimary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("personal_empty_add_button")
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add First Name", fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onSeedSampleDataClick,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, SaasBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("personal_seed_sample_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dataset,
                                contentDescription = null,
                                tint = SaasIndigoPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Load Sample Names", color = SaasInkPrimary)
                        }
                    }
                }
            }
        } else {
            items(parties, key = { it.id }) { party ->
                PersonalPartyBox(
                    party = party,
                    isSelected = party.id == selectedPartyId,
                    currencySymbol = currencySymbol,
                    onClick = { onPartyClick(party) },
                    modifier = Modifier.animateItem(
                        fadeInSpec = tween(220),
                        fadeOutSpec = tween(160),
                        placementSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                )
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun PersonalPartyBox(
    party: Party,
    isSelected: Boolean,
    currencySymbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val net = party.netBalanceCents
    val someoneOwesYou = net > 0L
    val youOweSomeone = net < 0L

    // Light SaaS Glass Tinted Card: Green glass when someone owes you, Red glass when you owe someone
    val targetBaseColor = when {
        someoneOwesYou -> EmeraldReceivableSoft
        youOweSomeone -> RubyPayableSoft
        else -> SaasSurface
    }
    val boxColor by animateColorAsState(
        targetValue = targetBaseColor,
        animationSpec = tween(240),
        label = "PersonalBoxBgColor"
    )

    val borderColor = when {
        isSelected -> SaasIndigoPrimary
        someoneOwesYou -> EmeraldReceivableBorder
        youOweSomeone -> RubyPayableBorder
        else -> SaasBorder
    }

    val nameColor = when {
        someoneOwesYou -> EmeraldDarkText
        youOweSomeone -> RubyDarkText
        else -> SaasInkPrimary
    }

    val amountColor = when {
        someoneOwesYou -> EmeraldReceivable
        youOweSomeone -> RubyPayable
        else -> SaasInkMuted
    }

    val glassBrush = when {
        someoneOwesYou -> Brush.verticalGradient(
            colors = listOf(
                SaasSurface.copy(alpha = 0.65f),
                EmeraldReceivableSoft,
                EmeraldReceivableBorder.copy(alpha = 0.30f)
            )
        )
        youOweSomeone -> Brush.verticalGradient(
            colors = listOf(
                SaasSurface.copy(alpha = 0.65f),
                RubyPayableSoft,
                RubyPayableBorder.copy(alpha = 0.30f)
            )
        )
        else -> Brush.verticalGradient(
            colors = listOf(SaasSurface, SaasSurfaceSubtle.copy(alpha = 0.5f))
        )
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = boxColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.5.dp, borderColor),
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .testTag("personal_party_box_${party.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(glassBrush)
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = party.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = nameColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = ShopLedgerViewModel.formatMoney(abs(net), currencySymbol),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = amountColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open detailed entries",
                tint = amountColor.copy(alpha = 0.75f),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(22.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsBottomSheet(
    currentLayoutMode: AppLayoutMode,
    currentThemePreset: AppThemePreset = LedgerThemeState.currentPreset,
    currencySymbol: String,
    isLocalMode: Boolean,
    userDisplayName: String,
    onSelectLayoutMode: (AppLayoutMode) -> Unit,
    onSelectThemePreset: (AppThemePreset) -> Unit = { LedgerThemeState.currentPreset = it },
    onSelectCurrency: (String) -> Unit,
    onSwitchToCloudLogin: () -> Unit,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                .testTag("settings_bottom_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = SaasInkPrimary
                    )
                    Text(
                        text = "Switch layout, customize theme, change currency & account",
                        style = MaterialTheme.typography.bodySmall,
                        color = SaasInkSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close settings",
                        tint = SaasInkSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "CHOOSE APP LAYOUT",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = SaasInkMuted
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Option 1: Shop Layout
            LayoutModeOptionCard(
                mode = AppLayoutMode.SHOP,
                icon = Icons.Default.Storefront,
                selected = currentLayoutMode == AppLayoutMode.SHOP,
                onClick = { onSelectLayoutMode(AppLayoutMode.SHOP) },
                testTag = "layout_option_shop"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Option 2: Personal Simple Layout
            LayoutModeOptionCard(
                mode = AppLayoutMode.PERSONAL,
                icon = Icons.Default.Person,
                selected = currentLayoutMode == AppLayoutMode.PERSONAL,
                onClick = { onSelectLayoutMode(AppLayoutMode.PERSONAL) },
                testTag = "layout_option_personal"
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = SaasBorder)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "APP THEME (5 POPULAR THEMES)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = SaasInkMuted
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                AppThemePreset.entries.forEach { preset ->
                    ThemePresetOptionRow(
                        preset = preset,
                        selected = currentThemePreset == preset,
                        onClick = { onSelectThemePreset(preset) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = SaasBorder)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "CURRENCY SELECTION",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = SaasInkMuted
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "$" to "USD ($)",
                    "₹" to "INR (₹)",
                    "₨" to "PKR (₨)",
                    "£" to "GBP (£)",
                    "€" to "EUR (€)",
                    "AED " to "AED"
                ).forEach { (symbol, label) ->
                    val isSelected = currencySymbol == symbol
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectCurrency(symbol) },
                        label = {
                            Text(
                                text = label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = SaasBorder,
                            selectedBorderColor = SaasIndigoBorder
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SaasSurfaceSubtle,
                            labelColor = SaasInkSecondary,
                            selectedContainerColor = SaasIndigoContainer,
                            selectedLabelColor = OnSaasIndigoContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = SaasBorder)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ACCOUNT & SYNC",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = SaasInkMuted
            )
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SaasBackground,
                border = BorderStroke(1.dp, SaasBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isLocalMode) "Offline Local Storage" else "Cloud Synced Account",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SaasInkPrimary
                        )
                        Text(
                            text = userDisplayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = SaasInkSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    if (isLocalMode) {
                        Button(
                            onClick = {
                                onDismiss()
                                onSwitchToCloudLogin()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SaasIndigoPrimary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("switch_to_cloud_login_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Login,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sign In", fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                onSignOut()
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, SaasBorder),
                            modifier = Modifier.testTag("sign_out_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                tint = SaasInkSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sign Out", color = SaasInkPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemePresetOptionRow(
    preset: AppThemePreset,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) SaasIndigoContainer.copy(alpha = 0.65f) else SaasSurface,
        animationSpec = tween(200),
        label = "ThemeRowBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) SaasIndigoPrimary else SaasBorder,
        animationSpec = tween(200),
        label = "ThemeRowBorder"
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("theme_option_${preset.key.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live Swatch Preview Pill
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = preset.palette.background,
                border = BorderStroke(1.dp, preset.palette.borderStrong),
                modifier = Modifier.size(width = 56.dp, height = 38.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(preset.palette.primary)
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(preset.palette.emeraldReceivable)
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(preset.palette.rubyPayable)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = preset.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaasInkPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (preset.isDark) Color(0xFF1E293B) else SaasSurfaceSubtle,
                        border = BorderStroke(1.dp, SaasBorder)
                    ) {
                        Text(
                            text = if (preset.isDark) "Dark" else "Light",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (preset.isDark) Color(0xFFE2E8F0) else SaasInkSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = preset.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = SaasInkSecondary
                )
            }

            if (selected) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected theme",
                    tint = SaasIndigoPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun LayoutModeOptionCard(
    mode: AppLayoutMode,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) SaasIndigoContainer.copy(alpha = 0.65f) else SaasSurface,
        animationSpec = tween(200),
        label = "LayoutOptionBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) SaasIndigoPrimary else SaasBorder,
        animationSpec = tween(200),
        label = "LayoutOptionBorder"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) SaasIndigoPrimary else SaasSurfaceSubtle)
                    .border(
                        1.dp,
                        if (selected) SaasIndigoPrimary else SaasBorder,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) Color.White else SaasInkSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = mode.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SaasInkPrimary
                    )
                    if (selected) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = EmeraldReceivableSoft,
                            border = BorderStroke(1.dp, EmeraldReceivableBorder)
                        ) {
                            Text(
                                text = "Active",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDarkText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = mode.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = SaasInkSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))
                if (mode == AppLayoutMode.PERSONAL) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 36.dp, height = 14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(EmeraldReceivableSoft)
                                .border(1.dp, EmeraldReceivableBorder, RoundedCornerShape(4.dp))
                        )
                        Box(
                            modifier = Modifier
                                .size(width = 36.dp, height = 14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(RubyPayableSoft)
                                .border(1.dp, RubyPayableBorder, RoundedCornerShape(4.dp))
                        )
                        Text(
                            text = "• Simple Name, Amount & Reason",
                            style = MaterialTheme.typography.labelSmall,
                            color = SaasInkMuted
                        )
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 24.dp, height = 12.dp)
                                .clip(CircleShape)
                                .background(SaasIndigoPrimary.copy(alpha = 0.25f))
                        )
                        Text(
                            text = "Accounts • Daily Register • Summary",
                            style = MaterialTheme.typography.labelSmall,
                            color = SaasInkMuted
                        )
                    }
                }
            }

            if (selected) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = SaasIndigoPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
