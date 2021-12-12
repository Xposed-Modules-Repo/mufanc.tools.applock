# Xposed
-keep class mufanc.tools.applock.xposed.XposedEntry
-keepclassmembers class mufanc.tools.applock.app.MyApplication$Companion {
    isModuleActivated();
}