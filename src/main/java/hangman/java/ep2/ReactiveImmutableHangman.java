package hangman.java.ep2;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import java.util.Arrays;
import java.util.List;

public class ReactiveImmutableHangman {

    public static final int MAX_LIFE = 7;

    private Observable<ImmutableHangman> states;

    public ReactiveImmutableHangman(String secretWord) {
        this(secretWord, PublishSubject.create());
    }

    public ReactiveImmutableHangman(String secretWord, Subject<Character> input) {

        this.states = Observable.create((e) -> {
            ImmutableHangman s = new ImmutableHangman(secretWord);
            e.onNext(s);

            input.scan(s, (currentState, newInput) -> {

                ImmutableHangman newState = currentState.play(newInput);
                e.onNext(newState);

                if (newState.isGameOver()) {
                    e.onComplete();
                    return currentState;
                }

                return newState;
            }).subscribe();
        });
    }

    public Observable<ImmutableHangman> getStates() {
        return states;
    }

    public static Observable<ImmutableHangman> reactiveHangman(String secretWord, Subject<Character> input) {
        return new ReactiveImmutableHangman(secretWord, input).getStates();
    }

    public static void main(String[] args) {

        PublishSubject<Character> input = PublishSubject.create();

        reactiveHangman("bigbear", input).subscribe(c -> System.out.println(c.getState()));

        List<Character> letters = Arrays.asList('b', 'i', 'g', 'b', 'e', 'a', 'r', 'x');
        letters.forEach(input::onNext);

        input = PublishSubject.create();
        reactiveHangman("bigbear", input).subscribe(c -> System.out.println(c.getState()));

        letters = Arrays.asList('b', 'a', 'i', 'x', 'f', 'c', 'z', 'g', 'v', 'u', 'x', 'o', 'm');
        for (Character letter : letters) {
            System.out.println(letter);
            input.onNext(letter);
        }
    }
}
