package tw.edu.ntu.lads.chouguting.java.cipers;


public class KyberEngine {

    enum KyberAlg {
        Kyber512(2),
        Kyber768(3),
        Kyber1024(4);
        private final int k;

        KyberAlg(int k) {
            this.k = k;
        }

        public int getK() {
            return k;
        }
    }

    final KyberAlg kyberAlg = KyberAlg.Kyber512;

    private final int KYBER_N = 256;
    private final int KYBER_Q = 3329;
    private final int KYBER_SYMBYTES = 32; /* size in bytes of hashes, and seeds */
    private final int KYBER_SSBYTES = 32; /* size in bytes of shared key */
    private final int KYBER_POLYBYTES = 384;
    final int KYBER_POLYVECBYTES = this.kyberAlg.getK() * KYBER_POLYBYTES;


    private final int KYBER_POLYVECCOMPRESSEDBYTES;
    private final int KYBER_POLYCOMPRESSEDBYTES;



    public final int KYBER_KEM_PUBLICKEYBYTES = KYBER_POLYVECBYTES + KYBER_SYMBYTES;
    public final int KYBER_KEM_PRIVATEKEYBYTES = KYBER_POLYVECBYTES+ KYBER_KEM_PUBLICKEYBYTES + 2*KYBER_SYMBYTES;
    public final int KYBER_KEM_CIPHERTEXTBYTES;

    public final int KYBER_KEM_SHAREDSECRETBYTES = KYBER_SSBYTES;

    public final int KYBER_KEM_SEEDBYTES = KYBER_SYMBYTES*2;

    public final int KYBER_PKE_PLAINTEXTBYTES = KYBER_SYMBYTES;
    public final int KYBER_PKE_PUBLICKEYBYTES = KYBER_POLYVECBYTES + KYBER_SYMBYTES;
    public final int KYBER_PKE_PRIVATEKEYBYTES = KYBER_POLYVECBYTES;

    public final int KYBER_PKE_CIPHERTEXTBYTES;

    public final int KYBER_PKE_SEEDBYTES = KYBER_SYMBYTES;


    public KyberEngine() {
        if(this.kyberAlg == KyberAlg.Kyber512){
            KYBER_POLYVECCOMPRESSEDBYTES = 128;
            KYBER_POLYCOMPRESSEDBYTES = this.kyberAlg.getK() * 320;
        }else if(this.kyberAlg == KyberAlg.Kyber768){
            KYBER_POLYVECCOMPRESSEDBYTES = 128;
            KYBER_POLYCOMPRESSEDBYTES = this.kyberAlg.getK() * 320;
        }else {
            KYBER_POLYVECCOMPRESSEDBYTES = 160;
            KYBER_POLYCOMPRESSEDBYTES = this.kyberAlg.getK() * 352;
        }
        KYBER_KEM_CIPHERTEXTBYTES = KYBER_POLYVECCOMPRESSEDBYTES+ KYBER_POLYCOMPRESSEDBYTES;
        KYBER_PKE_CIPHERTEXTBYTES = KYBER_POLYVECCOMPRESSEDBYTES + KYBER_POLYCOMPRESSEDBYTES;
    }


    //Array generator
    public byte[] getNewKemPublicKeyByteArray() {
        return new byte[KYBER_KEM_PUBLICKEYBYTES];
    }

    public byte[] getNewKemPrivateKeyByteArray() {
        return new byte[KYBER_KEM_PRIVATEKEYBYTES];
    }

    public byte[] getNewKemCipherTextByteArray() {
        return new byte[KYBER_KEM_CIPHERTEXTBYTES];
    }

    public byte[] getNewKemSharedSecretByteArray() {
        return new byte[KYBER_KEM_SHAREDSECRETBYTES];
    }

    public byte[] getNewKemSeedByteArray() {
        return new byte[KYBER_KEM_SEEDBYTES];
    }

    public byte[] getNewPkePublicKeyByteArray() {
        return new byte[KYBER_PKE_PUBLICKEYBYTES];
    }

