package lab3

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lab2.R
import java.util.Timer
import kotlin.concurrent.schedule

class Lab03Activity : AppCompatActivity() {
    var cols : Int = 0
    var rows : Int = 0
    lateinit var board : GridLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lab03)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        cols = intent.getIntExtra("cols", 3)
        rows = intent.getIntExtra("rows", 3)

        board = findViewById(R.id.main)
        board.columnCount = cols
        board.rowCount = rows

        val boardView = BoardView(board, cols, rows)

        boardView.setOnGameChangeListener { e ->
            run {
                when (e.state) {
                    GameState.Matching -> {
                        for (tile in e.tiles) tile.revealed = true
                    }
                    GameState.Match -> {
                        for (tile in e.tiles) tile.revealed = false
                    }
                    GameState.NoMatch -> {
                        for (tile in e.tiles) tile.revealed = true
                        Timer().schedule(1000) {
                            runOnUiThread {
                                for (tile in e.tiles) tile.revealed = false
                            }
                        }
                    }
                    GameState.Finished -> {
                        Toast.makeText(this, "Game finished", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}