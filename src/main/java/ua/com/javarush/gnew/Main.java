package ua.com.javarush.gnew;

import ua.com.javarush.gnew.crypto.BruteForce;
import ua.com.javarush.gnew.crypto.Cypher;
import ua.com.javarush.gnew.file.EncryptedFileNamer;
import ua.com.javarush.gnew.file.FileManager;
import ua.com.javarush.gnew.runner.ArgumentsParser;
import ua.com.javarush.gnew.runner.RunOptions;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        Cypher cypher = new Cypher();
        BruteForce bruteForce = new BruteForce();
        FileManager fileManager = new FileManager();
        EncryptedFileNamer namer = new EncryptedFileNamer();
        ArgumentsParser argumentsParser = new ArgumentsParser();

        try {
            RunOptions runOptions = argumentsParser.parse(args);

            switch (runOptions.getCommand()) {
                case ENCRYPT -> {
                    String content = fileManager.read(runOptions.getFilePath());
                    String encrypted = cypher.encrypt(content, runOptions.getKey());
                    Path output = namer.forEncrypted(runOptions.getFilePath());
                    fileManager.write(output, encrypted);
                }
                case DECRYPT -> {
                    String content = fileManager.read(runOptions.getFilePath());
                    String decrypted = cypher.decrypt(content, runOptions.getKey());
                    Path output = namer.forDecrypted(runOptions.getFilePath());
                    fileManager.write(output, decrypted);
                }
                case BRUTEFORCE -> {
                    String content = fileManager.read(runOptions.getFilePath());
                    String decrypted = bruteForce.bruteForce(content);
                    Path output = namer.forDecrypted(runOptions.getFilePath());
                    fileManager.write(output, decrypted);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
