package lab3

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.Gravity
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.GridLayout
import android.widget.ImageButton
import com.example.lab2.R
import java.util.Stack
import java.util.Random

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

    private val state: MutableList<MutableList<Int>> = MutableList(rows) { MutableList(cols) { -1 } }
    private val deckResource: Int = R.drawable.card_back_02
    private var onGameChangeStateListener: (GameEvent) -> Unit = { (e) -> }
    private val matchedPair: Stack<Tile> = Stack()
    private val logic: MemoryGameLogic = MemoryGameLogic(cols * rows / 2)
    init {
        val shuffledIcons: MutableList<Int> = mutableListOf<Int>().also {
            it.addAll(icons.subList(0, cols * rows / 2))
            it.addAll(icons.subList(0, cols * rows / 2))
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

    private fun animatePairedButton(button: ImageButton, action: Runnable) {
        val set = AnimatorSet()
        val random = Random()
        button.pivotX = random.nextFloat() * 200f
        button.pivotY = random.nextFloat() * 200f

        val rotation = ObjectAnimator.ofFloat(button, "rotation", 1080f)
        val scallingX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 4f)
        val scallingY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 4f)
        val fade = ObjectAnimator.ofFloat(button, "alpha", 1f, 0f)
        set.startDelay = 0
        set.duration = 500
        set.interpolator = DecelerateInterpolator()
        set.playTogether(rotation, scallingX, scallingY, fade)
        set.addListener(object: Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {
            }

            override fun onAnimationEnd(animator: Animator) {
                button.scaleX = 1f
                button.scaleY = 1f
                button.alpha = 0.0f
                action.run();
            }

            override fun onAnimationCancel(animator: Animator) {
            }

            override fun onAnimationRepeat(animator: Animator) {
            }
        })
        set.start()
    }

    private fun animateButtonsOnMatchFailure(tile: Tile) {
        val button = tile.button

        val set = AnimatorSet()

        val goRight = ObjectAnimator.ofFloat(button, "x", button.x, button.x + 300)
        val goLeft = ObjectAnimator.ofFloat(button, "x", button.x + 300, button.x - 300)
        val goStart = ObjectAnimator.ofFloat(button, "x", button.x - 300, button.x)
        set.startDelay = 0
        set.duration = 500
        set.interpolator = DecelerateInterpolator()
        set.playSequentially(goRight, goLeft, goStart)
        set.addListener(object: Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {
            }

            override fun onAnimationEnd(animator: Animator) {
                tile.revealed = false
            }

            override fun onAnimationCancel(animator: Animator) {
            }

            override fun onAnimationRepeat(animator: Animator) {
            }
        })
        set.start()
    }

    private fun onClickTile(v: View) {
        val tile = tiles[v.tag]
        if (matchedPair.lastOrNull() == tile) return;
        matchedPair.push(tile)

        val tileValue = tile?.tileResource ?: -1
        val matchResult = logic.process(tileValue)

        onGameChangeStateListener(GameEvent(matchedPair.toList(), matchResult))
        when (matchResult) {
            GameState.Matching -> {
                for (v in matchedPair) {
                    val tag = v.button.tag as String
                    val row = tag.substring(0, 1).toInt()
                    val col = tag.substring(2, 3).toInt()
                    state[row][col] = 2
                }
            }
            GameState.NoMatch -> {
                for (v in matchedPair) {
                    val tag = v.button.tag as String
                    val row = tag.substring(0, 1).toInt()
                    val col = tag.substring(2, 3).toInt()
                    state[row][col] = -1
                }
            }
            GameState.Match -> {
                for (v in matchedPair) {
                    val tag = v.button.tag as String
                    val row = tag.substring(0, 1).toInt()
                    val col = tag.substring(2, 3).toInt()
                    state[row][col] = 1
                }
            }
            else -> {

            }
        }

        if (matchResult != GameState.Matching) {
            matchedPair.clear()
        }
    }

    fun setOnGameChangeListener(listener: (event: GameEvent) -> Unit) {
        onGameChangeStateListener = { e ->
            if (e.state == GameState.NoMatch) {
                listener(e)
                for (tile in e.tiles) {
                    animateButtonsOnMatchFailure(tile)
                }
            } else if (e.state == GameState.Match) {
                for (tile in e.tiles) {
                    animatePairedButton(tile.button, { listener(e) })
                }
            } else {
                listener(e)
            }
        }
    }

    private fun addTile(button: ImageButton, resourceImage: Int) {
        button.setOnClickListener(::onClickTile)
        val tile = Tile(button, resourceImage, deckResource)
        tiles[button.tag.toString()] = tile
    }

    fun getState() : IntArray {
        return state.flatten().toIntArray()
    }

    fun setState(state: IntArray): Unit {
        var i = 0;
        for (r in 0..<rows) {
            for (c in 0..<cols) {
                this.state[r][c] = state[i]
                val tag = "${r}x${c}"
                if (state[i] == 1) {
                    tiles[tag]!!.button.alpha = 0.0f
                }
                else if (state[i] == 2) {
                    tiles[tag]!!.revealed = true
                    onClickTile(tiles[tag]!!.button)
                }
                ++i
            }
        }
    }
}