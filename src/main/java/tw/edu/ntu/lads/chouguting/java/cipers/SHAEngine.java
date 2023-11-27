package tw.edu.ntu.lads.chouguting.java.cipers;

import org.json.JSONObject;

import java.security.MessageDigest;

public class SHAEngine {
    public static String MODE_SHA256 = "SHA-256";
    public static String MODE_SHA384 = "SHA-384";
    public static String MODE_SHA512 = "SHA-512";

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




    public static void runSHAWithTestCase(JSONObject testCaseJsonObject, String shaMode){
        SHAEngine shaEngine = new SHAEngine(shaMode);
        String messageHexString = testCaseJsonObject.getString("msg");
        String digestHexString = shaEngine.hash(messageHexString);
        testCaseJsonObject.put("digest", digestHexString);
    }

    public static void main(String[] args) {
        System.out.println("Hello World!");
        SHAEngine shaEngine = new SHAEngine(SHAEngine.MODE_SHA256);
        System.out.println(shaEngine.hash("1234567890ABCDEF").toLowerCase());
    }
}
