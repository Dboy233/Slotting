package com.dboy.slotting.data

import java.util.*

/**
 * - 文件描述: 一个文件中的全局变量和方法的局部变量映射表
 * @author DBoy
 * @since 2021/11/5 16:31
 */
data class ClassFieldBean(
    /**
     * 此Class中的全局变量信息
     * - 只读map,防止多线程put操作
     */
    val globalField: Map<String, FieldInfo?>,
    /**
     * 此class中，需要用到的Method的局部变量信息
     * - 只读的map,防止多线程put操作
     */
    val localField: Map<String, Map<String, FieldInfo?>>,
    /**
     * 方法结束位置。插桩之前的return位置，一个方法可能有多个return的位置，此行号是字节码行号。
     */
    val methodReturnLine: Map<String, LinkedList<Int>>
)