package ua.com.javarush.j4;

import java.nio.file.Path;
import java.util.concurrent.Callable;


public class ArgumentsParser {

    public static RunOptions parse(String[] args) {
        Command command = null;
        Path filePath = null;
        int key = 0;

       for (int i = 0; i < args.length; i++) {
           String arg = args[i];
           switch (arg) {
               case "-e":
                   command = Command.ENCRYPT;
                   break;
               case "-d":
                   command = Command.DECRYPT;
                   break;
               case "-bf":
                   command = Command.BRUTEFORCE;
                   break;
               case "-k":
                   key = Integer.parseInt(args[i+1]);
                   i++;
                   break;
               case "-f":
                   filePath = Path.of(args[i+1]);
                   i++;
                   break;
           }

       }
       return  new RunOptions(command, filePath, key);

    }

}


