ackage ua.com.javarush.j4;

public class CaesarCipher {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz" +
                                           "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                                           "абвгґдеєжзиіїйклмнопрстуфхцчшщьюя" +
                                           "АБВГҐДЕЄЖЗИІЇЙКЛМНОПРСТУФХЦЧШЩЬЮЯ" +
                                           ".,\"-!? ";
    public char encryptChar(char ch, int key) {
        int index = ALPHABET.indexOf(ch);

        if(index == -1) {
            return ch;
        }
        int newIndex = (index + key) % ALPHABET.length();

        if(newIndex < 0) {
            newIndex += ALPHABET.length();
        }
        return ALPHABET.charAt(newIndex);

    }

    public String encryptString(String text, int key) {

        StringBuilder result = new StringBuilder();

        for(int i=0; i < text.length();i++){
            char originalChar = text.charAt(i);
            char encryptedChar = encryptChar(originalChar, key);

            result.append(encryptedChar);
        }
     return result.toString();

    }

}


