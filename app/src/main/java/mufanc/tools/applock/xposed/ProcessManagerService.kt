package mufanc.tools.applock.xposed

import android.app.ActivityThread
import android.content.Context
import android.os.Binder
import android.os.Parcel
import com.github.kyuubiran.ezxhelper.utils.*
import de.robv.android.xposed.XC_MethodHook
import mufanc.tools.applock.BuildConfig
import mufanc.tools.applock.app.CommandHelper
import java.io.File
import java.util.*

object ProcessManagerService {
    const val transactCode = 16716  // ascii "AL" from [A]pp[L]ock

    private val systemContext: Context
        get() = ActivityThread.currentActivityThread().systemContext

    private const val dataDir = "/data/system/app_lock"
    private const val miuiHome = "com.miui.home"

    private val lock = Any()
    private val whiteList = TreeSet<String>()


    private fun getNameForUid(uid: Int): String? {
        return systemContext.packageManager.invokeMethodAutoAs<String>(
            "getNameForUid", uid
        )
    }

    object CommunicationService : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            try {
                if (param.args[0] != transactCode) return
                if (getNameForUid(Binder.getCallingUid()) != BuildConfig.APPLICATION_ID) return

                CommandHelper.onCommand(param.args[1] as Parcel, param.args[2] as Parcel) {
                    operation, args ->
                    when (operation) {
                        "connectionTest" -> "OK"
                        "setWhiteList" -> updateWhiteList(args)
                        "getWhiteList" -> whiteList.joinToString("\n")
                        else -> "Unknown Operation"
                    }
                }
                param.result = true
            } catch (err: Throwable) {
                Log.e(err)
            }
        }
    }

    object AppLockService : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            try {
                val killer = getNameForUid(Binder.getCallingUid())
                if (killer == miuiHome) {
                    val processRecord = param.args[0]

                    val targetPackages = processRecord
                        .getObject("pkgList")
                        .getObject("mPkgList")
                        .invokeMethodAs<Set<String>>("keySet") ?: return

                    targetPackages.forEach {
                        if (whiteList.contains(it)) {
                            param.args[2] = 101  // set to "trimMemory"
                            Log.i("[prevented] killing: ${processRecord.getObject("processName")}")
                            return
                        }
                    }
                }
            } catch (err: Throwable) {
                Log.e(err)
            }
        }
    }

    private fun updateWhiteList() {
        synchronized (lock) {
            val config = File("$dataDir/whitelist.txt")
            if (!config.exists()) {
                config.createNewFile()
            }
            whiteList.clear()
            config.readLines().forEach {
                val pkg = it.trim()
                if (pkg != "") {
                    whiteList.add(pkg)
                }
            }
        }
    }

    private fun updateWhiteList(packages: Array<String>): String {
        synchronized (lock) {
            whiteList.clear()
            whiteList.addAll(packages)

            val config = File("$dataDir/whitelist.txt")
            config.writeText(packages.joinToString("\n"))
        }
        return "success"
    }

    fun main() {
        updateWhiteList()

        File(dataDir).mkdir()
        findMethod("miui.process.ProcessManagerNative") {
            name == "onTransact"
        }.hookMethod(CommunicationService)

        findMethod("com.android.server.am.ProcessManagerService") {
            name == "killOnce" && parameterCount == 4
        }.hookMethod(AppLockService)
    }
}
