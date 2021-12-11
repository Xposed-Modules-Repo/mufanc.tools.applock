package mufanc.tools.applock.xposed

import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.init.InitFields
import com.github.kyuubiran.ezxhelper.utils.Log
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookReplace
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage
import mufanc.tools.applock.BuildConfig
import mufanc.tools.applock.app.MyApplication

class XposedEntry : IXposedHookZygoteInit, IXposedHookLoadPackage {
    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        EzXHelperInit.initZygote(startupParam)
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        EzXHelperInit.initHandleLoadPackage(lpparam)
        EzXHelperInit.setLogTag("AppLock")

        try {
            if (InitFields.hostPackageName == BuildConfig.APPLICATION_ID) {
                findMethod(MyApplication.Companion::class.java.name) {
                    name == "isModuleActivated"
                }.hookReplace { true }
            } else if (InitFields.hostPackageName == "android") {
                ProcessManagerService.main()
            }
        } catch (err: Throwable) {
            Log.e(err)
        }
    }
}