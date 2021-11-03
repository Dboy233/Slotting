package com.dboy.slotting.msg

/**
 * - 文件描述:
 * @author DBoy
 * @since 2021/11/1 15:17
 */
interface Slotting {
    /**
     * 发送一个数组消息
     * - send("msg") ; send("abc",19,this.name)
     */
    fun send(vararg msg: Any?)

    /**
     * 发送一个Map消息
     * - val map = mutableMap<String,Any?>()
     * - map .put(key1,"value1")
     * - map.put(key2 ,this.value2 )
     * - send(map)
     */
    fun send(map: Map<String, Any?>)
}