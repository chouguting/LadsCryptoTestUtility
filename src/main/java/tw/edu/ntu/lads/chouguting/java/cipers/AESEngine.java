package tw.edu.ntu.lads.chouguting.java.cipers;

import org.json.JSONArray;
import org.json.JSONObject;


import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.spec.AlgorithmParameterSpec;

import static tw.edu.ntu.lads.chouguting.java.cipers.CipherUtils.hexStringToBytes;
import static tw.edu.ntu.lads.chouguting.java.cipers.CipherUtils.bytesToHexString;

public class AESEngine {


    public final static String MODE_ECB = "ECB";
    public final static String MODE_CBC = "CBC";

    public final static String MODE_CFB8 = "CFB8";

    public final static String MODE_CFB128 = "CFB128";

    public final static String MODE_CTR = "CTR";


    private String cipherMode = AESEngine.MODE_ECB;

    public AESEngine() {
        setCipherMode(AESEngine.MODE_ECB);
    }

    public AESEngine(String mode) {
        setCipherMode(mode);
    }

    public void setCipherMode(String cipherMode) {
        if (cipherMode.toUpperCase().contains(MODE_ECB)) {
            this.cipherMode = MODE_ECB;
        } else if (cipherMode.toUpperCase().contains(MODE_CBC)) {
            this.cipherMode = MODE_CBC;
        }
        else if (cipherMode.toUpperCase().contains(MODE_CFB8)) {
            this.cipherMode = MODE_CFB8;
        }else if (cipherMode.toUpperCase().contains(MODE_CFB128)) {
            this.cipherMode = MODE_CFB128;
        }else if (cipherMode.toUpperCase().contains(MODE_CTR)) {
            this.cipherMode = MODE_CTR;
        }

    }

