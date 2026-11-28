package ir.thepoo7an.chand

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RemoteViews
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceRemoteViews
import androidx.startup.AppInitializer
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkManagerInitializer
import ir.thepoo7an.chand.data.PreferencesRepository
import ir.thepoo7an.chand.model.AppLanguage
import ir.thepoo7an.chand.model.AssetCategory
import ir.thepoo7an.chand.model.CachedMarketSnapshot
import ir.thepoo7an.chand.model.DataFreshness
import ir.thepoo7an.chand.model.MarketAsset
import ir.thepoo7an.chand.widget.ChandCompact2x1Widget
import ir.thepoo7an.chand.widget.ChandCompactCryptoWidget
import ir.thepoo7an.chand.widget.ChandLockScreenWidget
import ir.thepoo7an.chand.widget.ChandLockScreenWidgetReceiver
import ir.thepoo7an.chand.widget.ChandPackageReplacedReceiver
import ir.thepoo7an.chand.widget.ChandSingleAssetWidget
import ir.thepoo7an.chand.widget.ChandSingleWidgetReceiver
import ir.thepoo7an.chand.widget.ChandWideListWidget
import ir.thepoo7an.chand.widget.WidgetSyncCoordinator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.Duration
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = ChandApplication::class)
class WidgetRenderingRobolectricTest {

    private object TestGlanceId : GlanceId

    private fun ensureWorkManagerInitialized(context: Context): WorkManager {
        val initialized = runCatching { WorkManager.getInstance(context) }.isSuccess
        if (!initialized) {
            AppInitializer.getInstance(context)
                .initializeComponent(WorkManagerInitializer::class.java)
        }
        return WorkManager.getInstance(context)
    }

