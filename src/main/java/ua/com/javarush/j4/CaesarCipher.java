package ua.com.javarush.j4;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class CaesarCipher {

    private static final String ENG_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ENG_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UA_UPPER =  "АБВГҐДЕЄЖЗИІЇЙКЛМНОПРСТУФХЦЧШЩЬЮЯ";
    private static final String UA_LOWER =  "абвгґдеєжзиіїйклмнопрстуфхцчшщьюя";

    public char encryptChar(char ch, int key) {
        String strongAlphabet = null;

        if(ENG_LOWER.indexOf(ch) != -1) strongAlphabet = ENG_LOWER;
        else if(ENG_UPPER.indexOf(ch) != -1) strongAlphabet = ENG_UPPER;
        else if (UA_LOWER.indexOf(ch) != -1) strongAlphabet = UA_LOWER;
        else if (UA_UPPER.indexOf(ch) != -1) strongAlphabet = UA_UPPER;

        if(strongAlphabet == null) {
            return ch;
        }


            int alphabetLength = strongAlphabet.length();
        int index = strongAlphabet.indexOf(ch);

          int safetyKey = (key % alphabetLength + alphabetLength) % alphabetLength;
          int newIndex = (index + safetyKey) % alphabetLength;

          char resultChar = strongAlphabet.charAt(newIndex);

          if(key < 0 && (index + key) < 0 && Math.abs(key) < alphabetLength){
              if(Character.isLowerCase(resultChar)){
                  return Character.toUpperCase(resultChar);
              } else if(Character.isUpperCase(resultChar)){
                  return Character.toLowerCase(resultChar);
              }
          }
//Math.abs(key) < alphabetLength.
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


