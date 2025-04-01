package lab3

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
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

class Lab03Activity : AppCompatActivity()  {
    var cols : Int = 0
    var rows : Int = 0
    var isSoundEnabled = true
    lateinit var board : GridLayout
    lateinit var boardView: BoardView
    lateinit var completionPlayer: MediaPlayer
    lateinit var negativePlayer: MediaPlayer

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

        boardView = BoardView(board, cols, rows)

        boardView.setOnGameChangeListener { e ->
            run {
                when (e.state) {
                    GameState.Matching -> {
                        for (tile in e.tiles) tile.revealed = true
                    }
                    GameState.Match -> {
                        for (tile in e.tiles) tile.revealed = true
                    }
                    GameState.NoMatch -> {
                        if (isSoundEnabled) negativePlayer.start()
                        for (tile in e.tiles) tile.revealed = true
                    }
                    GameState.Finished -> {
                        if (isSoundEnabled) completionPlayer.start()
                        for (tile in e.tiles) tile.revealed = true
                        Toast.makeText(this, "Game finished", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        if (savedInstanceState != null) {
            val state = savedInstanceState.getIntArray("state")
            boardView.setState(state!!)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray("state", boardView.getState())
    }

    override fun onResume() {
        super.onResume()
        completionPlayer = MediaPlayer.create(applicationContext, R.raw.completion)
        negativePlayer = MediaPlayer.create(applicationContext, R.raw.negative)
    }

    override fun onPause() {
        super.onPause()
        completionPlayer.reset()
        negativePlayer.reset()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.board_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.getItemId()) {
            R.id.board_activity_sound -> {
                if (item.getIcon()?.getConstantState()?.equals(getResources().getDrawable(R.drawable.baseline_volume_up_24, getTheme()).getConstantState()) ?: false) {
                    Toast.makeText(this, "Sound turn off", Toast.LENGTH_SHORT).show();
                    item.setIcon(R.drawable.baseline_volume_mute_24)
                    isSoundEnabled = false;
                } else {
                    Toast.makeText(this, "Sound turn on", Toast.LENGTH_SHORT).show()
                    item.setIcon(R.drawable.baseline_volume_up_24)
                    isSoundEnabled = true
                }
            }
        }
        return false
    }
}