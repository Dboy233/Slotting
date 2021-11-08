package com.dboy.slotting.data

/**
 * - 文件描述:
 * @author DBoy
 * @since 2021/10/20 8:31 上午
 */
data class EntryPointMethodBean(
    var id: Long = -1,
    val methodName: String = "",
    val event: String = "",
    var eventMap: Map<String, Any>? = null,
    var isFirstLine: Boolean = false
) {
    override fun toString(): String {
        return """{
            "id":$id,
            "methodName":$methodName',
            "event":'$event', 
            "eventMap":$eventMap, 
            "isFirstLine":$isFirstLine
            }"""
    }
}
