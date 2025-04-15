package com.example.lab1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lab2.R
import lab2.Lab2Activity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun onClickMainBtnRunLab01(v: View) {
        val intent = Intent(this, Lab01Activity::class.java);
        startActivity(intent);
    }

    fun onClickMainBtnRunLab02(v: View) {
        val intent = Intent(this, Lab2Activity::class.java);
        startActivity(intent);
    }

    fun onClickMainBtnRunLab06(v: View) {
        val intent = Intent(this, lab6.MainActivity::class.java)
        startActivity(intent)
    }
}