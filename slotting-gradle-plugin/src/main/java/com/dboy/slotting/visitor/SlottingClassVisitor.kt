package com.dboy.slotting.visitor

import com.dboy.slotting.data.EntryPointClassBean
import com.dboy.slotting.helper.FieldScanHelper
import com.dboy.slotting.utils.SlottingLog
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.util.CheckMethodAdapter

/**
 * - 文件描述: 代码插桩处理ClassVistor
 * @author DBoy
 * @since 2021/11/5 17:23
 */
class SlottingClassVisitor(
    api: Int,
    classVisitor: ClassVisitor?,
    private val entryPointData: EntryPointClassBean
) :
    ClassVisitor(api, classVisitor) {

    /**
     * 当前class owner Name
     *
     * com/dboy/slotting/simple.class
     */
    private var classOwner: String = ""

    /**
     * 保存方法和下标映射，便于查找判断
     */
    private val methodIndexMap = mutableMapOf<String, Int>()

    private val isDebug = false

    /**
     * 当前class中所有event所需要的全局变量
     */
    private val globalFieldsMap = FieldScanHelper.getClassGlobalField(entryPointData.classPath)

    /**
     * class的字段等信息
     */
    private val classInfo = FieldScanHelper.getClassFieldInfo(entryPointData.classPath)

    init {
        SlottingLog.info("${entryPointData.classPath} 全局参数 ${globalFieldsMap?.toString()}")
    }

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        //保存class 全量名称
        classOwner = name
        //获取方法信息在列表中的位置进行name->index的映射关系保存
        //便于之后从[entryPointData.entryPoints]中获取数据
        entryPointData.entryPoints.forEachIndexed { index, entryPointMethodBean ->
            methodIndexMap[entryPointMethodBean.methodName] = index
        }
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        var visitMethod = super.visitMethod(access, name, descriptor, signature, exceptions)
        //判断是否对这个方法进行处理
        if (methodIndexMap.containsKey(name)) {
            if (isDebug && visitMethod != null) {
                //调用链中增加一层方法检查适配器，检查[SlottingMethodVisit]字节码操作是否规范
                // 其中检查出一个问题 ：ldc of a constant class requires at least version 1.5
                //让我很懵逼，调用visitLdcInsn(Type.getType("Lcom/dboy/slotting/example/InfoActivity;"))检查出来的错误
                // 实际是在执行代码 xXX(A.class)的时候产生的异常，应该是CheckMethodAdapter的问题。
                visitMethod = CheckMethodAdapter(visitMethod)
            }
            //事件信息
            val entryPointMethodBean = entryPointData.entryPoints[methodIndexMap[name]!!]
            return SlottingMethodVisitor(
                Opcodes.ASM9,
                visitMethod,
                access,
                name,
                descriptor,
                classOwner,
                entryPointMethodBean,
                classInfo
            )
        }
        return visitMethod
    }

}