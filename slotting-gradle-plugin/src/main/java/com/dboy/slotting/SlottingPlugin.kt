package com.dboy.slotting

import com.android.build.gradle.AppExtension
import com.dboy.slotting.extensions.SlottingExtensions
import com.dboy.slotting.helper.ASMHelper
import com.dboy.slotting.helper.FieldScanHelper
import com.dboy.slotting.helper.SlottingJsonDataHelper
import com.dboy.slotting.transform.SlottingTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 *
 *  Copyright 2021 Dboy233
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * - 文件描述: Slotting 插件
 * @author DBoy
 * @since 2021/11/4 08:31
 */
class SlottingPlugin : Plugin<Project> {

    companion object{
        /**
         * gradle扩展名
         */
        const val EXTENSIONS_NAME = "slotting"
    }

    override fun apply(target: Project) {
        if (target.plugins.hasPlugin("com.android.application")) {
            //创建扩展
            val slotting: SlottingExtensions =
                target.extensions.create(EXTENSIONS_NAME, SlottingExtensions::class.java)
            //检查配置
            target.afterEvaluate {
                printSlottingSetting(slotting)
                checkSlottingConfig(target, slotting)
            }
            //注册Transform
            val appExtension = target.extensions.findByType(AppExtension::class.java)
            appExtension?.registerTransform(SlottingTransform())
        }
    }

    /**
     * 打印Slotting配置
     */
    private fun printSlottingSetting(slotting: SlottingExtensions) {
        println(
            """
             =================Slotting Config=================
                fileName    :   ${slotting.fileName}
                filePath    :   /${slotting.filePath}
                impClass    :   ${slotting.implementedClass}
             =================================================
         """.trimIndent()
        )
    }

    /**
     *  解析配置文件
     */
    private fun checkSlottingConfig(project: Project, slotting: SlottingExtensions) {
        val file = project.file(slotting.filePath + slotting.fileName)
        when {
            file.exists() -> {
                //数据清空,二次构建的时候内存里还保留里之前的对象重新设置数据
                FieldScanHelper.clean()
                SlottingJsonDataHelper.clean()

                //保存实现类
                ASMHelper.implementClass = slotting.implementedClass
                //解析数据配置文件信息并保存
                SlottingJsonDataHelper.parsing(file)
            }
            else -> {
                throw NoSuchFileException(
                    file, reason = """
                    文件配置不对，检查 /${slotting.filePath} 下是否有 ${slotting.fileName} 文件，如果没有请创建并配置。
                """.trimIndent()
                )
            }
        }
    }

}