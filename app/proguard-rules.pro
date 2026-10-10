# kotlinx.serialization: conservar los serializadores generados del backup.
-keepclassmembers @kotlinx.serialization.Serializable class com.crasty.app.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
