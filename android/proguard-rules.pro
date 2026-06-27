# Keep libGDX entry points.
-keep class com.badlogic.gdx.** { *; }
-keep class com.richeyworks.blackjack.** { *; }

# Reflection used by libGDX for input listeners.
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes *Annotation*
-dontwarn org.lwjgl.**
