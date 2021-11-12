package com.dboy.slotting.visitor

import com.dboy.slotting.data.ClassFieldBean
import com.dboy.slotting.data.EntryPointMethodBean
import com.dboy.slotting.data.FieldInfo
import com.dboy.slotting.helper.ASMHelper
import com.dboy.slotting.utils.SlottingLog
import com.dboy.slotting.utils.SlottingUtils
import com.dboy.slotting.utils.SlottingUtils.EventSplitType.*
import com.dboy.slotting.utils.StringUtils
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter
import java.util.*

/**
 * - 文件描述: 代码插桩MethodVisitor
 * @author DBoy
 * @since 2021/11/5 17:29
 */
class SlottingMethodVisitor(
    api: Int,
    methodVisitor: MethodVisitor?,
    access: Int,
    name: String?,
    descriptor: String?,
    private val classOwner: String,
    /**
     * 事件基础数据
     */
    private val entryPointMethodBean: EntryPointMethodBean,
    /**
     * 类字段信息
     */
    classInfo: ClassFieldBean?
) : AdviceAdapter(api, methodVisitor, access, name, descriptor) {

    /**
     * class的全局变量信息
     */
    private val globalFieldsMap: Map<String, FieldInfo?>? = classInfo?.globalField

    /**
     * 当前method使用的局部变量信息
     */
    private val localMethodFieldsMap: Map<String, FieldInfo?>? =
        classInfo?.localField?.get(getName())

    /**
     * 方法原始结束行号
     */
    private val methodReturnLink: LinkedList<Int>? = classInfo?.methodReturnLine?.get(getName())

    init {
        //打印方法需要的局部变量
        SlottingLog.info("${StringUtils.getSimpleClassNameForFullName(classOwner)}.class-${getName()}() Local Variable:\n ${localMethodFieldsMap?.toString()}")
    }

    /**
     * 在方法第一行插入
     *
     * 编译之后的模样：
     *
     * ```kotlin
     *      fun method(){
     *          //在这里插入代码send
     *          val wt = Human()
     *          wt.fk()
     *      }
     * ```
     */
    override fun onMethodEnter() {
        super.onMethodEnter()
        if (entryPointMethodBean.isFirstLine) {
            if (entryPointMethodBean.event.isNotEmpty()) {
                insertArrayEvent()
            } else if (!entryPointMethodBean.eventMap.isNullOrEmpty()) {
                insertMapEvent()
            }
        }
    }

    /**
     * 在方法最后一行return位置插入；不对 throw 位置插入，只对正常return位置插入
     *
     * 编译之后的模样：
     *
     *  ```kotlin
     *      fun method(isTrue:Boolean,isThrow:Bollean):String{
     *          if(isThrow){
     *              throw RuntimeException("throwReturn")
     *          }
     *          if(isTrue){
     *              return "在这里前一行插入代码"
     *          }else{
     *              return "在这里前一行插入代码"
     *          }
     *      }
     *  ```
     *
     */
    override fun onMethodExit(opcode: Int) {
        super.onMethodExit(opcode)
        if (opcode in IRETURN..RETURN) {
            if (!entryPointMethodBean.isFirstLine) {
                val markLine = methodReturnLink?.poll() ?: 0
                if (entryPointMethodBean.event.isNotEmpty()) {
                    insertArrayEvent(markLine)
                } else if (!entryPointMethodBean.eventMap.isNullOrEmpty()) {
                    insertMapEvent(markLine)
                }
            }
        }
    }

    /**
     * 插入数组事件
     */
    private fun insertArrayEvent(markLine: Int = 0) {
        //移除不可使用的变量，不可/无法 访问的变量，剩下的变量可访问，但是不保证不为NULL
        val events = SlottingUtils.handleMethodNeedEvent(
            entryPointMethodBean.event,
            globalFieldsMap,
            localMethodFieldsMap,
            markLine
        )
        val fieldSize = events.size
        //为什么重命名呢，因为ASMified的生成的是这个明，直接cv，给给给。
        val methodVisitor = mv
        //加载Slotting 实例 INSTANCE
        ASMHelper.asmInstance(methodVisitor)
        //创建一个new Object[fieldSize]数组
        methodVisitor.visitIntInsn(BIPUSH, fieldSize)
        methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/Object")
        //放到操作栈
        methodVisitor.visitInsn(DUP)
        //将所有events进行循环插入
        events.forEachIndexed { index, value ->
            if (value is FieldInfo) {
                //全局变量
                if (value.isGlobal) {
                    SlottingLog.info("${name}->ArrayEvent add Global：${value.name}")
                    //压入一个数组索引 index = array[0]。。array[1]。。。
                    methodVisitor.visitIntInsn(BIPUSH, index)
                    //加载this参数，全局变量通过 array[0] = this.xx 调用
                    methodVisitor.visitVarInsn(ALOAD, 0)
                    //获取一个变量压入操作栈
                    methodVisitor.visitFieldInsn(
                        GETFIELD,
                        classOwner,
                        value.name,
                        value.descriptor
                    )
                    //检查是否需要转成对象形势【int -> Integer】[boolean -> Boolean]
                    SlottingUtils.checkNeedToObject(methodVisitor, value.descriptor)
                    //执行数组赋值存储操作  array[0] = this.name
                    methodVisitor.visitInsn(AASTORE)
                    if (fieldSize - 1 == index) {
                        //不用干了，下一步就是send了
                    } else {
                        //放入栈
                        methodVisitor.visitInsn(DUP)
                    }
                } else {
                    SlottingLog.info("${name}->ArrayEvent add Local：${value.name}")
                    //压入一个数组索引 index = array[0]。。array[1]。。。
                    methodVisitor.visitIntInsn(BIPUSH, index)
                    //加载局部变量 第一个是变量的类型描述符，第二个是局部变量表索引下标。
                    methodVisitor.visitVarInsn(
                        SlottingUtils.getLoadForDescriptor(value.descriptor),
                        value.index
                    )
                    //检查是否需要转成对象形势【int -> Integer】[boolean -> Boolean]
                    SlottingUtils.checkNeedToObject(methodVisitor, value.descriptor)
                    //执行数组赋值存储操作  array[1] = age
                    methodVisitor.visitInsn(AASTORE)
                    if (fieldSize - 1 == index) {
                        //不用干了，下一步就是send了
                    } else {
                        //放入栈
                        methodVisitor.visitInsn(DUP)
                    }
                }
            } else {
                SlottingLog.info("${name}->ArrayEvent add Comm：${value}")
                //压入一个数组索引 index
                methodVisitor.visitIntInsn(BIPUSH, index)
                //将一个String数据压入操作栈
                methodVisitor.visitLdcInsn(value.toString())
                //执行数组赋值存储操作  array[2] = "wtf"
                methodVisitor.visitInsn(AASTORE)
                if (fieldSize - 1 == index) {
                    //不用干了，下一步就是send了
                } else {
                    //放入栈
                    methodVisitor.visitInsn(DUP)
                }
            }
        }

        //使用Slotting send方法
        ASMHelper.asmSendArray(methodVisitor)
    }

    /**
     * 插入Map事件
     */
    private fun insertMapEvent(markLine: Int = 0) {
        val methodVisitor = mv

        //局部变量Map地址的索引
        val mapLocalIndex = nextLocal
        //创建map对象
        methodVisitor.visitTypeInsn(NEW, "java/util/HashMap")
        methodVisitor.visitInsn(DUP)
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false)
        //存入局部变量表
        methodVisitor.visitVarInsn(ASTORE, mapLocalIndex)
        //遍历map信息
        SlottingUtils.handleMethodNeedEvent(
            entryPointMethodBean.eventMap!!,
            globalFieldsMap, localMethodFieldsMap,
            markLine
        ) { key, value, type ->
            when (type) {
                GLOBAL -> {
                    if (value is FieldInfo) {
                        SlottingLog.info("${name}->MapEvent add Global：$key -> ${value.name}")
                        //获取Map实例 从局部变量表中找到map实例加入操作栈
                        methodVisitor.visitVarInsn(ALOAD, mapLocalIndex)
                        //设置Key
                        methodVisitor.visitLdcInsn(key.toString())
                        //设置Value 全局变量使用[this.]调用
                        //加载this参数
                        methodVisitor.visitVarInsn(ALOAD, 0)
                        //获取一个变量压入操作栈
                        methodVisitor.visitFieldInsn(
                            GETFIELD,
                            classOwner,
                            value.name,
                            value.descriptor
                        )
                        //检查是否需要转成对象Object形势【int -> Integer】
                        SlottingUtils.checkNeedToObject(methodVisitor, value.descriptor)
                        //使用map的put方法 map.put("key",this.value)
                        methodVisitor.visitMethodInsn(
                            INVOKEINTERFACE,
                            "java/util/Map",
                            "put",
                            "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                            true
                        )
                        //出栈
                        methodVisitor.visitInsn(POP)
                    }
                }
                LOCAL -> {
                    if (value is FieldInfo) {
                        SlottingLog.info("${name}->MapEvent add Local：$key -> ${value.name}")
                        //获取Map实例
                        methodVisitor.visitVarInsn(ALOAD, mapLocalIndex)
                        //设置Key
                        methodVisitor.visitLdcInsn(key.toString())
                        //加载局部 value
                        methodVisitor.visitVarInsn(
                            SlottingUtils.getLoadForDescriptor(value.descriptor),
                            value.index
                        )
                        //检查是否需要转成对象Object形势【int -> Integer】
                        SlottingUtils.checkNeedToObject(methodVisitor, value.descriptor)
                        //使用map的put方法
                        methodVisitor.visitMethodInsn(
                            INVOKEINTERFACE,
                            "java/util/Map",
                            "put",
                            "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                            true
                        )
                        //出栈
                        methodVisitor.visitInsn(POP)
                    }
                }
                COMMON -> {
                    SlottingLog.info("${name}->MapEvent add Comm：$key -> $value")
                    //获取Map实例
                    methodVisitor.visitVarInsn(ALOAD, mapLocalIndex)
                    //设置key
                    methodVisitor.visitLdcInsn(key.toString())
                    //设置Value
                    //将一个String数据压入操作栈，不管什么类型的， int ，double ，都弄成String了。
                    // 这里在判断其类型，调用对应的操作符有点麻烦。所以全部Ldc。
                    //FIXME:DBoy->优化类型转换，将对应int，double转成Integer,Double对象
                    methodVisitor.visitLdcInsn(value.toString())
                    //使用map的put方法
                    methodVisitor.visitMethodInsn(
                        INVOKEINTERFACE,
                        "java/util/Map",
                        "put",
                        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                        true
                    )
                    //出栈
                    methodVisitor.visitInsn(POP)
                }
            }
        }
        //发送map
        ASMHelper.asmSendMap(methodVisitor, mapLocalIndex)
    }

}