package com.dboy.slotting.extensions

/**
 * - 文件描述:
 * @author DBoy
 * @since 2021/10/20 8:33 上午
 */
open class SlottingExtensions @JvmOverloads constructor(
    /**
     * 埋点配置文件名字
     */
    var fileName: String = "slotting.json",
    /**
     * 配置文件所在的文件夹，默认app/
     */
    var filePath: String = "",
    /**
     * 通知接口的实现类
     */
    var implementedClass: String = "com.dboy.slotting.api.SlottingDef"

)

