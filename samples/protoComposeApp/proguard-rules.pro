-dontwarn com.squareup.wire.AndroidMessage
-dontwarn com.squareup.wire.AndroidMessage$*
-dontwarn android.os.Parcelable
-dontwarn android.os.Parcelable$Creator
-dontwarn android.os.Parcel

# Required on JVM for JNA-based integrations.
-keep class com.sun.jna.** { *; }
-keep class * implements com.sun.jna.** { *; }

# Required when using FileKit Dialogs on Linux (XDG Desktop Portal / DBus).
-keep class org.freedesktop.dbus.** { *; }
-keep class io.github.vinceglb.filekit.dialogs.platform.xdg.** { *; }
-keepattributes Signature,InnerClasses,RuntimeVisibleAnnotations
