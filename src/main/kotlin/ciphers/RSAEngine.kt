package ciphers

import org.bouncycastle.crypto.generators.RSAKeyPairGenerator
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters
import org.bouncycastle.crypto.params.RSAKeyParameters
import org.json.JSONObject
import tw.edu.ntu.lads.chouguting.java.cipers.CipherUtils
import java.math.BigInteger
import java.security.*
import java.security.interfaces.RSAPrivateCrtKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPrivateKeySpec
import java.security.spec.RSAPublicKeySpec



class RSAEngine(val keyLength:Int)  {

    fun generateKeyPairWithE(eHexString:String): HashMap<String,String> {
        val keyPairMap = HashMap<String,String>()
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

        keyPairMap["n"] = publicKey.modulus.toString(16).uppercase()
        keyPairMap["e"] = publicKey.exponent.toString(16).uppercase()
        keyPairMap["p"] = privateKey.p.toString(16).uppercase()
        keyPairMap["q"] = privateKey.q.toString(16).uppercase()
        keyPairMap["d"] = privateKey.exponent.toString(16).uppercase()

        return keyPairMap
    }



    fun generateKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(this.keyLength)
        val keyPair = keyPairGenerator.generateKeyPair()
        return  keyPair
    }


    fun sign(messageHex: String, hashAlgorithm:String, nHex: String , dHex:String):String{
        // 獲取私鑰的模數和指數
        val keyFactory = KeyFactory.getInstance("RSA")
        val modulus = BigInteger(nHex, 16)  //n:模數
//        val publicExponent = BigInteger(eHex, 16) //e: 加密指數
        val privateExponent = BigInteger(dHex, 16) //d: 解密指數(簽章時用)

//        val publicKeySpec = RSAPublicKeySpec(modulus, publicExponent)
        val privateKeySpec = RSAPrivateKeySpec(modulus, privateExponent)

//        val publicKey = keyFactory.generatePublic(publicKeySpec)
        val privateKey = keyFactory.generatePrivate(privateKeySpec)


        // 計算訊息的 Hash
        val messageDigest = MessageDigest.getInstance(hashAlgorithm)
        val messageHash = messageDigest.digest(CipherUtils.hexStringToBytes(messageHex))

        // 使用私鑰簽章訊息摘要
        val signature = Signature.getInstance("NONEwithRSA")
        signature.initSign(privateKey)
        signature.update(messageHash)
        val digitalSignature = CipherUtils.bytesToHexString(signature.sign())
        return digitalSignature
    }

    fun verify(messageHex:String, signatureHex:String, hashAlgorithm:String, nHex:String, eHex:String):Boolean{
        // 這些值應該是你在產生簽章時獲得的
        val modulus = BigInteger(nHex,16)
        val exponent = BigInteger(eHex,16)
        val signatureByteArray = CipherUtils.hexStringToBytes(signatureHex)

        // 產生公鑰
        val keyFactory = KeyFactory.getInstance("RSA")
        val publicKeySpec = RSAPublicKeySpec(modulus, exponent)
        val publicKey = keyFactory.generatePublic(publicKeySpec)

        // 計算訊息的 SHA-256 摘要
        val messageDigest = MessageDigest.getInstance(hashAlgorithm)
        val messageHash = messageDigest.digest(CipherUtils.hexStringToBytes(messageHex))

        // 驗證簽章
        val signature = Signature.getInstance("NONEwithRSA")
        signature.initVerify(publicKey)
        signature.update(messageHash)
        val isSignatureValid = signature.verify(signatureByteArray)

        return isSignatureValid
    }







    companion object {
        fun runRsaWithTestCase(
            currentTestGroupJsonObject: JSONObject,
            testCaseJsonObject: JSONObject,
            rsaMode:String
        ) {
            val keyLength = currentTestGroupJsonObject.getInt("modulo")
            val rsaEngine = RSAEngine(keyLength)
            when(rsaMode){
                "keyGen" -> {

                    val publicEHexString = testCaseJsonObject.getString("e")
                    val resultMap = rsaEngine.generateKeyPairWithE(publicEHexString)
                    testCaseJsonObject.put("p", resultMap["p"])
                    testCaseJsonObject.put("q", resultMap["q"])
                    testCaseJsonObject.put("n", resultMap["n"])
                    testCaseJsonObject.put("d", resultMap["d"])
                    if(BigInteger(publicEHexString, 16) != BigInteger(resultMap["e"], 16)){
                        throw ArithmeticException("KeyGen Error: numbers are not right!!")
                    }

                }
                "sigGen" -> {
                    if(!currentTestGroupJsonObject.has("n")){
                        val keyPair = rsaEngine.generateKeyPair()
                        val keyMap= keyPair.toHexHashMap()
                        currentTestGroupJsonObject.put("n",keyMap["n"]!!)
                        currentTestGroupJsonObject.put("d",keyMap["d"]!!)
                        currentTestGroupJsonObject.put("e",keyMap["e"]!!)
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
                    val verifyResult = rsaEngine.verify(messageHex, signatureHex, hashAlgorithm, nHex, eHex)
                    testCaseJsonObject.put("testPassed", if(verifyResult) "true" else "false")
                }
            }



        }
    }


}

fun KeyPair.toHexHashMap():HashMap<String,String>{
    val keyPairMap = HashMap<String,String>()
    val keyPair = this
    val publicKey = keyPair.public as RSAPublicKey
    val privateKey = keyPair.private as RSAPrivateCrtKey

    val n = publicKey.modulus
    val e = publicKey.publicExponent

    val d = privateKey.privateExponent
    val p = privateKey.primeP
    val q = privateKey.primeQ

    keyPairMap["n"] = n.toString(16).uppercase()
    keyPairMap["e"] = e.toString(16).uppercase()
    keyPairMap["p"] = p.toString(16).uppercase()
    keyPairMap["q"] = q.toString(16).uppercase()
    keyPairMap["d"] = d.toString(16).uppercase()
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
    val rsaEngine = RSAEngine(1024)
    val keyPair = rsaEngine.generateKeyPair()
    val keyHashMap= keyPair.toHexHashMap()
    val n = keyHashMap["n"]!!
    val e = keyHashMap["e"]!!
    val d = keyHashMap["d"]!!
    val messageHex = "D50117104CFFFBA3B1DA762BE49DA680179536F248A3C840E0D5BDB4985419DD046040900CACD61939564AF9E089255570542ED7CF2557159223BC221935F9FBD357B40AF317D4456C06189FB85B649CF7D7E99250ED34B86A33A8E1F1421F52F6035444251D2C5AE67EC403DEC2493B05AE643987EFFF289CD9281F71BBC431"

    val signature = rsaEngine.sign(messageHex,"SHA-256", n, d)
    println("n: $n")
    println("e: $e")
    println("d: $d")
    println("signature: $signature")
    val verifyResult = rsaEngine.verify(messageHex, signature, "SHA-256", n, e)
    println("verify result: $verifyResult")
    val verifyResult2 = rsaEngine.verify(messageHex+"a5", signature, "SHA-256", n, e)
    println("verify result: $verifyResult2")




}


