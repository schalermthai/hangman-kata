package hangman.java.ep2;

import lombok.ToString;

import java.util.*;
import java.util.stream.Collectors;

import static com.sun.tools.internal.xjc.reader.Ring.add;

@ToString
public class ImmutableHangman {

    private String secretWord;
    private Integer lifeLeft;
    private Set<Character> selectedLetters;

    public ImmutableHangman(String secretWord) {
        this(secretWord, 7, new LinkedHashSet<>());
    }

    public ImmutableHangman(String secretWord, Integer lifeLeft, Set<Character> selectedLetters) {
        this.secretWord = secretWord;
        this.lifeLeft = lifeLeft;
        this.selectedLetters = Collections.unmodifiableSet(selectedLetters);
    }

    public Integer secretWordLength() {
        return secretWord.length();
    }

    public Integer lifeLeft() {
        return lifeLeft;
    }

    public List<Character> secretWordLetters() {
        return secretWord.chars().mapToObj(i -> (char) i).collect(Collectors.toList());
    }

    public String knownSecretWord() {
        return secretWordLetters().stream().map(i -> {
            if (selectedLetters.contains(i)) {
                return "" + i;
            } else {
                return "_";
            }
        }).collect(Collectors.joining());
    }

    public Boolean isWin() {
        return lifeLeft > 0 && knownSecretWord().equals(secretWord);
    }

    public Boolean isLose() {
        return lifeLeft <= 0;
    }

    public boolean isGameOver() {
        return isWin() || isLose();
    }

    public Boolean isInProgress() {
        return !isGameOver();
    }

    private String getStatus() {
        return (isInProgress()) ? "in-progress" : isWin() ? "won" : "lose";
    }


    @ToString
    public static class State {
        String status;
        Set<Character> selectedLetters;
        Integer lifeLeft;
        Integer secretWordLength;
        String knownSecretWord;
    }

    public ImmutableHangman play(Character chars) {

        if (!selectedLetters.contains(chars)) {

            LinkedHashSet<Character> newSelectedLetters = new LinkedHashSet<>(selectedLetters);
            newSelectedLetters.add(chars);

            if (!secretWordLetters().contains(chars)) {
                return new ImmutableHangman(this.secretWord, this.lifeLeft - 1, newSelectedLetters);
            } else {
                return new ImmutableHangman(this.secretWord, lifeLeft, newSelectedLetters);
            }
        }

        return this;
    }

    public State getState() {
        State state = new State();

        state.status = getStatus();
        state.selectedLetters = this.selectedLetters;
        state.lifeLeft = lifeLeft();
        state.secretWordLength = secretWordLength();
        state.knownSecretWord = knownSecretWord();

        return state;
    }


    public static void main(String[] args) {

        ImmutableHangman game = new ImmutableHangman("bigbear");

        List<Character> letters = Arrays.asList('b', 'a', 'i', 'x', 'f', 'c', 'z', 'g', 'v', 'u', 'x', 'o');

        for (Character letter : letters) {
            game = game.play(letter);
            State state = game.getState();
            System.out.println(state);
        }

        ImmutableHangman game2 = new ImmutableHangman("bigbear");

        letters = Arrays.asList('b', 'i', 'g', 'b', 'e', 'a', 'r');

        for (Character letter : letters) {
            game2 = game2.play(letter);
            State state = game2.getState();
            System.out.println(state);
        };

    }
}
