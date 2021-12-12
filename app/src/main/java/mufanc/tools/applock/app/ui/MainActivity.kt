package mufanc.tools.applock.app.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import mufanc.tools.applock.app.AppInfoHelper
import mufanc.tools.applock.app.CommandHelper
import mufanc.tools.applock.app.MyApplication
import mufanc.tools.applock.databinding.ActivityMainBinding
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private var isAvailable: Boolean = false

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isAvailable = checkEnvironment()
        if (isAvailable) {
            with (binding) {
                applist.layoutManager = LinearLayoutManager(
                    this@MainActivity,
                    LinearLayoutManager.VERTICAL,
                    false
                )

                DividerItemDecoration(
                    this@MainActivity, DividerItemDecoration.VERTICAL
                ).let { applist.addItemDecoration(it) }

                val listener = fun() {
                    AppSelectAdapter(
                        AppInfoHelper.getAppInfoList(this@MainActivity),
                        CommandHelper.command("readWhiteList", "").let {
                            it?.split("\n")?.toMutableSet() ?: mutableSetOf()
                        }
                    ).also {
                        runOnUiThread { applist.adapter = it }
                    }.notifyDataSetChanged()
                    thread {
                        Thread.sleep(500)
                        refresh.isRefreshing = false
                    }
                }
                refresh.setOnRefreshListener(listener)
                refresh.post {
                    refresh.isRefreshing = true
                    thread {
                        listener()
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (isAvailable) {
            CommandHelper.command("saveWhiteList", "")
        }
    }

    private fun checkEnvironment(): Boolean {
        val activated = MyApplication.isModuleActivated
        val serviceFound = MyApplication.processManager != null
        val connectionTest = CommandHelper.command("connectionTest", "") == "OK"
        if (activated && serviceFound && connectionTest) {
            return true
        }
        ErrorDialog.get(this, activated, serviceFound, connectionTest).show()
        return false
    }
}
