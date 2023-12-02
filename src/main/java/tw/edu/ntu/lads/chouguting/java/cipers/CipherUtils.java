package tw.edu.ntu.lads.chouguting.java.cipers;

public class CipherUtils {
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

    public static byte[] hexStringToBytes(String hexString) {
        int len = hexString.length();
        byte[] bytes = new byte[len / 2]; // 2個hex char 代表 1 byte
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) // higher 4 bits
                    + Character.digit(hexString.charAt(i + 1), 16)); // lower 4 bits
        }
        return bytes;
    }


    public static byte[] byteArrayXor(byte[] bytes1, byte[] bytes2) {
        if (bytes1.length != bytes2.length) {
            throw new IllegalArgumentException("The length of two byte arrays must be the same.");
        }
        byte[] result = new byte[bytes1.length];
        for (int i = 0; i < bytes1.length; i++) {
            result[i] = (byte) (bytes1[i] ^ bytes2[i]);
        }
        return result;
    }

    public static byte[] getSingleByte(byte[] array, int location){
        byte[] result = new byte[1];
        result[0] = array[location];
        return result;
    }
}
