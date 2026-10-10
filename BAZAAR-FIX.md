# رفع ایرادهای انتشار در کافه‌بازار — اپ وب‌ویو ParsBit

خبر خوب: رویکرد فعلی تو (اپ Kotlin + وب‌ویو + GitHub Actions) درست و ساده است؛ لازم نیست عوضش کنی.
فقط سه چیز را اصلاح می‌کنیم. ریشهٔ همهٔ ایرادها دو چیز بود:

| ایراد بازار | ریشهٔ واقعی | وضعیت |
|---|---|---|
| «Unsafe app blocked / نسخهٔ قدیمی اندروید» (targetSdk) | تذکر برای **version code 2** (نسخهٔ قدیمی) بود | ✅ در مخزن الان `targetSdk=36` است (≥۳۴). حل است. |
| «App blocked / Play Protect — ناشر ناشناس» | ورک‌فلو **APK دیباگ** می‌ساخت (`assembleDebug`) که با کلید دیباگ امضا می‌شود و بسته‌اش `com.parsbit.online.debug` بود | ✅ با فایل‌های جدید، **release امضاشده** ساخته می‌شود |
| خالی‌شدن فیلد رمز بعد از کپچا | چارچوب Autofill اندروید داخل وب‌ویو | ✅ در `MainActivity.kt` خاموش شد |

**هیچ‌وقت APK دیباگ را در فروشگاه منتشر نکن.** دیباگ فقط برای تست روی گوشی خودت است.

---

## سه فایل تغییر کرد (جایگزین کن)
1. `app/build.gradle.kts` — افزودن امضای release، `versionCode=5`، `versionName=1.0.5`
2. `.github/workflows/android-build.yml` — ساختِ **release امضاشده** به‌جای debug + تأیید امضای v2/v3
3. `app/src/main/java/com/parsbit/online/MainActivity.kt` — خاموش‌کردن Autofill (رفع باگ رمز) + کوکی شخص‌ثالث

---

## گام ۱: یک‌بار کلید امضا بساز (keystore)
این کلید «هویت ناشر» توست. روی کامپیوترت (هرجا Java باشد) این را اجرا کن:

```bash
keytool -genkeypair -v \
  -keystore parsbit-release.keystore \
  -alias parsbit \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass 'یک‌رمز‌قوی' -keypass 'یک‌رمز‌قوی' \
  -dname "CN=ParsBit, OU=Mobile, O=ParsBit, L=Tehran, C=IR"
```

> ⚠️ **خیلی مهم:** این فایل `parsbit-release.keystore` و رمزش را تا ابد نگه دار و گم نکن.
> هر آپدیت بعدیِ اپ باید با **همین کلید** امضا شود، وگرنه بازار/اندروید آپدیت را قبول نمی‌کنند.
> هشدار Play Protect هم با ثابت‌ماندنِ همین کلید و زیادترشدن نصب‌ها، به‌مرور کم و محو می‌شود.
> این فایل را در گیت کامیت **نکن**.

## گام ۲: کلید را به‌صورت Secret در گیت‌هاب بگذار
۴ مقدار باید در مخزن ثبت شود. اول فایل کلید را base64 کن:

```bash
base64 -w0 parsbit-release.keystore > keystore.b64     # روی مک: base64 -i parsbit-release.keystore | tr -d '\n' > keystore.b64
```

بعد در گیت‌هاب: **Settings → Secrets and variables → Actions → New repository secret** و این چهار را بساز:

| نام Secret | مقدار |
|---|---|
| `KEYSTORE_BASE64` | کل محتوای فایل `keystore.b64` |
| `KEYSTORE_PASSWORD` | همان `storepass` بالا |
| `KEY_ALIAS` | `parsbit` |
| `KEY_PASSWORD` | همان `keypass` بالا |

## گام ۳: فایل‌ها را جایگزین، کامیت و پوش کن
سه فایل این بسته را روی مخزن بگذار و:

```bash
git add -A && git commit -m "Build signed release APK for Bazaar" && git push
```

تب **Actions** در گیت‌هاب خودش build می‌کند. وقتی سبز شد، از پایین صفحهٔ همان اجرا،
آرتیفکت **`ParsBit-Release-APK`** را دانلود کن؛ داخلش `app-release.apk` امضاشده است.
(مرحلهٔ «Verify APK signature» خودش چک می‌کند که v2/v3 واقعاً اعمال شده باشد.)

## گام ۴: دوباره در بازار آپلود کن
همین `app-release.apk` (نسخهٔ ۵) را در پیشخان بازار آپلود و برای بازبینی بفرست.
چون هم `targetSdk≥34` است و هم release امضاشده، دو ایراد نصب برطرف می‌شوند.

---

## باگ رمز بعد از کپچا
در `MainActivity.kt` با یک خط، دخالت Autofillِ اندروید در وب‌ویو خاموش شد
(`importantForAutofill = NO_EXCLUDE_DESCENDANTS`). رایج‌ترین علتِ «رمزِ تایپ‌شده بعد از کپچا خالی می‌شود»
همین است. بعد از نصب نسخهٔ جدید، روی همان گوشی (Redmi Note 9S) دوباره تست کن.

اگر باز هم تکرار شد، یعنی مشکل سمتِ **خود سایت `pars-bit.com`** است، نه اپ. در آن صورت در صفحهٔ ورودِ سایت این‌ها را چک کنید:
- فیلد رمز `autocomplete="current-password"` داشته باشد (نه `off`؛ بعضی وب‌ویوها با `off` بد رفتار می‌کنند).
- دکمهٔ «ببین/مخفی رمز» یا اسکریپتِ کپچا، موقع تازه‌کردنِ کپچا فرم را `reset` یا فیلد رمز را دوباره نسازد.
- ثبت/اعتبارسنجی کپچا به‌صورت AJAX نباشد که بخشی از فرم (شاملِ رمز) را از نو رندر کند.

---

## اگر نخواستی با گیت‌هاب کار کنی (ساخت محلی در Android Studio)
کنار فایل `app/` یک فایل `keystore.properties` بساز (و آن را در گیت کامیت نکن):

```properties
storeFile=../parsbit-release.keystore
storePassword=یک‌رمز‌قوی
keyAlias=parsbit
keyPassword=یک‌رمز‌قوی
```

بعد در اندروید استودیو: **Build → Generate Signed Bundle / APK → APK → Release**
(یا در ترمینال `./gradlew assembleRelease`). خروجی در `app/build/outputs/apk/release/app-release.apk`.
`build.gradle.kts` خودش این فایل را می‌خواند.

## اگر CI روی android-36 خطا داد
در `app/build.gradle.kts` مقدار `compileSdk` و `targetSdk` را `35` کن و در ورک‌فلو `android-36`/`build-tools;36.0.0`
را به `android-35`/`build-tools;35.0.0` تغییر بده. اندروید ۱۵ (API 35) هم شرط «≥۳۴» بازار را برآورده می‌کند.
