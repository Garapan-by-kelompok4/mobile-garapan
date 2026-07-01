# Keep readable stack traces in Play Console crash reports.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Gson (reflection-based) ---
# DTOs have no @SerializedName; Gson maps JSON keys to Kotlin field names via
# reflection, so DTO class and field names must survive obfuscation.
-keep class com.app.garapan.data.remote.dto.** { *; }
# Generic type information for TypeToken<List<...>> deserialization.
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# --- Socket.io / Engine.io (reflection + org.json interop) ---
-keep class io.socket.** { *; }
-dontwarn io.socket.**
