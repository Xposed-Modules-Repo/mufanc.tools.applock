package mufanc.tools.applock.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mufanc.tools.applock.R
import mufanc.tools.applock.app.AppInfoHelper.AppInfo
import mufanc.tools.applock.databinding.ApplistItemBinding
import java.text.Collator
import java.util.*

class AppSelectAdapter(
    private val appList: MutableList<AppInfo>,
    private val selectedApps: MutableSet<String>
) : RecyclerView.Adapter<AppSelectAdapter.ViewHolder>() {
    init {
        appList.sortWith { o1, o2 ->
            val c1 = selectedApps.contains(o1.packageName)
            val c2 = selectedApps.contains(o2.packageName)
            if (c1 != c2) return@sortWith if (c1) -1 else 1
            Collator.getInstance(Locale.getDefault()).compare(o1.appName, o2.appName)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var appIconHolder: ImageView
        var appNameHolder: TextView
        var packageNameHolder: TextView
        val checkboxHolder: CheckBox

        init {
            with (ApplistItemBinding.bind(view)) {
                appIconHolder = appIcon
                appNameHolder = appName
                packageNameHolder = packageName
                checkboxHolder = checkbox
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.applist_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appInfo = appList[position]
        with (holder) {
            appIconHolder.setImageDrawable(appInfo.icon)
            appNameHolder.text = appInfo.appName
            packageNameHolder.text = appInfo.packageName

            checkboxHolder.setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    selectedApps.add(appInfo.packageName)
                } else {
                    selectedApps.remove(appInfo.packageName)
                }
            }
            checkboxHolder.isChecked = selectedApps.contains(appInfo.packageName)
        }
    }

    override fun getItemCount() = appList.size
}