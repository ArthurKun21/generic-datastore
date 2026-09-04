-dontwarn com.squareup.wire.AndroidMessage
-dontwarn com.squareup.wire.AndroidMessage$*
-dontwarn android.os.Parcelable
-dontwarn android.os.Parcelable$Creator
-dontwarn android.os.Parcel

# https://square.github.io/wire/#generating-code-with-wire
-keep class com.squareup.wire.** { *; }
-keep class io.github.arthurkun.generic.datastore.proto.app.wire.** { *; }

# Required on JVM for JNA-based integrations.
-keep class com.sun.jna.** { *; }
-keep class * implements com.sun.jna.** { *; }

# Required when using FileKit Dialogs on Linux (XDG Desktop Portal / DBus).
-keep class org.freedesktop.dbus.** { *; }
-keep class io.github.vinceglb.filekit.dialogs.platform.xdg.** { *; }
-keepattributes Signature,InnerClasses,RuntimeVisibleAnnotations
