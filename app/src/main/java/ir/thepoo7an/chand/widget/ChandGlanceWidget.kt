package ir.thepoo7an.chand.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import ir.thepoo7an.chand.MainActivity
import ir.thepoo7an.chand.R
import ir.thepoo7an.chand.data.PreferencesRepository
import ir.thepoo7an.chand.model.AppLanguage
import ir.thepoo7an.chand.model.AppPreferences
import ir.thepoo7an.chand.model.AssetCategory
import ir.thepoo7an.chand.model.CachedMarketSnapshot
import ir.thepoo7an.chand.model.DataFreshness
import ir.thepoo7an.chand.model.MarketAsset
import ir.thepoo7an.chand.network.FreshnessAgeTier
import ir.thepoo7an.chand.network.FreshnessTimeFormatter
import ir.thepoo7an.chand.network.MarketValidationConfig
import ir.thepoo7an.chand.ui.strings.NumberFormatters
import ir.thepoo7an.chand.ui.strings.stringsFor
import ir.thepoo7an.chand.ui.theme.PriceTrendDirection
import ir.thepoo7an.chand.ui.theme.resolvePriceTrend
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.io.File
import kotlin.math.floor

/**
 * Type scale and paddings of the single-asset hero widget.
 *
 * A 1x1 launcher cell is ~70dp while the default placement is 110dp, so the hero picks a scale from
 * the size the launcher actually gave it instead of guessing. `WidgetHeroFitTest` measures every string
 * against these numbers at fontScale 1.0 and 1.3, which is what "no clipping" means here.
 */
internal object WidgetHeroMetrics {

    data class HeroScale(
        val cellSize: Dp,
        val padding: Dp,
        /** A 1x1 cell spends its width on the price and the symbol; the decorative badge would eat it. */
        val showBadge: Boolean,
        val badgeSize: Dp,
        val badgeGlyphSize: TextUnit,
        val badgeTextGap: Dp,
        val nameSize: TextUnit,
        val statusSize: TextUnit,
        val priceSize: TextUnit,
        val deltaSize: TextUnit,
        val priceDeltaGap: Dp
    )

    private const val ESTIMATED_CHAR_EM = 0.55f
    private const val LINE_HEIGHT_FACTOR = 1.25f

    /** A 1x1 cell: two rows, symbol-only, no badge, nothing repeated. */
    val COMPACT = HeroScale(
        cellSize = 70.dp,
        padding = 6.dp,
        showBadge = false,
        badgeSize = 18.dp,
        badgeGlyphSize = 9.sp,
        badgeTextGap = 4.dp,
        nameSize = 8.sp,
        statusSize = 7.sp,
        priceSize = 15.sp,
        deltaSize = 8.sp,
        priceDeltaGap = 3.dp
    )

    /** The 2x2 default placement: same two rows, comfortable sizes, badge included. */
    val REGULAR = HeroScale(
        cellSize = 110.dp,
        padding = 12.dp,
        showBadge = true,
        badgeSize = 26.dp,
        badgeGlyphSize = 13.sp,
        badgeTextGap = 5.dp,
        nameSize = 10.sp,
        statusSize = 8.sp,
        priceSize = 22.sp,
        deltaSize = 10.sp,
        priceDeltaGap = 4.dp
    )

    /** Below ~80dp the launcher has given us a single cell, so the compact scale is the safe one. */
    fun forSize(size: DpSize): HeroScale =
        if (size.width <= 80.dp || size.height <= 80.dp) COMPACT else REGULAR

    /**
     * Resolves the hero price font size for the actual launcher [cellSize], active [fontScale], and
     * [priceText] length. Starts from the bold pre-compaction hero scale and auto-shrinks gently in
     * 0.5sp steps only when horizontal or vertical bounds require it so `maxLines = 1` never clips
     * at `fontScale` up to 1.3.
     */
    fun resolvePriceSize(
        scale: HeroScale,
        priceText: String,
        cellSize: DpSize = DpSize(scale.cellSize, scale.cellSize),
        fontScale: Float = 1.0f
    ): TextUnit {
        val safeFontScale = fontScale.coerceAtLeast(1.0f)
        val cellWidth = if (cellSize.width.value.isFinite() && cellSize.width > 0.dp) {
            cellSize.width.value
        } else {
            scale.cellSize.value
        }
        val cellHeight = if (cellSize.height.value.isFinite() && cellSize.height > 0.dp) {
            cellSize.height.value
        } else {
            scale.cellSize.value
        }

        val availableWidth = (cellWidth - 2f * scale.padding.value).coerceAtLeast(24f)
        val rowOneHeight = (scale.nameSize.value + scale.statusSize.value) * safeFontScale * LINE_HEIGHT_FACTOR
        val availableHeight = (cellHeight - 2f * scale.padding.value - rowOneHeight - scale.priceDeltaGap.value)
            .coerceAtLeast(12f)

        val charCount = priceText.length.coerceAtLeast(1)
        val maxHorizontalSp = availableWidth / (charCount * ESTIMATED_CHAR_EM * safeFontScale)
        val maxVerticalSp = availableHeight / (safeFontScale * LINE_HEIGHT_FACTOR)
        val ceilingSp = minOf(maxHorizontalSp, maxVerticalSp)

        val preferredSp = if (scale == REGULAR && cellWidth >= 140f && cellHeight >= 140f) {
            24f
        } else {
            scale.priceSize.value
        }

        val steppedSp = floor(minOf(preferredSp, ceilingSp) * 2f) / 2f
        return steppedSp.coerceAtLeast(6f).sp
    }
}

