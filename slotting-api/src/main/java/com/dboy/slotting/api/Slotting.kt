package com.dboy.slotting.api

/**
 *
 *  Copyright 2021 Dboy233
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * - 文件描述: 消息接受接口。在自己的项目中实现此接口。
 * - app/build.gradle：
 * ```groovy
 *      slotting{
 *          implementedClass "com.example.SimpleSlotting"
 *      }
 * ```
 * @author DBoy
 * @since 2021/11/1 15:17
 */
interface Slotting {
    /**
     * 发送一个数组消息
     * - send("msg") ; send("abc",19,this.name)
     */
    fun send(vararg msg: Any?)

    /**
     * 发送一个Map消息
     * - val map = mutableMap<String,Any?>()
     * - map .put(key1,"value1")
     * - map.put(key2 ,this.value2 )
     * - send(map)
     */
    fun send(map: Map<String, Any?>)
}