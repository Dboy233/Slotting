package com.dboy.slotting.utils

/**
 * - 文件描述:
 * @author DBoy
 * @since 2021/11/5 15:54
 */
object StringUtils {

    fun path2Point(path: String): String {
        return path.replace('/', '.')
    }

    fun point2Path(path: String): String {
        return path.replace('.', '/')
    }

    fun getClassName(fullQualifiedClassName: String?): String? {
        if (fullQualifiedClassName != null) {
            val indexOf = fullQualifiedClassName.indexOf(".class")
            if (indexOf != -1) {
                return fullQualifiedClassName.substring(0, indexOf)
            }
        }
        return fullQualifiedClassName
    }
}