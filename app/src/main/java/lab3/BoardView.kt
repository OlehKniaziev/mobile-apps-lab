package lab3

import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import com.example.lab2.R
import java.util.Stack

class BoardView(
    private val gridLayout: GridLayout,
    private val cols: Int,
    private val rows: Int
) {
    private val tiles: MutableMap<String, Tile> = mutableMapOf()
    private val icons: List<Int> = listOf(
        R.drawable.clubs_2,
        R.drawable.clubs_3,
        R.drawable.clubs_4,
        R.drawable.clubs_5,
        R.drawable.clubs_6,
        R.drawable.clubs_7,
        R.drawable.clubs_8,
        R.drawable.clubs_9,
        R.drawable.clubs_10,
        R.drawable.clubs_jack,
        R.drawable.clubs_queen,
        R.drawable.clubs_king,
        R.drawable.clubs_ace,
        R.drawable.hearts_ace,
        R.drawable.hearts_king,
        R.drawable.spades_ace,
        R.drawable.spades_king,
        R.drawable.diamonds_ace,
        R.drawable.diamonds_king,
    )

    private val deckResource: Int = R.drawable.card_back_02
    private var onGameChangeStateListener: (GameEvent) -> Unit = { (e) -> }
    private val matchedPair: Stack<Tile> = Stack()
    private val logic: MemoryGameLogic = MemoryGameLogic(cols * rows / 2)
    init {
        val shuffledIcons: MutableList<Int> = mutableListOf<Int>().also {
            it.addAll(icons.subList(0, cols * rows / 2))
            it.addAll(icons.subList(0, cols * rows / 2))
            it.shuffle()
        }

        // tu umieść kod pętli tworzący wszystkie karty, który jest obecnie
        // w aktywności Lab03Activity

        for (row in 0..<rows) {
            for (col in 0..<cols) {
                val buttonTag = "${row}x${col}"

                val button = ImageButton(gridLayout.context).also {
                    it.tag = buttonTag
                    val layoutParams = GridLayout.LayoutParams()
                    it.setImageResource(R.drawable.baseline_5g_24)
                    layoutParams.width = 0
                    layoutParams.height = 0
                    layoutParams.setGravity(Gravity.CENTER)
                    layoutParams.columnSpec = GridLayout.spec(col, 1, 1f)
                    layoutParams.rowSpec = GridLayout.spec(row, 1, 1f)
                    it.layoutParams = layoutParams

                }
                addTile(button, shuffledIcons.removeLast())
                gridLayout.addView(button)
            }
        }
    }
    private fun onClickTile(v: View) {
        val tile = tiles[v.tag]
        matchedPair.push(tile)

        val tileValue = tile?.tileResource ?: -1
        val matchResult = logic.process(tileValue)

        onGameChangeStateListener(GameEvent(matchedPair.toList(), matchResult))
        if (matchResult != GameState.Matching) {
            matchedPair.clear()
        }
    }

    fun setOnGameChangeListener(listener: (event: GameEvent) -> Unit) {
        onGameChangeStateListener = listener
    }

    private fun addTile(button: ImageButton, resourceImage: Int) {
        button.setOnClickListener(::onClickTile)
        val tile = Tile(button, resourceImage, deckResource)
        tiles[button.tag.toString()] = tile
    }
}