package tw.edu.ntu.lads.chouguting.java;

import java.io.File;
import java.util.Scanner;

public class FileReader {

    Scanner scanner;

    public FileReader(String filename) {
        File file = new File(filename);
        try {
            scanner = new Scanner(file);
        } catch (Exception e) {
            System.out.println("讀取檔案錯誤!!!!");
        }
    }

    public String readLine() {
        return scanner.nextLine();
    }

    public boolean hasNextLine() {
        return scanner.hasNextLine();
    }




}
