package hangman.kotlin.ep2

import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.ReplaySubject
import io.reactivex.subjects.Subject

class ReactiveHangman(val secretWord: String, private val inputStream: Subject<Char> = ReplaySubject.create(), maxLife: Int = MAX_LIFE) {

    val selectedLettersStream = inputStream.scan<Set<Char>>(linkedSetOf()) { selectedLetters, c ->
        selectedLetters + c
    }

    val knownSecretWordStream = selectedLettersStream.map { selectedLetters ->
        secretWord.map {
                    if (it in selectedLetters) it
                    else '_'
        }.joinToString("")
    }

    val lifeLeftStream = Observables.zip(selectedLettersStream, inputStream)
            .scan(maxLife) { life, (secretLetters, c) ->
                if (c in secretWord) life
                else if (c in secretLetters) life
                else life - 1
            }

    val statusStream = Observables.zip(lifeLeftStream, knownSecretWordStream) { life, knownSecretWord ->
        if (life > 0 && knownSecretWord == secretWord) Status.WON
        else if (life <= 0) Status.LOSE
        else Status.INPROGRESS
    }

    val states = Observable.create<State> { e ->
        Observables.zip(statusStream, lifeLeftStream, selectedLettersStream, knownSecretWordStream)
            { status, life, secretLetters, knownSecretWord ->
            e.onNext(State(status.text, life, secretLetters, knownSecretWord, secretWord.length))
            if (status != Status.INPROGRESS) e.onComplete()
        }.subscribe()
    }

    data class State(val status: String, val lifeLeft: Int, val selectedLetters: Set<Char>, val knownSecretWord: String, val secretWordLength: Int)

    enum class Status(val text: String) {
        WON("won"), LOSE("lose"), INPROGRESS("in-progress")
    }

    companion object {
        val MAX_LIFE = 7
        fun reactiveHangman(secretWord: String, input: Subject<Char>) = ReactiveHangman(secretWord, input).states
    }
}

private fun test(secretWord: String, inputs: List<Char>, observer: (s: ReactiveHangman.State) -> Unit) {
    val input = ReplaySubject.create<Char>()
    val states = ReactiveHangman.reactiveHangman("bigbear", input)

    inputs.forEach { input.onNext(it) }

    states.subscribe(observer)
}

fun main(args: Array<String>) {

    test("bigbear", listOf('b', 'i', 'g', 'e', 'a', 'r')) { s ->
        println(s)
    }

    test("bigbear", listOf('b', 'a', 'i', 'x', 'f', 'c', 'z', 'g', 'v', 'u', 'x', 'o')) { s ->
        println(s)
    }
}