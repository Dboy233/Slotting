package com.dboy.slotting.utils

import com.dboy.slotting.data.FieldInfo
import com.dboy.slotting.data.SlottingConstant
import com.dboy.slotting.utils.SlottingUtils.EventSplitType.*
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

/**
 * - 文件描述:
 * @author DBoy
 * @since 2021/11/5 16:19
 */
object SlottingUtils {

    enum class EventSplitType {
        /**
         * 全局变量
         */
        GLOBAL,

        /**
         * 本地变量
         */
        LOCAL,

        /**
         * 普通字符
         */
        COMMON
    }

    /**
     * 检查并分割事件
     * [splitAction] value -> 如果是全局或局部变量 value = 变量的名字，如果是普通是String类型字符，将返回文本
     */
    inline fun splitEvent(
        event: String,
        splitAction: (index: Int, value: String, type: EventSplitType) -> Unit
    ) {
        event.split(SlottingConstant.DELIMITER_EVENT).forEachIndexed { index, it ->

            val placeholderBeforeIndex = it.indexOf(SlottingConstant.PLACEHOLDER_BEFORE)
            val placeholderAfterIndex = it.indexOf(SlottingConstant.PLACEHOLDER_AFTER)

            if (placeholderBeforeIndex != -1 && placeholderAfterIndex != -1) {

                var placeFieldName = it.substring(
                    placeholderBeforeIndex + SlottingConstant.PLACEHOLDER_BEFORE.length,
                    placeholderAfterIndex
                )

                val localDistinguishingSubscript =
                    placeFieldName.indexOf(SlottingConstant.GLOBAL_DISTINCTION)

                if (localDistinguishingSubscript != -1) {
                    placeFieldName =
                        placeFieldName.substring(localDistinguishingSubscript + SlottingConstant.GLOBAL_DISTINCTION.length)
                    //Class全局参数事件
                    splitAction(index, placeFieldName, GLOBAL)
                } else {
                    //方法局部参数事件
                    splitAction(index, placeFieldName, LOCAL)
                }
            } else {
                //普通字符事件
                splitAction(index, it, COMMON)
            }
        }
    }


    /**
     *  从MapEvent中提取
     */
    inline fun extractFideForMap(
        eventMap: Map<String, Any>,
        action: (key: String, value: String, type: SlottingUtils.EventSplitType) -> Unit
    ) {
        eventMap.forEach { (key, value) ->
            val stringValue = value.toString()
            val placeholderBeforeIndex = stringValue.indexOf(SlottingConstant.PLACEHOLDER_BEFORE)
            val placeholderAfterIndex = stringValue.indexOf(SlottingConstant.PLACEHOLDER_AFTER)
            if (placeholderBeforeIndex != -1 && placeholderAfterIndex != -1) {
                var placeFieldName = stringValue.substring(
                    placeholderBeforeIndex + SlottingConstant.PLACEHOLDER_BEFORE.length,
                    placeholderAfterIndex
                )

                val localDistinguishingSubscript =
                    placeFieldName.indexOf(SlottingConstant.GLOBAL_DISTINCTION)

                if (localDistinguishingSubscript != -1) {
                    placeFieldName =
                        placeFieldName.substring(localDistinguishingSubscript + SlottingConstant.GLOBAL_DISTINCTION.length)
                    //Class全局参数事件
                    action(key, placeFieldName, GLOBAL)
                } else {
                    //方法局部参数事件
                    action(key, placeFieldName, LOCAL)
                }
            } else {
                //普通String类型的值
                action(key, stringValue, COMMON)
            }
        }
    }

    /**
     * 将所需要的event，或者field信息进行提取。
     * 移除不可使用的变量，不可/无法 访问的变量，剩下的变量可访问，但是不保证不为NULL
     * @param orgEvent 原始event = "msg1,${this.msg2},${msg}"
     * @param globalFieldsMap class的全局变量map
     * @param localMethodFieldsMap method的局部变量map
     * @param markLine 代码插入执行行号
     */
    fun handleMethodNeedEvent(
        orgEvent: String,
        globalFieldsMap: Map<String, FieldInfo?>?,
        localMethodFieldsMap: Map<String, FieldInfo?>?,
        markLine: Int = 0,
    ): List<Any> {
        val eventArray = mutableListOf<Any>()
        splitEvent(orgEvent) { _, value, type ->
            when (type) {
                GLOBAL -> {
                    if (globalFieldsMap?.containsKey(value) == true) {
                        val entryPointField = globalFieldsMap[value]
                        if (entryPointField != null) {
                            eventArray.add(entryPointField)
                        }
                    }
                }
                LOCAL -> {
                    if (localMethodFieldsMap?.containsKey(value) == true) {
                        localMethodFieldsMap[value]?.apply {
                            //这个变量必须保证是代码插入markLine的范围之内 。
                            //插入行号 10 ，Field(0,15) 可以访问此局部变量。
                            //插入行号 18 ，Field(0,15) 字节码插入时是无法访问此局部变量的。
                            if (markLine in startLine..endLine) {
                                eventArray.add(this)
                            }
                        }

                    }
                }
                COMMON -> {
                    eventArray.add(value)
                }
            }
        }
        return eventArray
    }

