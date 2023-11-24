package cipers;

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

public class AESEngine {


    public final static String MODE_ECB = "ECB";
    public final static String MODE_CBC = "CBC";

    private String cipherMode = AESEngine.MODE_ECB;

    public AESEngine() {
        setCipherMode(AESEngine.MODE_ECB);
    }

    public AESEngine(String mode) {
        setCipherMode(mode);
    }

    public void setCipherMode(String cipherMode) {
        if (cipherMode.toLowerCase().contains(MODE_ECB)) {
            this.cipherMode = MODE_ECB;
        } else if (cipherMode.toLowerCase().contains(MODE_CBC)) {
            this.cipherMode = MODE_CBC;
        }

    }

    public String encrypt(String textHexString, String ivHexString, String keyHexString) {
        try {
            //Hex String 轉成 Byte
            byte[] textByte = AESEngine.HexStringToBytes(textHexString);
            byte[] ivBytes = AESEngine.HexStringToBytes(ivHexString);
            byte[] keyBytes = AESEngine.HexStringToBytes(keyHexString);
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
            String byte2HexStr = AESEngine.bytesToHexString(cipherTextByte);
            return byte2HexStr.toLowerCase();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    public String decrypt(String cipherTextHexString, String ivHexString, String keyHexString) {
        try {
            //轉成 Byte
            byte[] cipherBytes = AESEngine.HexStringToBytes(cipherTextHexString);
//            byte[] ivBytes = ivString.getBytes("UTF-8");
            byte[] ivBytes = AESEngine.HexStringToBytes(ivHexString);
//            byte[] keyBytes = keyString.getBytes("UTF-8");
            byte[] keyBytes = AESEngine.HexStringToBytes(keyHexString);
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
            return AESEngine.bytesToHexString(plainTextByte).toLowerCase();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String bytesToHexString(byte[] bytes) {
        char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] hexChars = new char[bytes.length * 2]; // 1 byte represent 2 hex chars
        int v;
        //byte 0 => char[0], char[1]
        //byte 1 => char[2], char[3]
        //byte 2 => char[4], char[5] ...
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF; // 0xFF = 1111 1111
            hexChars[j * 2] = hexArray[v / 16]; // v/16 = v >> 4, is the higher 4 bits
            hexChars[j * 2 + 1] = hexArray[v % 16]; // v%16 is the lower 4 bits
        }
        return new String(hexChars);
    }

    public static byte[] HexStringToBytes(String hexString) {
        int len = hexString.length();
        byte[] bytes = new byte[len / 2]; // 2個hex char 代表 1 byte
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) // higher 4 bits
                    + Character.digit(hexString.charAt(i + 1), 16)); // lower 4 bits
        }
        return bytes;
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

        //Test json
//        File file = new File("src/tw/edu/ntu/lads/chouguting/test.json");
        String jsonString =  Files.readString(Path.of("ECBGFSbox128.json"), StandardCharsets.UTF_8);
        JSONObject inputJson = new JSONObject(jsonString);
        JSONObject outputJson = AESEngine.runAESWithJson(inputJson);
        System.out.println(outputJson.toString(4));

    }
}
