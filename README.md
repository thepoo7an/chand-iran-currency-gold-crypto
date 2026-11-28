<div align="center">

<img src="docs/store-listing/icon-512.png" width="104" height="104" alt="لوگوی نرخ‌نما (Nerkhnama)" />

# نرخ‌نما (Nerkhnama)

**ردیاب لحظه‌ای و بدون تبلیغات قیمت دلار، ارزهای رایج، طلا، سکه، نفت خام و رمزارزها به تومان**
<br/>
ساخته‌شده با **Kotlin**، **Jetpack Compose** و **Jetpack Glance** • طراحی مشکی مطلق **AMOLED** • تقویم **شمسی** • ۵ ویجت صفحه اصلی و قفل‌صفحه

<p>
  <a href="https://github.com/thepoo7an/chand-iran-currency-gold-crypto/releases/latest">
    <img src="https://img.shields.io/github/v/release/thepoo7an/chand-iran-currency-gold-crypto?style=for-the-badge&color=10B981&label=%D8%A2%D8%AE%D8%B1%DB%8C%D9%86%20%D9%86%D8%B3%D8%AE%D9%87%20APK" alt="Latest Release" />
  </a>
  <a href="https://github.com/thepoo7an/chand-iran-currency-gold-crypto/releases">
    <img src="https://img.shields.io/github/downloads/thepoo7an/chand-iran-currency-gold-crypto/total?style=for-the-badge&color=3B82F6&label=%D8%AA%D8%B9%D8%AF%D8%A7%D8%AF%20%D8%AF%D8%A7%D9%86%D9%84%D9%88%D8%AF" alt="Total Downloads" />
  </a>
  <a href="https://github.com/thepoo7an/chand-iran-currency-gold-crypto/stargazers">
    <img src="https://img.shields.io/github/stars/thepoo7an/chand-iran-currency-gold-crypto?style=for-the-badge&color=F59E0B&label=%D8%B3%D8%AA%D8%A7%D8%B1%D9%87%E2%80%8C%D9%87%D8%A7" alt="GitHub Stars" />
  </a>
</p>

<p>
  <img src="https://img.shields.io/badge/Android-7.0%2B%20(API%2024--36)-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android API 24-36" />
  <img src="https://img.shields.io/badge/Kotlin-Jetpack%20Compose%20%2B%20Glance-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin & Jetpack Compose" />
  <img src="https://img.shields.io/badge/%D8%AA%D8%A8%D9%84%DB%8C%D8%BA%D8%A7%D8%AA%20%D9%88%20%D8%B1%D8%AF%DB%8C%D8%A7%D8%A8-%D8%B5%D9%81%D8%B1%20(100%25%20%D9%BE%D8%A7%DA%A9)-10B981?style=flat-square" alt="Zero Ads" />
  <img src="https://img.shields.io/badge/License-MIT-blue?style=flat-square" alt="MIT License" />
  <a href=".github/workflows/ci.yml">
    <img src=".github/workflows/ci.yml/badge.svg" alt="Android CI" />
  </a>
  <a href=".github/workflows/release-apk.yml">
    <img src=".github/workflows/release-apk.yml/badge.svg" alt="Release APKs" />
  </a>
</p>

