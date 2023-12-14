package ciphers

import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.prng.*
import org.bouncycastle.crypto.prng.drbg.HashSP800DRBG
import org.bouncycastle.util.encoders.Hex
import org.json.JSONObject
import tw.edu.ntu.lads.chouguting.java.cipers.CipherUtils
import java.util.*


class DRBGEngine(
    var entropyInputHexString: String,
    var nonceHexString: String,
    var personalizationHexString: String
) {

    //固定式 entropy source provider
    class FixedEntropySourceProvider(private val entropy: ByteArray) : EntropySourceProvider {

        override fun get(entropySizeInBits: Int): EntropySource {
            return FixedEntropySource(entropy)
        }

        private class FixedEntropySource(private val data: ByteArray) : EntropySource {
            override fun getEntropy(): ByteArray {
                return data
            }

            override fun entropySize(): Int {
                return data.size * 8  //1 byte = 8 bits
            }

            override fun isPredictionResistant(): Boolean {
                return false
            }


        }
    }


    // 建立 DRBG
    lateinit var drbg: SP800SecureRandom


    init {
        instantiateDrbg()
    }

    private fun instantiateDrbg() {
        // 將 entropy 和 nonce 從 hex string 轉換為 byte array
        val entropyByteArray = CipherUtils.hexStringToBytes(entropyInputHexString)
        val nonceByteArray = CipherUtils.hexStringToBytes(nonceHexString)
        val personalizationByteArray = CipherUtils.hexStringToBytes(personalizationHexString)

        // 建立 entropy source provider
        val entropySourceProvider = FixedEntropySourceProvider(entropyByteArray)

        // 利用 entropy source provider 建立 SP800SecureRandomBuilder
        val builder = SP800SecureRandomBuilder(entropySourceProvider)

        // 設定 personalization string
        builder.setPersonalizationString(personalizationByteArray)


        drbg = builder.buildHash(SHA256Digest(), nonceByteArray, false)
    }


    fun reseed(entropyInputHexString: String) {
        val entropyByteArray = CipherUtils.hexStringToBytes(entropyInputHexString)
        drbg.reseed(entropyByteArray)
    }


    fun generateRandomNumber(returnedBitsLen: Int): String {
        val outputByteLength = returnedBitsLen / 8 //1 byte = 8 bits
        val random = ByteArray(outputByteLength)
//        drbg.nextBytes(random) //cavp test 的文件說要生成兩次，第一次的結果不要用
        drbg.nextBytes(random)
        val randomHexString = CipherUtils.bytesToHexString(random)
        return randomHexString
    }


    companion object {
        fun runDrbgWithTestCase(testCaseJsonObject: JSONObject, drbgReturnedBitsLen: Int) {
            val entropyInputHexString = testCaseJsonObject.getString("entropyInput")
            val nonceHexString = testCaseJsonObject.getString("nonce")
            val personalizationHexString = testCaseJsonObject.getString("persoString")
            val drbgEngine = DRBGEngine(entropyInputHexString, nonceHexString, personalizationHexString)
//            testCaseJsonObject.put("returnedbits", drbgEngine.generateRandomNumber(drbgReturnedBitsLen))
            var resultHexString = ""
            //addtional tasks
            if (testCaseJsonObject.has("otherInput")) {
                val otherJsonArray = testCaseJsonObject.getJSONArray("otherInput")
                for (i in 0 until otherJsonArray.length()) {
                    val otherJsonObject = otherJsonArray.getJSONObject(i)
                    val intendedUse = otherJsonObject.getString("intendedUse")
                    if (intendedUse.lowercase() == "reseed") {
                        val reseedEntropyInputHexString = otherJsonObject.getString("entropyInput")
                        drbgEngine.reseed(reseedEntropyInputHexString)
                    } else if (intendedUse.lowercase() == "generate") {
//                        val returnedBitsHexString = drbgEngine.generateRandomNumber(drbgReturnedBitsLen)
                        resultHexString = drbgEngine.generateRandomNumber(drbgReturnedBitsLen)
//                        otherJsonObject.put("returnedBits", returnedBitsHexString)
                    }
                }
            }

                testCaseJsonObject.put("returnedbits", resultHexString)

        }


    }
}


//fun main() {
//    // 將 entropy 和 nonce 從 hex string 轉換為 byte array
//    val entropy = Base64.getDecoder().decode("0DE588E9341E0AE225E16D7A06C6F197C862AAEF19BCB5EC1548F68620948D58")
//    val nonce = Base64.getDecoder().decode("EC8696027779A54074556DE4CF8653FE")
//
//    // 建立 entropy source provider
//    val entropySourceProvider = DRBGEngine.FixedEntropySourceProvider(entropy)
//
//    // 建立 DRBG
//    val builder = SP800SecureRandomBuilder(entropySourceProvider)
//    builder.setPersonalizationString("personalization".toByteArray())
//    val drbg: SP800SecureRandom = builder.buildHash(SHA256Digest(), nonce, false)
//
//
//    // 產生隨機數字
//    val random = ByteArray(16)
//    drbg.nextBytes(random)
//
//
//    // 輸出隨機數字
//    println(Base64.getEncoder().encodeToString(random))
//}


fun main() {
    val entropyInput = Hex.decode("E8FD044146D3A49259FA33ED026FEF1EFA91BF2F5A688ADFC7BA1862EDE97502")
    val nonce = Hex.decode("FE3217672BD59C7FD980E4EEC4E0A718")
    val persoString = Hex.decode("44FD95") // Replace with your persoString

    val entropySource = FixedEntropySource(entropyInput)
    val hashDrbg = HashSP800DRBG(SHA256Digest(),entropyInput.size*8, entropySource, persoString, nonce)

    // Reseed
    val additionalInput = Hex.decode("")
    entropySource.data = Hex.decode("6B3E3D8B62F827BD6171CFB6B56339F1E3D1D83AE212572689663A6D91060329")
    hashDrbg.reseed(additionalInput)


    // Generate random bits without printing
    entropySource.data = Hex.decode("")
    val firstRandomBits = ByteArray(256 / 8)
    hashDrbg.generate(firstRandomBits, null, false)
//    println(Hex.toHexString(firstRandomBits))

    // Generate random bits and print
    val secondRandomBits = ByteArray(256 / 8)
    hashDrbg.generate(secondRandomBits, null, false)
    println(Hex.toHexString(secondRandomBits))
}


class FixedEntropySource(var data: ByteArray) : EntropySource {
    override fun isPredictionResistant(): Boolean = false

    override fun getEntropy(): ByteArray = data
    override fun entropySize(): Int = data.size * 8
}