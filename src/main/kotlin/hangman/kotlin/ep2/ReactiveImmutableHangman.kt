package hangman.kotlin.ep2

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import io.reactivex.subjects.Subject


class ReactiveImmutableHangman(val secretWord: String, val input: Subject<Char> = ReplaySubject.create()) {

    val states: Observable<ImmutableHangman>

    init {
        states = Observable.create {e ->
            val game = ImmutableHangman(secretWord)
            e.onNext(game)

            input.scan(game) { g, c ->
                val newState = g.play(c)
                e.onNext(newState)

                if (newState.isGameOver()) {
                    e.onComplete()
                    return@scan g
                }

                return@scan newState
            }.subscribe()
        }
    }

    fun play(c: Char) {
        input.onNext(c)
        this
    }

    companion object {
        fun reactiveHangman(secretWord: String, input: Subject<Char>): Observable<ImmutableHangman> = ReactiveImmutableHangman(secretWord, input).states;
    }
}

fun main(args: Array<String>) {
    val input = PublishSubject.create<Char>()

    ReactiveImmutableHangman.reactiveHangman("bigbear", input)
            .subscribe { println(it) }

    listOf('b', 'a', 'i', 'x', 'f', 'c', 'z', 'g', 'v', 'u', 'x', 'o')
        .forEach { input.onNext(it) }

    val g = ReactiveImmutableHangman("bigbear")

    listOf('b', 'i', 'g', 'b', 'e', 'a', 'r')
            .forEach { g.play(it) }

    g.states.subscribe { println(it) }

}