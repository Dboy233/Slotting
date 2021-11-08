package com.dboy.slotting.utils

object SlottingLog {
    @JvmStatic
    fun info(msg: Any) {
        try {
            println((String.format("Slotting -- {%s}", msg.toString())))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}