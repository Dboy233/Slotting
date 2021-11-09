package com.dboy.slotting.data

/**
 * - 文件描述:
 * @author DBoy
 * @since 2021/10/20 8:29 上午
 */
data class EntryPointClassBean(
    val id: Long = -1,
    /**
     * class名字，包含包名。
     * - 例如 ：com.dboy.slotting.data.EntryPointClassBean
     */
    val classPath: String = "",
    /**
     * 代码切入点，当前[classPath]需要埋点的方法信息。
     */
    val entryPoints: MutableList<EntryPointMethodBean> = mutableListOf()
)
