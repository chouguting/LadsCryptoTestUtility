package ciphers

import org.json.JSONArray
import org.json.JSONObject
import org.kotlincrypto.hash.sha3.SHAKE128
import org.kotlincrypto.hash.sha3.SHAKE256
import tw.edu.ntu.lads.chouguting.java.cipers.CipherUtils
import tw.edu.ntu.lads.chouguting.java.cipers.XmlUtils
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor

class SHAKEEngine() {

    private var shakeMode: String = MODE_SHAKE_128

    companion object {
        const val MODE_SHAKE_128 = "SHAKE_128"
        const val MODE_SHAKE_256 = "SHAKE_256"

        fun runSHAKEWithTestCase(
            testCaseJsonObject: JSONObject,
            shakeMode: String,
            testType: String,
            mctEnabled: Boolean,
            shakeMaxOutLen: Int,
            shakeMinOutLen: Int
        ) {

            val shakeEngine = SHAKEEngine(shakeMode)
            val messageHexString = testCaseJsonObject.getString("msg")

            if (testType.lowercase().contains("mct") && mctEnabled) { //Monte Carlo Test
                shakeEngine.doMCTHash(testCaseJsonObject,messageHexString, shakeMaxOutLen, shakeMinOutLen)
                return
            }else if(testType.lowercase().contains("mct")){
                return
            }
            val shakeOutLength = testCaseJsonObject.getInt("outLen")   //想要幾個bit
            val shakeOutByteLength = shakeOutLength / 8        //1個byte = 8個bit
            val digestHexString: String = shakeEngine.hash(messageHexString, shakeOutByteLength)
            testCaseJsonObject.put("md", digestHexString)
        }

        fun getHardwareTestInput(testId:String, testCaseJsonObject:JSONObject, algorithmName:String, testType:String, shakeMaxOutLen:Int, shakeMinOutLen:Int):ArrayList<String>{
            val shakeEngine = SHAKEEngine(algorithmName)
            val result = ArrayList<String>()
            var currentLine = "$testId "
            currentLine += "4 "  //SHAKE
            if(testType.lowercase().contains("mct")){
                currentLine += "3 " //mct test
            }else if(testType.lowercase().contains("vot")){
                currentLine += "2 " //vot test
            }else{
                currentLine += "1 " //aft test
            }

            if(shakeEngine.shakeMode == MODE_SHAKE_128){
                currentLine += "128 "
            }else{
                currentLine += "256 "
            }

            result.add(currentLine)
            result.add(testCaseJsonObject.getString("msg"))

            if(testType.lowercase().contains("vot")) {
                result.add(testCaseJsonObject.getInt("outLen").toString())
            }else if(testType.lowercase().contains("mct")) {
                result.add("$shakeMinOutLen $shakeMaxOutLen")
            }
            return  result

        }

        fun fillInHardwareTestOutput(
            testId: String,
            testCaseJsonObject: JSONObject,
            outputtedXml: String,
            testType: String
        ) {
            val contentXml = XmlUtils.getLabelValue(outputtedXml, testId)
            if (testType.uppercase(Locale.getDefault()) == "MCT") { //Monte Carlo Test
                //parse MCT result
                val resultsArray = JSONArray()
                for (i in 0..99) {
                    if (XmlUtils.labelExists(contentXml, "round$i")) {
                        val roundXml = XmlUtils.getLabelValue(
                            contentXml,
                            "round$i"
                        )
                        val roundResult = JSONObject()
                        if (XmlUtils.labelExists(roundXml, "md")) {
                            roundResult.put("md", XmlUtils.getLabelValue(roundXml, "md"))
                            roundResult.put("outLen", XmlUtils.getLabelValue(roundXml, "outLen").toInt())
                        }
                        resultsArray.put(roundResult)
                    }
                }
                testCaseJsonObject.put("resultsArray", resultsArray)
            } else {  //VOT or AFT test
                //parse normal test result
                if (XmlUtils.labelExists(contentXml, "md")) {
                    testCaseJsonObject.put("md", XmlUtils.getLabelValue(contentXml, "md"))
                }
            }
        }
    }

