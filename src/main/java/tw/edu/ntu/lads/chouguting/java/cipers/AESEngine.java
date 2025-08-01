package tw.edu.ntu.lads.chouguting.java.cipers;

import org.json.JSONArray;
import org.json.JSONObject;
import views.validationSystem.ValidationTestCase;


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

    public static String getHardwareTestInput(String testId, JSONObject testCaseJsonObject, String direction, String aesMode, int keyLength, String testType){
        AESEngine aesEngine = new AESEngine(aesMode);
        String result = (testId+" ");
        result += "1 "; //AES algorithm code
        switch (aesEngine.cipherMode){
            case AESEngine.MODE_ECB:
                result += "1 "; //ECB mode code
                break;
            case AESEngine.MODE_CBC:
                result += "2 "; //CBC mode code
                break;
            case AESEngine.MODE_CFB8:
                result += "3 "; //CFB8 mode code
                break;
            case AESEngine.MODE_CFB128:
                result += "4 "; //CFB128 mode code
                break;
            case AESEngine.MODE_CTR:
                result += "5 "; //CTR mode code
                break;
        }

        if(direction.equalsIgnoreCase("encrypt")){
            result += "1 "; //encrypt
        }else{
            result += "2 "; //decrypt
        }

        if (testType.toUpperCase().equals("MCT")){
            result += "2 "; //MCT
        }else {
            result += "1 "; //normal test
        }

        result += (keyLength + " "); //key length

        switch (aesEngine.cipherMode){
            case AESEngine.MODE_ECB:
                if(direction.equalsIgnoreCase("encrypt")){
                    result += (testCaseJsonObject.getString("pt") + " ");
                }else{
                    result += (testCaseJsonObject.getString("ct") + " ");
                }
                result += testCaseJsonObject.getString("key");
                break;
            case AESEngine.MODE_CBC:
            case AESEngine.MODE_CFB8:
            case AESEngine.MODE_CFB128:
            case AESEngine.MODE_CTR:
                if(direction.equalsIgnoreCase("encrypt")){
                    result += (testCaseJsonObject.getString("pt") + " ");
                }else{
                    result += (testCaseJsonObject.getString("ct") + " ");
                }
                result += (testCaseJsonObject.getString("key") + " ");
                result += testCaseJsonObject.getString("iv");
                break;
        }
        return result;
    }


    public static void fillInHardwareTestOutput(String testId, JSONObject testCaseJsonObject, String outputtedXml,String testType){
        String contentXml = XmlUtils.getLabelValue(outputtedXml, testId);
        if (testType.toUpperCase().equals("MCT")){
            //parse MCT result
            JSONArray resultsArray = new JSONArray();
            for(int i=0;i<100;i++){
                if(XmlUtils.labelExists(contentXml,"round"+i)){
                    String roundXml = XmlUtils.getLabelValue(contentXml, "round"+i);
                    JSONObject roundResult = new JSONObject();
                    if(XmlUtils.labelExists(roundXml, "key")){
                        roundResult.put("key", XmlUtils.getLabelValue(roundXml, "key"));
                    }
                    if(XmlUtils.labelExists(roundXml, "iv")){
                        roundResult.put("iv", XmlUtils.getLabelValue(roundXml, "iv"));
                    }
                    if(XmlUtils.labelExists(roundXml, "pt")){
                        roundResult.put("pt", XmlUtils.getLabelValue(roundXml, "pt"));
                    }
                    if(XmlUtils.labelExists(roundXml, "ct")){
                        roundResult.put("ct", XmlUtils.getLabelValue(roundXml, "ct"));
                    }
                    resultsArray.put(roundResult);
                }
            }
            testCaseJsonObject.put("resultsArray", resultsArray);
        }else {
            //parse normal test result
            if(XmlUtils.labelExists(contentXml, "ct")){
                testCaseJsonObject.put("ct", XmlUtils.getLabelValue(contentXml, "ct"));
            }
            if(XmlUtils.labelExists(contentXml, "pt")){
                testCaseJsonObject.put("pt", XmlUtils.getLabelValue(contentXml, "pt"));
            }
        }
    }

    public static int getCipherModeCode(String cipherMode, int keyLength) {
        if (cipherMode.toUpperCase().contains(MODE_ECB)) {
            if(keyLength == 128) return 10;
            if(keyLength == 192) return 11;
            if(keyLength == 256) return 12;
        } else if (cipherMode.toUpperCase().contains(MODE_CBC)) {
            if(keyLength == 128) return 1;
            if(keyLength == 192) return 2;
            if(keyLength == 256) return 3;
        } else if (cipherMode.toUpperCase().contains(MODE_CFB8)) {
            if(keyLength == 128) return 4;
            if(keyLength == 192) return 5;
            if(keyLength == 256) return 6;
        } else if (cipherMode.toUpperCase().contains(MODE_CFB128)) {
            if(keyLength == 128) return 7;
            if(keyLength == 192) return 8;
            if(keyLength == 256) return 9;
        } else if (cipherMode.toUpperCase().contains(MODE_CTR)) {
            if(keyLength == 128) return 13;
            if(keyLength == 192) return 14;
            if(keyLength == 256) return 15;
        } else {
            throw new IllegalArgumentException("Unsupported cipher mode: " + cipherMode);
        }
        return -1;
    }

    public static void extractValidationTestCase(ValidationTestCase validationTestCase, JSONObject testCaseJsonObject, String direction, String aesMode, int keyLength, String testType){

        String textHexString = (direction.equalsIgnoreCase("encrypt")) ?
                testCaseJsonObject.getString("pt") : testCaseJsonObject.getString("ct");
        String keyHexString = testCaseJsonObject.getString("key");
        String ivHexString = (testCaseJsonObject.has("iv")) ?
                testCaseJsonObject.getString("iv") : "";

        if(direction.equalsIgnoreCase("encrypt")) {
            validationTestCase.getInputMap().put("pt", textHexString);
        }else{
            validationTestCase.getInputMap().put("ct", textHexString);
        }
        validationTestCase.getInputMap().put("key", keyHexString);

        if(testCaseJsonObject.has("iv")) validationTestCase.getInputMap().put("iv", ivHexString);

        int textByteLength = (textHexString.length()/2); //兩個字元代表一個byte，所以要除以2
        int keyByteLength = (keyHexString.length()/2);
        int ivByteLength = (ivHexString.length()/2);


        String resultHexString = (direction.equalsIgnoreCase("encrypt")) ?
                testCaseJsonObject.getString("ct") : testCaseJsonObject.getString("pt");

        //aes algorithm code = 1
        validationTestCase.getInputList().add("1");

        //cipher mode code
        int cipherModeCode = getCipherModeCode(aesMode, keyLength);
        validationTestCase.getInputList().add(String.valueOf(cipherModeCode));


        //key length in bytes
        if(aesMode.toUpperCase().contains(MODE_ECB)){
            validationTestCase.getInputList().add(String.valueOf(textByteLength+keyByteLength));
            validationTestCase.getInputList().add(keyHexString+textHexString);
        }else if(aesMode.toUpperCase().contains(MODE_CBC)){
            //如果不是ECB 要有IV
            validationTestCase.getInputList().add(String.valueOf(keyByteLength+textByteLength+ivByteLength));
            validationTestCase.getInputList().add(keyHexString+textHexString+ivHexString);
        }else if(aesMode.toUpperCase().contains(MODE_CTR)){
            validationTestCase.getInputList().add(String.valueOf(keyByteLength+textByteLength+ivByteLength));
            validationTestCase.getInputList().add(keyHexString+ivHexString+textHexString);
        }else{
            validationTestCase.getInputList().add(String.valueOf(keyByteLength+textByteLength+ivByteLength));
            validationTestCase.getInputList().add(keyHexString+ivHexString+textHexString);
        }

        //actual message
        validationTestCase.getExpectedOutputList().add( resultHexString);


        if(direction.equalsIgnoreCase("encrypt")) {
            validationTestCase.getExpectedOutputMap().put("ct", resultHexString);
        }else{
            validationTestCase.getExpectedOutputMap().put("pt", resultHexString);
        }

//        if(direction.equalsIgnoreCase("encrypt")){
//            validationTestCase.getInputs().add(textHexString+keyHexString);
////            validationTestCase.getInputs().add(keyHexString);
////            validationTestCase.getInputs().add(ivHexString);
//            validationTestCase.getExpectedOutput().add( resultHexString);
//        }else{
//            validationTestCase.getInputs().add(textHexString+keyHexString);
////            validationTestCase.getInputs().add();
////            validationTestCase.getInputs().add(ivHexString);
//            validationTestCase.getExpectedOutput().add( resultHexString);
//        }
    }


}