    public byte[] getNewPkePrivateKeyByteArray() {
        return new byte[KYBER_PKE_PRIVATEKEYBYTES];
    }

    public byte[] getNewPkeCipherTextByteArray() {
        return new byte[KYBER_PKE_CIPHERTEXTBYTES];
    }

    public byte[] getNewPkeSeedByteArray() {
        return new byte[KYBER_PKE_SEEDBYTES];
    }

    public byte[] getNewPkePlainTextByteArray() {
        return new byte[KYBER_PKE_PLAINTEXTBYTES];
    }


    static {
        System.loadLibrary("app/resources/kyber");
    }
    public native void kemKeypair(byte[] publicKey, byte[] privateKey);

    public native void kemEncapsulate(byte[] cipherText, byte[] sharedSecret, byte[] seed ,byte[] publicKey);

    public native void kemDecapsulate( byte[] sharedSecret, byte[] cipherText, byte[] privateKey);

    public native void pkeKeypair( byte[] publicKey, byte[] privateKey);
    public native void pkeEncrypt( byte[] cipherText, byte[] plainText, byte[] seed ,byte[] publicKey);
    public native void pkeDecrypt( byte[] plainText, byte[] cipherText, byte[] privateKey);

    public static void main(String[] args) {
        KyberEngine kyberEngine = new KyberEngine();
        byte[] publicKey = new byte[kyberEngine.KYBER_KEM_PUBLICKEYBYTES];
        byte[] privateKey = new byte[kyberEngine.KYBER_KEM_PRIVATEKEYBYTES];
        byte[] cipherText = new byte[kyberEngine.KYBER_KEM_CIPHERTEXTBYTES];
        byte[] seed = new byte[kyberEngine.KYBER_KEM_SEEDBYTES];
        byte[] sharedSecretA = new byte[kyberEngine.KYBER_KEM_SHAREDSECRETBYTES];
        byte[] sharedSecretB = new byte[kyberEngine.KYBER_KEM_SHAREDSECRETBYTES];

        System.out.println("====================\ninit\n====================");
        System.out.println("public key length: "+kyberEngine.KYBER_KEM_PUBLICKEYBYTES);
        System.out.println("private key length: "+kyberEngine.KYBER_KEM_PRIVATEKEYBYTES);
        System.out.println("cipher text length: "+kyberEngine.KYBER_KEM_CIPHERTEXTBYTES);
        System.out.println("public key: "+CipherUtils.bytesToHexString(publicKey));
        System.out.println("private key: "+CipherUtils.bytesToHexString(privateKey));
        System.out.println();

        kyberEngine.kemKeypair(publicKey, privateKey);
        System.out.println("====================\nkey pair generated\n====================");
        System.out.println("public key: "+CipherUtils.bytesToHexString(publicKey));
        System.out.println("private key: "+CipherUtils.bytesToHexString(privateKey));
        System.out.println();

        for(int h=0;h<200;h++){
            CipherUtils.copyByteArray(seed, CipherUtils.hexStringToBytes("1E"));
            kyberEngine.kemEncapsulate(cipherText, sharedSecretA, seed, publicKey);
            System.out.println("====================\nencapsulated\n====================");
            System.out.println("cipher text: "+CipherUtils.bytesToHexString(cipherText));
            System.out.println("shared secretA: "+CipherUtils.bytesToHexString(sharedSecretA));
            System.out.println("public key: "+CipherUtils.bytesToHexString(publicKey));
            System.out.println("seed: "+CipherUtils.bytesToHexString(seed));
            System.out.println();
        }


        kyberEngine.kemDecapsulate(sharedSecretB, cipherText, privateKey);
        System.out.println("====================\ndecapsulated\n====================");
        System.out.println("shared secretB: "+CipherUtils.bytesToHexString(sharedSecretB));
        System.out.println("shared secretA: "+CipherUtils.bytesToHexString(sharedSecretA));
        System.out.println();

    }

}
