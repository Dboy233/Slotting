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

    /**
     * 从全路径Class名中提取简单名字
     * {com.example.XXX.class}->{XXX}
     * {com/example/XXX.class}->{XXX}
     */
    fun getSimpleClassNameForFullName(fullClassName: String): String {
        val className = getClassName(path2Point(fullClassName))
        val lastIndex = className?.lastIndexOf(".") ?: -1
        if (lastIndex != -1) {
            return className?.let {
                it.substring(lastIndex + 1, it.length)
            } ?: fullClassName
        }
        return fullClassName
    }

    /**
     * 提取class名字，排除[.class]标识符
     */
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