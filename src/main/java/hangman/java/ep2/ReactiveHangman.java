package hangman.java.ep2;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReactiveHangman {

    public static final int MAX_LIFE = 7;

    private final Subject<Character> input;
    private final Observable<Integer> lifeLeft;
    private final Observable<Set<String>> selectedLetters;
    private final Observable<State> states;

    public ReactiveHangman(String secretWord) {
        this(secretWord, PublishSubject.create());
    }

    public ReactiveHangman(String secretWord, Subject<Character> input) {

        this.input = input;

        lifeLeft = this.input.scan(MAX_LIFE, (life, c) -> computeLifeLeft(secretWord, life, c));

        selectedLetters = input.scan(new LinkedHashSet<>(), this::computeSelectedLetters);

        Observable<String> knownSecretWord = selectedLetters.map((selectedLetters) -> computeKnownSecretWord(secretWord, selectedLetters));

        Observable<Status> status = selectedLetters.concatMap(i ->
                Observable.zip(lifeLeft, knownSecretWord, (life, ksw) -> Status.compute(secretWord, life, ksw))
        );

        this.states = Observable.create((e) -> {
            Observable.zip(this.lifeLeft, this.selectedLetters, knownSecretWord, status,
                    (life, selectedLetter, ksw, state) -> new State(state, selectedLetter, life, ksw.length(), ksw))
                    .subscribe(s -> createStateStream(e, s));
        });

    }

    public Observable<State> getStates() {
        return states;
    }

    public void play(Character chars) {
        input.onNext(chars);
    }

    private int computeLifeLeft(String secretWord, Integer life, Character newInput) {
        return (secretWord.contains("" + newInput)) ? life : life - 1;
    }

    @NotNull
    private Set<String> computeSelectedLetters(Set<String> selectedLetters, Character newInput) {
        selectedLetters.add(newInput + "");
        return selectedLetters;
    }

    private String computeKnownSecretWord(String secretWord, Set<String> selectedLetters) {
        return Stream.of(secretWord.split(""))
                .map(c -> selectedLetters.contains(c) ? c : "_")
                .collect(Collectors.joining());
    }

    private boolean hasLose(String secretWord, Integer lifeLeft, String knownSecretWord) {
        return lifeLeft <= 0;
    }

    private boolean hasWon(String secretWord, Integer lifeLeft, String knownSecretWord) {
        return lifeLeft > 0 && secretWord.equals(knownSecretWord);
    }

    private void createStateStream(ObservableEmitter<State> e, State s) {
        e.onNext(s);
        if (s.status != Status.INPROGRESS) {
            e.onComplete();
        }
    }

    @Data @AllArgsConstructor
    public static class State {
        Status status;
        Set<String> selectedLetters;
        Integer lifeLeft;
        Integer secretWordLength;
        String knownSecretWord;
    }

    public static enum Status {
        INPROGRESS("in-progress"), WON("won"), LOSE("lose");

        private final String text;

        Status(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public static Status compute(String secretWord, Integer lifeLeft, String knownSecretWord) {
            if (hasWon(secretWord, lifeLeft, knownSecretWord)) return Status.WON;
            if (hasLose(secretWord, lifeLeft, knownSecretWord)) return Status.LOSE;
            return Status.INPROGRESS;
        }

        private static boolean hasLose(String secretWord, Integer lifeLeft, String knownSecretWord) {
            return lifeLeft <= 0;
        }

        private static boolean hasWon(String secretWord, Integer lifeLeft, String knownSecretWord) {
            return lifeLeft > 0 && secretWord.equals(knownSecretWord);
        }
    }

    public static Observable<State> reactiveHangman(String secretWord, Subject<Character> input) {
        return new ReactiveHangman(secretWord, input).getStates();
    }

    public static void main(String[] args) {

        PublishSubject<Character> input = PublishSubject.create();

        reactiveHangman("bigbear", input).subscribe(System.out::println);

        List<Character> letters = Arrays.asList('b', 'i', 'g', 'b', 'e', 'a', 'r');
        letters.forEach(input::onNext);

//        ====

        input = PublishSubject.create();
        reactiveHangman("bigbear", input).subscribe(System.out::println);

        letters = Arrays.asList('b', 'a', 'i', 'x', 'f', 'c', 'z', 'g', 'v', 'u', 'x', 'o', 'm');
        letters.forEach(input::onNext);



    }
}
