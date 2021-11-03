package com.dboy.slotting.msg

import android.util.Log
import java.lang.StringBuilder
import kotlin.math.log

/**
 * - 文件描述: 默认实现，只是打印一下发送的内容，具体打印实现，自行配置实现类。
 * @author DBoy
 * @since 2021/10/20 4:59 下午
 */
object SlottingDef : Slotting {

    private val TAG = "Slotting"

    override fun send(vararg msg: Any?) {
        val srt = StringBuilder()
        msg.forEach {
            if (it == null) {
                srt.append("[").append("null").append("] ")
            } else {
                srt.append("[").append(it.toString()).append("] ")
            }
        }
        Log.d(TAG, "SlottingDef.send: ${srt.toString()}")
    }

    override fun send(map: Map<String, Any?>) {
        Log.d(TAG, "SlottingDef.send: $map")
    }

}