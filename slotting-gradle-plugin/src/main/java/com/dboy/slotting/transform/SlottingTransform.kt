package com.dboy.slotting.transform

import com.android.build.api.transform.Context
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.dboy.slotting.extensions.SlottingExtensions
import com.dboy.slotting.helper.ASMHelper
import com.dboy.slotting.helper.DataHelper
import com.dboy.slotting.helper.FieldScanHelper
import com.dboy.slotting.utils.SlottingLog
import com.quinn.hunter.transform.HunterTransform
import org.gradle.api.Project

/**
 * - 文件描述:
 * @author DBoy
 * @since 2021/11/5 17:21
 */
class SlottingTransform(private val project: Project) : HunterTransform(project) {
    //检查配置
    val slotting: SlottingExtensions =
        project.extensions.create("slotting", SlottingExtensions::class.java)

    init {
        bytecodeWeaver = SlottingWeaver()
    }

    override fun transform(transformInvocation: TransformInvocation?) {
        checkSlottingConfig(project, slotting)
        super.transform(transformInvocation)
    }

    /**
     *  解析配置文件
     */
    private fun checkSlottingConfig(project: Project, slotting: SlottingExtensions) {
        val file = project.file(slotting.filePath + slotting.fileName)
        if (!file.exists()) {
            throw NoSuchFileException(
                file, reason = """
                文件配置不对，检查 /${slotting.filePath} 下是否有 ${slotting.fileName} 文件，如果没有请创建并配置。
            """.trimIndent()
            )
        } else {
            //数据清空,二次构建的时候内存里还保留里之前的对象重新设置数据
            FieldScanHelper.clean()
            DataHelper.clean()

            //保存实现类
            ASMHelper.implementClass = slotting.implementedClass
            SlottingLog.info("实现类：${ASMHelper.implementClass}")
            //解析数据配置文件信息并保存
            DataHelper.parsing(file)

        }
    }

}