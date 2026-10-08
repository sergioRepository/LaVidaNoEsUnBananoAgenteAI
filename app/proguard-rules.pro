# ProGuard rules for LaVidaNoEsUnBanano
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* *;
}
-keep class kotlinx.serialization.** { *; }
