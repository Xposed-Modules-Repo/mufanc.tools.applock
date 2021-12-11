package mufanc.tools.applock.app

import android.os.Parcel
import mufanc.tools.applock.xposed.ProcessManagerService

object CommandHelper {
    fun command(operation: String, args: Array<String>): String? {
        val sender = Parcel.obtain()
        sender.writeString(operation)
        sender.writeInt(args.size)
        sender.writeStringArray(args)
        val receiver = Parcel.obtain()
        MyApplication.processManager?.transact(
            ProcessManagerService.transactCode,
            sender, receiver, 0
        ) ?: return null
        return receiver.readString()
    }

    fun onCommand(
        data: Parcel, reply: Parcel,
        callback: (String, Array<String>) -> String
    ) {
        val operation = data.readString()!!
        val size = data.readInt()
        val args = arrayOfNulls<String>(size)
        data.readStringArray(args)
        @Suppress("Unchecked_Cast")
        reply.writeString(callback(operation, args as Array<String>))
    }
}