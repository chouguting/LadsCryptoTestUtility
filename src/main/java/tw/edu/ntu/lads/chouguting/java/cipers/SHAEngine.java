package tw.edu.ntu.lads.chouguting.java.cipers;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.ArrayList;

public class SHAEngine {
    public static String MODE_SHA256 = "SHA-256";
    public static String MODE_SHA384 = "SHA-384";
    public static String MODE_SHA512 = "SHA-512";
    public static String MODE_SHA3_256 = "SHA3-256";
    public static String MODE_SHA3_384 = "SHA3-384";
    public static String MODE_SHA3_512 = "SHA3-512";

    private String shaMode = SHAEngine.MODE_SHA256;


    public SHAEngine(String mode) {
        setShaMode(mode);
    }

    public void setShaMode(String shaMode) {
        if (shaMode.toUpperCase().contains("SHA2-256")){
            this.shaMode = MODE_SHA256;
        }else if (shaMode.toUpperCase().contains("SHA2-384")){
            this.shaMode = MODE_SHA384;
        }else if(shaMode.toUpperCase().contains("SHA2-512")){
            this.shaMode = MODE_SHA512;
        }else if(shaMode.toUpperCase().contains("SHA3-256")){
            this.shaMode = MODE_SHA3_256;
        }else if(shaMode.toUpperCase().contains("SHA3-384")){
            this.shaMode = MODE_SHA3_384;
        }else if(shaMode.toUpperCase().contains("SHA3-512")){
            this.shaMode = MODE_SHA3_512;
        }else {
            throw new IllegalArgumentException("Unsupported SHA mode: " + shaMode);
        }
    }

    public String hash(String textHexString) {
        try {
            byte[] textBytes = CipherUtils.hexStringToBytes(textHexString);
            MessageDigest digest = MessageDigest.getInstance(this.shaMode);
            byte[] hash = digest.digest(textBytes);
            return CipherUtils.bytesToHexString(hash);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    //MCT: Monte Carlo Test
    public ArrayList<String> doMCTHash(String initialTextHexString){
        if(this.shaMode.toUpperCase().contains("SHA3")) {
            return doSHA3MCTHash(initialTextHexString);
        }else{
            return doSHA2MCTHash(initialTextHexString);
        }
    }

    //SHA 2 MCT: Monte Carlo Test
    public ArrayList<String> doSHA2MCTHash(String initialTextHexString){
        ArrayList<String> outputResult = new ArrayList<String>();
        byte[] seedByte = CipherUtils.hexStringToBytes(initialTextHexString);
        try {

            MessageDigest digest = MessageDigest.getInstance(this.shaMode);

            for(int i=0; i<100;i++){
                ArrayList<byte[]> messageDigestList = new ArrayList<byte[]>();
                messageDigestList.add(seedByte);
                messageDigestList.add(seedByte);
                messageDigestList.add(seedByte); //add 3 times
                for(int j=0;j<1000;j++){
                    byte[] lastDigest = messageDigestList.get(messageDigestList.size()-1);
                    byte[] secondLastDigest = messageDigestList.get(messageDigestList.size()-2);
                    byte[] thirdLastDigest = messageDigestList.get(messageDigestList.size()-3);
                    //concatenate of last 3 message digest
                    byte[] newMessage = new byte[thirdLastDigest.length + secondLastDigest.length + lastDigest.length];
                    System.arraycopy(thirdLastDigest, 0, newMessage, 0, thirdLastDigest.length);
                    System.arraycopy(secondLastDigest, 0, newMessage, thirdLastDigest.length, secondLastDigest.length);
                    System.arraycopy(lastDigest, 0, newMessage, thirdLastDigest.length + secondLastDigest.length, lastDigest.length);
                    byte[] newDigest = digest.digest(newMessage);
                    messageDigestList.add(newDigest);
                }
                seedByte = messageDigestList.get(messageDigestList.size()-1);
                outputResult.add(CipherUtils.bytesToHexString(seedByte));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return outputResult;
    }

    //SHA 3 MCT: Monte Carlo Test
    public ArrayList<String> doSHA3MCTHash(String initialTextHexString){
        ArrayList<String> outputResult = new ArrayList<String>();
        byte[] seedByte = CipherUtils.hexStringToBytes(initialTextHexString);
        try {

            MessageDigest digest = MessageDigest.getInstance(this.shaMode);

            for(int i=0; i<100;i++){
                ArrayList<byte[]> messageDigestList = new ArrayList<byte[]>();
                messageDigestList.add(seedByte); //add 1 times
                for(int j=0;j<1000;j++){
                    byte[] lastDigest = messageDigestList.get(messageDigestList.size()-1);
                    //use last message digest as new message
                    byte[] newDigest = digest.digest(lastDigest);
                    messageDigestList.add(newDigest);
                }
                //use last message digest as new seed
                seedByte = messageDigestList.get(messageDigestList.size()-1);
                outputResult.add(CipherUtils.bytesToHexString(seedByte));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return outputResult;
    }




    public static void runSHAWithTestCase(JSONObject testCaseJsonObject, String shaMode, String testType, Boolean mctEnabled){
        SHAEngine shaEngine = new SHAEngine(shaMode);
        String messageHexString = testCaseJsonObject.getString("msg");

        if(testType.toUpperCase().equals("MCT") && mctEnabled){

            ArrayList<String> mctResults = shaEngine.doMCTHash(messageHexString);
            JSONArray mctResultJsonArray = new JSONArray();
            for(String mctResultHex: mctResults){
                JSONObject mctResultJsonObject = new JSONObject();
                mctResultJsonObject.put("md", mctResultHex);
                mctResultJsonArray.put(mctResultJsonObject);
            }
            testCaseJsonObject.put("resultsArray", mctResultJsonArray); //mct: Monte Carlo Test

            return;
        }

        String digestHexString = shaEngine.hash(messageHexString);
        testCaseJsonObject.put("md", digestHexString);  //md: message digest
    }

    public static void main(String[] args) {
        System.out.println("Hello World!");
        SHAEngine shaEngine = new SHAEngine(SHAEngine.MODE_SHA256);
        System.out.println(shaEngine.hash("1234567890ABCDEF").toLowerCase());
    }
}
