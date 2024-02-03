package ciphers

import org.bouncycastle.crypto.generators.RSAKeyPairGenerator
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters
import org.bouncycastle.crypto.params.RSAKeyParameters
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.json.JSONObject
import tw.edu.ntu.lads.chouguting.java.cipers.CipherUtils
import java.math.BigInteger
import java.security.*
import java.security.interfaces.RSAPrivateCrtKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.MGF1ParameterSpec
import java.security.spec.PSSParameterSpec
import java.security.spec.RSAPrivateKeySpec
import java.security.spec.RSAPublicKeySpec
import kotlin.collections.HashMap


class RSAEngine(val keyLength: Int) {

    fun generateKeyPairWithE(eHexString: String): HashMap<String, String> {
        val keyPairMap = HashMap<String, String>()
        val publicExponent = BigInteger(eHexString, 16) // 預先設定的公鑰指數 e
        /*
      在 RSA 加密中，模數（Modulus）和鍵長（Key Length）是相同的概念。模數是 RSA 公鑰和私鑰的一部分，它是兩個質數的乘積。模數的位數（以二進制表示）就是 RSA 鍵的長度。
      例如，如果你有一個 2048 位的 RSA 鍵，那麼它的模數就是一個 2048 位長的數字。這個數字是兩個 1024 位的質數的乘積。
      所以，當你在生成 RSA 鍵對時設定模數（或鍵長），你實際上是在設定生成的公鑰和私鑰的大小。更大的鍵長會提供更強的安全性，但也會使加密和解密的過程更慢。*/
        val strength = this.keyLength // 鍵的長度
        val certainty = 100 // 質數的確定性

        val keyGenParams = RSAKeyGenerationParameters(publicExponent, SecureRandom(), strength, certainty)
        val generator = RSAKeyPairGenerator()
        generator.init(keyGenParams)

        val keyPair = generator.generateKeyPair()

        val publicKey = keyPair.public as RSAKeyParameters
        val privateKey = keyPair.private as RSAPrivateCrtKeyParameters

        keyPairMap["n"] = CipherUtils.bytesToHexString(publicKey.modulus.toByteArray())
        keyPairMap["e"] = CipherUtils.bytesToHexString(publicKey.exponent.toByteArray())
        keyPairMap["p"] = CipherUtils.bytesToHexString(privateKey.p.toByteArray())
        keyPairMap["q"] = CipherUtils.bytesToHexString(privateKey.q.toByteArray())
        keyPairMap["d"] = CipherUtils.bytesToHexString(privateKey.exponent.toByteArray())

        return keyPairMap
    }


