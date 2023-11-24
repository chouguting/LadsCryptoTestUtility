import cipers.AESEngine;
import org.json.JSONArray;
import org.json.JSONObject;


import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class JsonUtils {

    public static JSONObject reqFileToJson(String filepath){
        String[] splitFilePath = filepath.split("\\\\");
        String filename = splitFilePath[splitFilePath.length-1];
        FileReader fileReader = new FileReader(filepath);
        JSONObject jsonObject = new JSONObject();

        if(filename.toLowerCase().startsWith("cbc")){
            jsonObject.put("cipher_mode", "CBC");
        }else {
            jsonObject.put("cipher_mode", "ECB");
        }
        //read header
        String lastLine = "";

        //pass1: header
        String header_comments = "";

        while (fileReader.hasNextLine()) {
            String line = fileReader.readLine();
            if(line.startsWith("#")){
                header_comments += line+"\n";
            }
            if(line.contains("Generated on")){
                continue;
            }
            if(line.contains(":")){
                String[] split_comment = line.split("#",2);
                String[] split = split_comment[1].split(":",2);
                jsonObject.put(split[0].trim().toLowerCase(),split[1].trim());
            }
            if(line.trim().startsWith("[") ){
                break;
            }

        }

        jsonObject.put("header_comments", header_comments);


        //read body
        fileReader = new FileReader(filepath); //restart

        ArrayList<JSONObject> jobList = new ArrayList<>();

        String testString = "";

        JSONObject currentJsonObject = null;
        ArrayList<JSONObject> testList = new ArrayList<>();
        Set<String> parameters = new HashSet<>();

        while (fileReader.hasNextLine()) {
            String line = fileReader.readLine();


            if(line.trim().startsWith("#")){
                continue;
            }
            if(line.startsWith("[")){

                if(currentJsonObject != null){ //save
                    currentJsonObject.put("parameters", parameters);
                    currentJsonObject.put("tests", testList);
                    jobList.add(currentJsonObject);
                }
                testList = new ArrayList<>();
                parameters = new HashSet<>();
                currentJsonObject = new JSONObject();
                String currentJob = line.substring(1,line.length()-1);
                currentJsonObject.put("job_name", currentJob);
                fileReader.readLine();
                continue;
            }
            testString += line+"\n";

            if(line.contains("=")){
                String[] splitLine = line.split("=",2);
                String parameter = splitLine[0].trim().toLowerCase();
                parameters.add(parameter);
            }

            if(line.isBlank() && !testString.isBlank()){
                JSONObject testObject = JsonUtils.stringToJson(testString);
                testList.add(testObject);
                testString = "";
            }
        }
        currentJsonObject.put("parameters", parameters);
        currentJsonObject.put("tests", testList);
        jobList.add(currentJsonObject);
        jsonObject.put("jobs", jobList);
        return jsonObject;
    }
    public static JSONObject stringToJson(String blockString) {
        Scanner scanner = new Scanner(blockString);
        JSONObject jsonObject = new JSONObject();
        while(scanner.hasNext()){
            String line = scanner.nextLine();
            if(line.contains("=")){
                String[] split = line.split("=",2);
                String key = split[0].trim().toLowerCase();
                String value = split[1].trim();
                jsonObject.put(key, value);
            }
        }
        return jsonObject;
    }


    public static void saveJsonToRspFile(String pathname, String filename, JSONObject jsonObject) {
        try {
            FileWriter fileWriter = new FileWriter(pathname+"\\"+filename);
            String header_comments = jsonObject.getString("header_comments");
            fileWriter.write(header_comments);
            fileWriter.write("\n");

            JSONArray jobs = jsonObject.getJSONArray("jobs");
            for(int i=0;i< jobs.length();i++){
                JSONObject job = jobs.getJSONObject(i);
                String job_name = job.getString("job_name");
                JSONArray parameters = job.getJSONArray("parameters");
                fileWriter.write("["+job_name+"]\n");
                JSONArray tests = job.getJSONArray("tests");

                for(int j=0;j<tests.length();j++){
                    JSONObject test = tests.getJSONObject(j);
                    for(Object parameter : parameters){
                        String value = test.getString((String) parameter);
                        fileWriter.write(parameter+" = "+value+"\n");
                    }
                    fileWriter.write("\n");
                }



                fileWriter.write("\n");
            }
            fileWriter.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }


    public static void main(String[] args) throws IOException {
        String jsonString =  Files.readString(Path.of("ECBGFSbox128.json"), StandardCharsets.UTF_8);
        JSONObject inputJson = new JSONObject(jsonString);
        JSONObject outputJson = AESEngine.runAESWithJson(inputJson);
        System.out.println(outputJson.toString(4));
        saveJsonToRspFile("./", "output.rsp", outputJson);

    }


    public static void saveJsonToFile(String pathname, String filename, JSONObject jsonObject) {
        try {
            FileWriter fileWriter = new FileWriter(pathname+"\\"+filename);
            fileWriter.write(jsonObject.toString(4));
            fileWriter.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }


}
