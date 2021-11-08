package com.dboy.slotting

import com.android.build.gradle.AppExtension
import com.dboy.slotting.extensions.SlottingExtensions
import com.dboy.slotting.helper.ASMHelper
import com.dboy.slotting.helper.DataHelper
import com.dboy.slotting.helper.FieldScanHelper
import com.dboy.slotting.transform.SlottingTransform
import com.dboy.slotting.utils.SlottingLog
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * - 文件描述:
 * @author DBoy
 * @since 2021/11/4 08:31
 */
class SlottingPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        if (target.plugins.hasPlugin("com.android.application")) {
            SlottingLog.info("开始注册插件")


            //注册
            val appExtension = target.extensions.findByType(AppExtension::class.java)
            appExtension?.registerTransform(SlottingTransform(target))
        }
    }


}