package com.dboy.slotting.transform

import com.dboy.slotting.data.EntryPointClassBean
import com.dboy.slotting.helper.DataHelper
import com.dboy.slotting.utils.SlottingLog
import com.dboy.slotting.utils.StringUtils
import com.dboy.slotting.visitor.ScanFieldClassVisitor
import com.dboy.slotting.visitor.SlottingClassVisitor
import com.quinn.hunter.transform.asm.BaseWeaver
import com.quinn.hunter.transform.asm.ExtendClassWriter
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.InputStream

/**
 * - 文件描述:
 * @author DBoy
 * @since 2021/11/5 17:21
 */
class SlottingWeaver : BaseWeaver() {

    lateinit var entryPointData: EntryPointClassBean

    override fun weaveSingleClassToByteArray(inputStream: InputStream?): ByteArray {
        SlottingLog.info("扫描Class")
        //先扫
        val classReader = ClassReader(inputStream)
        val classWriterScan: ClassWriter = ExtendClassWriter(classLoader, ClassWriter.COMPUTE_MAXS)
        val classScanVisitor = ScanFieldClassVisitor(Opcodes.ASM9, classWriterScan, entryPointData)
        classReader.accept(classScanVisitor, ClassReader.EXPAND_FRAMES)
        SlottingLog.info("插桩Class")
        //插入字节码
        val classWriter: ClassWriter = ExtendClassWriter(classLoader, ClassWriter.COMPUTE_MAXS)
        val classVisitor = SlottingClassVisitor(Opcodes.ASM9, classWriter, entryPointData)
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)

        return classWriter.toByteArray()
    }

    override fun isWeavableClass(fullQualifiedClassName: String?): Boolean {
        val className = StringUtils.getClassName(fullQualifiedClassName)
        if (className != null) {
            val query = DataHelper.query(className)
            return if (query != null) {
                entryPointData = query
                true
            } else {
                false
            }
        }
        return false
    }

}