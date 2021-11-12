package com.dboy.slotting.example

import android.content.Intent
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatEditText
import com.dboy.slotting.R

/**
 * - 文件描述:
 * @author DBoy
 * @since 2021/11/3 16:13
 */
class MainActivity : AppCompatActivity() {

    private val businessName = "Login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val checkBox = findViewById<AppCompatCheckBox>(R.id.agree_cb)
        checkBox.setOnCheckedChangeListener(this::onCheckChange)
    }

    private fun onCheckChange(btn: CompoundButton, isChecked: Boolean) {
        val agree = if (isChecked) "agree" else "deny"
        showToast(agree)
        //这里会插入
    }

    fun onCancel(view: android.view.View) {
        showToast("cancel login")
        //这里会插入
    }

    /**
     * 当埋点需要在方法最后一行插入的时候，所有return的位置都有可能是方法结束时的最后一行。
     * 所以所有return位置都会被插入同样的埋点信息。
     * 如果你携带了局部变量。当局部变量不在可索引范围内的时候，埋点事件框架不会将无法索引的局部变量添加到事件中。
     * 例如：
     *  ```kotlin
     *      // 埋点：需要将[a]和[b]的值都发送
     *      fun check(isTrue:Boolean){
     *          var a = ""
     *          //...
     *          if(isTrue){
     *          //...在这里只能访问到变量a，变量b无法访问，所以b会直接被忽略
     *              return
     *          }else if(a.isEmpty()){
     *          //...在这里只能访问到变量a，变量b无法访问，所以b会直接被忽略
     *              return
     *          }
     *
     *          var b =10
     *          //....在这里，a和b的值都可以被所引导，所以发送的事件中会携带a和b
     *
     *      }
     *  ```
     *  如果你不喜欢这样的判断结构。可以将最终要执行的代码抽离成一个独立的方法。
     *  例如：
     *  ```kotlin
     *      fun check(){
     *          var a = ...
     *          if(xxx){
     *              return
     *          }
     *
     *          if(YYY){
     *              return
     *          }
     *
     *          var b = ...
     *
     *          commit(a,b)
     *      }
     *
     *      fun commit(a:Any,b:Any){
     *
     *          //...在这里，事件将会完整的发送，a和b都可被索引到
     *      }
     *  ```
     */
    fun onLogin(view: android.view.View) {
        val name = findViewById<AppCompatEditText>(R.id.user_name_edt).text?.toString()
        if (name.isNullOrEmpty()) {
            loginError("user_name")
            //如果要对这个方法进行代码最后一行插入的话，这里会插入
            return
        }
        val password = findViewById<AppCompatEditText>(R.id.user_password_edt).text?.toString()
        if (password.isNullOrEmpty()) {
            loginError("password")
            //如果要对这个方法进行代码最后一行插入的话，这里会插入
            return
        }
        loginSuccess(name, password)
        //如果要对这个方法进行代码最后一行插入的话，这里会插入
    }

    /**
     * 登录错误
     */
    private fun loginError(errorName: String) {
        showToast("$errorName = null")
        //这里会插入
    }

    /**
     * 登录成功，进入下个信息页面
     */
    private fun loginSuccess(name: String, password: String) {
        val bundle = Bundle()
        bundle.putString("user_name", name)
        bundle.putString("user_password", password)
        val intent = Intent(this, InfoActivity::class.java)
        intent.putExtras(bundle)
        startActivity(intent)
        finish()
        //这个时候将用户成功登录的事件进行发送
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

}