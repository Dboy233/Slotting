package com.dboy.slotting.data

/**
 * - 文件描述:
 * @author DBoy
 * @since 2021/10/20 8:31 上午
 */
data class EntryPointMethodBean(
    /**
     * emmm这个暂时保留，没啥用
     */
    var id: Long = -1,
    /**
     * 方法名字
     */
    val methodName: String = "",
    /**
     * 事件信息，多个事件通过英文 [,] 逗号进行分割
     * - event:"msg,msg2,msg3"
     */
    val event: String = "",
    /**
     * 具有key->value映射关系的事件
     */
    var eventMap: Map<String, Any>? = null,
    /**
     * 是否在方法[methodName]第一行插入，false为在方法return的位置和结束的位置插入。
     */
    var isFirstLine: Boolean = false
)
