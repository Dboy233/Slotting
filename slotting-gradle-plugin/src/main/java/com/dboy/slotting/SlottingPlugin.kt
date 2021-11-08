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
    override fun apply(target: Project) {
        if (target.plugins.hasPlugin("com.android.application")) {
            SlottingLog.info("开始注册插件")

            //注册
            val appExtension = target.extensions.findByType(AppExtension::class.java)
            appExtension?.registerTransform(SlottingTransform(target))
        }
    }


}