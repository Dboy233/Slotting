package com.dboy.slotting.example

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

    fun onLogin(view: android.view.View) {
        val name = findViewById<AppCompatEditText>(R.id.user_name_edt).text?.toString()
        if (name.isNullOrEmpty()) {
            showToast("name = null")
            //这里会插入
            return
        }
        val password = findViewById<AppCompatEditText>(R.id.user_password_edt).text?.toString()
        if (password.isNullOrEmpty()) {
            showToast("password = null")
            //这里会插入
            return
        }
        val bundle = Bundle()
        bundle.putString("user_name", name)
        bundle.putString("user_password", password)
        val intent = Intent(this, InfoActivity::class.java)
        intent.putExtras(bundle)
        startActivity(intent)
        finish()
        //这里会插入
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    /**
     * 例如： 如果给这个父类方法插入代码，需要重写这个方法。
     */
    override fun onDestroy() {
        super.onDestroy()
        //这里会插入
    }

}