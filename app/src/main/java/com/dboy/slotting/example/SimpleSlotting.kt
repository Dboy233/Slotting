package com.dboy.slotting.example

import android.util.Log
import com.dboy.slotting.api.Slotting

/**
 * - 文件描述: 消息接受实现类。如果不实现这个class的话。消息会通知到[com.dboy.slotting.api.SlottingDef]
 * @author DBoy
 * @since 2021/11/6 11:21
 */
object SimpleSlotting : Slotting {
    private val TAG = "SimpleSlotting"
    override fun send(vararg msg: Any?) {
        val srt = StringBuilder()
        msg.forEach {
            if (it == null) {
                srt.append("null")
            } else {
                srt.append(it.toString())
            }
        }
        Log.d(TAG, "SlottingDef.send: ${srt.toString()}")
    }

    override fun send(map: Map<String, Any?>) {
        Log.d(TAG, "SlottingDef.send: $map")
    }
}