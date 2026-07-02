# kotlinx-serialization — 保护 @Serializable 类的字段名和序列化器
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.stark.miuix.data.model.**$$serializer { *; }
-keepclassmembers class com.stark.miuix.data.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.stark.miuix.data.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Ktor — 保护引擎和插件注册
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Kamel — 图片加载
-keep class io.kamel.** { *; }

# Miuix — UI 组件
-keep class top.yukonga.miuix.** { *; }

# Compose — 保护 Stability 和 Composable 元数据
-dontwarn androidx.compose.**
