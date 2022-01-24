# Slotting 无痕埋点框架

 > 最近工作比较忙，AS的插件的开发，刚起步就暂停了。本库还有很多可优化的点，暂时也没时间维护。欢迎大家PR，issues。

 > 通过文件配置埋点信息+Transform+ASM 字节码插桩，无痕埋点。掘金链接 "[手动埋点转无痕埋点，如何做到代码“零”入侵](https://juejin.cn/post/7028405590984491022)"

![GitHub Repo stars](https://img.shields.io/github/stars/Dboy233/Slotting?style=plastic) [![Hex.pm](https://img.shields.io/hexpm/l/plug.svg?style=plastic)](https://www.apache.org/licenses/LICENSE-2.0)


|    Artifact     |                           Version                            |
| :-------------: | :----------------------------------------------------------: |
|  Slotting-Api   | [![Maven Central](https://img.shields.io/maven-central/v/io.github.dboy233/slotting-api)](https://search.maven.org/artifact/io.github.dboy233/slotting-api) |
| Slotting-Plugin | [![Maven Central](https://img.shields.io/maven-central/v/io.github.dboy233/slotting-plugin)](https://search.maven.org/artifact/io.github.dboy233/slotting-plugin) |

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
**当你需要在最后一行插入代码的时候需要注意：**

当埋点需要在方法最后一行插入的时候，所有return的位置都有可能是方法结束时的最后一行。 所以所有return位置都会被插入同样的埋点信息。

如果你携带了局部变量。当局部变量不在可索引范围内的时候，埋点事件框架不会将无法索引的局部变量添加到事件中。

例如埋点：上传检查后的`a`和`b`的值
```kotlin 
      fun check(){
           var a = ""
           //...
           if(a==null){
          //...在这里只能访问到变量a，变量b无法访问 , 最后会插入Slotting.send(a)
               return
           }
           var b = ...
           if(b==null){
             //....在这里，a和b变量都可以被访问到,最后会插入Slotting.send(a,b)
             return
           }
         //...Slotting.send(a,b)
       }
   ```
   上面的做法显然有点问题,数据检查和数据的使用应该分开，这样就更有利于代码插装，和业务上的明细。
   
   不如模拟一个正经的场景：用户登录。
   
   埋点描述：用户登录失败，上传失败原因`user_login_error_xxx`(xxx是哪一步错了),成功上传`user_login_success`
   ```kotlin
       //不对这个方法插码
       fun checkUserInfo(){
           //检查用户名是否输入正确
           var name = ...
           if(name==null){
               showLoginErrorToast("userName")
               return
           }
           //检查密码格式是否输入正确
           var password = ...
           if(password == null){
               showLoginErrorToast("userPassword")
               return
           }
           //提交信息
           commit(name,password)
       }
       //对这个方法插码
       fun showLoginErrorToast(errorMsg:String){
           toast(errorMsg)
           //...json配置这个方法发送错误 event:"user_login_error_,${errorMsg}"
           //这里将会插入Slotting.send("user_login_error_",errorMsg)
           //在接收处做拼接，上传事件
       }
      //对这个方法插码
       fun commit(name:Any,paddword:Any){
            //做点什么...
            //...
           //...在json配置这个方法发送登录成功event:"user_login_success"
       }

```
向这样的，在编写代码的时候，尽量做到，方法的职责单一。

 ## 添加依赖：

project 的 build.gradle

 ```groovy

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        //添加插件
        classpath 'io.github.dboy233:slotting-plugin:${last-version}'
    }
}

 ```

 可能你的是在setting.gradle中进行的设置
 ```groovy
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
 ```

 以前版本还是在allprojects

 ```groovy
allprojects {
    repositories {
        mavenCentral()
    }
}
 ```

app 模块下的build.gradle
```groovy
plugins {
    id 'com.dboy.slotting'
}

dependencies {
    //引入Api
    implementation 'io.github.dboy233:slotting-api:${last-version}'
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
