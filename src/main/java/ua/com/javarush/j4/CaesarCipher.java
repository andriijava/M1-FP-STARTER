package ua.com.javarush.j4;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class CaesarCipher {

    private static final String ENG_ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String UA_ALPHABET  = "абвгґдеєжзиіїйклмнопрстуфхцчшщьюяАБВГҐДЕЄЖЗИІЇЙКЛМНОПРСТУФХЦЧШЩЬЮЯ";


    public char encryptChar(char ch, int key) {
        String strongAlphabet = null;

        if(ENG_ALPHABET.indexOf(ch) != -1) strongAlphabet = ENG_ALPHABET;
          else if(UA_ALPHABET.indexOf(ch) != -1) strongAlphabet = UA_ALPHABET;


        if(strongAlphabet == null) {
            return ch;
        }

            int alphabetLength = strongAlphabet.length();
        int index = strongAlphabet.indexOf(ch);

          int safetyKey = (key % alphabetLength + alphabetLength) % alphabetLength;
          int newIndex = (index + safetyKey) % alphabetLength;

          char resultChar = strongAlphabet.charAt(newIndex);

        return  resultChar;

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

     public void execute(RunOptions options) {
        try {
            Path sourcePath = options.getFilePath();
            String content = Files.readString(sourcePath);

            int newKey = (options.getCommand() == Command.DECRYPT) ? -options.getKey() : options.getKey();

            String encryptedContent = encryptString(content, newKey);

            String originalName =  sourcePath.getFileName().toString();

            String cleanName = originalName.replace("[ENCRYPTED", "").replace("[DECRYPTED", "");

            String correct = (options.getCommand() == Command.DECRYPT) ? "[DECRYPTED]" : "[ENCRYPTED]";
            String newName ;

            int dotIndex = cleanName.lastIndexOf('.');
            if(dotIndex != -1) {
                newName = cleanName.substring(0, dotIndex) + correct + cleanName.substring(dotIndex);

               } else {
                newName = cleanName + correct;
            }
            Path outPath = sourcePath.resolveSibling(newName);
            Files.writeString(outPath, encryptedContent);

           } catch (IOException e){
            System.out.println("Ошибка при работе с файлом :" +  e.getMessage());
        }
     }
}


