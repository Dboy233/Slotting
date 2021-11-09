package com.dboy.slotting.data

/**
 * - 文件描述: class中的全局变量信息和局部变量信息
 * @author DBoy
 * @since 2021/10/28 4:16 下午
 */
data class FieldInfo(
    /**
     * 字段名字
     */
    val name: String,
    /**
     * 变量描述符
     */
    val descriptor: String,
    /**
     * 是否是全局变量
     */
    val isGlobal: Boolean = true,
    /**
     * 本地变量索引，全局变量不需要
     */
    val index: Int = 0,
    /**
     * 变量的可用开始范围
     */
    val startLine: Int = 0,
    /**
     * 变量的可用结束范围
     */
    val endLine: Int = 0
)
