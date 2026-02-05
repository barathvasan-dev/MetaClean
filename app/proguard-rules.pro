# Add project specific ProGuard rules here.
# Metadata Extractor
-keep class com.drew.** { *; }
-dontwarn com.drew.**

# EXIF Interface
-keep class androidx.exifinterface.** { *; }
-dontwarn androidx.exifinterface.**
