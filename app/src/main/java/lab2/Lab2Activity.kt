package lab2

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lab2.R

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
        when (v.id) {
            R.id.main_6_6_board -> Toast.makeText(this, "6x6", Toast.LENGTH_LONG).show()
            R.id.main_4_4_board -> Toast.makeText(this, "4x4", Toast.LENGTH_LONG).show()
            R.id.main_4_3_board -> Toast.makeText(this, "4x3", Toast.LENGTH_LONG).show()
            R.id.main_3_2_board -> Toast.makeText(this, "3x2", Toast.LENGTH_LONG).show()
            else -> Toast.makeText(this, "Unknown grid size", Toast.LENGTH_LONG).show()
        }
    }
}