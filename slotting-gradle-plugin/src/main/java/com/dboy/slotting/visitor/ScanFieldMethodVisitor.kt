package com.dboy.slotting.visitor

import com.dboy.slotting.data.FieldInfo
import com.dboy.slotting.utils.SlottingLog
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter
import java.util.*

/**
 * - 文件描述: 扫描方法的局部变量信息
 * @author DBoy
 * @since 2021/11/5 16:26
 */
class ScanFieldMethodVisitor(
    api: Int,
    methodVisitor: MethodVisitor?,
    access: Int,
    name: String?,
    descriptor: String?,
    /**
     * 扫描当前方法所需的所有变量，并补充映射表信息
     */
    private val localFieldMap: MutableMap<String, FieldInfo?>,
    /**
     * 方法结束位置行号标记
     */
    private val methodReturnLinked: LinkedList<Int>
) :
    AdviceAdapter(api, methodVisitor, access, name, descriptor) {


    override fun onMethodExit(opcode: Int) {
        super.onMethodExit(opcode)
        if (opcode in IRETURN..RETURN) {
            val exitLine = mark().offset
            SlottingLog.info(" $name onMethodExitLine : $exitLine")
            methodReturnLinked.add(exitLine)
        }
    }

    override fun visitLocalVariable(
        name: String?,
        descriptor: String?,
        signature: String?,
        start: Label?,
        end: Label?,
        index: Int
    ) {
        super.visitLocalVariable(name, descriptor, signature, start, end, index)
        if (localFieldMap.containsKey(name)) {
            val startLine = start?.offset ?: -1
            val endLine = end?.offset ?: -1
            localFieldMap[name!!] = FieldInfo(
                name,
                descriptor!!,
                false,
                index,
                startLine,
                endLine
            )
        }

    }

    override fun visitEnd() {
        super.visitEnd()
    }
}