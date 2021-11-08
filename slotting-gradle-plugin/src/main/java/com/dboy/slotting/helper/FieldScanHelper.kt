package com.dboy.slotting.helper

import com.dboy.slotting.data.ClassFieldBean
import com.dboy.slotting.data.FieldInfo

/**
 * - 文件描述: class的变量信息保存
 * @author DBoy
 * @since 2021/11/5 15:49
 */
object FieldScanHelper {
    /**
     *  保存类和类的方法 字段信息
     */
    private val mapData = mutableMapOf<String, ClassFieldBean>()

    /**
     * 添加一个类的全局变量和局部变量
     * - 使用同步锁，防止多线程操作异常
     */
    @Synchronized
    fun addClassFieldInfo(className: String, classFieldBean: ClassFieldBean) {
        mapData[className] = classFieldBean
    }

    /**
     * 获取[classPath]下需要用到的全局变量和局部变量
     */
    fun getClassFieldInfo(classPath: String): ClassFieldBean? {
        return mapData[classPath]
    }

    /**
     * 获取[classPath]下需要的全局变量
     */
    fun getClassGlobalField(classPath: String): Map<String, FieldInfo?>? {
        return getClassFieldInfo(classPath)?.globalField
    }

    /**
     * 获取[classPath]下[methodName]所需要的局部变量信息
     */
    fun getClassMethodLocalField(
        classPath: String,
        methodName: String
    ): Map<String, FieldInfo?>? {
        return getClassFieldInfo(classPath)?.localField?.get(methodName)
    }

    @Synchronized
    fun clean(){
        mapData.clear()
    }

}