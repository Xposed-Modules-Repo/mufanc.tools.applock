# Xposed
-keep class mufanc.tools.applock.xposed.XposedEntry
-keep class mufanc.tools.applock.xposed.ProcessManagerService
-keepclassmembers class mufanc.tools.applock.app.MyApplication {
    static final boolean isModuleActivated;
}