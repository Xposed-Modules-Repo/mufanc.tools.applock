package mufanc.tools.applock.app

import android.app.Application
import android.os.IBinder
import android.os.ServiceManager
import org.lsposed.hiddenapibypass.HiddenApiBypass

class MyApplication : Application() {
    companion object {
        val isModuleActivated: Boolean
            get() = false

        val processManager: IBinder? = ServiceManager.getService("ProcessManager")
    }

    override fun onCreate() {
        super.onCreate()
        HiddenApiBypass.addHiddenApiExemptions("")
    }
}
