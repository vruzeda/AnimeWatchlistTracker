# Preserve line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Moshi (reflection-based with KotlinJsonAdapterFactory) ---
-keep class com.vuzeda.animewatchlist.tracker.data.api.dto.** { *; }
-keepclassmembers class com.vuzeda.animewatchlist.tracker.data.api.dto.** { *; }

# Keep Moshi's built-in adapters and annotations
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-keepclassmembers @com.squareup.moshi.JsonClass class * { *; }
-keep @com.squareup.moshi.JsonQualifier @interface *
-keepclassmembers class kotlin.Metadata { *; }

# --- Retrofit ---
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keep,allowshrinking,allowobfuscation class retrofit2.Response
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# --- OkHttp ---
-dontwarn okhttp3.**
-dontwarn okio.**

# --- Kotlin Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# --- Kotlin Serialization (keep metadata for reflection) ---
-keep class kotlin.reflect.** { *; }
-dontwarn kotlin.reflect.**

# --- Firebase ---
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**
