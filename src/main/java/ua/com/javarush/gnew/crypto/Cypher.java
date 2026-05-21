package ua.com.javarush.gnew.crypto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Cypher {
    private final ArrayList<Character> originalAlphabet = new ArrayList<>(Arrays.asList('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'));


    public String encrypt(String input, int key) {
        key = Math.negateExact(key);

        ArrayList<Character> rotatedAlphabet = new ArrayList<>(originalAlphabet);
        Collections.rotate(rotatedAlphabet, key);
        char[] charArray = input.toCharArray();

        StringBuilder builder = new StringBuilder();
        for (char symbol : charArray) {
            builder.append(processSymbol(symbol, rotatedAlphabet));
        }
        return builder.toString();
    }

    /**
     * Reverse of {@link #encrypt}. The relationship to encrypt is the key
     * to keeping this short.
     */
    public String decrypt(String input, int key) {
        throw new UnsupportedOperationException("TODO: implement decrypt");
    }

    private Character processSymbol(char symbol, ArrayList<Character> rotatedAlphabet) {
        if (!originalAlphabet.contains(symbol)) {
            return symbol;
        }
        int index = originalAlphabet.indexOf(symbol);

        return rotatedAlphabet.get(index);
    }
}