private object WidgetColors {
    val solidDivider = ColorProvider(day = Color(0xFFECE8E0), night = Color(0xFF1D232E))
    val glassDivider = ColorProvider(day = Color(0x240F172A), night = Color(0x26FFFFFF))
    val title = ColorProvider(day = Color(0xFF111317), night = Color(0xFFFFFFFF))
    val sub = ColorProvider(day = Color(0xFF525B69), night = Color(0xFFCBD5E1))
    // Inverted Iranian market price change colors: Up = Red, Down = Green, Neutral = Neutral gray
    val priceUp = ColorProvider(day = Color(0xFFDC2626), night = Color(0xFFF87171))
    val priceDown = ColorProvider(day = Color(0xFF059669), night = Color(0xFF34D399))
    val priceNeutral = sub
    // System status colors remain standard semantic Green / Amber / Gray
    val statusLive = ColorProvider(day = Color(0xFF059669), night = Color(0xFF34D399))
    val warn = ColorProvider(day = Color(0xFFD97706), night = Color(0xFFFBBF24))
    val solidBadgeBg = ColorProvider(day = Color(0xFFF3F1EC), night = Color(0xFF161D28))
    val glassBadgeBg = ColorProvider(day = Color(0x260F172A), night = Color(0x29FFFFFF))

    fun freshnessColor(freshness: DataFreshness): androidx.glance.unit.ColorProvider = when (freshness) {
        DataFreshness.LIVE -> statusLive
        DataFreshness.DELAYED, DataFreshness.CACHED -> warn
        DataFreshness.UNAVAILABLE -> sub
    }

    fun freshnessStatusColor(
        freshness: DataFreshness,
        ageTier: FreshnessAgeTier
    ): androidx.glance.unit.ColorProvider = when {
        freshness == DataFreshness.UNAVAILABLE || ageTier == FreshnessAgeTier.UNAVAILABLE -> priceUp
        ageTier == FreshnessAgeTier.CRITICAL_RED -> priceUp
        ageTier == FreshnessAgeTier.STALE_AMBER -> warn
        else -> freshnessColor(freshness)
    }

    fun trendColor(direction: PriceTrendDirection): androidx.glance.unit.ColorProvider = when (direction) {
        PriceTrendDirection.UP -> priceUp
        PriceTrendDirection.DOWN -> priceDown
        PriceTrendDirection.NEUTRAL -> priceNeutral
    }
}

internal object WidgetStatusHelper {
    fun resolveFreshness(
        displayedAssets: List<MarketAsset>,
        snapshotFreshness: DataFreshness,
        updatedAtMillis: Long,
        nowMillis: Long = System.currentTimeMillis()
    ): DataFreshness {
        if (updatedAtMillis <= 0L) return DataFreshness.UNAVAILABLE
        val validAssets = displayedAssets.filter {
            it.hasUsablePrimaryPrice && it.freshness != DataFreshness.UNAVAILABLE
        }
        if (validAssets.isEmpty()) return DataFreshness.UNAVAILABLE

        val recorded = DataFreshness.stalest(
            snapshotFreshness,
            DataFreshness.stalest(*validAssets.map { it.freshness }.toTypedArray())
        )
        if (recorded == DataFreshness.UNAVAILABLE) return DataFreshness.UNAVAILABLE
        if (recorded == DataFreshness.CACHED) return DataFreshness.CACHED

        val maxLiveAgeMs = if (validAssets.any { it.category == AssetCategory.CRYPTO }) {
            MarketValidationConfig.CRYPTO_LIVE_MAX_AGE_MILLIS
        } else {
            MarketValidationConfig.FIAT_GOLD_LIVE_MAX_AGE_MILLIS
        }
        val snapshotAgeMs = (nowMillis - updatedAtMillis).coerceAtLeast(0L)
        return if (snapshotAgeMs > maxLiveAgeMs) {
            DataFreshness.CACHED
        } else {
            recorded
        }
    }

    fun formatStatusLabel(
        freshness: DataFreshness,
        updatedAtMillis: Long,
        language: AppLanguage,
        usePersianDigits: Boolean
    ): String {
        val strings = stringsFor(language)
        val badge = when (freshness) {
            DataFreshness.LIVE -> strings.liveBadge
            DataFreshness.DELAYED -> strings.delayedBadge
            DataFreshness.CACHED -> strings.cachedBadge
            DataFreshness.UNAVAILABLE -> strings.unavailableBadge
        }
        val timeTag = NumberFormatters.formatTime(updatedAtMillis, usePersianDigits)
        if (freshness == DataFreshness.UNAVAILABLE || timeTag.isBlank()) {
            return badge
        }
        return "$badge • $timeTag"
    }

    fun formatHeroSubLine(
        symbol: String,
        freshness: DataFreshness,
        updatedAtMillis: Long,
        language: AppLanguage,
        usePersianDigits: Boolean
    ): String {
        val status = formatStatusLabel(freshness, updatedAtMillis, language, usePersianDigits)
        return "$symbol • $status"
    }

    fun formatLockScreenStatusPill(
        freshness: DataFreshness,
        updatedAtMillis: Long,
        language: AppLanguage,
        usePersianDigits: Boolean
    ): String = formatStatusLabel(freshness, updatedAtMillis, language, usePersianDigits)

