import java.io.File;
import java.util.ArrayList;

public class Utils {
    public static ArrayList<String> findSupportedFilesInDir(String directoryPath) {
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();
        ArrayList<String> reqFiles = new ArrayList<>();
        for(File file : files) {
            if (file.getName().endsWith(".req")||file.getName().endsWith(".json")){
                reqFiles.add(file.getName());
//                System.out.println(file.getName());
            }
        }
        return reqFiles;
    }
}