    fun generateKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(this.keyLength)
        val keyPair = keyPairGenerator.generateKeyPair()
        return keyPair
    }


    fun sign(
        messageHex: String,
        hashAlgorithm: String,
        nHex: String,
        dHex: String,
        isPSSMode: Boolean = false
    ): String {
        // 加入BouncyCastleProvider
        Security.addProvider(BouncyCastleProvider())
        // 獲取私鑰的模數和指數
        val keyFactory = KeyFactory.getInstance("RSA")
        val modulus = BigInteger(nHex, 16)  //n:模數
        val privateExponent = BigInteger(dHex, 16) //d: 解密指數(簽章時用)

        val privateKeySpec = RSAPrivateKeySpec(modulus, privateExponent)

        val privateKey = keyFactory.generatePrivate(privateKeySpec)

        // 計算訊息的 Hash
        val messageBytes = CipherUtils.hexStringToBytes(messageHex)
        // 使用私鑰簽章訊息摘要
        val signature = hashAlgorithm.getSignatureInstance(isPSSMode)
        if (isPSSMode) {
            signature.setParameter(PSSParameterSpec(hashAlgorithm, "MGF1", MGF1ParameterSpec(hashAlgorithm), 0, 1))
        }
        signature.initSign(privateKey)
        signature.update(messageBytes)
        val digitalSignature = CipherUtils.bytesToHexString(signature.sign())
        return digitalSignature
    }

    fun verify(
        messageHex: String,
        signatureHex: String,
        hashAlgorithm: String,
        nHex: String,
        eHex: String,
        isPSSMode: Boolean = false
    ): Boolean {
        // 加入BouncyCastleProvider
        Security.addProvider(BouncyCastleProvider())

        // 從JSON中取得的公鑰參數
        val modulus = BigInteger(nHex, 16)
        val publicExponent = BigInteger(eHex, 16)

        // 建立公鑰
        val keyFactory = KeyFactory.getInstance("RSA", "BC")
        val publicKeySpec = RSAPublicKeySpec(modulus, publicExponent)
        val publicKey = keyFactory.generatePublic(publicKeySpec)

        // 從JSON中取得的訊息和簽名
        val messageBytes = CipherUtils.hexStringToBytes(messageHex)
        val signatureBytes = CipherUtils.hexStringToBytes(signatureHex)

        // 驗證簽名
        var signature = hashAlgorithm.getSignatureInstance(isPSSMode)
        //if PSS mode
        if (isPSSMode) {
            signature.setParameter(PSSParameterSpec(hashAlgorithm, "MGF1", MGF1ParameterSpec(hashAlgorithm), 0, 1))
        }
        signature.initVerify(publicKey)
        signature.update(messageBytes)
        val isSignatureValid = signature.verify(signatureBytes)

        return isSignatureValid
    }

    fun String.getSignatureInstance(isPSSmode: Boolean = false): Signature {
        var instance = when (this) {
            "SHA-1" -> Signature.getInstance("SHA1withRSA", "BC")
            "SHA-224" -> Signature.getInstance("SHA224withRSA", "BC")
            "SHA-256" -> Signature.getInstance("SHA256withRSA", "BC")
            "SHA-384" -> Signature.getInstance("SHA384withRSA", "BC")
            "SHA-512" -> Signature.getInstance("SHA512withRSA", "BC")
            "SHA3-224" -> Signature.getInstance("SHA3-224withRSA", "BC")
            "SHA3-256" -> Signature.getInstance("SHA3-256withRSA", "BC")
            "SHA3-384" -> Signature.getInstance("SHA3-384withRSA", "BC")
            "SHA3-512" -> Signature.getInstance("SHA3-512withRSA", "BC")
            else -> {
                throw IllegalArgumentException("hash function:$this not supported yet!!!")
            }
        }
        if (isPSSmode) {
            instance = when (this) {
                "SHA-1" -> Signature.getInstance("SHA1withRSA/PSS", "BC")
                "SHA-224" -> Signature.getInstance("SHA224withRSA/PSS", "BC")
                "SHA-256" -> Signature.getInstance("SHA256withRSA/PSS", "BC")
                "SHA-384" -> Signature.getInstance("SHA384withRSA/PSS", "BC")
                "SHA-512" -> Signature.getInstance("SHA512withRSA/PSS", "BC")
                "SHA3-224" -> Signature.getInstance("SHA3-224withRSA/PSS", "BC")
                "SHA3-256" -> Signature.getInstance("SHA3-256withRSA/PSS", "BC")
                "SHA3-384" -> Signature.getInstance("SHA3-384withRSA/PSS", "BC")
                "SHA3-512" -> Signature.getInstance("SHA3-512withRSA/PSS", "BC")
                else -> {
                    throw IllegalArgumentException("hash function:$this not supported yet!!!")
                }
            }
        }
        return instance
    }

    companion object {
        fun runRsaWithTestCase(
            currentTestGroupJsonObject: JSONObject,
            testCaseJsonObject: JSONObject,
            rsaMode: String
        ) {
            val keyLength = currentTestGroupJsonObject.getInt("modulo")
            val rsaEngine = RSAEngine(keyLength)
            when (rsaMode) {
                "keyGen" -> {
                    val publicEHexString = testCaseJsonObject.getString("e")
                    val resultMap = rsaEngine.generateKeyPairWithE(publicEHexString)
                    testCaseJsonObject.put("p", resultMap["p"])
                    testCaseJsonObject.put("q", resultMap["q"])
                    testCaseJsonObject.put("n", resultMap["n"])
                    testCaseJsonObject.put("d", resultMap["d"])
                    if (BigInteger(publicEHexString, 16) != BigInteger(resultMap["e"], 16)) {
                        throw ArithmeticException("KeyGen Error: numbers are not right!!")
                    }
                }

                "sigGen" -> {
                    if (!currentTestGroupJsonObject.has("n")) {
                        val keyPair = rsaEngine.generateKeyPair()
                        val keyMap = keyPair.toHexHashMap()
                        currentTestGroupJsonObject.put("n", keyMap["n"]!!)
                        currentTestGroupJsonObject.put("d", keyMap["d"]!!)
                        currentTestGroupJsonObject.put("e", keyMap["e"]!!)
//                        println("new key generated")
                    }
                    val nHex = currentTestGroupJsonObject.getString("n")
                    val dHex = currentTestGroupJsonObject.getString("d")
                    val messageHex = testCaseJsonObject.getString("message")
                    val hashAlgorithm = currentTestGroupJsonObject.getString("hashAlg").fixHashAlgorithmString()
                    val signatureHex = rsaEngine.sign(messageHex, hashAlgorithm, nHex, dHex)
                    testCaseJsonObject.put("signature", signatureHex)
                }

                "sigVer" -> {
                    val nHex = currentTestGroupJsonObject.getString("n")
                    val eHex = currentTestGroupJsonObject.getString("e")
                    val messageHex = testCaseJsonObject.getString("message")
                    val signatureHex = testCaseJsonObject.getString("signature")
                    val hashAlgorithm = currentTestGroupJsonObject.getString("hashAlg").fixHashAlgorithmString()
                    val isPSSmode = currentTestGroupJsonObject.getString("sigType") == "pss"
                    val verifyResult = rsaEngine.verify(messageHex, signatureHex, hashAlgorithm, nHex, eHex, isPSSmode)
                    testCaseJsonObject.put("testPassed", if (verifyResult) "true" else "false")
                }
            }
        }
    }


}