[⬇️ دانلود مستقیم APK](#download) • [📸 تصاویر نسخه فعلی](#screenshots) • [✨ امکانات و ویژگی‌ها](#features) • [🛡 امنیت و دسترسی‌ها](#security) • [🛠 ساخت از سورس](#build) • [🇬🇧 English Summary](#english-summary)

<br/>

<img src="docs/store-listing/feature-graphic-1-1280x720.png" width="100%" alt="بنر معرفی نرخ‌نما — ردیاب لحظه‌ای ارز، طلا، سکه و رمزارز" />

</div>

---

## چرا نرخ‌نما؟

بسیاری از برنامه‌های اعلام نرخ، پر از تبلیغات آزاردهنده، ردیاب‌های پنهان، قیمت‌های بدون منبع یا دسترسی‌های مشکوک هستند. **نرخ‌نما** دقیقاً برعکس این رویکرد ساخته شده است:

- **کاملاً رایگان، متن‌باز و بدون تبلیغات:** هیچ بنر تبلیغاتی، پاپ‌آپ، سرویس آنالیتیکس یا SDK اختصاصی فروشگاه‌ها در کد وجود ندارد.
- **شفافیت کامل در منابع نرخ‌گذاری:** تجمیع واقعی قیمت از صرافی‌ها و مراجع معتبر داخلی و جهانی؛ با نمایش زمان اعلام منبع، زمان دریافت و اختلاف قیمت صرافی‌ها روی هر دارایی.
- **نمایش فوری از حافظه موقت (Cache-First):** با باز کردن اپ یا ویجت‌ها، آخرین نرخ ذخیره‌شده فوراً نمایش داده می‌شود و فقط در صورت قدیمی‌شدن داده (بیش از ۳۰ ثانیه) به‌روزرسانی پس‌زمینه انجام می‌گیرد.
- **معماری امن و قابل راستی‌آزمایی:** تمام سورس‌کد در دسترس است؛ پیکربندی زنده با امضای رمزنگاری `ECDSA P-256` محافظت می‌شود و آپدیت درون‌برنامه‌ای فقط APK امضاشده با کلید اصلی و هش تاییدشده `SHA-256` را می‌پذیرد.

---

<a id="download"></a>
## ⬇️ دانلود مستقیم آخرین نسخه (APK)

آخرین نسخه امضاشده را مستقیماً از **[بخش Releases گیت‌هاب](https://github.com/thepoo7an/chand-iran-currency-gold-crypto/releases/latest)** دریافت کنید:

| نوع فایل | مناسب برای | نام فایل در Release |
| :--- | :--- | :--- |
| **`arm64-v8a`** *(پیشنهادی)* | ۹۹٪ گوشی‌های اندروید ۶۴ بیتی مدرن (اندروید ۱۰ به بالا) | `Chand-arm64-v8a-release.apk` |
| **`armeabi-v7a`** | گوشی‌های قدیمی‌تر ۳۲ بیتی | `Chand-armeabi-v7a-release.apk` |
| **`universal`** | سازگار با تمام معماری‌های پردازنده | `Chand-universal-release.apk` |

> **نکته برای آپدیت‌های بعدی:** برای حفظ امضای نصب و دریافت به‌روزرسانی‌های بدون خطا، همیشه نسخه متناسب با معماری گوشی خود (ترجیحاً `arm64-v8a`) را نصب کنید. در داخل برنامه نیز از مسیر **تنظیمات ← بررسی نسخه جدید** می‌توانید مستقیماً آپدیت‌های امضاشده را دریافت و بررسی کنید.  
> *(شناسه بسته `ir.thepoo7an.chand` برای حفظ سازگاری کامل با نسخه‌های قبلی بدون تغییر باقی مانده است.)*

---

<a id="screenshots"></a>
## 📸 تصاویر نسخه فعلی برنامه

تمامی تصاویر زیر مربوط به رابط کاربری **نسخه فعلی نرخ‌نما** در تم مشکی مطلق **AMOLED** هستند:

<table>
  <tr>
    <td align="center" width="33%">
      <img src="docs/store-listing/screenshots/01-market-bento-amoled.png" width="240" alt="نمای اصلی بازار Bento AMOLED در نرخ‌نما" />
      <br/>
      <b>نمای اصلی بازار (Bento AMOLED)</b>
      <br/>
      <sub>نرخ لحظه‌ای، اسپارک‌لاین ۷ روزه، شاخص تازگی و مقایسه صرافی‌ها</sub>
    </td>
    <td align="center" width="33%">
      <img src="docs/store-listing/screenshots/02-asset-chart-jalali.png" width="240" alt="نمودار تعاملی تاریخچه با تقویم جلالی" />
      <br/>
      <b>نمودار تاریخچه با تقویم شمسی</b>
      <br/>
      <sub>حالت خطی و میله‌ای در بازه‌های ۲۴ساعت، ۷روز، ۱ماه و ۱سال</sub>
    </td>
    <td align="center" width="33%">
      <img src="docs/store-listing/screenshots/03-source-breakdown.png" width="240" alt="جزئیات منابع نرخ بازار و زمان منبع" />
      <br/>
      <b>شفافیت منابع و اختلاف صرافی‌ها</b>
      <br/>
      <sub>تفکیک زمان منبع از زمان دریافت + دلیل رد نرخ نامتعارف</sub>
    </td>
  </tr>
  <tr>
    <td align="center" width="33%">
      <img src="docs/store-listing/screenshots/04-price-alerts.png" width="240" alt="مدیریت هشدارهای قیمت در نرخ‌نما" />
      <br/>
      <b>هشدارهای هوشمند عبور از حد قیمت</b>
      <br/>
      <sub>تعیین سقف و کف تومانی با پایش خودکار ۳۰ دقیقه‌ای در پس‌زمینه</sub>
    </td>
    <td align="center" width="33%">
      <img src="docs/store-listing/screenshots/05-currency-converter.png" width="240" alt="مبدل فوری ارز، طلا، سکه و رمزارز" />
      <br/>
      <b>مبدل سریع ارز، طلا و رمزارز</b>
      <br/>
      <sub>تبدیل فوری به تومان همراه با معادل لحظه‌ای در سایر بازارها</sub>
    </td>
    <td align="center" width="33%">
      <img src="docs/store-listing/screenshots/06-widgets-and-lockscreen.png" width="240" alt="۵ ویجت صفحه اصلی و ویجت شفاف لاک‌اسکرین" />
      <br/>
      <b>۵ ویجت صفحه اصلی و لاک‌اسکرین</b>
      <br/>
      <sub>ویجت‌های ۱×۱، ۲×۱ شیشه‌ای، ۴×۲، ۲×۲ رمزارز و قفل‌صفحه (اندروید ۱۲+)</sub>
    </td>
  </tr>
</table>

<div align="center">
  <img src="docs/store-listing/feature-graphic-2-1280x720.png" width="100%" alt="ویجت‌های شیشه‌ای و هشدارهای قیمت نرخ‌نما" />
</div>

---

<a id="features"></a>
## ✨ امکانات کامل نسخه فعلی

### 📊 ۱. پوشش کامل ۳۲ دارایی در ۴ بازار کلیدی
| دسته بازار | تعداد | دارایی‌های پشتیبانی‌شده | واحد و منبع مبنا |
| :--- | :---: | :--- | :--- |
| **ارزهای آزاد و بین‌المللی** | **۱۳** | دلار آمریکا (`USD`)، یورو (`EUR`)، درهم (`AED`)، پوند (`GBP`)، لیر ترکیه (`TRY`)، دینار کویت (`KWD`)، دلار کانادا (`CAD`)، فرانک سوئیس (`CHF`)، یوان چین (`CNY`)، دلار استرالیا (`AUD`)، ین ژاپن (`JPY`)، ریال عربستان (`SAR`)، دینار عراق (`IQD`) | تومان (`TMN`) • تجمیع از `TGJU`، صرافی‌های کریپتویی تترپایه و نرخ متقاطع `ECB / Frankfurter` |
| **طلا و انواع سکه** | **۹** | طلای ۱۸ عیار (`GRAM`)، ۲۴ عیار (`GRAM24`)، مثقال طلا (`MESGHAL`)، سکه امامی (`SEKE`)، بهار آزادی (`BAHAR`)، نیم‌سکه (`HALF`)، ربع‌سکه (`QUARTER`)، سکه گرمی (`GERAMI`)، انس جهانی (`XAU`) | تومان (`TMN`) و دلار برای انس • همراه با **کنترل تطابق واحد** با مبنای `XAU × USD` و برچسب هشدار «بازبینی واحد» در اختلاف بالای ۲۰٪ |
| **نفت خام جهانی** | **۲** | نفت برنت (`BRENT`) و نفت تگزاس (`WTI`) | دلار به ازای هر بشکه (`USD/bbl`) همراه با نمایش اختیاری معادل تخمینی تومانی |
| **رمزارزها** | **۸** | تتر (`USDT` با برچسب شفاف `ثابت $۱`)، بیت‌کوین (`BTC`)، اتریوم (`ETH`)، تون‌کوین (`TON`)، سولانا (`SOL`)، `BNB`، ریپل (`XRP`)، دوج‌کوین (`DOGE`) | تومان (`TMN`) + قیمت جهانی دلاری از `Binance` و صرافی‌های ایرانی |

### 🔍 ۲. شفافیت چندمنبعی، مقایسه صرافی‌ها و تازگی داده
- **تجمیع از منابع واقعی:** `TGJU`، `Wallex`، `Bitpin`، `Nobitex`، `Binance`، `Frankfurter (ECB)`، `Currency-API`، `Open-ER`، `Gold-API` و خوراک‌های نفتی جهانی.
- **مقایسه لحظه‌ای صرافی‌ها:** نمایش اختلاف قیمت صرافی‌ها روی کارت هر دارایی (مثلاً `Wallex +300 vs TGJU`) و علامت‌گذاری اختلاف‌های غیرعادی (`disputed` در انحراف بالای `۰.۵٪`).
- **برگه جزئیات منابع:** با لمس نشانگر منبع یا دکمه `Sources`، فهرست کامل منابع پذیرفته‌شده یا ردشده، **زمان اعلام منبع**، **زمان دریافت واقعی** و دلیل حذف نرخ نامتعارف نمایش داده می‌شود.
- **شاخص تازگی هر دارایی (Freshness):** برچسب سن داده با رنگ‌بندی وضعیت (`<= ۳۰ دقیقه` عادی، `> ۳۰ دقیقه` کهربایی، `> ۲ ساعت` قرمز) روی کارت‌ها، ردیف‌ها و تمام ویجت‌ها.

### 📈 ۳. نمودارهای صادقانه و تاریخچه شمسی
- **۴ بازه زمانی و ۲ حالت نمایش:** بازه‌های `۲۴ ساعت`، `۷ روز`، `۱ ماه` و `۱ سال` در دو حالت **خطی** و **میله‌ای** با امکان لمس و اسکرول روی نقاط نمودار همراه با **تاریخ و ساعت دقیق جلالی**.
- **الگوریتم تمیزسازی صادقانه (`HistoryNormalizer`):** حذف داده‌های پرت بدون از بین بردن جهش‌های واقعی بازار، جلوگیری از تشخیص گپ مصنوعی در سری‌های کوتاه (کمتر از ۵ نقطه)، و مخفی‌سازی هوشمند اسپارک‌لاین یا درصد تغییر هنگام کهنه‌بودن یا نبود داده معتبر (به‌جای نمایش `0.0%` گمراه‌کننده).

### 🔔 ۴. هشدارهای هوشمند قیمت (WorkManager)
- ثبت حد بالا و/یا پایین تومانی برای هر دارایی به‌صورت کاملاً محلی در `DataStore`.
- بررسی خودکار قیمت‌ها هر **۳۰ دقیقه** در پس‌زمینه (و همچنین هنگام باز کردن برنامه).
- ارسال اعلان عبور از حد با مکانیزم **Hysteresis** (جلوگیری از تکرار پی‌درپی اعلان) و امکان فعال‌سازی مجدد دستی یا ارسال **اعلان آزمایشی** از صفحه مدیریت هشدارها.

### 🧩 ۵. پنج ویجت بومی Jetpack Glance (صفحه اصلی و قفل‌صفحه)
1. **ویجت تک‌نرخی `۱×۱`:** انتخاب دارایی دلخواه از تنظیمات (دلار به‌صورت پیش‌فرض) با مقیاس تایپوگرافی دقیق (`SizeMode.Exact`) متناسب با ابعاد خانه.
2. **ویجت شیشه‌ای `۲×۱`:** نمایش هم‌زمان دلار، یورو و طلای ۱۸ عیار با تنظیم میزان شفافیت پس‌زمینه (`۵۰٪`، `۷۰٪` و `۸۵٪`).
3. **ویجت فهرست برگزیده‌ها `۴×۲`:** نمایش دارایی‌های پین‌شده همراه با تغییر روزانه و سن تازگی داده.
4. **ویجت رمزارزها `۲×۲`:** نمایش سریع `BTC`، `ETH` و `USDT`.
5. **ویجت شفاف لاک‌اسکرین (الهام‌گرفته از Nothing OS):** بدون پس‌زمینه و با خوانایی بالا روی صفحه قفل در **اندروید ۱۲ و بالاتر (`API 31+`)**؛ در اندروید ۷ تا ۱۱ به‌صورت خودکار به‌عنوان ویجت صفحه اصلی در دسترس است.

> **همگام‌سازی هوشمند ویجت‌ها:** با برگشت اتصال اینترنت (`NetworkCallback`) ویجت‌ها بلافاصله تازه می‌شوند و زمان‌بندی ۳۰ دقیقه‌ای `WorkManager` حتی پس از راه‌اندازی مجدد گوشی (Reboot) پایدار می‌ماند.

### 🎨 ۶. تجربه کاربری، مبدل سریع و شخصی‌سازی
- **۳ تم اختصاصی:** مشکی مطلق **AMOLED** (بهینه برای مصرف باتری)، تیره و روشن.
- **نمای Bento گرید ۲ ستونه یا لیست فشرده** با امکان پین‌کردن دارایی‌های مهم و جستجوی آنی.
- **مبدل سریع ارز، طلا، سکه و رمزارز:** دارای صفحه‌کلید عددی اختصاصی (با دکمه `000`) و نمایش هم‌زمان معادل در ۴ بازار پرکاربرد.
- **حفظ انتخاب ارقام:** تنظیم مستقل زبان برنامه (**فارسی راست‌چین** / **انگلیسی چپ‌چین**) و نوع ارقام (**فارسی `۱۲۳`** / **لاتین `123`**).
- **کارت حمایت از پروژه (اختیاری):** به‌صورت پیش‌فرض کاملاً مخفی است و در تنظیمات قابل فعال‌سازی است.

---

<a id="security"></a>
## 🛡 امنیت، حریم خصوصی و دسترسی‌ها

نرخ‌نما فقط حداقل مجوزهای فنی لازم را درخواست می‌کند و **هیچ دسترسی به موقعیت مکانی، دوربین، مخاطبین، حافظه یا تلفن ندارد**:

| دسترسی در مانیفست | کاربرد دقیق و شفاف |
| :--- | :--- |
| `INTERNET` | دریافت نرخ لحظه‌ای ارز، طلا، نفت و رمزارزها و بررسی انتشار جدید در گیت‌هاب |
| `ACCESS_NETWORK_STATE` | تشخیص بازگشت اینترنت برای تازه‌سازی فوری ویجت‌ها و جلوگیری از درخواست بی‌نتیجه در حالت آفلاین |
| `POST_NOTIFICATIONS` *(اندروید ۱۳+)* | فقط در صورت تمایل کاربر برای دریافت اعلان هشدارهای عبور از حد قیمت |
| `REQUEST_INSTALL_PACKAGES` | صرفاً برای نصب اختیاری فایل APK به‌روزرسانی از بخش تنظیمات برنامه |

### لایه‌های امنیتی داخلی
- **تایید امضای پیکربندی زنده (`chand-live-config.json`):** هر تغییر آدرس سرویس‌دهنده یا آستانه‌ها باید با کلید `ECDSA P-256` امضا شده و منقضی نشده باشد؛ آدرس‌های غیرHTTPS یا IPهای خصوصی مسدود می‌شوند و در صورت ۳ خطای پیاپی، پیکربندی به‌صورت خودکار به تنظیمات داخلی امن بازمی‌گردد.
- **آپدیت درون‌برنامه‌ای سخت‌گیرانه:** دانلود فقط از دامنه‌های مجاز گیت‌هاب با HTTPS، تایید اجباری هش `SHA-256` و حجم فایل از `chand-update-metadata.json`، بررسی دقیق `packageName` و `versionCode`، و تطبیق اثرانگشت گواهی امضای Release (`EXPECTED_RELEASE_CERT_SHA256`). فایل‌های نامعتبر بلافاصله حذف می‌شوند.

جزئیات بیشتر را در [`SECURITY.md`](SECURITY.md) و [`docs/live-config.md`](docs/live-config.md) بخوانید.

---

<a id="build"></a>
## 🛠 ساخت از سورس و معماری فنی

### پیش‌نیازها
- **JDK 21** (Temurin / OpenJDK)
- **Android SDK** (`compileSdk 36`، `minSdk 24`، `targetSdk 36`)
- رپر داخلی Gradle (`./gradlew` — نسخه ۹.۳.۱) بدون نیاز به نصب سراسری Gradle

### دستورات ساخت و تست
```bash
# اجرای تست‌های واحد و Robolectric
./gradlew :app:testDebugUnitTest

# ساخت نسخه دیباگ (APK)
./gradlew :app:assembleDebug

# ساخت نسخه‌های انتشار با تفکیک معماری (Universal + arm64-v8a + armeabi-v7a)
KEYSTORE_PATH=/path/to/your-upload-key.jks \
STORE_PASSWORD=your_store_password \
KEY_PASSWORD=your_key_password \
CERT_SHA256=<your_release_cert_sha256> \
./gradlew :app:assembleRelease -PenableAbiSplits=true
```

> **نکته مهم برای فورک‌ها (Forks):** فایل `app/build.gradle.kts` اثرانگشت گواهی `EXPECTED_RELEASE_CERT_SHA256` را هنگام بیلد داخل APK قرار می‌دهد. هنگام ساخت بیلد شخصی، مقدار `CERT_SHA256` را برابر با گواهی امضای خودتان تنظیم کنید؛ در غیر این صورت آپدیت درون‌برنامه‌ای به دلیل تطابق‌نداشتن امضا متوقف می‌شود.

### پشته فناوری (Tech Stack)
- **زبان و هم‌زمانی:** Kotlin + Coroutines & StateFlow
- **رابط کاربری و ویجت:** Jetpack Compose، Material 3، Type-Safe Navigation Compose، Jetpack Glance AppWidgets
- **شبکه و سریال‌سازی:** Retrofit 2، OkHttp، Moshi KSP Codegen (بدون `kotlin-reflect`)، Kotlinx Serialization
- **ذخیره‌سازی و پس‌زمینه:** Jetpack DataStore Preferences (`chand_prefs`)، WorkManager
- **تست و CI/CD:** JUnit 4، Robolectric، Compose UI Test، GitHub Actions (`ci.yml` و `release-apk.yml`)

### اسناد تکمیلی مخزن
- [`CHANGELOG.md`](CHANGELOG.md) — فهرست کامل تغییرات و بهبودها
- [`SECURITY.md`](SECURITY.md) — سیاست امنیت و گزارش آسیب‌پذیری
- [`docs/live-config.md`](docs/live-config.md) — مستندات ساختار و امضای پیکربندی زنده
- [`docs/provider-split.md`](docs/provider-split.md) — مستندات جداسازی سرویس‌دهنده‌های بازار
- [`docs/store-listing/README.md`](docs/store-listing/README.md) — بسته آماده انتشار کافه‌بازار و مایکت

---

## ⭐ حمایت و مشارکت

اگر **نرخ‌نما** به شما کمک می‌کند قیمت‌ها را سریع‌تر، دقیق‌تر و بدون تبلیغات دنبال کنید، لطفاً با دادن یک **ستاره (Star ⭐)** در بالای صفحه از پروژه حمایت کنید تا افراد بیشتری آن را پیدا کنند.

- گزارش باگ یا پیشنهاد ارز/ویژگی جدید: **[ثبت Issue جدید](https://github.com/thepoo7an/chand-iran-currency-gold-crypto/issues/new/choose)**
- مشارکت در کد یا مستندات: ارسال Pull Request با رعایت تست‌ها و قوانین حریم خصوصی پروژه

---

<a id="english-summary"></a>
<details>
<summary><b>🇬🇧 English Overview & Build Quickstart (Click to expand)</b></summary>

<br/>

**Nerkhnama (نرخ‌نما)** (`ir.thepoo7an.chand`) is a free, open-source, zero-ads AMOLED Android app built with Kotlin, Jetpack Compose, Material 3, and Jetpack Glance for tracking live **Iranian Toman (TMN)** market rates across **32 assets**:
- **13 Fiat Currencies** (`USD`, `EUR`, `AED`, `GBP`, `TRY`, `KWD`, `CAD`, `CHF`, `CNY`, `AUD`, `JPY`, `SAR`, `IQD`)
- **9 Gold & Iranian Coins** (`GRAM` 18K, `GRAM24`, `MESGHAL`, `SEKE`, `BAHAR`, `HALF`, `QUARTER`, `GERAMI`, `XAU`) with unit-consistency cross-checking
- **2 Global Crude Oil Benchmarks** (`BRENT`, `WTI` in `USD/bbl` + estimated TMN equivalent)
- **8 Cryptocurrencies** (`USDT` with `$1 = peg` badge, `BTC`, `ETH`, `TON`, `SOL`, `BNB`, `XRP`, `DOGE`)

### Key Highlights
- **True Multi-Provider Consensus & Transparency:** Aggregates `TGJU`, `Wallex`, `Bitpin`, `Nobitex`, `Binance`, `Frankfurter (ECB)`, `Currency-API`, `Open-ER`, `Gold-API`, and crude oil benchmarks with per-provider quotes, divergence detection, separate source vs. fetch timestamps, and per-asset freshness indicators (`<= 30m` normal, `> 30m` amber, `> 2h` red).
- **Honest Jalali Charts & Cache-First UX:** Interactive `24H` / `7D` / `1M` / `1Y` line & bar charts with Jalali dates, `HistoryNormalizer` shock preservation, and 30-second soft-TTL cache-first rendering across feed and widgets.
- **WorkManager Price Alerts:** Local upper/lower TMN thresholds with 30-minute background evaluation, re-arm hysteresis, and Android 13+ `POST_NOTIFICATIONS` support.
- **5 Native Glance Widgets:** `1×1` customizable hero, `2×1` translucent glass (`50%`/`70%`/`85%`), `4×2` pinned watchlist, `2×2` crypto watch, and an Android 12+ transparent lock-screen widget (with pre-Android 12 home-screen fallback).
- **Hardened Security:** `ECDSA P-256`-signed `chand-live-config.json` with auto-rollback + expiry, and GitHub Releases in-app updater enforcing HTTPS allowlist, `SHA-256`, size, `versionCode` monotonicity, and pinned `EXPECTED_RELEASE_CERT_SHA256`.

### Optional Build Properties
- `EIA_API_KEY` / `-PeiaApiKey=...` — enables official U.S. EIA Brent/WTI spot benchmark validation at build time.
- `LIVE_CONFIG_PUBLIC_KEYS` / `-PliveConfigPublicKeys=...` — overrides `gradle/live-config-keys.properties` trust store for forks.
- GitHub Actions release secrets (`KEYSTORE_BASE64`, `STORE_PASSWORD`, `KEY_PASSWORD`) power `.github/workflows/release-apk.yml`.

Licensed under the **[MIT License](LICENSE)**.

</details>

<div align="center">
  <sub>منتشرشده تحت مجوز <a href="LICENSE">MIT License</a> • ساخته‌شده با ❤️ برای جامعه متن‌باز ایران</sub>
</div>
