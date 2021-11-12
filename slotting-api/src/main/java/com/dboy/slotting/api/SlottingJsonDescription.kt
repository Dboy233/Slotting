package com.dboy.slotting.api

/**
 * - 文件描述: slotting.json 配置描述
 * ```json
 *  [
 *      {
 *          "classPath":"com.simple.XXX"
 *          "entryPoints":[
 *              {
 *                  "methodName":"funXXX",
 *                  "isFirstLine":true,
 *                  "event":"xxx,yyy,${this.global},${local}"
 *                  "eventMap":{
 *                      "eventXXX":"msg",
 *                      "eventYYY":"${this.globalXXX}",
 *                      "eventDDD":"${localXXX}"
 *                  }
 *              }
 *          ]
 *      },
 *      {
 *           "classPath":"com.simple.DDD"
 *      }
 *  ]
 * ```
 * - classPath： 需要埋点的Class。
 * - entryPoints: 这个Class下所有需要埋点的方法信息。
 * - methodName: 需要埋点的具体方法。
 * - isFirstLine: 这个埋点是否需要插在方法的第一行。true=在方法第一行插入，false=在方法所有return的位置和方法结束的位置插入。默认是false
 * - event : 埋点事件，如果是多个事件使用`[,]`英文逗号分割。
 * - eventMap :具有key->value映射关系的埋点类型。
 *
 * - 全局变量使用${this.xxx}来进行标记。
 * - 方法的局部变量使用${xxx}来标记。
 *
 * @author DBoy
 * @since 2021/11/12 16:01
 */
class SlottingJsonDescription