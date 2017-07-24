# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in F:\Android studio\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}


# 不使用大小写混合
-dontusemixedcaseclassnames
# 如果应用程序引入的有jar包,并且想混淆jar包里面的class
-dontskipnonpubliclibraryclasses
# 混淆后生产映射文件 map 类名->转化后类名的映射
-verbose
-ignorewarnings

# Optimization is turned off by default. Dex does not like code run
# through the ProGuard optimize and preverify steps (and performs some
# of these optimizations on its own).
# 混淆时不做预校验
-dontpreverify

# If you want to enable optimization, you should include the following:
# 混淆采用的算法
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
# 设置混淆的压缩比率 0 ~ 7
-optimizationpasses 5
-allowaccessmodification


# Add any project specific keep options here:
-keepattributes *Annotation*
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgent
-keep public class * extends android.preference.Preference
-keep public class * extends android.support.v4.app.Fragment
-keep public class * extends android.app.Fragment
-keep public class com.android.vending.licensing.ILicensingService

-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**
-dontwarn java.awt.**,javax.security.**,java.beans.**

-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}

-keep class com.solo.search.** { *; }
-dontwarn com.solo.search.**

-keep class com.yahoo.mobile.client.share.search.** { *; }
-keep interface com.yahoo.mobile.client.share.search.** { *; }
-keep class * implements com.yahoo.mobile.client.share.search.interfaces.IFactory
-dontwarn com.yahoo.mobile.client.share.search.**
-dontwarn com.yahoo.data.**


# !!!!!!!!!!!!!!!!!!!!!!!!!!!!! 请不要在这之前添加东西！！！！！！！！！！！！！！！！！！！！

# 从这里开始添加我们所希望不被混淆的类
-keep public class com.cooeeui.zenlauncher.R$*{
    public static final int *;
}

# end

# MobvistaSDK *begin*
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.mobvista.** {*; }
-keep interface com.mobvista.** {*; }
-keep class android.support.v4.** { *; }
-keep class android.app.**{*;}
-keep class com.facebook.** {*;}
-dontwarn android.app.**
# MobvistaSDK *end*

#webview与js交互 add start
-keepclassmembers class com.cooeeui.brand.zenlauncher.searchbar.SearchActivity$JavaScriptObject{
  public *;
}
-keepattributes *Annotation*
-keepattributes *JavascriptInterface*
#webview与js交互 add end

#nano widget反射类 add start
-keep class com.cooeeui.brand.zenlauncher.widgets.IWidgetProxy
-keep class * implements com.cooeeui.brand.zenlauncher.widgets.IWidgetProxy
-keep public class com.cooeeui.brand.zenlauncher.widgets.weather.** { *; }
-keep public class com.cooeeui.brand.zenlauncher.widgets.hotapp.** { *; }
#nano widget反射类 add end