    /**
     * 将所需要的event，或者field信息进行提取。
     * 移除不可使用的变量，不可/无法 访问的变量，剩下的变量可访问，但是不保证不为NULL
     * @param mapEvent 从json中解析的mapEvent数据
     * @param globalFieldsMap class的全局变量map
     * @param localMethodFieldsMap method的局部变量map
     * @param markLine 代码插入执行行号
     */
    fun handleMethodNeedEvent(
        mapEvent: Map<String, Any>,
        globalFieldsMap: Map<String, FieldInfo?>?,
        localMethodFieldsMap: Map<String, FieldInfo?>?,
        markLine: Int = 0,
        action: (key: String, value: Any, type: EventSplitType) -> Unit
    ) {
        extractFideForMap(mapEvent) { key, value, type ->
            when (type) {
                GLOBAL -> {
                    if (globalFieldsMap?.containsKey(value) == true) {
                        globalFieldsMap[value]?.apply {
                            action(key, this, GLOBAL)
                        }
                    }
                }
                LOCAL -> {
                    if (localMethodFieldsMap?.containsKey(value) == true) {
                        localMethodFieldsMap[value]?.apply {
                            //这个变量必须保证是代码插入markLine的范围之内 。
                            //插入行号 10 ，Field(0,15) 可以访问此局部变量。
                            //插入行号 18 ，Field(0,15) 字节码插入时是无法访问此局部变量的。
                            if (markLine in startLine..endLine) {
                                action(key, this, LOCAL)
                            }
                        }
                    }
                }
                COMMON -> {
                    action(key, value, COMMON)
                }
            }

        }
    }


    /**
     * 检查是否是基础变量是否需要转成对象参数
     */
    fun checkNeedToObject(methodVisitor: MethodVisitor, descriptor: String) {
        when (descriptor) {
            Type.CHAR_TYPE.descriptor -> methodVisitor.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "java/lang/Character",
                "valueOf",
                "(C)Ljava/lang/Character;",
                false
            );
            Type.BOOLEAN_TYPE.descriptor -> methodVisitor.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "java/lang/Boolean",
                "valueOf",
                "(Z)Ljava/lang/Boolean;",
                false
            )
            Type.BYTE_TYPE.descriptor -> methodVisitor.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "java/lang/Byte",
                "valueOf",
                "(B)Ljava/lang/Byte;",
                false
            );
            Type.INT_TYPE.descriptor -> methodVisitor.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "java/lang/Integer",
                "valueOf",
                "(I)Ljava/lang/Integer;",
                false
            );

            Type.LONG_TYPE.descriptor -> methodVisitor.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "java/lang/Long",
                "valueOf",
                "(J)Ljava/lang/Long;",
                false
            );

            Type.FLOAT_TYPE.descriptor -> methodVisitor.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "java/lang/Float",
                "valueOf",
                "(F)Ljava/lang/Float;",
                false
            );

            Type.DOUBLE_TYPE.descriptor -> methodVisitor.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "java/lang/Double",
                "valueOf",
                "(D)Ljava/lang/Double;",
                false
            );
            else -> {

            }
        }
    }

    /**
     * 获取对应数据类型的 xLoad操作符
     */
    fun getLoadForDescriptor(descriptor: String): Int {
        return when (descriptor) {
            Type.CHAR_TYPE.descriptor,
            Type.BOOLEAN_TYPE.descriptor,
            Type.BYTE_TYPE.descriptor,
            Type.INT_TYPE.descriptor -> Opcodes.ILOAD
            Type.LONG_TYPE.descriptor -> Opcodes.LLOAD
            Type.FLOAT_TYPE.descriptor -> Opcodes.FLOAD
            Type.DOUBLE_TYPE.descriptor -> Opcodes.DLOAD
            else -> Opcodes.ALOAD
        }
    }

}