package ciphers

import org.json.JSONArray
import org.json.JSONObject
import org.kotlincrypto.hash.sha3.SHAKE128
import org.kotlincrypto.hash.sha3.SHAKE256
import tw.edu.ntu.lads.chouguting.java.cipers.CipherUtils

class SHAKEEngine() {

    private var shakeMode:String = MODE_SHAKE_128
    companion object{
        const val MODE_SHAKE_128= "SHAKE_128"
        const val MODE_SHAKE_256= "SHAKE_256"

        fun runSHAKEWithTestCase(testCaseJsonObject: JSONObject, shakeMode:String, testType:String, mctEnabled:Boolean){

            val shakeEngine = SHAKEEngine(shakeMode)
            val messageHexString = testCaseJsonObject.getString("msg")

            if(testType == "MCT" && mctEnabled) { //Monte Carlo Test
                val shakeLength = testCaseJsonObject.getInt("len")   //想要幾個bit
                val shakeByteLength = shakeLength / 8        //1個byte = 8個bit
                val mctResultList = shakeEngine.doMCTHash(messageHexString, shakeByteLength)
                val mctResultJsonArray = JSONArray()
                for(mctResult in mctResultList){
                    mctResultJsonArray.put(JSONObject().put("md", mctResult))
                }
                testCaseJsonObject.put("resultsArray", mctResultJsonArray)
                return
            }
            val shakeOutLength = testCaseJsonObject.getInt("outLen")   //想要幾個bit
            val shakeOutByteLength = shakeOutLength / 8        //1個byte = 8個bit
            val digestHexString: String = shakeEngine.hash(messageHexString, shakeOutByteLength)
            testCaseJsonObject.put("md", digestHexString)
        }
    }

    constructor(mode:String):this(){
        setShakeMode(mode)
    }

    private fun setShakeMode(shakeMode:String){
        if(shakeMode.uppercase().contains("SHAKE-128")){
            this.shakeMode = MODE_SHAKE_128
        }else if(shakeMode.uppercase().contains("SHAKE-256")){
            this.shakeMode = MODE_SHAKE_256
        }else{
            throw Exception("Unsupported SHAKE mode: $shakeMode")
        }
    }


    fun hash(textHexString:String, outputByteLength:Int): String{
        try {
            val shakeAlgorithm = when (shakeMode) {
                MODE_SHAKE_128 -> SHAKE128(outputByteLength)
                MODE_SHAKE_256 -> SHAKE256(outputByteLength)
                else -> SHAKE128(outputByteLength)
            }
            val textBytes = CipherUtils.hexStringToBytes(textHexString)
            val hash = shakeAlgorithm.digest(textBytes)
            return CipherUtils.bytesToHexString(hash)
        }catch (e:Exception){
            print(e.stackTrace)
            return ""
        }

    }

    //Monte Carlo Test
    fun doMCTHash(textHexString:String, byteLength:Int): MutableList<String>{
        val outputResult = mutableListOf<String>()
        val shakeAlgorithm = when (shakeMode) {
            MODE_SHAKE_128 -> SHAKE128(byteLength)
            MODE_SHAKE_256 -> SHAKE256(byteLength)
            else -> SHAKE128(byteLength)
        }
        var seedBytes = CipherUtils.hexStringToBytes(textHexString)
        for(i in 0 until 100){
            val messageDigestList = mutableListOf(seedBytes, seedBytes, seedBytes) //initial 3 seed
            for(j in 0 until 1000){
                val last = messageDigestList.last()
                val second_last = messageDigestList[messageDigestList.size-2]
                val third_last = messageDigestList[messageDigestList.size-3]
                val newMessage = third_last + second_last + last
                val newMessageDigest = shakeAlgorithm.digest(newMessage)
                messageDigestList.add(newMessageDigest)
            }
            seedBytes = messageDigestList.last()
            outputResult.add(CipherUtils.bytesToHexString(seedBytes))
        }
        return outputResult
    }




}