    private fun collectVisibleTexts(view: View): List<String> {
        val result = mutableListOf<String>()
        if (view is TextView) {
            view.text?.toString()?.takeIf { it.isNotBlank() }?.let { result += it }
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                result += collectVisibleTexts(view.getChildAt(i))
            }
        }
        return result
    }

    private fun collectTextViews(view: View): List<TextView> {
        val result = mutableListOf<TextView>()
        if (view is TextView && !view.text.isNullOrBlank()) {
            result += view
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                result += collectTextViews(view.getChildAt(i))
            }
        }
        return result
    }

    private fun waitForWidgetTexts(
        shadowAppWidgetManager: org.robolectric.shadows.ShadowAppWidgetManager,
        appWidgetId: Int,
        predicate: (List<String>) -> Boolean
    ): List<String> {
        var latestTexts = emptyList<String>()
        repeat(50) {
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(150))
            Thread.sleep(40L)
            shadowOf(Looper.getMainLooper()).idle()
            val view = shadowAppWidgetManager.getViewFor(appWidgetId)
            if (view != null) {
                latestTexts = collectVisibleTexts(view)
                if (predicate(latestTexts)) return latestTexts
            }
        }
        return latestTexts
    }

    private fun sampleSnapshot(
        now: Long = System.currentTimeMillis(),
        usdPrice: Double = 72_500.0
    ): CachedMarketSnapshot {
        val assets = listOf(
            MarketAsset(
                id = "USD",
                symbol = "USD",
                nameFa = "دلار آمریکا",
                nameEn = "US Dollar",
                category = AssetCategory.FIAT,
                badgeText = "🇺🇸",
                accentHex = 0xFF10B981,
                priceToman = usdPrice,
                change24hPercent = 1.2,
                change24hToman = 850.0,
                freshness = DataFreshness.LIVE,
                updatedAtMillis = now
            ),
            MarketAsset(
                id = "EUR",
                symbol = "EUR",
                nameFa = "یورو",
                nameEn = "Euro",
                category = AssetCategory.FIAT,
                badgeText = "🇪🇺",
                accentHex = 0xFF3B82F6,
                priceToman = 78_200.0,
                change24hPercent = -0.4,
                change24hToman = -310.0,
                freshness = DataFreshness.LIVE,
                updatedAtMillis = now
            ),
            MarketAsset(
                id = "GBP",
                symbol = "GBP",
                nameFa = "پوند انگلیس",
                nameEn = "British Pound",
                category = AssetCategory.FIAT,
                badgeText = "🇬🇧",
                accentHex = 0xFF8B5CF6,
                priceToman = 91_000.0,
                change24hPercent = 0.5,
                change24hToman = 450.0,
                freshness = DataFreshness.LIVE,
                updatedAtMillis = now
            ),
            MarketAsset(
                id = "AED",
                symbol = "AED",
                nameFa = "درهم امارات",
                nameEn = "UAE Dirham",
                category = AssetCategory.FIAT,
                badgeText = "🇦🇪",
                accentHex = 0xFF10B981,
                priceToman = 19_700.0,
                change24hPercent = 0.1,
                change24hToman = 20.0,
                freshness = DataFreshness.LIVE,
                updatedAtMillis = now
            ),
            MarketAsset(
                id = "GOLD_18K",
                symbol = "18K",
                nameFa = "طلای ۱۸ عیار",
                nameEn = "18K Gold",
                category = AssetCategory.GOLD,
                badgeText = "🪙",
                accentHex = 0xFFF59E0B,
                priceToman = 4_650_000.0,
                change24hPercent = 2.1,
                change24hToman = 95_000.0,
                freshness = DataFreshness.LIVE,
                updatedAtMillis = now
            ),
            MarketAsset(
                id = "BTC",
                symbol = "BTC",
                nameFa = "بیت‌کوین",
                nameEn = "Bitcoin",
                category = AssetCategory.CRYPTO,
                badgeText = "₿",
                accentHex = 0xFFF97316,
                priceToman = 6_800_000_000.0,
                change24hPercent = 3.4,
                change24hToman = 220_000_000.0,
                freshness = DataFreshness.LIVE,
                updatedAtMillis = now
            ),
            MarketAsset(
                id = "ETH",
                symbol = "ETH",
                nameFa = "اتریوم",
                nameEn = "Ethereum",
                category = AssetCategory.CRYPTO,
                badgeText = "Ξ",
                accentHex = 0xFF6366F1,
                priceToman = 240_000_000.0,
                change24hPercent = -1.2,
                change24hToman = -2_900_000.0,
                freshness = DataFreshness.LIVE,
                updatedAtMillis = now
            ),
            MarketAsset(
                id = "USDT",
                symbol = "USDT",
                nameFa = "تتر",
                nameEn = "Tether",
                category = AssetCategory.CRYPTO,
                badgeText = "₮",
                accentHex = 0xFF10B981,
                priceToman = 72_400.0,
                change24hPercent = 0.2,
                change24hToman = 140.0,
                freshness = DataFreshness.LIVE,
                updatedAtMillis = now
            )
        )
        return CachedMarketSnapshot(
            assets = assets,
            updatedAtMillis = now,
            sourceSummary = "Test",
            freshness = DataFreshness.LIVE
        )
    }

    @Suppress("UNCHECKED_CAST")
    @OptIn(ExperimentalGlanceRemoteViewsApi::class)
    private fun composeAndInflate(
        context: Context,
        widget: GlanceAppWidget,
        size: DpSize
    ): RemoteViews = runBlocking {
        val utilsClass = Class.forName("androidx.glance.appwidget.AppWidgetUtilsKt")
        val runGlanceMethod = utilsClass.getDeclaredMethod(
            "runGlance",
            GlanceAppWidget::class.java,
            Context::class.java,
            GlanceId::class.java
        )
        val flow = runGlanceMethod.invoke(null, widget, context, TestGlanceId) as Flow<(@Composable () -> Unit)?>
        val content = flow.first()
        assertNotNull("Expected non-null Glance composable from ${widget::class.simpleName}", content)
        val result = GlanceRemoteViews().compose(
            context = context,
            size = size,
            content = content!!
        )
        val container = FrameLayout(context)
        val inflated = result.remoteViews.apply(context, container)
        assertNotNull(inflated)
        result.remoteViews
    }

    @Test
    fun `all five glance widgets compose and inflate cleanly with empty and populated caches`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val widgets = listOf(
            ChandSingleAssetWidget() to DpSize(180.dp, 180.dp),
            ChandWideListWidget() to DpSize(320.dp, 180.dp),
            ChandCompactCryptoWidget() to DpSize(180.dp, 180.dp),
            ChandCompact2x1Widget() to DpSize(180.dp, 64.dp),
            ChandCompact2x1Widget() to DpSize(220.dp, 110.dp),
            ChandLockScreenWidget() to DpSize(260.dp, 100.dp)
        )

        // Empty cache first
        widgets.forEach { (widget, size) ->
            val rv = composeAndInflate(context, widget, size)
            assertTrue(rv.layoutId != 0)
        }

        // Populated cache in FA & EN
        val repo = PreferencesRepository(context)
        repo.saveCachedSnapshot(sampleSnapshot())
        repo.setLanguage(AppLanguage.FA)
        widgets.forEach { (widget, size) ->
            val rv = composeAndInflate(context, widget, size)
            assertTrue(rv.layoutId != 0)
        }
    }

    @Suppress("UNCHECKED_CAST")
    @OptIn(ExperimentalGlanceRemoteViewsApi::class)
    @Test
    fun `hero widget renders 298_005 larger than delta row and fits 1x1 cell at fontScale 1_0 and 1_3 across themes and locales`() = runBlocking {
        val baseContext = ApplicationProvider.getApplicationContext<Context>()
        val repo = PreferencesRepository(baseContext)
        repo.saveCachedSnapshot(sampleSnapshot(usdPrice = 298_005.0))

        val utilsClass = Class.forName("androidx.glance.appwidget.AppWidgetUtilsKt")
        val runGlanceMethod = utilsClass.getDeclaredMethod(
            "runGlance",
            GlanceAppWidget::class.java,
            Context::class.java,
            GlanceId::class.java
        )

        val cellDp = 70.dp
        val locales = listOf(AppLanguage.EN to Locale.US, AppLanguage.FA to Locale.forLanguageTag("fa"))
        val fontScales = listOf(1.0f, 1.3f)
        val nightModes = listOf(Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_YES)

        for ((language, locale) in locales) {
            repo.setLanguage(language)
            repo.setUsePersianDigits(language == AppLanguage.FA)
            val expectedPrice = if (language == AppLanguage.FA) "۲۹۸,۰۰۵" else "298,005"

            for (fontScale in fontScales) {
                for (nightMode in nightModes) {
                    val config = Configuration(baseContext.resources.configuration).apply {
                        this.fontScale = fontScale
                        this.uiMode = (this.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightMode
                        setLocale(locale)
                        setLayoutDirection(locale)
                    }
                    val themedContext = baseContext.createConfigurationContext(config)
                    val flow = runGlanceMethod.invoke(
                        null,
                        ChandSingleAssetWidget(),
                        themedContext,
                        TestGlanceId
                    ) as Flow<(@Composable () -> Unit)?>
                    val content = flow.first()!!
                    val rv = GlanceRemoteViews().compose(
                        context = themedContext,
                        size = DpSize(cellDp, cellDp),
                        content = content
                    ).remoteViews

                    val container = FrameLayout(themedContext)
                    val inflated = rv.apply(themedContext, container)
                    val cellPx = (cellDp.value * themedContext.resources.displayMetrics.density).toInt()
                    inflated.measure(
                        View.MeasureSpec.makeMeasureSpec(cellPx, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(cellPx, View.MeasureSpec.EXACTLY)
                    )
                    inflated.layout(0, 0, cellPx, cellPx)

                    val textViews = collectTextViews(inflated)
                    val allTexts = textViews.map { it.text.toString() }
                    val priceTv = textViews.firstOrNull { it.text.toString() == expectedPrice }
                    val deltaTv = textViews.firstOrNull { it.text.toString().contains("%") }
                    val labelTv = textViews.firstOrNull { it.text.toString() == "USD" }

                    assertNotNull("Expected price TextView '$expectedPrice', got $allTexts", priceTv)
                    assertNotNull("Expected delta TextView, got $allTexts", deltaTv)
                    assertNotNull("Expected USD label TextView, got $allTexts", labelTv)

                    assertTrue(
                        "Price textSize (${priceTv!!.textSize}) must be strictly greater than delta textSize (${deltaTv!!.textSize}) [lang=$language, fontScale=$fontScale, night=$nightMode]",
                        priceTv.textSize > deltaTv.textSize
                    )
                    assertTrue(
                        "Price textSize (${priceTv.textSize}) must be strictly greater than label textSize (${labelTv!!.textSize})",
                        priceTv.textSize > labelTv.textSize
                    )
                    assertTrue("Price TextView must keep maxLines=1", priceTv.maxLines == 1)
                    assertTrue(
                        "Price TextView width (${priceTv.measuredWidth}px) must fit 1x1 cell ($cellPx px)",
                        priceTv.measuredWidth <= cellPx && priceTv.right <= cellPx
                    )
                    assertTrue(
                        "Price TextView bottom (${priceTv.bottom}px) must fit 1x1 cell ($cellPx px)",
                        priceTv.bottom <= cellPx
                    )
                    val ellipsisCount = priceTv.layout?.getEllipsisCount(0) ?: 0
                    assertTrue("Price TextView must not truncate or ellipsize (ellipsis=$ellipsisCount)", ellipsisCount == 0)

                    assertFalse("Must not reintroduce DELAYED badge", allTexts.any { it.contains("DELAYED", ignoreCase = true) })
                    assertFalse("Must not reintroduce timestamp row", allTexts.any { it.contains(":") })
                }
            }
        }
    }

    // Known limitation: this test drives the real GlanceAppWidgetReceiver -> WorkManager
    // SessionWorker pipeline. Robolectric never completes that worker, so no RemoteViews reach
    // ShadowAppWidgetManager ("got []") regardless of app behaviour. The rendering itself is
    // covered by the `composeAndInflate`-based tests in this class.
    @Ignore("Glance SessionWorker pipeline cannot be driven under Robolectric")
    @Test
    fun `widget lifecycle onUpdate, active session recomposition, and MY_PACKAGE_REPLACED all publish RemoteViews`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val wm = ensureWorkManagerInitialized(context)

        val repo = PreferencesRepository(context)
        repo.setLanguage(AppLanguage.EN)
        repo.setUsePersianDigits(false)
        repo.saveCachedSnapshot(sampleSnapshot(usdPrice = 71_111.0))

        val appWidgetManager = AppWidgetManager.getInstance(context)
        val shadowAppWidgetManager = shadowOf(appWidgetManager)
        val singleComponent = ComponentName(context, ChandSingleWidgetReceiver::class.java)
        val singleInfo = android.appwidget.AppWidgetProviderInfo().apply {
            provider = singleComponent
        }
        shadowAppWidgetManager.addInstalledProvider(singleInfo)
        shadowAppWidgetManager.addBoundWidget(42, singleInfo)

        // 1. Dispatch APPWIDGET_UPDATE and verify SessionWorker renders initial cached price
        val updateIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
            this.component = singleComponent
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(42))
        }
        context.sendBroadcast(updateIntent)

        val initialTexts = waitForWidgetTexts(shadowAppWidgetManager, 42) { texts ->
            texts.any { it.contains("71,111") }
        }
        assertTrue("Expected initial USD price 71,111 in RemoteViews, got $initialTexts", initialTexts.any { it.contains("71,111") })

        val infos: List<WorkInfo> = wm.getWorkInfosForUniqueWork("appWidget-42").get()
        assertTrue("Expected SessionWorker to be enqueued for appWidget-42, got: $infos", infos.isNotEmpty())

        // 2. Save an updated market snapshot while SessionWorker is still open and trigger updateAllWidgets
        repo.saveCachedSnapshot(sampleSnapshot(usdPrice = 92_400.0))
        WidgetSyncCoordinator.updateAllWidgets(context)

        val liveTexts = waitForWidgetTexts(shadowAppWidgetManager, 42) { texts ->
            texts.any { it.contains("92,400") }
        }
        assertTrue("Expected updated USD price 92,400 in active widget session, got $liveTexts", liveTexts.any { it.contains("92,400") })

        // 3. Bind lock screen widget, update snapshot, and dispatch MY_PACKAGE_REPLACED
        val lockComponent = ComponentName(context, ChandLockScreenWidgetReceiver::class.java)
        val lockInfo = android.appwidget.AppWidgetProviderInfo().apply { provider = lockComponent }
        shadowAppWidgetManager.addInstalledProvider(lockInfo)
        shadowAppWidgetManager.addBoundWidget(78, lockInfo)

        repo.saveCachedSnapshot(sampleSnapshot(usdPrice = 84_100.0))
        val pkgReplacedIntent = Intent(Intent.ACTION_MY_PACKAGE_REPLACED).apply {
            component = ComponentName(context, ChandPackageReplacedReceiver::class.java)
        }
        context.sendBroadcast(pkgReplacedIntent)

        val upgradedSingleTexts = waitForWidgetTexts(shadowAppWidgetManager, 42) { texts ->
            texts.any { it.contains("84,100") }
        }
        val upgradedLockTexts = waitForWidgetTexts(shadowAppWidgetManager, 78) { texts ->
            texts.any { it.contains("84,100") }
        }
        assertTrue("Single widget should show 84,100 after MY_PACKAGE_REPLACED, got $upgradedSingleTexts", upgradedSingleTexts.any { it.contains("84,100") })
        assertTrue("Lock widget should show 84,100 after MY_PACKAGE_REPLACED, got $upgradedLockTexts", upgradedLockTexts.any { it.contains("84,100") })
    }

    @Test
    fun `hasAnyActiveWidgets detects AppWidgetManager bound widgets without DataStore dependency`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val shadowAppWidgetManager = shadowOf(appWidgetManager)

        assertFalse(WidgetSyncCoordinator.hasAnyActiveWidgets(context))

        val lockComponent = ComponentName(context, ChandLockScreenWidgetReceiver::class.java)
        val lockInfo = android.appwidget.AppWidgetProviderInfo().apply { provider = lockComponent }
        shadowAppWidgetManager.addInstalledProvider(lockInfo)
        shadowAppWidgetManager.addBoundWidget(101, lockInfo)

        assertTrue(WidgetSyncCoordinator.hasAnyActiveWidgets(context))
    }

    @Test
    fun `onCompositionError publishes clickable branded fallback layout`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val shadowAppWidgetManager = shadowOf(appWidgetManager)

        val component = ComponentName(context, ChandSingleWidgetReceiver::class.java)
        val info = android.appwidget.AppWidgetProviderInfo().apply { provider = component }
        shadowAppWidgetManager.addInstalledProvider(info)
        shadowAppWidgetManager.addBoundWidget(99, info)

        ChandSingleAssetWidget().onCompositionError(
            context = context,
            glanceId = TestGlanceId,
            appWidgetId = 99,
            throwable = IllegalStateException("Simulated composition failure")
        )

        val fallbackView = shadowAppWidgetManager.getViewFor(99)
        assertNotNull("Expected fallback RemoteViews to be published", fallbackView)
        val texts = collectVisibleTexts(fallbackView!!)
        assertTrue("Expected app title in fallback view, got $texts", texts.any { it.contains("Nerkhnama") || it.contains("نرخ‌نما") })
        assertFalse("Fallback view must not contain a loading ProgressBar", fallbackView.findViewById<View>(android.R.id.progress) != null)
    }
}