    constructor(mode: String) : this() {
        setShakeMode(mode)
    }

    private fun setShakeMode(shakeMode: String) {
        if (shakeMode.uppercase().contains("SHAKE-128")) {
            this.shakeMode = MODE_SHAKE_128
        } else if (shakeMode.uppercase().contains("SHAKE-256")) {
            this.shakeMode = MODE_SHAKE_256
        } else {
            throw Exception("Unsupported SHAKE mode: $shakeMode")
        }
    }


    fun hash(textHexString: String, outputByteLength: Int): String {
        try {
            val shakeAlgorithm = when (shakeMode) {
                MODE_SHAKE_128 -> SHAKE128(outputByteLength)
                MODE_SHAKE_256 -> SHAKE256(outputByteLength)
                else -> SHAKE128(outputByteLength)
            }
            val textBytes = CipherUtils.hexStringToBytes(textHexString)
            val hash = shakeAlgorithm.digest(textBytes)
            return CipherUtils.bytesToHexString(hash)
        } catch (e: Exception) {
            print(e.stackTrace)
            return ""
        }

    }

    //Monte Carlo Test
    fun doMCTHash(testCaseJsonObject:JSONObject, textHexString: String, maxOutBitLength: Int, minOutBitLength:Int){


        val resultJsonArray = JSONArray()


        val maxOutByteLength = floor((maxOutBitLength*1.0)/8).toInt()
        val minOutByteLength = ceil((minOutBitLength*1.0)/8).toInt()
        val byteLengthRange = maxOutByteLength - minOutByteLength + 1

        var outputByteLength = maxOutByteLength  //initial output length

        var seedBytes = CipherUtils.hexStringToBytes(textHexString)

        for (i in 0 until 100) {
            val currentRoundResult = JSONObject()
            val messageDigestList = mutableListOf(seedBytes) //initial seed
            for (j in 0 until 1000) {
                //每一輪長度都不一樣
                val shakeAlgorithm = when (shakeMode) {
                    //1 byte = 8 bit
                    MODE_SHAKE_128 -> SHAKE128(outputByteLength)
                    MODE_SHAKE_256 -> SHAKE256(outputByteLength)
                    else -> SHAKE128(outputByteLength)
                }
                var lastMessage = messageDigestList.last()

                if (lastMessage.size < 16) {
                    lastMessage += ByteArray(16 - lastMessage.size)
                }
                //take left most 16 bytes, 16 bytes = 128 bits
                val leftMost16Bytes = lastMessage.take(16).toByteArray()

                val newMessageDigest = shakeAlgorithm.digest(leftMost16Bytes)
                messageDigestList.add(newMessageDigest)

                //take right most 2 bytes
                val rightMost2Bytes = newMessageDigest.takeLast(2).toByteArray()

                if(j == 999){ //last inner round
                    seedBytes = messageDigestList.last()
                    currentRoundResult.put("md", CipherUtils.bytesToHexString(seedBytes))
                    currentRoundResult.put("outLen", outputByteLength*8)
                }
                outputByteLength = minOutByteLength + (CipherUtils.bytesToHexString(rightMost2Bytes).toInt(16) % byteLengthRange)
            }
            resultJsonArray.put(currentRoundResult)
        }
        testCaseJsonObject.put("resultsArray", resultJsonArray)

    }


}


fun ByteArray.binaryIntValue(): Int {
    val length = this.size
    var value = 0
    var powerOf2 = 1 //start from 2^0
    for(i in length-1 downTo 0){
        val byte = this[i]
        for(j in 7 downTo 0){
            val bit = byte.getBit(j)
            value += bit * powerOf2
            powerOf2 *= 2
        }
    }
    return value
}

fun Byte.getBit(position: Int): Int {
    return (this.toInt() shr position) and 1;
}

