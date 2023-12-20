package ciphers

import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.prng.*
import org.bouncycastle.crypto.prng.drbg.HashSP800DRBG
import org.bouncycastle.util.encoders.Hex
import org.json.JSONObject
import tw.edu.ntu.lads.chouguting.java.cipers.CipherUtils


class DRBGEngine(
    var entropyInputHexString: String,
    var nonceHexString: String,
    var personalizationHexString: String
) {




    // 建立 DRBG
    lateinit var drbg: HashSP800DRBG
    lateinit var entropySource: FixedEntropySource //Entropy值得來源


    init {
        instantiateDrbg()
    }


    class FixedEntropySource(var data: ByteArray) : EntropySource {
        override fun isPredictionResistant(): Boolean = false

        override fun getEntropy(): ByteArray = data
        override fun entropySize(): Int = data.size * 8
    }

    private fun instantiateDrbg() {
        // 將 entropy 和 nonce 從 hex string 轉換為 byte array
        val entropyByteArray = CipherUtils.hexStringToBytes(entropyInputHexString)
        val nonceByteArray = CipherUtils.hexStringToBytes(nonceHexString)
        val personalizationByteArray = CipherUtils.hexStringToBytes(personalizationHexString)

        entropySource = FixedEntropySource(entropyByteArray)

        drbg = HashSP800DRBG(SHA256Digest(),entropyByteArray.size*8, entropySource, personalizationByteArray, nonceByteArray)
    }


    fun reseed(entropyInputHexString: String, additionalInputHexString: String) {
        val additionalInputByteArray = CipherUtils.hexStringToBytes(additionalInputHexString)
        entropySource.data = CipherUtils.hexStringToBytes(entropyInputHexString)
        drbg.reseed(additionalInputByteArray)
    }


    fun generateRandomNumber(returnedBitsLen: Int, entropyInputHexString: String, additionalInputHexString: String): String {

        val entropyInputByteArray = CipherUtils.hexStringToBytes(entropyInputHexString)
        val additionalInputByteArray = if(additionalInputHexString.isEmpty()) null else CipherUtils.hexStringToBytes(additionalInputHexString)

        entropySource.data = entropyInputByteArray

        val randomBytes = ByteArray(returnedBitsLen / 8) //1 byte = 8 bits
        drbg.generate(randomBytes, additionalInputByteArray, false)
        return CipherUtils.bytesToHexString(randomBytes)
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
                    val otherEntropyInputHexString = otherJsonObject.getString("entropyInput")
                    val additionalInputHexString = otherJsonObject.getString("additionalInput")
                    if (intendedUse.lowercase() == "reseed") {
                        drbgEngine.reseed(otherEntropyInputHexString, additionalInputHexString)
                    } else if (intendedUse.lowercase() == "generate") {
                        resultHexString = drbgEngine.generateRandomNumber(drbgReturnedBitsLen, otherEntropyInputHexString, additionalInputHexString)
                    }
                }
            }

                testCaseJsonObject.put("returnedbits", resultHexString)

        }


    }
}




fun main() {
    val entropyInput = Hex.decode("E8FD044146D3A49259FA33ED026FEF1EFA91BF2F5A688ADFC7BA1862EDE97502")
    val nonce = Hex.decode("FE3217672BD59C7FD980E4EEC4E0A718")
    val persoString = Hex.decode("44FD95") // Replace with your persoString

    val entropySource = FixedEntropySource2(entropyInput)
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


class FixedEntropySource2(var data: ByteArray) : EntropySource {
    override fun isPredictionResistant(): Boolean = false

    override fun getEntropy(): ByteArray = data
    override fun entropySize(): Int = data.size * 8
}