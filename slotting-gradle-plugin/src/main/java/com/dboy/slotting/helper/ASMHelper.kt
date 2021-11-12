package com.dboy.slotting.helper

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * - 文件描述: 辅助插装
 * @author DBoy
 * @since 2021/11/5 15:42
 */
object ASMHelper {
    /**
     * 实现类
     */
    var implementClass = "com.dboy.slotting.api.SlottingDef"


    /**
     * 获取消息发送实现类
     */
    fun asmInstance(methodVisitor: MethodVisitor) {
        methodVisitor.visitFieldInsn(
            Opcodes.GETSTATIC,
            formatClassPath(implementClass),
            "INSTANCE",
            "L${formatClassPath(implementClass)};"
        );
    }

    /**
     * 插入字节码，使用实现类发送Array类型消息。array的消息和获取实例是分开的。
     */
    fun asmSendArray(methodVisitor: MethodVisitor) {
        //调用send(array)
        methodVisitor.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            formatClassPath(implementClass),
            "send",
            "([Ljava/lang/Object;)V",
            false
        );
    }

    /**
     * 插入字节码，使用实现类发送Map类型消息
     */
    fun asmSendMap(methodVisitor: MethodVisitor, mapLocalIndex: Int) {
        //获取实例
        asmInstance(methodVisitor)
        //加载map
        methodVisitor.visitVarInsn(Opcodes.ALOAD, mapLocalIndex)
        //调用send(map)
        methodVisitor.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            formatClassPath(implementClass),
            "send",
            "(Ljava/util/Map;)V",
            false
        );
    }

    /**
     * 格式化class路径名字.
     * - com.dboy.slotting.api.SlottingDef = com/dboy/slotting/api/SlottingDef
     */
    private fun formatClassPath(classPath: String): String {
        return classPath.replace('.', '/')
    }
}