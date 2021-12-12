package mufanc.tools.applock.app

import android.os.Parcel
import mufanc.tools.applock.xposed.ProcessManagerService

object CommandHelper {
    fun command(operation: String, args: String): String? {
        val sender = Parcel.obtain()
        sender.writeString(operation)
        sender.writeString(args)
        val receiver = Parcel.obtain()
        MyApplication.processManager?.transact(
            ProcessManagerService.transactCode,
            sender, receiver, 0
        ) ?: return null
        return receiver.readString()
    }

    fun onCommand(
        data: Any, reply: Any,
        callback: (String, String) -> String
    ) {
        val operation = (data as Parcel).readString()!!
        val args = data.readString()!!
        (reply as Parcel).writeString(callback(operation, args))
    }
}