    fun resolveFreshnessAgeTier(
        displayedAssets: List<MarketAsset>,
        updatedAtMillis: Long,
        nowMillis: Long = System.currentTimeMillis()
    ): FreshnessAgeTier {
        val assetFetch = displayedAssets
            .mapNotNull { FreshnessTimeFormatter.lastSuccessfulFetchMillis(it).takeIf { ts -> ts > 0L } }
            .minOrNull()
        val effectiveFetch = assetFetch ?: updatedAtMillis.takeIf { it > 0L } ?: 0L
        val unavailable = displayedAssets.isEmpty() ||
            displayedAssets.all { it.freshness == DataFreshness.UNAVAILABLE || !it.hasUsablePrimaryPrice }
        return FreshnessTimeFormatter.resolveAgeTier(
            lastFetchMillis = effectiveFetch,
            nowMillis = nowMillis,
            isUnavailable = unavailable
        )
    }

    fun formatCompactAgeSuffix(
        displayedAssets: List<MarketAsset>,
        updatedAtMillis: Long,
        language: AppLanguage,
        usePersianDigits: Boolean,
        nowMillis: Long = System.currentTimeMillis()
    ): String {
        val assetFetch = displayedAssets
            .mapNotNull { FreshnessTimeFormatter.lastSuccessfulFetchMillis(it).takeIf { ts -> ts > 0L } }
            .minOrNull()
        val effectiveFetch = assetFetch ?: updatedAtMillis.takeIf { it > 0L } ?: 0L
        val unavailable = displayedAssets.isEmpty() ||
            displayedAssets.all { it.freshness == DataFreshness.UNAVAILABLE || !it.hasUsablePrimaryPrice }
        return FreshnessTimeFormatter.formatCompactDuration(
            lastFetchMillis = effectiveFetch,
            nowMillis = nowMillis,
            language = language,
            usePersianDigits = usePersianDigits,
            isUnavailable = unavailable
        )
    }

    fun formatWidgetStatusWithAge(
        freshness: DataFreshness,
        displayedAssets: List<MarketAsset>,
        updatedAtMillis: Long,
        language: AppLanguage,
        usePersianDigits: Boolean,
        nowMillis: Long = System.currentTimeMillis()
    ): String {
        val base = formatStatusLabel(freshness, updatedAtMillis, language, usePersianDigits)
        if (freshness == DataFreshness.UNAVAILABLE || updatedAtMillis <= 0L) return base
        val age = formatCompactAgeSuffix(displayedAssets, updatedAtMillis, language, usePersianDigits, nowMillis)
        return if (age.isNotBlank()) "$base · $age" else base
    }
}

@DrawableRes
private fun widgetBackgroundDrawableRes(prefs: AppPreferences): Int {
    if (!prefs.widgetTranslucent) return R.drawable.widget_bg_solid
    return when {
        prefs.widgetOpacityPercent <= 58 -> R.drawable.widget_bg_glass_50
        prefs.widgetOpacityPercent >= 78 -> R.drawable.widget_bg_glass_85
        else -> R.drawable.widget_bg_glass_70
    }
}

data class ChandWidgetState(
    val prefs: AppPreferences = AppPreferences(),
    val snapshot: CachedMarketSnapshot? = null
)

/**
 * Glance state definition backed by [PreferencesRepository] (`chand_prefs`).
 * Using Glance's native state pipeline avoids launching infinite Flow collectors inside
 * `provideContent` (which would block `GlanceRemoteViews.compose()`), while guaranteeing that
 * calling `widget.update(context, glanceId)` refreshes active `SessionWorker` sessions immediately.
 */
object ChandWidgetStateDefinition : GlanceStateDefinition<ChandWidgetState> {
    override fun getLocation(context: Context, fileKey: String): File =
        File(context.applicationContext.filesDir, "datastore/chand_widget_state_$fileKey.meta")

    override suspend fun getDataStore(
        context: Context,
        fileKey: String
    ): DataStore<ChandWidgetState> {
        val appContext = context.applicationContext
        val prefsRepo = PreferencesRepository(appContext)
        return object : DataStore<ChandWidgetState> {
            override val data: Flow<ChandWidgetState> = combine(
                prefsRepo.preferencesFlow,
                prefsRepo.cachedSnapshotFlow
            ) { prefs, snapshot ->
                ChandWidgetState(prefs = prefs, snapshot = snapshot)
            }

            override suspend fun updateData(
                transform: suspend (t: ChandWidgetState) -> ChandWidgetState
            ): ChandWidgetState {
                val current = ChandWidgetState(
                    prefs = prefsRepo.getPreferences(),
                    snapshot = prefsRepo.loadCachedSnapshot()
                )
                return transform(current)
            }
        }
    }
}

/**
 * Shared base widget that:
 * 1) Uses [ChandWidgetStateDefinition] so active Glance `SessionWorker` sessions update cleanly
 *    on `widget.update(...)` without blocking one-shot `GlanceRemoteViews` compositions,
 * 2) Safely preloads initial state with fallback defaults,
 * 3) Publishes a clickable branded fallback RemoteViews layout if composition ever throws.
 */
