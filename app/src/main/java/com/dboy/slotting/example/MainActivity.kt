package com.dboy.slotting.example

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatEditText
import com.dboy.slotting.R

/**
 * - 文件描述:
 * @author DBoy
 * @since 2021/11/3 16:13
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val checkBox = findViewById<AppCompatCheckBox>(R.id.agree_cb)
        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            val agree = if (isChecked) "agree" else "deny"
            showToast(agree)
        }
    }

    fun onCancel(view: android.view.View) {
        showToast("cancel login")
    }

    fun onLogin(view: android.view.View) {
        val bundle = Bundle()
        val name = findViewById<AppCompatEditText>(R.id.user_name_edt).text?.toString()
        if (name.isNullOrEmpty()) {
            showToast("name = null")
            return
        }
        val password = findViewById<AppCompatEditText>(R.id.user_password_edt).text?.toString()
        if (password.isNullOrEmpty()) {
            showToast("password = null")
            return
        }
        bundle.putString("user_name", name)
        bundle.putString("user_password", password)
        val intent = Intent(this, InfoActivity::class.java)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    }
}