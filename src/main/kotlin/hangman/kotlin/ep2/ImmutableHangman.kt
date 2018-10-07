package hangman.kotlin.ep2

import kotlin.collections.LinkedHashSet

class ImmutableHangman(val secretWord: String, val life: Int = 7, val selectedLetters: Set<String> = LinkedHashSet()) {

    val secretWordLetters = secretWord.toCharArray().map { it.toString() }

    val knownSecretWord: String = secretWordLetters.map {
        if (it in selectedLetters) it
        else "_"
    }.joinToString("")

    val status =
            if (hasWon()) Status.WON
            else if(hasLose()) Status.LOSE
            else Status.INPROGRESS

    fun hasWon() = life > 0 && knownSecretWord == secretWord

    fun hasLose() = life <= 0

    fun isGameOver() = hasWon() || hasLose()

    fun isInProgress() = !isGameOver()

    fun play(chars: Char): ImmutableHangman {

        val c = chars.toString()

        if (c !in selectedLetters) {
            if (isMatchingSecretWord(c))
                return ImmutableHangman(secretWord, life - 1, selectedLetters + c)
            else
                return ImmutableHangman(secretWord, life, selectedLetters + c)
        }

        return this
    }

    private fun isMatchingSecretWord(c: String) = c !in secretWordLetters

    enum class Status(val text: String) {

        WON("won"), LOSE("lose"), INPROGRESS("in-progress")
    }

    override fun toString(): String {
        return "hangman.java.ep2.ImmutableHangman(status=${status},selectedLetters=$selectedLetters, lifeLeft=$life, secretWordLength=${secretWord.length}, knownSecretWord='$knownSecretWord')"
    }
}

fun main(args : Array<String>) {
    var game = ImmutableHangman("bigbear")

    val letters = listOf('b', 'i', 'g', 'b', 'e', 'a', 'r')

    letters.fold(game) { acc, e ->
        val g = acc.play(e)
        println(g)
        return@fold g
    }


}