fun KeyPair.toHexHashMap(): HashMap<String, String> {
    val keyPairMap = HashMap<String, String>()
    val keyPair = this
    val publicKey = keyPair.public as RSAPublicKey
    val privateKey = keyPair.private as RSAPrivateCrtKey

    val n = publicKey.modulus
    val e = publicKey.publicExponent

    val d = privateKey.privateExponent
    val p = privateKey.primeP
    val q = privateKey.primeQ

    keyPairMap["n"] = CipherUtils.bytesToHexString(n.toByteArray())
    keyPairMap["e"] = CipherUtils.bytesToHexString(e.toByteArray())
    keyPairMap["p"] = CipherUtils.bytesToHexString(p.toByteArray())
    keyPairMap["q"] = CipherUtils.bytesToHexString(q.toByteArray())
    keyPairMap["d"] = CipherUtils.bytesToHexString(d.toByteArray())
    return keyPairMap
}

fun String.fixHashAlgorithmString(): String {
    var hashAlgorithmString = this

    //fix the hash algorithm string to match the one in BC
    if (hashAlgorithmString.contains("SHAKE-")) {
        hashAlgorithmString = hashAlgorithmString.replace("SHAKE-", "SHAKE")
    } else if (hashAlgorithmString.contains("SHA2-")) {
        hashAlgorithmString = hashAlgorithmString.replace("SHA2-", "SHA-")
    }
    return hashAlgorithmString
}


fun main() {
    val rsaEngine = RSAEngine(2048)
    val keyPair = rsaEngine.generateKeyPair()
    val keyHashMap = keyPair.toHexHashMap()
    val n = keyHashMap["n"]!!
    val e = keyHashMap["e"]!!
    val d = keyHashMap["d"]!!
    val messageHex =
        "924F36A50B41DB1BD15B3AEEB1E7496239379DDF8C757084BFF1F380DDF918C6288B4FA02390F3FC91272E19089577A71CDA87ACFC90A1E2EF5A35FE3D6D43FF744384298E653DC1E3996F2E585FDEF4EF31ED21F98D09ADB4DE066151D69601558679E78A67D80AFC8039053D55C1F5BC86D5D36E9986EE2A0F90677039411C"

    val signature = rsaEngine.sign(messageHex, "SHA-256", n, d)
    println("n: $n")
    println("e: $e")
    println("d: $d")
    println("signature: $signature")

    val verifyResult2 = rsaEngine.verify(messageHex, signature, "SHA-256", n, e)
    println("verify result: $verifyResult2")


}


