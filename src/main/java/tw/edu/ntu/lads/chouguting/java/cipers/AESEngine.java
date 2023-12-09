package tw.edu.ntu.lads.chouguting.java.cipers;

import org.json.JSONArray;
import org.json.JSONObject;


import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;

import static tw.edu.ntu.lads.chouguting.java.cipers.CipherUtils.*;

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
        } else if (cipherMode.toUpperCase().contains(MODE_CFB8)) {
            this.cipherMode = MODE_CFB8;
        } else if (cipherMode.toUpperCase().contains(MODE_CFB128)) {
            this.cipherMode = MODE_CFB128;
        } else if (cipherMode.toUpperCase().contains(MODE_CTR)) {
            this.cipherMode = MODE_CTR;
        } else {
            throw new IllegalArgumentException("Unsupported cipher mode: " + cipherMode);
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
            return byte2HexStr;

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
            return bytesToHexString(plainTextByte);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    //ECB monte carlo test
    //we use the same function for encrypt and decrypt
    //direction: encrypt or decrypt
    //textHexString: plaintext or ciphertext
    //when in decrypt mode, we use plaintext as Ciphertext
    private JSONArray doGeneralMct(String direction, String textHexString, String keyHexString, String ivHexString, int keyLength) {
        //we use the same
//        ArrayList<String> outputResult = new ArrayList<String>();
        JSONArray outputResult = new JSONArray();
        try {
            //Hex String 轉成 Byte
            byte[] initTextByte = hexStringToBytes(textHexString);
            byte[] initKeyBytes = hexStringToBytes(keyHexString);
            byte[] initIvBytes = hexStringToBytes(ivHexString);

            ArrayList<byte[]> keyByteList = new ArrayList<byte[]>(); //for next round
            keyByteList.add(initKeyBytes);
            ArrayList<byte[]> ivByteList = new ArrayList<byte[]>(); //for next round
            ivByteList.add(initIvBytes);
//            System.out.println("direction: " + direction);

            for (int i = 0; i < 100; i++) {
//                System.out.println("Round: " + i);
                ArrayList<byte[]> plainTextByteList = new ArrayList<byte[]>();
                plainTextByteList.add(initTextByte);

                byte[] currentKeyByte = keyByteList.get(keyByteList.size() - 1);
                byte[] currentIvByte = ivByteList.get(ivByteList.size() - 1);

                Cipher cipher = Cipher.getInstance("AES/" + cipherMode + "/NoPadding");
                SecretKeySpec keyObject = new SecretKeySpec(currentKeyByte, "AES");
                AlgorithmParameterSpec currentIvObject = new IvParameterSpec(currentIvByte);


                for (int j = 0; j < 1000; j++) {
                    byte[] currentPlainTextByte = plainTextByteList.get(plainTextByteList.size() - 1);



                    if (!cipherMode.equals(AESEngine.MODE_ECB) && j > 0) {
                        if (j == 1) {
                            currentPlainTextByte = currentIvByte.clone(); //second round(j=1), we use iv as plaintext
                        } else if (j > 1) {
                            currentPlainTextByte = plainTextByteList.get(plainTextByteList.size() - 2); //get second last plaintext
                        }
                    }

                    if (direction.equalsIgnoreCase("encrypt")) {
                        if (cipherMode.equals(AESEngine.MODE_ECB)) {
                            cipher.init(Cipher.ENCRYPT_MODE, keyObject);
                        } else {
                            if (j == 0) {
                                cipher.init(Cipher.ENCRYPT_MODE, keyObject, currentIvObject);
                            }
                        }
                    } else if (direction.equalsIgnoreCase("decrypt")) {
                        if (cipherMode.equals(AESEngine.MODE_ECB)) {
                            cipher.init(Cipher.DECRYPT_MODE, keyObject);
                        } else {
                            if (j == 0) {
                                cipher.init(Cipher.DECRYPT_MODE, keyObject, currentIvObject);
                            }
                        }
                    }

                    byte[] cipherTextByte = cipher.update(currentPlainTextByte);
                    plainTextByteList.add(cipherTextByte); //plaintext for next round is iv of current round(only for first round)
                }
                byte[] lastCipherTextByte = plainTextByteList.get(plainTextByteList.size() - 1); //get last ciphertext
//                outputResult.add(bytesToHexString(lastCipherTextByte));  //add to output result
                //create new json object
                JSONObject currentRoundResult = new JSONObject();
                currentRoundResult.put("key", bytesToHexString(currentKeyByte)); //add key to output result
                if(!this.cipherMode.equals(AESEngine.MODE_ECB)){ //if not ECB, add iv to currentRoundResult
                    currentRoundResult.put("iv", bytesToHexString(currentIvByte)); //add iv to output result
                }
                if (direction.equalsIgnoreCase("encrypt")) {
                    currentRoundResult.put("pt", bytesToHexString(initTextByte)); //initial plaintext
                    currentRoundResult.put("ct", bytesToHexString(lastCipherTextByte)); //calculated ciphertext

                } else if (direction.equalsIgnoreCase("decrypt")) {
                    currentRoundResult.put("ct", bytesToHexString(initTextByte)); //initial ciphertext
                    currentRoundResult.put("pt", bytesToHexString(lastCipherTextByte)); //calculated plaintext
                }


                outputResult.put(currentRoundResult); //add current round result to output result

                //for next round
                byte[] secondLastCipherTextByte = plainTextByteList.get(plainTextByteList.size() - 2); //get second last ciphertext
                byte[] lastKey = keyByteList.get(keyByteList.size() - 1);
                if (keyLength == 128) {
                    byte[] newKey = byteArrayXor(lastKey, lastCipherTextByte);
                    keyByteList.add(newKey);
                } else if (keyLength == 192) {
                    byte[] last8BytesOfSecondLastCipherTextByte = new byte[8]; //8 bytes = 64 bits
                    System.arraycopy(secondLastCipherTextByte, 8, last8BytesOfSecondLastCipherTextByte, 0, 8); //copy last 8 bytes
                    byte[] bytesForXor = new byte[24]; //24 bytes = 192 bits, 8+16 = 24
                    System.arraycopy(last8BytesOfSecondLastCipherTextByte, 0, bytesForXor, 0, 8); //copy 8 bytes first
                    System.arraycopy(lastCipherTextByte, 0, bytesForXor, 8, 16); //copy 16 bytes
                    byte[] newKey = byteArrayXor(lastKey, bytesForXor);
                    keyByteList.add(newKey);
                } else if (keyLength == 256) {
                    byte[] bytesForXor = new byte[32]; //32 bytes = 256 bits, 16+16 = 32
                    System.arraycopy(secondLastCipherTextByte, 0, bytesForXor, 0, 16); //copy 16 bytes first
                    System.arraycopy(lastCipherTextByte, 0, bytesForXor, 16, 16); //copy 16 bytes
                    byte[] newKey = byteArrayXor(lastKey, bytesForXor);
                    keyByteList.add(newKey);
                }
                ivByteList.add(lastCipherTextByte); //iv for next round is last ciphertext of current round
                if (cipherMode.equals(AESEngine.MODE_ECB)) {
                    initTextByte = lastCipherTextByte.clone(); //plaintext for next round is ciphertext of current round
                } else {
                    initTextByte = secondLastCipherTextByte.clone(); //plaintext for next round is second last ciphertext of current round
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputResult;
    }


    private JSONArray doCfb8Mct(String direction, String textHexString, String keyHexString, String ivHexString, int keyLength) {
        //we use the same
//        ArrayList<String> outputResult = new ArrayList<String>();
        JSONArray outputResult = new JSONArray();
        try {
            //Hex String 轉成 Byte
            byte[] initTextByte = hexStringToBytes(textHexString);
            byte[] initKeyBytes = hexStringToBytes(keyHexString);
            byte[] initIvBytes = hexStringToBytes(ivHexString);

            ArrayList<byte[]> keyByteList = new ArrayList<byte[]>(); //for next round
            keyByteList.add(initKeyBytes);
            ArrayList<byte[]> ivByteList = new ArrayList<byte[]>(); //for next round
            ivByteList.add(initIvBytes);

            for (int i = 0; i < 100; i++) {
                ArrayList<byte[]> plainTextByteList = new ArrayList<byte[]>();
                plainTextByteList.add(initTextByte);

                byte[] currentKeyByte = keyByteList.get(keyByteList.size() - 1);
                byte[] currentIvByte = ivByteList.get(ivByteList.size() - 1);
                Cipher cipher = Cipher.getInstance("AES/" + cipherMode + "/NoPadding");

                SecretKeySpec keyObject = new SecretKeySpec(currentKeyByte, "AES");
                AlgorithmParameterSpec currentIvObject = new IvParameterSpec(currentIvByte);

                if (direction.equalsIgnoreCase("encrypt")) {
                    cipher.init(Cipher.ENCRYPT_MODE, keyObject, currentIvObject);
                } else if (direction.equalsIgnoreCase("decrypt")) {
                    cipher.init(Cipher.DECRYPT_MODE, keyObject, currentIvObject);
                }

                for (int j = 0; j < 1000; j++) {
                    byte[] currentPlainTextByte = plainTextByteList.get(plainTextByteList.size() - 1);

                    if (j >= 1 && j<= 16) {
                        currentPlainTextByte = getSingleByte(currentIvByte,j-1); //get second last plaintext
                    } else if (j > 16) {
                        currentPlainTextByte = plainTextByteList.get(plainTextByteList.size()-1 - 16); //get second last plaintext
                    }

                    byte[] cipherTextByte = cipher.update(currentPlainTextByte);
                    plainTextByteList.add(cipherTextByte); //plaintext for next round is iv of current round(only for first round)
                }
                byte[] lastCipherTextByte = plainTextByteList.get(plainTextByteList.size() - 1); //get last ciphertext
//                outputResult.add(bytesToHexString(lastCipherTextByte));  //add to output result
                JSONObject currentRoundResult = new JSONObject();
                currentRoundResult.put("key",bytesToHexString(currentKeyByte)); //add key to output result
                currentRoundResult.put("iv", bytesToHexString(currentIvByte)); //add iv to output result
                if(direction.equalsIgnoreCase("encrypt")){
                    currentRoundResult.put("pt", bytesToHexString(initTextByte)); //initial plaintext
                    currentRoundResult.put("ct", bytesToHexString(lastCipherTextByte)); //calculated ciphertext
                }else if(direction.equalsIgnoreCase("decrypt")){
                    currentRoundResult.put("ct", bytesToHexString(initTextByte)); //initial ciphertext
                    currentRoundResult.put("pt", bytesToHexString(lastCipherTextByte)); //calculated plaintext
                }


                outputResult.put(currentRoundResult); //add current round result to output result


                byte[] lastKey = keyByteList.get(keyByteList.size() - 1);
                if (keyLength == 128) {
                    byte[] bytesForXor = new byte[16]; //16 bytes = 128 bits
                    //copy 16 bytes of data
                    for(int index=0;index<16;index++){
                        System.arraycopy(plainTextByteList.get(plainTextByteList.size()-(16-index)), 0, bytesForXor, 0+index, 1);
                    }
                    byte[] newKey = byteArrayXor(lastKey, bytesForXor);
                    keyByteList.add(newKey);
                } else if (keyLength == 192) {
                    byte[] bytesForXor = new byte[24]; //24 bytes = 192 bits
                    //copy 24 bytes of data
                    for(int index=0;index<24;index++){
                        System.arraycopy(plainTextByteList.get(plainTextByteList.size()-(24-index)), 0, bytesForXor, 0+index, 1);
                    }
                    byte[] newKey = byteArrayXor(lastKey, bytesForXor);
                    keyByteList.add(newKey);
                } else if (keyLength == 256) {
                    byte[] bytesForXor = new byte[32]; //32 bytes = 256 bits
                    //copy 32 bytes of data
                    for(int index=0;index<32;index++){
                        System.arraycopy(plainTextByteList.get(plainTextByteList.size()-(32-index)), 0, bytesForXor, 0+index, 1);
                    }
                    byte[] newKey = byteArrayXor(lastKey, bytesForXor);
                    keyByteList.add(newKey);
                }

                //build new iv
                byte[] bytesForIv = new byte[16]; //16 bytes = 128 bits
                //copy 16 bytes of data
                for(int index=0;index<16;index++){
                    System.arraycopy(plainTextByteList.get(plainTextByteList.size()-(16-index)), 0, bytesForIv, 0+index, 1);
                }
                ivByteList.add(bytesForIv); //iv for next round is last ciphertext of current round
                initTextByte = plainTextByteList.get(plainTextByteList.size()-1-16); //plaintext for next round is second last ciphertext of current round
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputResult;
    }


    public JSONArray doMctAES(String direction, String textHexString, String keyHexString, String ivHexString, int keyLength) {
        if (cipherMode.equals(AESEngine.MODE_ECB) || cipherMode.equals(AESEngine.MODE_CBC)  || cipherMode.equals(AESEngine.MODE_CFB128)) {
            return doGeneralMct(direction, textHexString, keyHexString, ivHexString, keyLength);
        }else if(cipherMode.equals(AESEngine.MODE_CFB8)){
            return doCfb8Mct(direction, textHexString, keyHexString, ivHexString, keyLength);
        }

        return null;
    }


    public static void runAESWithTestCase(JSONObject testCaseJsonObject, String direction, String aesMode, int keyLength, String testType, Boolean mctEnabled) {
        AESEngine aesEngine = new AESEngine(aesMode);

        String textHexString = (direction.equalsIgnoreCase("encrypt")) ?
                testCaseJsonObject.getString("pt") : testCaseJsonObject.getString("ct");
        String keyHexString = testCaseJsonObject.getString("key");
        String ivHexString = (testCaseJsonObject.has("iv")) ?
                testCaseJsonObject.getString("iv") : "";


        if (testType.toUpperCase().equals("MCT") && mctEnabled) {
            JSONArray mctResultJsonArray = aesEngine.doMctAES(direction, textHexString, keyHexString, ivHexString, keyLength);
            testCaseJsonObject.put("resultsArray", mctResultJsonArray); //mct: Monte Carlo Test
            return;
        }


        if (direction.equalsIgnoreCase("encrypt")) {
            String cipherText = aesEngine.encrypt(textHexString, ivHexString, keyHexString);
            testCaseJsonObject.put("ct", cipherText);
        } else if (direction.equalsIgnoreCase("decrypt")) {
            String plainText = aesEngine.decrypt(textHexString, ivHexString, keyHexString);
            testCaseJsonObject.put("pt", plainText);
        }
    }


}
