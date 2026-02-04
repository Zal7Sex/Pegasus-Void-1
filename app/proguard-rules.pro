# Add project specific ProGuard rules here.
-keep class com.pegasus.void.** { *; }
-keepclassmembers class * {
    public <init>(...);
}
-dontwarn org.java_websocket.**
-keep class org.java_websocket.** { *; }