//fun main() {
//
//
//    // 從JSON中取得的公鑰參數
//    val nHex = "E20F4B1E54671F2DB790FBA2B7A61D0627C70590ABE826AC7A941199F871E4B7B475D64AF9FA4AE2591399A6343FED4ACC32E862698DF42455CAE7841BAD191A2A5A1E47DB0EB7D8BA090CE30B705A5F7AF5FBE1CA36958E374F97919F3EC9B23CF65ABF4E821342E46CE7E5E56089F8C955819CD154AA0CD505D4F8E02F4C402017AA6DD1FC9D4F5E36F3551A197A70292BD18E658B0884FEF467D4B15AE5498CE44639467EF7FD2D4307415648D3B8EC2F95A90619BAF0B85799C941E01CD2DCA3BE5C9FA885429872F7B4B4233EB2613863DCD2B73B3F63D9240623514A09013912CAECADB9AE240394753C9ADEF11E2EC771E93FD3A5AF44590F3FFA7679"
//    val eHex = "10714EF10A2F53"
//
//    // 從JSON中取得的訊息和簽名
//    val messageHex = "1E9B885FE49A41E350CF2F568DCEBF0603AB7F1FBBF4BCB08E1D589638B449DEADD5B7D31A559EC3190B52A178E0FC833BF73FCE256694D56FA2B5825E5BEFA371A7378951FAFD4379A5864FCA2B53439FB6F12757BA86E45F09CE189818689F5A1DCDCB249C3A2C1F9688AC4AF3DF3DBA4E9AC7FA49C03E725B268B271CD902"
//    val signatureHex = "A8BF23A73C40298561E600FFB181ABE9DCBC42C7676D130C3B9CF671C0AF7BD04059E547867B6DDABC91CA1EE6B901CFAC5E15A0EE8FE840655AA9C430D38FA29F424D6D62AC51EF831E72EF7F75C2634FED97D2E2704169EAB400CBDBDBE461AE8B08364BAEB26CA1D5BAAAED3A0C07B5A0FF84705254992085CE3DBA171956794354D11A6FA536302098D354097EFE7195F07B845905747CFA7A69E349386B5BF9AD7ED19AD6D0163E66154CF7D885F4B99DF9719C8F8A66991208D417A62129EF09300351CA3FFCBFF090C764114917F4E285D19EF964946E017393371D04A46BEDEF7894B427DFB3A847CA55F019CFB3175BEC7E666AE2D86A88F1354CEC"
//    val hashAlgo = "SHA-1"
//    // 驗證簽名
//    val isVerified = RSAEngine(2048).verify(messageHex, signatureHex,hashAlgo,nHex,eHex)
//
//    println("簽名驗證結果: $isVerified")
//}

//fun main() {
//    Security.addProvider(BouncyCastleProvider())
//
//    val n = BigInteger("B52B08008B855EF024FFAF4C7E41A79FA80DD119672BE064F2EF11E8EB8ED31CAA28E67318FA03534F470D0713AFD5280A8E2E563BF3E9DEFE4ADA150299A03C16B1EC73E023EA482F3C4C4F04253335D08B856BAF8A5FE43CCA8E954B5F52D3C0CD9960EE3D5445C32073AF263B90D605FF348BDD2786B111321B7A83FE3529FF4EB42388A9E7AA9EB188F0A1E6702B9EC02054779CB33C6706EDF4710DF19B974DE6A23A3A62B45C2702FE9EA97F519909CCAF1D732F8015294C8EC57C9A4C32F2E118AC31F4B68E0C0793A008E8C783B80B0CFF2034BC0D814862CBCB6072A6720CB2A96B773ACD6A6CF6204D0F2FE288F31B04A5DB0265E5F8031626A5FB", 16)
//    val e = BigInteger("029CA860BE0BE7", 16)
//    val publicKeySpec = RSAPublicKeySpec(n, e)
//    val keyFactory = KeyFactory.getInstance("RSA", "BC")
//    val publicKey = keyFactory.generatePublic(publicKeySpec)
//
//    val signature = Signature.getInstance("SHA256withRSA/PSS", "BC")
//    signature.setParameter(PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec("SHA-256"), 0, 1))
//    signature.initVerify(publicKey)
//
//    val message = CipherUtils.hexStringToBytes("D33BC9E94BD9AA670B7A62ACB0EF7179784487617E5C72B19BE7D9EE57144A5CDC0F54B3228B35DAC405B56B8DFBF4DF00377F017D579B28EFEB7580B46E6DC1E6F0A8A5CF71B0F74604B91984629C11688D5CC4D5304C84B42325D3837BDF7D1F3ACE2A0D5C6D5CFC5532C9DB4F4C14BEF9FC94EB8CFBB41AB5023CCEAD4D78")
//    signature.update(message)
//
//    val sigBytes = CipherUtils.hexStringToBytes("6EA5EF449005CA257AD4F8BD0AF73351FC27754B493F5F7E9C3F6958EC1F30E54AE028DD9D77429EE79EA5652808874FE3505614F62C7F50E585A297F36A5FBDFCE121CFEA98F7BD6FA7D439B342E84A997E55A4DFBF92B729C11E6BC9FD93EA5553034E14DD0B220C3B707998E23153609B3B742B6FB2E8A3D945CF40D07BE2BB483F3A3C8A7CC40E4A1AD9CB05FC7777398E2013FBAB7C9CF1A4741A3CD1423904787E83C5B2C85E8EC6EDFD2CD5F53DF753464A8E20DD0DCA1A81CAE68F355329D4E6EA52642EC98A66A55FAD92F404B65CB9BF962B0ED0789F073C75EBE80F71B3F0EC40A14BCBF6AA2FD44D3F383CA7F81673583F927AA699643956BCB1")
//    val isVerified = signature.verify(sigBytes)
//
//    println("Signature verification: $isVerified")
//}