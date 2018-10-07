package hangman.kotlin.ep2

class ImmutableHangman(val secretWord: String, val life: Int = 7, val selectedLetters: Set<Char> = linkedSetOf()) {

    val knownSecretWord = secretWord.map {
        if (it in selectedLetters) it
        else '_'
    }.joinToString("")

    val status =
            if (hasWon()) Status.WON
            else if(hasLose()) Status.LOSE
            else Status.INPROGRESS

    fun hasWon() = life > 0 && knownSecretWord == secretWord

    fun hasLose() = life <= 0

    fun isGameOver() = hasWon() || hasLose()

    fun isInProgress() = !isGameOver()

    fun play(c: Char): ImmutableHangman {

        if (c !in selectedLetters) {
            if (c !in secretWord)
                return ImmutableHangman(secretWord, life - 1, selectedLetters + c)
            else
                return ImmutableHangman(secretWord, life, selectedLetters + c)
        }

        return this
    }

    enum class Status(val text: String) {
        WON("won"), LOSE("lose"), INPROGRESS("in-progress")
    }

    override fun toString(): String {
        return "ImmutableHangman(status=${status},selectedLetters=$selectedLetters, lifeLeft=$life, secretWordLength=${secretWord.length}, knownSecretWord='$knownSecretWord')"
    }
}

fun main(args : Array<String>) {
    val game = ImmutableHangman("bigbear")

    val letters = listOf('b', 'i', 'g', 'b', 'e', 'a', 'r')

    letters.fold(game) { acc, e ->
        val g = acc.play(e)
        println(g)
        return@fold g
    }


}
