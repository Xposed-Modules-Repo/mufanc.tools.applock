package mufanc.tools.applock.app

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable

object AppInfoHelper {
    data class AppInfo(
        val appName: String,
        val packageName: String,
        val icon: Drawable
    )

    private lateinit var appInfoList: MutableList<AppInfo>

    fun getAppInfoList(context: Context, reload: Boolean): MutableList<AppInfo> {
        if (AppInfoHelper::appInfoList.isInitialized && !reload) return appInfoList
        appInfoList = mutableListOf()
        val packageManager = context.packageManager
        packageManager.getInstalledApplications(0).forEach { info ->
            if (info.flags and ApplicationInfo.FLAG_SYSTEM != 0) return@forEach
            AppInfo(
                info.loadLabel(packageManager).toString(),
                info.packageName,
                info.loadIcon(packageManager),
            ).let { appInfoList.add(it) }
        }
        return appInfoList
    }
}