    public String encrypt(String textHexString, String ivHexString, String keyHexString) {
        try {
            //Hex String 轉成 Byte
            byte[] textByte = hexStringToBytes(textHexString);
            byte[] ivBytes = hexStringToBytes(ivHexString);
            byte[] keyBytes = hexStringToBytes(keyHexString);
            AlgorithmParameterSpec ivObject = new IvParameterSpec(ivBytes);
            SecretKeySpec keyObject = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/" + cipherMode + "/NoPadding");
            if (cipherMode.equals(AESEngine.MODE_ECB)) {  //EBC 不需要 IV
                cipher.init(Cipher.ENCRYPT_MODE, keyObject);
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, keyObject, ivObject);
            }
            // byte 轉成 Hex String
            byte[] cipherTextByte = cipher.doFinal(textByte);
            String byte2HexStr = bytesToHexString(cipherTextByte);
//            System.out.println("AES "+this.cipherMode+" encrypt success");
            return byte2HexStr.toLowerCase();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    public String decrypt(String cipherTextHexString, String ivHexString, String keyHexString) {
        try {
            //轉成 Byte
            byte[] cipherBytes = hexStringToBytes(cipherTextHexString);
//            byte[] ivBytes = ivString.getBytes("UTF-8");
            byte[] ivBytes = hexStringToBytes(ivHexString);
//            byte[] keyBytes = keyString.getBytes("UTF-8");
            byte[] keyBytes = hexStringToBytes(keyHexString);
            AlgorithmParameterSpec ivObject = new IvParameterSpec(ivBytes);
            SecretKeySpec keyObject = new SecretKeySpec(keyBytes, "AES");
//            Cipher cipher = Cipher.getInstance("AES/"+cipherMode+"/PKCS5Padding");
            Cipher cipher = Cipher.getInstance("AES/" + cipherMode + "/NoPadding");
            if (cipherMode.equals(AESEngine.MODE_ECB)) {  //EBC 不需要 IV
                cipher.init(Cipher.DECRYPT_MODE, keyObject);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, keyObject, ivObject);
            }
            // byte 轉成 Hex String
            byte[] plainTextByte = cipher.doFinal(cipherBytes);
//            System.out.println("AES "+this.cipherMode+" decrypt success");
            return bytesToHexString(plainTextByte).toLowerCase();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    

    public static void runAESWithTestCase(JSONObject testCaseJsonObject, String direction, String aesMode) {
        AESEngine aesEngine = new AESEngine(aesMode);

        String textHexString = (direction.equalsIgnoreCase("encrypt")) ?
                testCaseJsonObject.getString("pt") : testCaseJsonObject.getString("ct");
        String keyHexString = testCaseJsonObject.getString("key");
        String ivHexString = (testCaseJsonObject.has("iv")) ?
                testCaseJsonObject.getString("iv") : "";
        if (direction.equalsIgnoreCase("encrypt")) {
            String cipherText = aesEngine.encrypt(textHexString, ivHexString, keyHexString);
            testCaseJsonObject.put("ct", cipherText);
        } else if (direction.equalsIgnoreCase("decrypt")) {
            String plainText = aesEngine.decrypt(textHexString, ivHexString, keyHexString);
            testCaseJsonObject.put("pt", plainText);
        }



    }

    public static JSONObject runAESWithJson(JSONObject inputJson) {
        JSONArray jobArray = inputJson.getJSONArray("jobs");
        String cipherMode = inputJson.getString("cipher_mode");
        AESEngine aesEngine = new AESEngine(cipherMode);

        for (int i = 0; i < jobArray.length(); i++) {
            JSONObject current_job = jobArray.getJSONObject(i);
            String current_task = current_job.getString("job_name").toLowerCase();  //ENCRYPT or DECRYPT
            JSONArray testArray = current_job.getJSONArray("tests");
            JSONArray parameters = current_job.getJSONArray("parameters");

            if(current_task.equals("encrypt")){
                parameters.put("ciphertext");
            }else if (current_task.equals("decrypt")){
                parameters.put("plaintext");
            }

            for (int j = 0; j < testArray.length(); j++) {
                JSONObject current_test = testArray.getJSONObject(j);

                String textHexString = (current_task.equals("encrypt")) ?
                        current_test.getString("plaintext") : current_test.getString("ciphertext");
                String keyHexString = current_test.getString("key");
                String ivHexString = (current_test.has("iv")) ?
                        current_test.getString("iv") : "00000000000000000000000000000000";
                if (current_task.equals("encrypt")) {
                    String cipherText = aesEngine.encrypt(textHexString, ivHexString, keyHexString);
                    current_test.put("ciphertext", cipherText);
                } else if (current_task.equals("decrypt")) {
                    String plainText = aesEngine.decrypt(textHexString, ivHexString, keyHexString);
                    current_test.put("plaintext", plainText);
                }


            }


        }
        return inputJson;


    }

    public static void main(String[] args) throws IOException {
        //test AES
//        String text = "014730f80ac625fe84f026c60bfd547d"; //長度: 32 hex char = 16 byte = 128 bit
//        String ivString = "0000000000000000";//長度16字長度
//        String keyString = "0000000000000000000000000000000000000000000000000000000000000000";//長度: 32 hex char = 16 byte = 128 bit
//
//        AESEngine aesEngine = new AESEngine(AESEngine.MODE_ECB);
//        String cipherText = aesEngine.encrypt(text, ivString, keyString);
//        System.out.println("cipherText: " + cipherText.toLowerCase());
//        String plainText = aesEngine.decrypt(cipherText, ivString, keyString);
//        System.out.println("plainText: " + plainText.toLowerCase());

        //tw.edu.ntu.lads.chouguting.Test json
//        File file = new File("src/tw/edu/ntu/lads/chouguting/test.json");
        String jsonString =  Files.readString(Path.of("ECBGFSbox128.json"), StandardCharsets.UTF_8);
        JSONObject inputJson = new JSONObject(jsonString);
        JSONObject outputJson = AESEngine.runAESWithJson(inputJson);
        System.out.println(outputJson.toString(4));

    }
}
