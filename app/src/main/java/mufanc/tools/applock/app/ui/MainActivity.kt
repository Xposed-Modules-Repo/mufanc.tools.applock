package mufanc.tools.applock.app.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import mufanc.tools.applock.R
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
            var whiteList = mutableSetOf<String>()

            with (binding) {
                applist.layoutManager = LinearLayoutManager(
                    this@MainActivity,
                    LinearLayoutManager.VERTICAL,
                    false
                )

                DividerItemDecoration(
                    this@MainActivity, DividerItemDecoration.VERTICAL
                ).let { applist.addItemDecoration(it) }

                fun listener(reload: Boolean = true) {
                    AppSelectAdapter(
                        AppInfoHelper.getAppInfoList(this@MainActivity, reload),
                        CommandHelper.command("readWhiteList", "").let {
                            it?.split("\n")?.toMutableSet() ?: mutableSetOf()
                        }.also { whiteList = it }
                    ).also {
                        runOnUiThread {
                            applist.adapter = it
                            it.notifyDataSetChanged()
                        }
                    }
                    thread {
                        Thread.sleep(700)
                        runOnUiThread { refresh.isRefreshing = false }
                    }
                }
                refresh.setOnRefreshListener { listener() }
                refresh.post {
                    refresh.isRefreshing = true
                    thread { listener() }
                }

                applist.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        if (dy > 0) fab.hide() else fab.show()
                    }
                })

                fab.setOnClickListener { view ->
                    if(CommandHelper.command(
                        "updateWhiteList",
                        whiteList.joinToString("\n")
                    ) == "OK") {
                        Snackbar.make(view, R.string.save_complete, Snackbar.LENGTH_SHORT).show()
                        listener(false)
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
