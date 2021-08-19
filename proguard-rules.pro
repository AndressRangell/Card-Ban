# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep class com.newpos.** {*;}
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

-dontwarn com.bancard.cajas.*
-dontwarn sun.misc.**

-keep public class * {
    public protected *;
}
-keep class Sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

-keepclassmembers enum * { *; }

-keep class com.google.gson.examples.android.model.** { <fields>; }

-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

-keep class javax.** {*;}
-keep class com.sun.** {*;}
-keep public class Mail {*;}
-keep class com.sun.mail.handlers.** {*;}
-keep class com.newpos.bypay.*{*;}
-keep class emvl2 {public *;}
-keep class desert-email-1709191523 {public *;}
-keep class libpboc {public *;}
-keep class jpos {public *;}
-keep class emvl2_new {public *;}
-dontwarn java.awt.**
-dontwarn com.sun.mail.handlers.handler_base**
-dontwarn xxdroid.support.v4.**
-dontwarn xxdroid.app.Notification