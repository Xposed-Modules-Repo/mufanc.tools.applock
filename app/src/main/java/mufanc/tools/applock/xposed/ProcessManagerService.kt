package mufanc.tools.applock.xposed

import android.annotation.SuppressLint
import android.app.ActivityThread
import android.content.Context
import android.os.Binder
import android.util.ArrayMap
import com.github.kyuubiran.ezxhelper.utils.*
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import miui.process.ProcessConfig
import mufanc.tools.applock.BuildConfig
import mufanc.tools.applock.app.CommandHelper
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.*

@SuppressLint("StaticFieldLeak")
object ProcessManagerService {
    const val transactCode = 1801678668  // struct.unpack('I', b'Lock')

    private val systemContext: Context = ActivityThread.currentActivityThread().systemContext

    private const val dataDir = "/data/system/app_lock"
    private val killerSet = setOf("com.miui.home", "com.android.systemui")

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

                CommandHelper.onCommand(param.args[1], param.args[2]) {
                    operation, args ->
                    when (operation) {
                        "connectionTest" -> "OK"
                        "updateWhiteList" -> updateWhiteList(args)
                        "readWhiteList" -> readWhiteList()
                        "saveWhiteList" -> saveWhiteList()
                        else -> "Unknown Operation"
                    }
                }
                param.result = true
            } catch (err: Throwable) {
                Log.e(err)
                XposedBridge.log(err)
            }
        }
    }

    object AppLockService : XC_MethodHook() {
        // Compatible with different Android version
        private lateinit var outerField: Field
        private lateinit var innerField: Field
        private lateinit var keySetMethod: Method

        private fun getPackageList(record: Any): Set<String> {
            if (!this::outerField.isInitialized) {
                outerField = findField(record::class.java) {
                    name == "pkgList" || name == "mPkgList"
                }.also { it.isAccessible = true }
            }
            val outer = outerField.get(record)
            return if (outer is ArrayMap<*, *>) {
                @Suppress("Unchecked_Cast")
                outer.keys as Set<String>
            } else {
                if (!this::innerField.isInitialized) {
                    innerField = findField(outer::class.java) { name == "mPkgList" }
                    keySetMethod = findMethod(innerField.get(outer)::class.java) { name == "keySet" }
                }
                keySetMethod.invokeAs<Set<String>>(innerField.get(outer))!!
            }
        }

        override fun beforeHookedMethod(param: MethodHookParam) {
            try {
                val killer = File("/proc/${Binder.getCallingPid()}/cmdline").readText().trim { it == '\u0000' }
                if (killerSet.contains(killer)) {
                    val processRecord = param.args[0]

                    getPackageList(processRecord).forEach {
                        if (whiteList.contains(it)) {
                            param.args[2] = ProcessConfig.KILL_LEVEL_TRIM_MEMORY
                            Log.i("@Protect: ${processRecord.getObject("processName")}")
                            return
                        }
                    }
                }
            } catch (err: Throwable) {
                Log.e(err)
                XposedBridge.log(err)
            }
        }
    }

    private fun readWhiteList(): String {
        synchronized (lock) {
            return whiteList.joinToString("\n")
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

    private fun updateWhiteList(args: String): String {
        synchronized (lock) {
            val packageName = args.substring(1)
            when (args[0]) {
                '+' -> whiteList.add(packageName)
                '-' -> whiteList.remove(packageName)
                else -> return "Unknown Operation"
            }
        }
        return ""
    }

    private fun saveWhiteList(): String {
        synchronized (lock) {
            val config = File("$dataDir/whitelist.txt")
            config.writeText(whiteList.joinToString("\n"))
        }
        return ""
    }

    fun main() {
        File(dataDir).mkdir()
        updateWhiteList()

        findMethod("miui.process.ProcessManagerNative") {
            name == "onTransact"
        }.hookMethod(CommunicationService)

        findMethod("com.android.server.am.ProcessManagerService") {
            name == "killOnce" && parameterCount == 4
        }.hookMethod(AppLockService)
    }
}
