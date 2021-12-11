package mufanc.tools.applock.app.ui

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import mufanc.tools.applock.R
import mufanc.tools.applock.app.AppInfoHelper
import mufanc.tools.applock.app.CommandHelper
import mufanc.tools.applock.app.MyApplication
import mufanc.tools.applock.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var isAvailable: Boolean = false
    private lateinit var whiteList: MutableSet<String>

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

                applist.adapter = AppSelectAdapter(
                    AppInfoHelper.getAppInfoList(this@MainActivity),
                    CommandHelper.command("getWhiteList", emptyArray()).let {
                        it?.split("\n")?.toMutableSet() ?: mutableSetOf()
                    }.also { whiteList = it }
                )

                DividerItemDecoration(
                    this@MainActivity, DividerItemDecoration.VERTICAL
                ).let { applist.addItemDecoration(it) }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (isAvailable) {
            CommandHelper.command("setWhiteList", whiteList.toTypedArray())
        }
    }

    private fun checkEnvironment(): Boolean {
        val activated = MyApplication.isModuleActivated
        val serviceFound = MyApplication.processManager != null
        val connectionTest = CommandHelper.command("connectionTest", emptyArray()) == "OK"
        if (activated && serviceFound && connectionTest) {
            return true
        }
        ErrorDialog.get(this, activated, serviceFound, connectionTest).show()
        return false
    }
}
