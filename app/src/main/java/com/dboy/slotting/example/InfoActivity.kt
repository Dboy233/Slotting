package com.dboy.slotting.example

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dboy.slotting.R

/**
 * - 文件描述:
 * @author DBoy
 * @since 2021/11/3 16:03
 */
class InfoActivity : AppCompatActivity(R.layout.info_activity) {

    private lateinit var userNameTv: TextView
    private lateinit var userPasswordTv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userNameTv = findViewById<TextView>(R.id.user_name_tv)
        userPasswordTv = findViewById<TextView>(R.id.user_password_tv)
        intent.extras?.let {
            showInfo(it)
        }
    }

   private fun showInfo(bundle: Bundle) {
        val name = bundle.getString("user_name", "Null")
        val password = bundle.getString("user_password", "Null")
        userNameTv.text = "Name: $name"
        userPasswordTv.text = "Password: $password"
    }

    fun closePager(view: android.view.View) {
        onBackPressed()
    }

}