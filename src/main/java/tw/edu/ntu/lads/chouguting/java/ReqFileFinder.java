package tw.edu.ntu.lads.chouguting.java;

import java.io.File;
import java.util.ArrayList;

public class ReqFileFinder {
    String directoryPath;

    public ReqFileFinder(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public ArrayList<String> findReqFiles() {
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();
        ArrayList<String> reqFiles = new ArrayList<>();
        for(File file : files) {
            if (file.getName().endsWith(".req")){
                reqFiles.add(file.getName());
//                System.out.println(file.getName());
            }
        }
        return reqFiles;
    }
}