abstract class BaseChandGlanceWidget : GlanceAppWidget(
    errorUiLayout = R.layout.widget_fallback_layout
) {
    override val stateDefinition: GlanceStateDefinition<ChandWidgetState> = ChandWidgetStateDefinition

    final override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appContext = context.applicationContext
        val prefsRepo = PreferencesRepository(appContext)
        val fallbackState = ChandWidgetState(
            prefs = runCatching { prefsRepo.getPreferences() }.getOrDefault(AppPreferences()),
            snapshot = runCatching { prefsRepo.loadCachedSnapshot() }.getOrNull()
        )

        provideContent {
            val state = currentState<ChandWidgetState?>() ?: fallbackState
            Content(prefs = state.prefs, snapshot = state.snapshot)
        }
    }

    @Composable
    protected abstract fun Content(prefs: AppPreferences, snapshot: CachedMarketSnapshot?)

    override fun onCompositionError(
        context: Context,
        glanceId: GlanceId,
        appWidgetId: Int,
        throwable: Throwable
    ) {
        runCatching {
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val fallback = RemoteViews(context.packageName, R.layout.widget_fallback_layout).apply {
                setOnClickPendingIntent(R.id.widget_fallback_root, pendingIntent)
            }
            AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, fallback)
        }
    }
}

/**
 * Widget 1 (2x2 Hero Card): Single primary currency card (US Dollar or top pinned),
 * mirroring the top-left Chand?! iOS widget.
 */
class ChandSingleAssetWidget : BaseChandGlanceWidget() {
    // The default SizeMode.Single leaves LocalSize unspecified, which is why the hero could not tell a
    // 1x1 cell from the 2x2 placement and kept overshooting the smaller one.
    override val sizeMode: SizeMode = SizeMode.Exact

    @Composable
    override fun Content(prefs: AppPreferences, snapshot: CachedMarketSnapshot?) {
        val assets = snapshot?.assets.orEmpty()
        val hero = assets.firstOrNull { it.id == prefs.widgetHeroAssetId }
            ?: assets.firstOrNull { it.id == "USD" }
            ?: assets.firstOrNull { it.id in prefs.pinnedIds }
            ?: assets.firstOrNull()

        SingleHeroWidgetContent(
            asset = hero,
            snapshotFreshness = snapshot?.freshness ?: DataFreshness.UNAVAILABLE,
            updatedAtMillis = snapshot?.updatedAtMillis ?: 0L,
            prefs = prefs
        )
    }
}

