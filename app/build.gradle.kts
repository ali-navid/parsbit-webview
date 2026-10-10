import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.parsbit.online"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.parsbit.online"
        minSdk = 24
        targetSdk = 36            // بازار حداقل ۳۴ می‌خواهد؛ ۳۶ از این هم جلوتر است. اگر CI روی android-36 خطا داد، این دو (و compileSdk) را ۳۵ کنید.
        versionCode = 5           // باید از آخرین نسخهٔ آپلودشده در بازار بزرگ‌تر باشد
        versionName = "1.0.5"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // کلید امضای release: از فایل keystore.properties (ساخت محلی) یا از متغیرهای محیطی (GitHub Actions) خوانده می‌شود.
    val keystorePropsFile = rootProject.file("keystore.properties")
    val hasKeystoreFile = keystorePropsFile.exists()
    val keystoreProps = Properties().apply { if (hasKeystoreFile) load(FileInputStream(keystorePropsFile)) }
    val envStore = System.getenv("KEYSTORE_FILE")

    signingConfigs {
        create("release") {
            when {
                hasKeystoreFile -> {
                    storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                    storePassword = keystoreProps.getProperty("storePassword")
                    keyAlias = keystoreProps.getProperty("keyAlias")
                    keyPassword = keystoreProps.getProperty("keyPassword")
                }
                envStore != null -> {
                    storeFile = file(envStore)
                    storePassword = System.getenv("KEYSTORE_PASSWORD")
                    keyAlias = System.getenv("KEY_ALIAS")
                    keyPassword = System.getenv("KEY_PASSWORD")
                }
            }
            // امضای مدرن v2/v3 (برای کاهش هشدار Play Protect و سازگاری با اندروید جدید)
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            // اگر کلید در دسترس باشد، release با آن امضا می‌شود؛ وگرنه بدون امضا می‌ماند (برای جلوگیری از انتشار اتفاقیِ APK دیباگ)
            signingConfig = if (hasKeystoreFile || envStore != null) signingConfigs.getByName("release") else null
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-beta01")
    implementation("androidx.webkit:webkit:1.17.1")
}
