package com.dboy.slotting.data

/**
 * - 文件描述:
 * @author DBoy
 * @since 2021/10/20 8:29 上午
 */
data class EntryPointClassBean(
    val id: Long = -1,
    val classPath: String = "",
    val entryPoints: MutableList<EntryPointMethodBean> = mutableListOf()
){
    override fun toString(): String {
        return """{ "id":$id,
            "classPath":$classPath,
            "entryPoints":$entryPoints
        """.trimMargin()
    }
}
