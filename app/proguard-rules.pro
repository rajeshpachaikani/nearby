# R8/ProGuard rules for release builds.

# Crash report line numbers (upload mapping.txt to Play Console).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin metadata.
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-keepattributes Signature,InnerClasses,EnclosingMethod
-keep class kotlin.Metadata { *; }

# Gson reflection-based (de)serialization.
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# App data models serialized via Gson (export-as-JSON feature).
-keep class in.unartech.nearbydevs.data.model.** { *; }
-keepclassmembers class in.unartech.nearbydevs.data.model.** { *; }

# Compose runtime.
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# Coroutines.
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# AndroidX DataStore.
-keep class androidx.datastore.*.** { *; }

# Enum values accessed via reflection (Gson + general).
-keepclassmembers enum * { *; }

# Strip verbose/debug logs from release.
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
}
