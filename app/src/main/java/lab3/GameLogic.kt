package lab3

class MemoryGameLogic(private val maxMatches: Int) {
    private var values: MutableList<Int> = mutableListOf()

    private var matches: Int = 0

    fun process(value: Int):  GameState {
        if (values.size < 1) {
            values.add(value)
            return GameState.Matching
        }

        values.add(value)
        val result = values[0] == values[1]
        if (result) matches++
        values.clear()

        if (!result) return GameState.NoMatch

        return if (matches == maxMatches) GameState.Finished else GameState.Match
    }
}