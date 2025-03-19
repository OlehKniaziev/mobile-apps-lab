package lab2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lab2.R
import lab3.Lab03Activity

class Lab2Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lab2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.favorites_grid)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun onClick(v: View) {
        val (cols, rows) = when (v.id) {
            R.id.main_6_6_board -> Pair(6, 6)
            R.id.main_4_4_board -> Pair(4, 4)
            R.id.main_4_3_board -> Pair(4, 3)
            R.id.main_3_2_board -> Pair(3, 2)
            else -> throw Exception("Unknown board size selected")
        }

        val intent = Intent(this, Lab03Activity::class.java)
        intent.putExtra("cols", cols)
        intent.putExtra("rows", rows)
        startActivity(intent)
    }
}