package mufanc.tools.applock.xposed

import android.os.Process
import mufanc.tools.applock.IAppLockManager
import java.io.File

class AppLockManagerService : IAppLockManager.Stub() {

    companion object {
        private const val DATA_DIR = "/data/system/app_lock"
        private const val CONFIG_FILE = "$DATA_DIR/whitelist.txt"

        init {
            File(DATA_DIR).mkdir()
            File(CONFIG_FILE).let {
                if (!it.exists()) it.createNewFile()
            }
        }
    }

    val whitelist = File(CONFIG_FILE).readLines()
        .map { it.trim() }.filterNot { it.isEmpty() }
        .toMutableSet()

    override fun handshake(): IntArray {
        return intArrayOf(Process.myPid(), Process.myUid())
    }

    @Synchronized
    override fun writePackageList(packageList: Array<out String>) {
        whitelist.clear()
        whitelist.addAll(packageList)
        File(CONFIG_FILE).writeText(packageList.joinToString("\n"))
    }

    @Synchronized
    override fun readPackageList(): Array<String> {
        return whitelist.toTypedArray()
    }
}