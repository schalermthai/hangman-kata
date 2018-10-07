package deans4j

import com.sun.management.jmx.Trace.send
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.consume
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.reactive.consumeEach
import kotlinx.coroutines.experimental.reactive.openSubscription
import kotlinx.coroutines.experimental.reactive.publish
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.rx2.rxFlowable

import kotlin.coroutines.experimental.buildSequence


fun main(args: Array<String>) = runBlocking<Unit> {

    // coroutine -- fast producer of elements in the context of the hangman.kotlin.main thread
    val source = rxFlowable {
        for (x in 1..3) {
            send(x) // this is a suspending function
            println("Sent $x") // print after successfully sent item
        }
    }
    // subscribe on another thread with a slow subscriber using Rx
    source
            .observeOn(Schedulers.io(), false, 1) // specify buffer size of 1 item
            .doOnComplete { println("Complete") }
            .subscribe { x ->
                Thread.sleep(500) // 500ms to process each item
                println("Processed $x")
            }
    delay(2000) // suspend the hangman.kotlin.main thread for a few seconds
}