@Composable
private fun SingleHeroWidgetContent(
    asset: MarketAsset?,
    snapshotFreshness: DataFreshness,
    updatedAtMillis: Long,
    prefs: AppPreferences
) {
    val context = LocalContext.current
    val cellSize = LocalSize.current
    val fontScale = context.resources.configuration.fontScale.coerceAtLeast(1.0f)
    val isFa = prefs.language == AppLanguage.FA
    val badgeBg = if (prefs.widgetTranslucent) WidgetColors.glassBadgeBg else WidgetColors.solidBadgeBg
    // The launcher decides how big the cell is (~70dp for 1x1, 110dp for the default 2x2); the type
    // scale follows it instead of overshooting the smaller one.
    val scale = WidgetHeroMetrics.forSize(cellSize)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(widgetBackgroundDrawableRes(prefs)))
            .padding(scale.padding)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        if (asset == null) {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .clickable(actionRunCallback<RefreshAllWidgetsAction>()),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isFa) "نرخ‌نما" else "Nerkhnama",
                        style = TextStyle(
                            color = WidgetColors.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = if (isFa) "لمس برای نرخ" else "Tap to sync",
                        style = TextStyle(
                            color = WidgetColors.sub,
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                }
            }
        } else {
            // One model decides both rows, so the widget cannot contradict itself: a stale quote gets
            // a single "stale" marker and no delta row at all (never a "• 0.0%" next to a DELAYED
            // badge), and the name is the symbol so a 1x1 cell does not clip it.
            val hero = WidgetHeroPresentation.build(
                asset = asset,
                prefs = prefs,
                snapshotFreshness = snapshotFreshness,
                updatedAtMillis = updatedAtMillis
            )
            val resolvedPriceSize = WidgetHeroMetrics.resolvePriceSize(
                scale = scale,
                priceText = hero.priceText,
                cellSize = cellSize,
                fontScale = fontScale
            )
            val trendDirection = resolvePriceTrend(
                changeAmount = if (asset.isUsdPerBarrel) asset.change24hUsd else asset.change24hToman,
                changePct = asset.change24hPercent,
                hasChangeData = hero.deltaText != null
            )
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Top
            ) {
                // Row 1: badge + symbol + one short status token.
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (scale.showBadge) {
                        Box(
                            modifier = GlanceModifier
                                .size(scale.badgeSize)
                                .background(badgeBg)
                                .cornerRadius(scale.badgeSize / 2)
                                .clickable(actionRunCallback<RefreshAllWidgetsAction>()),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = asset.badgeText,
                                style = TextStyle(fontSize = scale.badgeGlyphSize)
                            )
                        }
                        Spacer(modifier = GlanceModifier.width(scale.badgeTextGap))
                    }
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = hero.assetLabel,
                            style = TextStyle(
                                color = WidgetColors.title,
                                fontSize = scale.nameSize,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = hero.statusLine,
                            style = TextStyle(
                                color = WidgetColors.freshnessStatusColor(
                                    if (hero.isStale) DataFreshness.CACHED else DataFreshness.LIVE,
                                    FreshnessAgeTier.NORMAL
                                ),
                                fontSize = scale.statusSize,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                // Row 2: the price alone, full width. The percent lives on row 1 (as the status token),
                // because sharing this row is what pushed the number out of a 1x1 cell.
                Text(
                    text = hero.priceText,
                    style = TextStyle(
                        color = WidgetColors.title,
                        fontSize = resolvedPriceSize,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Widget 2 (4x2 Wide Strip): 3-row pinned currency list (EUR, GBP, AED or pinned favorites),
 * mirroring the middle wide Chand?! iOS widget.
 */
class ChandWideListWidget : BaseChandGlanceWidget() {
    @Composable
    override fun Content(prefs: AppPreferences, snapshot: CachedMarketSnapshot?) {
        val allAssets = snapshot?.assets.orEmpty()
        val preferredIds = listOf("EUR", "GBP", "AED", "USD", "GOLD_18K")
        val pinnedNonCrypto = allAssets.filter {
            it.id in prefs.pinnedIds && it.category != AssetCategory.CRYPTO && it.id != "USD"
        }
        val fallbackNonCrypto = allAssets.filter { it.id in preferredIds }
        val rows = (if (pinnedNonCrypto.size >= 3) pinnedNonCrypto else pinnedNonCrypto + fallbackNonCrypto)
            .distinctBy { it.id }
            .take(3)

        MultiRowWidgetContent(
            rows = rows,
            snapshotFreshness = snapshot?.freshness ?: DataFreshness.UNAVAILABLE,
            updatedAtMillis = snapshot?.updatedAtMillis ?: 0L,
            prefs = prefs,
            compactMode = false
        )
    }
}

/**
 * Widget 3 (2x2 Compact Crypto Watch): 3-row compact BTC / ETH / USDT card,
 * mirroring the bottom-right Chand?! iOS widget.
 */
class ChandCompactCryptoWidget : BaseChandGlanceWidget() {
    @Composable
    override fun Content(prefs: AppPreferences, snapshot: CachedMarketSnapshot?) {
        val allAssets = snapshot?.assets.orEmpty()
        val cryptoOrder = listOf("BTC", "ETH", "USDT", "TON", "SOL")
        val cryptos = cryptoOrder.mapNotNull { id -> allAssets.firstOrNull { it.id == id } }.take(3)
        val rows = cryptos.ifEmpty { allAssets.take(3) }

        MultiRowWidgetContent(
            rows = rows,
            snapshotFreshness = snapshot?.freshness ?: DataFreshness.UNAVAILABLE,
            updatedAtMillis = snapshot?.updatedAtMillis ?: 0L,
            prefs = prefs,
            compactMode = true
        )
    }
}

/**
 * Widget 4 (2x1 Compact Essential Strip): US Dollar, Euro, and 18K Gold with translucent glass support.
 */
class ChandCompact2x1Widget : BaseChandGlanceWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    @Composable
    override fun Content(prefs: AppPreferences, snapshot: CachedMarketSnapshot?) {
        val allAssets = snapshot?.assets.orEmpty()
        val essentialIds = listOf("USD", "EUR", "GOLD_18K")
        val rows = essentialIds.mapNotNull { targetId ->
            allAssets.firstOrNull { it.id == targetId }
        }

        Compact2x1WidgetContent(
            rows = rows,
            snapshotFreshness = snapshot?.freshness ?: DataFreshness.UNAVAILABLE,
            updatedAtMillis = snapshot?.updatedAtMillis ?: 0L,
            prefs = prefs
        )
    }
}

@Composable
private fun Compact2x1WidgetContent(
    rows: List<MarketAsset>,
    snapshotFreshness: DataFreshness,
    updatedAtMillis: Long,
    prefs: AppPreferences
) {
    val size = LocalSize.current
    val isFa = prefs.language == AppLanguage.FA
    val isTall = size.height >= 92.dp
    val showFullDelta = size.width >= 165.dp || isTall
    val effectiveFreshness = WidgetStatusHelper.resolveFreshness(
        displayedAssets = rows,
        snapshotFreshness = snapshotFreshness,
        updatedAtMillis = updatedAtMillis
    )
    val ageTier = WidgetStatusHelper.resolveFreshnessAgeTier(
        displayedAssets = rows,
        updatedAtMillis = updatedAtMillis
    )
    val statusLabel = WidgetStatusHelper.formatWidgetStatusWithAge(
        freshness = effectiveFreshness,
        displayedAssets = rows,
        updatedAtMillis = updatedAtMillis,
        language = prefs.language,
        usePersianDigits = prefs.usePersianDigits
    )
    val dividerColor = if (prefs.widgetTranslucent) WidgetColors.glassDivider else WidgetColors.solidDivider

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(widgetBackgroundDrawableRes(prefs)))
            .padding(
                horizontal = if (isTall) 12.dp else 10.dp,
                vertical = if (isTall) 8.dp else 5.dp
            )
            .clickable(actionStartActivity<MainActivity>())
    ) {
        if (rows.isEmpty()) {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .clickable(actionRunCallback<RefreshAllWidgetsAction>()),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isFa) "دلار • یورو • طلا (لمس)" else "USD • EUR • GOLD",
                    style = TextStyle(
                        color = WidgetColors.sub,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )
            }
        } else {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                rows.forEachIndexed { idx, asset ->
                    val rowUnavailable = asset.freshness == DataFreshness.UNAVAILABLE || !asset.hasUsablePrimaryPrice
                    val trendDirection = resolvePriceTrend(
                        changeAmount = asset.change24hToman,
                        changePct = asset.change24hPercent,
                        hasChangeData = !rowUnavailable
                    )
                    val trendColor = WidgetColors.trendColor(trendDirection)
                    val shortLabel = when (asset.id) {
                        "USD" -> if (isFa) "دلار" else "USD"
                        "EUR" -> if (isFa) "یورو" else "EUR"
                        "GOLD_18K" -> if (isFa) "طلا" else "GOLD"
                        else -> asset.symbol
                    }
                    val arrowSymbol = when {
                        rowUnavailable -> "—"
                        trendDirection == PriceTrendDirection.UP -> "↑"
                        trendDirection == PriceTrendDirection.DOWN -> "↓"
                        else -> "•"
                    }

                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .defaultWeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = GlanceModifier.clickable(actionRunCallback<RefreshAllWidgetsAction>())
                        ) {
                            Text(
                                text = asset.badgeText,
                                style = TextStyle(fontSize = if (isTall) 13.sp else 11.sp)
                            )
                            Spacer(modifier = GlanceModifier.width(4.dp))
                            Text(
                                text = shortLabel,
                                style = TextStyle(
                                    color = WidgetColors.sub,
                                    fontSize = if (isTall) 12.sp else 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = GlanceModifier.defaultWeight())

                        if (showFullDelta) {
                            Text(
                                text = if (rowUnavailable) {
                                    "—"
                                } else {
                                    NumberFormatters.formatDeltaWithArrow(
                                        asset.change24hToman,
                                        asset.change24hPercent,
                                        prefs.usePersianDigits
                                    )
                                },
                                style = TextStyle(
                                    color = trendColor,
                                    fontSize = if (isTall) 10.sp else 9.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1
                            )
                            Spacer(modifier = GlanceModifier.width(5.dp))
                        } else {
                            Text(
                                text = arrowSymbol,
                                style = TextStyle(
                                    color = trendColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1
                            )
                            Spacer(modifier = GlanceModifier.width(3.dp))
                        }

                        Text(
                            text = if (rowUnavailable) {
                                "—"
                            } else {
                                NumberFormatters.formatCardPrice(
                                    asset.priceToman,
                                    prefs.usePersianDigits
                                )
                            },
                            style = TextStyle(
                                color = WidgetColors.title,
                                fontSize = if (isTall) 13.sp else 11.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                    }

                    if (isTall && idx < rows.lastIndex) {
                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(dividerColor)
                        ) {}
                    }
                }
                if (isTall && statusLabel.isNotBlank()) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        Text(
                            text = statusLabel,
                            style = TextStyle(
                                color = WidgetColors.freshnessStatusColor(effectiveFreshness, ageTier),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MultiRowWidgetContent(
    rows: List<MarketAsset>,
    snapshotFreshness: DataFreshness,
    updatedAtMillis: Long,
    prefs: AppPreferences,
    compactMode: Boolean
) {
    val isFa = prefs.language == AppLanguage.FA
    val effectiveFreshness = WidgetStatusHelper.resolveFreshness(
        displayedAssets = rows,
        snapshotFreshness = snapshotFreshness,
        updatedAtMillis = updatedAtMillis
    )
    val ageTier = WidgetStatusHelper.resolveFreshnessAgeTier(
        displayedAssets = rows,
        updatedAtMillis = updatedAtMillis
    )
    val statusLabel = WidgetStatusHelper.formatWidgetStatusWithAge(
        freshness = effectiveFreshness,
        displayedAssets = rows,
        updatedAtMillis = updatedAtMillis,
        language = prefs.language,
        usePersianDigits = prefs.usePersianDigits
    )
    val dividerColor = if (prefs.widgetTranslucent) WidgetColors.glassDivider else WidgetColors.solidDivider
    val badgeBg = if (prefs.widgetTranslucent) WidgetColors.glassBadgeBg else WidgetColors.solidBadgeBg

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(widgetBackgroundDrawableRes(prefs)))
            .padding(horizontal = 14.dp, vertical = 9.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        if (rows.isEmpty()) {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .clickable(actionRunCallback<RefreshAllWidgetsAction>()),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isFa) "برای دریافت نرخ لمس کنید" else "Tap to load rates",
                    style = TextStyle(color = WidgetColors.sub, fontSize = 12.sp)
                )
            }
        } else {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                rows.forEachIndexed { idx, asset ->
                    val rowUnavailable = asset.freshness == DataFreshness.UNAVAILABLE || !asset.hasUsablePrimaryPrice
                    val hasChange = !rowUnavailable && (!asset.isUsdPerBarrel || asset.isChange24hAvailable)
                    val trendDirection = resolvePriceTrend(
                        changeAmount = if (asset.isUsdPerBarrel) asset.change24hUsd else asset.change24hToman,
                        changePct = asset.change24hPercent,
                        hasChangeData = hasChange
                    )
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .defaultWeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = GlanceModifier
                                .size(if (compactMode) 22.dp else 24.dp)
                                .background(badgeBg)
                                .cornerRadius(12.dp)
                                .clickable(actionRunCallback<RefreshAllWidgetsAction>()),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = asset.badgeText,
                                style = TextStyle(fontSize = if (compactMode) 12.sp else 14.sp)
                            )
                        }
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        Text(
                            text = asset.symbol,
                            style = TextStyle(
                                color = WidgetColors.title,
                                fontSize = if (compactMode) 13.sp else 14.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = when {
                                    rowUnavailable -> "—"
                                    asset.isUsdPerBarrel && !asset.isChange24hAvailable -> "—"
                                    asset.isUsdPerBarrel -> NumberFormatters.formatPercent(asset.change24hPercent, prefs.usePersianDigits)
                                    else -> NumberFormatters.formatDeltaWithArrow(
                                        asset.change24hToman,
                                        asset.change24hPercent,
                                        prefs.usePersianDigits
                                    )
                                },
                                style = TextStyle(
                                    color = WidgetColors.trendColor(trendDirection),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1
                            )
                            Text(
                                text = when {
                                    rowUnavailable -> "—"
                                    asset.isUsdPerBarrel -> NumberFormatters.formatUsd(asset.priceUsd ?: 0.0, prefs.usePersianDigits)
                                    else -> NumberFormatters.formatCardPrice(
                                        asset.priceToman,
                                        prefs.usePersianDigits
                                    )
                                },
                                style = TextStyle(
                                    color = WidgetColors.title,
                                    fontSize = if (compactMode) 14.sp else 15.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1
                            )
                        }
                    }
                    if (idx < rows.lastIndex) {
                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(dividerColor)
                        ) {}
                    }
                }
                if (statusLabel.isNotBlank()) {
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        Text(
                            text = statusLabel,
                            style = TextStyle(
                                color = WidgetColors.freshnessStatusColor(effectiveFreshness, ageTier),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

class RefreshAllWidgetsAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val appContext = context.applicationContext
        try {
            WidgetSyncCoordinator.registerConnectivityMonitor(appContext)
            val ok = WidgetSyncCoordinator.syncWidgetsNow(appContext, force = true)
            if (!ok) {
                WidgetSyncCoordinator.updateAllWidgets(appContext)
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (_: Throwable) {
            WidgetSyncCoordinator.updateAllWidgets(appContext)
        }
    }
}

private object LockWidgetColors {
    val pureWhite = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFFFFFFFF))
    val softWhite = ColorProvider(day = Color(0xD9FFFFFF), night = Color(0xD9FFFFFF))
    val mutedWhite = ColorProvider(day = Color(0x99FFFFFF), night = Color(0x99FFFFFF))
    val pureBlack = ColorProvider(day = Color(0xFF000000), night = Color(0xFF000000))
}

/**
 * Widget 5 (Lock Screen / Transparent): Background-free monochrome pill aesthetic inspired by
 * Nothing OS lock-screen typography (high-contrast white text, solid white pills, outlined capsules).
 */
class ChandLockScreenWidget : BaseChandGlanceWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    @Composable
    override fun Content(prefs: AppPreferences, snapshot: CachedMarketSnapshot?) {
        val allAssets = snapshot?.assets.orEmpty()
        val hero = allAssets.firstOrNull { it.id == "USD" }
            ?: allAssets.firstOrNull { it.id in prefs.pinnedIds }
            ?: allAssets.firstOrNull()

        val secondaryTargets = listOf("EUR", "GOLD_18K", "GBP", "AED", "BTC")
        val secondary = secondaryTargets
            .filter { it != hero?.id }
            .mapNotNull { targetId -> allAssets.firstOrNull { it.id == targetId } }
            .take(2)

        LockScreenWidgetContent(
            hero = hero,
            secondary = secondary,
            snapshotFreshness = snapshot?.freshness ?: DataFreshness.UNAVAILABLE,
            updatedAtMillis = snapshot?.updatedAtMillis ?: 0L,
            prefs = prefs
        )
    }
}

@Composable
private fun LockScreenWidgetContent(
    hero: MarketAsset?,
    secondary: List<MarketAsset>,
    snapshotFreshness: DataFreshness,
    updatedAtMillis: Long,
    prefs: AppPreferences
) {
    val size = LocalSize.current
    val isTall = size.height >= 84.dp
    val isWide = size.width >= 215.dp
    val isFa = prefs.language == AppLanguage.FA
    val displayedAssets = buildList {
        if (hero != null) add(hero)
        addAll(secondary)
    }
    val effectiveFreshness = WidgetStatusHelper.resolveFreshness(
        displayedAssets = displayedAssets,
        snapshotFreshness = snapshotFreshness,
        updatedAtMillis = updatedAtMillis
    )
    val lockAgeTier = WidgetStatusHelper.resolveFreshnessAgeTier(
        displayedAssets = displayedAssets,
        updatedAtMillis = updatedAtMillis
    )
    val baseLockStatusPill = WidgetStatusHelper.formatLockScreenStatusPill(
        freshness = effectiveFreshness,
        updatedAtMillis = updatedAtMillis,
        language = prefs.language,
        usePersianDigits = prefs.usePersianDigits
    )
    val lockAgeSuffix = WidgetStatusHelper.formatCompactAgeSuffix(
        displayedAssets = displayedAssets,
        updatedAtMillis = updatedAtMillis,
        language = prefs.language,
        usePersianDigits = prefs.usePersianDigits
    )
    val statusPillText = if (effectiveFreshness == DataFreshness.UNAVAILABLE || lockAgeSuffix.isBlank()) {
        baseLockStatusPill
    } else if (lockAgeTier == FreshnessAgeTier.CRITICAL_RED) {
        "$baseLockStatusPill · $lockAgeSuffix !"
    } else {
        "$baseLockStatusPill · $lockAgeSuffix"
    }

    // Root Box intentionally has NO background so the lock screen / home wallpaper shines through.
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = if (isTall) 6.dp else 4.dp)
            .clickable(actionStartActivity<MainActivity>()),
        contentAlignment = Alignment.CenterStart
    ) {
        if (hero == null) {
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .clickable(actionRunCallback<RefreshAllWidgetsAction>()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isFa) "نرخ‌نما" else "NERKHNAMA",
                    style = TextStyle(
                        color = LockWidgetColors.pureWhite,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = GlanceModifier.width(6.dp))
                Box(
                    modifier = GlanceModifier
                        .background(ImageProvider(R.drawable.widget_lock_pill_solid))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isFa) "لمس برای نرخ" else "TAP TO SYNC",
                        style = TextStyle(
                            color = LockWidgetColors.pureBlack,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                }
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = "✳",
                    style = TextStyle(
                        color = LockWidgetColors.pureWhite,
                        fontSize = 15.sp
                    )
                )
            }
        } else {
            val heroUnavailable = hero.freshness == DataFreshness.UNAVAILABLE || hero.priceToman <= 0.0
            val deltaText = if (heroUnavailable) {
                "—"
            } else {
                NumberFormatters.formatDeltaWithArrow(
                    hero.change24hToman,
                    hero.change24hPercent,
                    prefs.usePersianDigits
                )
            }
            val pctText = if (heroUnavailable) {
                ""
            } else {
                NumberFormatters.formatPercent(hero.change24hPercent, prefs.usePersianDigits)
            }
            val statusBadgeText = if (!heroUnavailable && isWide && isTall) "$deltaText • $pctText" else deltaText

            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Row 1: Monochrome pill header inspired by [20] (NOV) LIVE • 21:45 ▮  ✳
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isFa) "نرخ‌نما" else "NERKHNAMA",
                        style = TextStyle(
                            color = LockWidgetColors.softWhite,
                            fontSize = if (isTall) 11.sp else 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1
                    )
                    Spacer(modifier = GlanceModifier.width(5.dp))
                    Box(
                        modifier = GlanceModifier
                            .background(ImageProvider(R.drawable.widget_lock_pill_solid))
                            .padding(horizontal = 7.dp, vertical = 1.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = hero.symbol,
                            style = TextStyle(
                                color = LockWidgetColors.pureBlack,
                                fontSize = if (isTall) 10.sp else 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    Box(
                        modifier = GlanceModifier
                            .background(ImageProvider(R.drawable.widget_lock_pill_outline))
                            .padding(horizontal = 7.dp, vertical = 1.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = statusPillText,
                            style = TextStyle(
                                color = LockWidgetColors.pureWhite,
                                fontSize = if (isTall) 10.sp else 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                    }
                    if (isWide) {
                        Spacer(modifier = GlanceModifier.width(5.dp))
                        Box(
                            modifier = GlanceModifier
                                .width(4.dp)
                                .height(10.dp)
                                .background(LockWidgetColors.pureWhite)
                        ) {}
                    }
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Box(
                        modifier = GlanceModifier
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .clickable(actionRunCallback<RefreshAllWidgetsAction>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✳",
                            style = TextStyle(
                                color = LockWidgetColors.pureWhite,
                                fontSize = if (isTall) 15.sp else 13.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(if (isTall) 5.dp else 3.dp))

                // Row 2: Hero rate + inverted solid white delta pill (like CLEAR NIGHT)
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (heroUnavailable) "—" else NumberFormatters.formatCardPrice(hero.priceToman, prefs.usePersianDigits),
                        style = TextStyle(
                            color = LockWidgetColors.pureWhite,
                            fontSize = if (isTall) 25.sp else 19.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                    Spacer(modifier = GlanceModifier.width(5.dp))
                    Text(
                        text = "TMN",
                        style = TextStyle(
                            color = LockWidgetColors.mutedWhite,
                            fontSize = if (isTall) 10.sp else 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Box(
                        modifier = GlanceModifier
                            .background(ImageProvider(R.drawable.widget_lock_pill_solid))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = statusBadgeText,
                            style = TextStyle(
                                color = LockWidgetColors.pureBlack,
                                fontSize = if (isTall) 10.sp else 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                    }
                }

                // Row 3 (Tall lock screen layout): Outlined EUR + 18K Gold sub-tickers without background
                if (isTall && secondary.isNotEmpty()) {
                    Spacer(modifier = GlanceModifier.height(6.dp))
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        secondary.forEachIndexed { idx, item ->
                            val itemUnavailable = item.freshness == DataFreshness.UNAVAILABLE || item.priceToman <= 0.0
                            val shortCode = when (item.id) {
                                "GOLD_18K" -> "18K"
                                else -> item.symbol
                            }
                            val arrow = when {
                                itemUnavailable -> ""
                                item.change24hToman > 0.001 || item.change24hPercent > 0.005 -> "↑"
                                item.change24hToman < -0.001 || item.change24hPercent < -0.005 -> "↓"
                                else -> "•"
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = GlanceModifier
                                        .background(ImageProvider(R.drawable.widget_lock_pill_outline))
                                        .padding(horizontal = 6.dp, vertical = 1.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = shortCode,
                                        style = TextStyle(
                                            color = LockWidgetColors.pureWhite,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = GlanceModifier.width(5.dp))
                                Text(
                                    text = if (itemUnavailable) {
                                        "—"
                                    } else {
                                        NumberFormatters.formatCardPrice(
                                            item.priceToman,
                                            prefs.usePersianDigits
                                        )
                                    },
                                    style = TextStyle(
                                        color = LockWidgetColors.pureWhite,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 1
                                )
                                if (isWide && arrow.isNotEmpty()) {
                                    Spacer(modifier = GlanceModifier.width(3.dp))
                                    Text(
                                        text = arrow,
                                        style = TextStyle(
                                            color = LockWidgetColors.mutedWhite,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }

                            if (idx < secondary.lastIndex) {
                                Spacer(modifier = GlanceModifier.defaultWeight())
                            }
                        }
                    }
                }
            }
        }
    }
}
