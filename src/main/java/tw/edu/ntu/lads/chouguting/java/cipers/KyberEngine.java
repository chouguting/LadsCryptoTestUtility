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
    public final int KYBER_SYMBYTES = 32; /* size in bytes of hashes, and seeds */
    public final int KYBER_SSBYTES = 32; /* size in bytes of shared key */
    private final int KYBER_POLYBYTES = 384;
    final int KYBER_POLYVECBYTES = this.kyberAlg.getK() * KYBER_POLYBYTES;


    private final int KYBER_POLYVECCOMPRESSEDBYTES;
    private final int KYBER_POLYCOMPRESSEDBYTES;



    public final int KYBER_PUBLICKEYBYTES = KYBER_POLYVECBYTES + KYBER_SYMBYTES;
    public final int KYBER_PRIVATEKEYBYTES = KYBER_POLYVECBYTES+KYBER_PUBLICKEYBYTES+ 2*KYBER_SYMBYTES;
    public final int KYBER_CIPHERTEXTBYTES;

    public final int KYBER_SEEDBYTES = KYBER_SYMBYTES*2;


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
        KYBER_CIPHERTEXTBYTES = KYBER_POLYVECCOMPRESSEDBYTES+ KYBER_POLYCOMPRESSEDBYTES;
    }


    static {
        System.loadLibrary("app/resources/kyber");
    }
    public native void keypair(byte[] publicKey, byte[] privateKey);

    public native void encapsulate(byte[] cipherText, byte[] sharedSecret, byte[] seed ,byte[] publicKey);

    public native void decapsulate( byte[] sharedSecret, byte[] cipherText, byte[] privateKey);

    public static void main(String[] args) {
        KyberEngine kyberEngine = new KyberEngine();
        byte[] publicKey = new byte[kyberEngine.KYBER_PUBLICKEYBYTES];
        byte[] privateKey = new byte[kyberEngine.KYBER_PRIVATEKEYBYTES];
        byte[] cipherText = new byte[kyberEngine.KYBER_CIPHERTEXTBYTES];
        byte[] seed = new byte[kyberEngine.KYBER_SYMBYTES];
        byte[] sharedSecretA = new byte[kyberEngine.KYBER_SSBYTES];
        byte[] sharedSecretB = new byte[kyberEngine.KYBER_SSBYTES];

        System.out.println("====================\ninit\n====================");
        System.out.println("public key length: "+kyberEngine.KYBER_PUBLICKEYBYTES);
        System.out.println("private key length: "+kyberEngine.KYBER_PRIVATEKEYBYTES);
        System.out.println("cipher text length: "+kyberEngine.KYBER_CIPHERTEXTBYTES);
        System.out.println("public key: "+CipherUtils.bytesToHexString(publicKey));
        System.out.println("private key: "+CipherUtils.bytesToHexString(privateKey));
        System.out.println();

        kyberEngine.keypair(publicKey, privateKey);
        System.out.println("====================\nkey pair generated\n====================");
        System.out.println("public key: "+CipherUtils.bytesToHexString(publicKey));
        System.out.println("private key: "+CipherUtils.bytesToHexString(privateKey));
        System.out.println();

        for(int h=0;h<2;h++){
            CipherUtils.copyByteArray(seed, CipherUtils.hexStringToBytes("1E"));
            kyberEngine.encapsulate(cipherText, sharedSecretA, seed, publicKey);
            System.out.println("====================\nencapsulated\n====================");
            System.out.println("cipher text: "+CipherUtils.bytesToHexString(cipherText));
            System.out.println("shared secretA: "+CipherUtils.bytesToHexString(sharedSecretA));
            System.out.println("public key: "+CipherUtils.bytesToHexString(publicKey));
            System.out.println("seed: "+CipherUtils.bytesToHexString(seed));
            System.out.println();
        }


        kyberEngine.decapsulate(sharedSecretB, cipherText, privateKey);
        System.out.println("====================\ndecapsulated\n====================");
        System.out.println("shared secretB: "+CipherUtils.bytesToHexString(sharedSecretB));
        System.out.println("shared secretA: "+CipherUtils.bytesToHexString(sharedSecretA));
        System.out.println();

    }

}
