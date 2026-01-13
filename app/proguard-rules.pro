# Keep JNI methods
-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class io.github.dovecoteescapee.byedpi.core.ByeDpiProxy { *; }
-keep,allowoptimization class io.github.dovecoteescapee.byedpi.core.TProxyService { *; }
-keep,allowoptimization class io.github.dovecoteescapee.byedpi.activities.** { *; }
-keep,allowoptimization class io.github.dovecoteescapee.byedpi.services.** { *; }
-keep,allowoptimization class io.github.dovecoteescapee.byedpi.receiver.** { *; }

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

-repackageclasses 'ru.gdlbo'
-renamesourcefileattribute ''
-keepattributes SourceFile,InnerClasses,EnclosingMethod,Signature,RuntimeVisibleAnnotations,*Annotation*,*Parcelable*
-allowaccessmodification
-overloadaggressively
-optimizationpasses 5
-verbose
-dontusemixedcaseclassnames
-adaptclassstrings
-adaptresourcefilecontents **.xml,**.json
-adaptresourcefilenames **.xml,**.json