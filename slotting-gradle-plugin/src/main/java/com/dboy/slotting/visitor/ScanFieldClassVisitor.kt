package com.dboy.slotting.visitor

import com.dboy.slotting.data.ClassFieldBean
import com.dboy.slotting.data.EntryPointClassBean
import com.dboy.slotting.data.FieldInfo
import com.dboy.slotting.data.EntryPointMethodBean
import com.dboy.slotting.helper.FieldScanHelper
import com.dboy.slotting.utils.SlottingUtils
import com.dboy.slotting.utils.SlottingUtils.EventSplitType.*
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.util.*

/**
 * - 文件描述: 扫描字段ClassVisitor
 * @author DBoy
 * @since 2021/11/5 16:03
 */
class ScanFieldClassVisitor(
    api: Int,
    classVisitor: ClassVisitor?,
    private val data: EntryPointClassBean
) :
    ClassVisitor(api, classVisitor) {


    /**
     * 保存当前页面需要使用的字段信息
     * {字段名字->字段的信息}
     */
    private val globalFieldMap = mutableMapOf<String, FieldInfo?>()

    /**
     * 局部变量映射关系表
     * {方法名字->局部字段名字:字段的信息}
     */
    private val localFieldMap = mutableMapOf<String, MutableMap<String, FieldInfo?>>()

    /**
     * 方法结束行号标记
     */
    private val methodReturnLine = mutableMapOf<String, LinkedList<Int>>()

    /**
     * class自己名字.例如 com/dboy/MainActivity 这里就不是 [.] 了是 [/]
     */
    private var classOwner: String = ""

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        classOwner = name
        data.entryPoints.forEachIndexed { _, entryPointMethodBean ->
            //检查事件中所需要的全局变量，只保存变量name
            if (entryPointMethodBean.event.isNotEmpty()) {
                //拆分event事件中的变量信息
                SlottingUtils.splitEvent(entryPointMethodBean.event) { _, value, type ->
                    when (type) {
                        //保存全局变量名字，等会visitField的时候对这个变量赋值信息
                        GLOBAL -> globalFieldMap[value] = null
                        LOCAL -> registerLocalFieldName(entryPointMethodBean, value)
                        COMMON -> {

                        }
                    }
                }
            } else if (!entryPointMethodBean.eventMap.isNullOrEmpty()) {
                //提取eventMap中的变量信息
                SlottingUtils.extractFideForMap(entryPointMethodBean.eventMap!!) { _, value, type ->
                    when (type) {
                        GLOBAL -> globalFieldMap[value] = null
                        LOCAL -> registerLocalFieldName(entryPointMethodBean, value)
                        COMMON -> {

                        }
                    }
                }
            }
        }
    }

    /**
     * 注册一个局部变量名，
     * 这个注册的数据交给visitMethod的实现类进行扫描处理
     */
    private fun registerLocalFieldName(
        entryPointMethodBean: EntryPointMethodBean,
        value: String
    ) {
        if (localFieldMap[entryPointMethodBean.methodName] == null) {
            //对于检测到的新的方法增加一个新的映射表
            val mutableMapOf = mutableMapOf<String, FieldInfo?>()
            //当前局部field只保存field name
            mutableMapOf[value] = null
            //添加到整体局部表中
            localFieldMap[entryPointMethodBean.methodName] = mutableMapOf
        } else {
            val localMethod = localFieldMap[entryPointMethodBean.methodName]!!
            //当前局部变量只保存field name
            localMethod[value] = null
        }
    }

    override fun visitField(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        value: Any?
    ): FieldVisitor {
        if (globalFieldMap.containsKey(name)) {
            globalFieldMap[name!!] = FieldInfo(name, descriptor!!)
        }
        return super.visitField(access, name, descriptor, signature, value)
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        //访问方法扫描局部变量信息
        val visitMethod = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (localFieldMap.containsKey(name)) {
            //获取当前方法局部变量表
            val currentMethodFieldMap: MutableMap<String, FieldInfo?> = localFieldMap[name]!!
            //为当前方法创建一个return结束标记链表。
            val linkedList = LinkedList<Int>()
            methodReturnLine[name!!] = linkedList
            return ScanFieldMethodVisitor(
                Opcodes.ASM9,
                visitMethod,
                access,
                name,
                descriptor,
                currentMethodFieldMap,
                linkedList
            )
        }
        return visitMethod
    }

    override fun visitEnd() {
        super.visitEnd()
        val classGlobalFieldMap = ClassFieldBean(globalFieldMap, localFieldMap, methodReturnLine)
        FieldScanHelper.addClassFieldInfo(data.classPath, classGlobalFieldMap)
    }

}