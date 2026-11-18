# Add project specific ProGuard / R8 rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# --------------------------------------------------------------------------
# General R8 / DEX Code Optimization Settings
# --------------------------------------------------------------------------
-optimizationpasses 5
-allowaccessmodification

# Preserve line number information for crash stack traces while hiding source filenames
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep EdgeToEdge entry points un-obfuscated for Play Console static edge-to-edge detection
-keep class androidx.activity.EdgeToEdge { *; }
-keep class androidx.activity.SystemBarStyle { *; }
-keep class androidx.activity.SystemBarStyle$Companion { *; }

# Preserve generic type signatures and annotations required by Retrofit, Moshi, and Room
-keepattributes Signature,InnerClasses,EnclosingMethod,RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault

# Strip verbose/debug Android log calls in release DEX builds
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}

# --------------------------------------------------------------------------
# Retrofit 2 & OkHttp 3 Rules
# --------------------------------------------------------------------------
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Keep Retrofit service interfaces and method signatures
-keep interface com.example.data.remote.CurrencyApiService { *; }
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# --------------------------------------------------------------------------
# Moshi & Moshi Kotlin Codegen Rules
# --------------------------------------------------------------------------
-dontwarn com.squareup.moshi.**

# Keep @JsonClass annotated classes and their KSP-generated JsonAdapters
-keep @com.squareup.moshi.JsonClass class * { *; }
-keep class **JsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
    public <init>(com.squareup.moshi.Moshi, java.lang.reflect.Type[]);
}

# Explicitly preserve API response model
-keep class com.example.data.model.ExchangeRateResponse { *; }

# --------------------------------------------------------------------------
# Room Database Rules
# --------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase {
    <init>();
}
-keep @androidx.room.Entity class com.example.data.model.** { *; }
-keep @androidx.room.Dao interface com.example.data.local.** { *; }

# --------------------------------------------------------------------------
# Kotlin Coroutines
# --------------------------------------------------------------------------
-dontwarn kotlinx.coroutines.**
