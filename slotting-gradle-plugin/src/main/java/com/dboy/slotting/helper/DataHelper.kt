package com.dboy.slotting.helper

import com.dboy.slotting.data.EntryPointClassBean
import com.dboy.slotting.utils.SlottingLog
import com.google.gson.Gson
import com.google.gson.JsonParser
import java.io.File

/**
 * - 文件描述:
 * @author DBoy
 * @since 2021/11/5 15:41
 */
object DataHelper {

    private var entryPointClassList: MutableList<EntryPointClassBean> = mutableListOf()

    private var slottingMap = mutableMapOf<String, Int>()

    private val gson = Gson()

    /**
     * 解析slottring.json文件
     */
    fun parsing(file: File) {
        val lists = mutableListOf<EntryPointClassBean>()
        //将文件读取文本，再转成JsonArray
        val readText = file.readText()
        if (readText.isEmpty()) {
            return
        }
        val jsonArray = JsonParser.parseString(readText).asJsonArray
        SlottingLog.info(jsonArray)
        //对每一个JsonBean格式化，添加到列表
        for (jsonElement in jsonArray) {
            val slottingBean = gson.fromJson(jsonElement, EntryPointClassBean::class.java)
            lists.add(slottingBean)
        }
        saveData(lists)
    }

    /**
     * 保存数据
     */
    private fun saveData(data: List<EntryPointClassBean>) {
        data.forEachIndexed { index, slottingBean ->
            slottingMap[slottingBean.classPath] = index
            entryPointClassList.add(slottingBean)
        }
    }

    fun getData(): List<EntryPointClassBean> {
        return entryPointClassList
    }

    /**
     * 查询对应class信息
     * - [key] 就是class的名字 : "com.dboy.slotting.helper.DataHelper"
     */
    fun query(key: String): EntryPointClassBean? {
        if (slottingMap.containsKey(key)) {
            return entryPointClassList.get(slottingMap[key]!!)
        }
        return null
    }

    fun clean(){
        slottingMap.clear()
        entryPointClassList.clear()
    }
}