package tw.edu.ntu.lads.chouguting.java.cipers;

public class KyberEngine {



    public native void keypair(byte[] publicKey, byte[] privateKey);

    public native void encapsulate(byte[] cipherText, byte[] sharedSecret, byte[] seed ,byte[] publicKey);

    public native void decapsulate( byte[] sharedSecret, byte[] cipherText, byte[] privateKey);


}
