# Slotting 无痕埋点框架

 > 通过文件配置埋点信息+Transform+ASM 字节码插桩，无痕埋点。

## 使用方式

### 1.实现[Slotting.kt](https://github.com/Dboy233/Slotting/blob/master/slotting-api/src/main/java/com/dboy/slotting/api/Slotting.kt)接口

kotlin:

```kotlin

 object SimpleSlotting : Slotting{
    
    override fun send(vararg msg: Any?) {
    }

    override fun send(map: Map<String, Any?>) {
    }
 }
```

java

```java
public class JavaSlotting implements Slotting {
    
    public static JavaSlotting INSTANCE = new JavaSlotting();

    @Override
    public void send(@NonNull Object... objects) {
        
    }

    @Override
    public void send(@NonNull Map<String, ?> map) {

    }
}
```


Kotlin中使用`object`实现接口。

Java中实现接口之后需要再创建一个静态对象`INSTANCE`便于调用。这个调用不需要手动调用。是插桩框架生成字节码调用。

### 2.创建埋点配置文件

在app目录下创建`slotting.json`文件.

```
app/
  |-libs/
  |-src/
  |-slotting.json  <----
```

可以放在其他目录中：

```
app/
  |-libs/
  |-src/
  |-simpleDir/
            |-slotting.json <----
    
```
### 3.添加脚本配置
在app的`build.gradle`中引入插件，并修改插件配置信息。

```groovy

plugins {
    id 'com.dboy.slotting'
}

slotting{
    //配置文件名,要包含扩展名.json ， 默认名称:slotting.json
    fileName "slotting.json"
    //配置文件路径, 默认位置是app模块根目录
    //simple: filePath = "simpleDir/"
    filePath ""
    //消息接收实现类,实现接口 [com.dboy.slotting.api.Slotting]
    implementedClass "com.example.SimpleSlotting"
}

```

### 4.编写`slotting.json`文件
这个文件是json格式的。

```json
[
  {
    "classPath": "com.xxx.MainActivity",
    "entryPoints": [
      {
        "methodName": "simpleEventMethod",
        "event": "event,from,${name}"
      },
      {
        "methodName": "simpleEventMapMethod",
        "eventMap": {
          "msg": "is msg 1",
          "msg2": "${this.msg2}"
        }
      }
    ]
  },
  {
    "classPath": "com.xxx.MainActivity"
  }
]

```

此json根是一个List表。
实体对象内容为：

 - `classPath` : 指明需要埋点的类文件全量名称,排除`.class`后缀。
 - `entryPonts`: 切入点/埋点位置。这是个list列表,内部包含了当前`classPath`所有需要触发埋点的方法信息。
    - `methodName`: 需要埋点的方法名字。
    - `event`: 埋点触发事件，可以是单个字符串，也可以多个埋点事件，通过英文 `“,”` 逗号进行分割。接收此事件方法`fun send(vararg msg: Any?)`.
    - `eventMap`: 具有Key->value映射的事件。接收此事件方法`fun send(map: Map<String, Any?>)`
    - `isFirstLine` : 这个是一个`boolean`数据表明这个埋点事件是插入`method`第一行，还是`method`的`return`时的位置。默认是`false`


`event`事件和`eventMap`事件的Value值可以使用占位符来获取全局变量和局部变量。


> 使用`${...}`来进行占位标识,`${this.xxx}`表示获取全局变量xxx。`${xxx}`表示获取方法的局部变量xxx

    

例如：
```

    event :"全局变量:,${this.globalName},局部变量:,${localName}" 

    eventMap :{
        "全局变量":"${this.globalName}",
        "方法局部变量":"${localName}"
    }

```
`event`和`eventMap`两种类型的事件只取其一，如果两者都有数据,优先使用`event`数据

配置完成之后即可进行项目构建。

> 注意：修改class文件不需要clean项目，如果修改了`slotting{}`脚本配置，或者修改了`slotting.json`文件需要clean整个项目重新build or rebuild。由于`Transform`的增量更新，不会检测`slotting.json`文件，所以当配置修改后，检测字节码的时候原class没有变更是不会二次插桩修改的。


## 字节码插桩生成演示

编写自己的事件接收文件`SimpleSlotting.kt`
```kotlin

object SimpleSlotting : Slotting {

    override fun send(vararg msg: Any?) {
        //使用统计平台对msg进行处理发送
    }

    override fun send(map: Map<String, Any?>) {
        //使用统计平台对map进行处理发送
    }
}

```

原始`SimpleClass.kt`

```kotlin
class SimpleClass {
    
    fun testEvent1(){
        
    }
    
    fun testEvent2(){
        
    }

    fun testEvent3(userName: String) {
        
    }
    
}
```

`slotting.json`配置文件

```json
[
    {
        "classPath":"com.simple.SimpleClass",
        "entryPonts":[
            {
                "methodName":"testEvent1",
                "event":"testEvent1"
            },
            {
                "methodName":"testEvent2",
                "event":"testEvent2,two"
            }
            {
                "methodName":"testEvent3",
                "event":"testEvent3,${userName}"
            }
        ]
    }
]
```

字节码插桩后的`SimpleClass.kt`

```kotlin
class SimpleClass {

    fun testEvent1(){
        SimpleSlotting.send("testEvent1")
    }

    fun testEvent2(){
        SimpleSlotting.send("testEvent2","two")
    }

    fun testEvent3(userName: String) {
        SimpleSlotting.send("testEvent3",userName)
    }

}
```


 ## 添加依赖：


> 因为使用了[Hunter](https://github.com/Leaking/Hunter) 的transform封装库，所以需要添加一个`GithubPackages`的Maven仓库

project 的 build.gradle

 ```groovy

buildscript {
    repositories {
        mavenCentral()
        maven {
            name = "GithubPackages"
            url = uri("https://maven.pkg.github.com/Leaking/Hunter")
            credentials {
                username = 'Leaking'
                password = '\u0067\u0068\u0070\u005f\u0058\u006d\u0038\u006e\u0062\u0057\u0031\u0053\u0053\u0042\u006a\u004a\u0064\u006f\u0071\u0048\u0064\u006b\u0036\u0034\u0077\u0031\u0054\u0066\u0074\u0071\u0052\u0046\u0068\u0042\u0032\u0047\u0057\u0037\u0046\u0070'
            }
        }
    }
    dependencies {
        //添加插件
        classpath 'io.github.dboy233:slotting-plugin:1.0.0'
    }
}

 ```

 AGP 7.0之后新建项目在`setting.gradle`中设置
 ```groovy
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
 ```

 以前版本还是在all

 ```groovy
allprojects {
    repositories {
        mavenCentral()
    }
}
 ```

app 模块下的build.gradle
```groovy
dependencies {
    //引入Api
    implementation 'io.github.dboy233:slotting-api:1.0.0'
}
 ```

 # License
 ```
Copyright 2021 Dboy233

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

 ```
