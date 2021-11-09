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

    /**
     * 保存return的位置和最后一行正常结束的位置。因为可能存在这样的代码
     * ```
     * fun simple(isCheck:Boolean){
     *      if (isCheck){
     *          val msg = true
     *          return
     *      }else{
     *          val msg2 = 100
     *          return
     *      }
     * }
     *
     * ```
     * 如果需要插入代码时使用 msg 的信息，和msg2的信息。
     * 当代码插入msg下一行的时候，是访问不到 msg2的，
     * 同理插入msg2下一行的时候，无法访问msg
     * 所以这里要记录每一个return的字节码行号位置，和记录局部变量的作用范围。
     * 在字节码编译之后，msg的作用范围可能是3-10，msg2的作用范围可能是13-18
     * 触发return的字节码行号第一个能检测到的就是msg的return。以此类推。
     */
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
}