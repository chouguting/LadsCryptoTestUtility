package cavpTestUtils

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import ciphers.DRBGEngine
import ciphers.ECDSAEngine
import ciphers.RSAEngine
import ciphers.SHAKEEngine
import kotlinx.coroutines.delay
import org.json.JSONObject
import tw.edu.ntu.lads.chouguting.java.cipers.AESEngine
import tw.edu.ntu.lads.chouguting.java.cipers.SHAEngine
import utils.SerialCommunicator

suspend fun runHardwareCavp(
    serialCommunicator: SerialCommunicator,
    cavpTestFiles: ArrayList<CavpTestFile>,
    saveToFolder: String,
    save: Boolean = true,
    progress: MutableState<Float> = mutableStateOf(0f),
    validateResult: Boolean = false
) {

    println("run And Test Hardware on ${Thread.currentThread().name}")
    val totalTestCount = cavpTestFiles.sumOf { it.getNumberOfAllTestCases() }
    var currentTestCount = 0
    for (cavpTestFile in cavpTestFiles) {
        val numberOfAlgorithm = cavpTestFile.numberOfAlgorithm
        for (algorithmIndex in 0 until numberOfAlgorithm) {
            val algorithmName = cavpTestFile.algorithmJsonLists[algorithmIndex].getString("algorithm")
            val numberOfTestGroup = cavpTestFile.numberOfTestGroups(algorithmIndex)

            val ecdsaOperationMode = if (algorithmName.lowercase().contains("ecdsa")) {
                cavpTestFile.algorithmJsonLists[algorithmIndex].getString("mode")
            } else {
                ""
            }

            val rsaOperationMode = if (algorithmName.lowercase().contains("rsa")) {
                cavpTestFile.algorithmJsonLists[algorithmIndex].getString("mode")
            } else {
                ""
            }


            for (testGroupIndex in 0 until numberOfTestGroup) {
                val numberOfTestCases = cavpTestFile.numberOfTestCases(algorithmIndex, testGroupIndex)
                val testType = cavpTestFile.getTestGroup(algorithmIndex, testGroupIndex).getString("testType")
                val currentTestGroup = cavpTestFile.getTestGroup(algorithmIndex, testGroupIndex)
                val aesDirection =
                    if (algorithmName.lowercase().contains("aes")) {
                        cavpTestFile.getTestGroup(algorithmIndex, testGroupIndex).getString("direction")
                    } else {
                        ""
                    }
                val aesKeyLength = if (algorithmName.lowercase().contains("aes")) {
                    cavpTestFile.getTestGroup(algorithmIndex, testGroupIndex).getInt("keyLen")
                } else {
                    0
                }

                val shakeMaxOutLen = if (algorithmName.lowercase().contains("shake") && cavpTestFile.getTestGroup(
                        algorithmIndex,
                        testGroupIndex
                    ).has("maxOutLen")
                ) {
                    cavpTestFile.getTestGroup(algorithmIndex, testGroupIndex).getInt("maxOutLen")
                } else {
                    0
                }

                val shakeMinOutLen = if (algorithmName.lowercase().contains("shake") && cavpTestFile.getTestGroup(
                        algorithmIndex,
                        testGroupIndex
                    ).has("minOutLen")
                ) {
                    cavpTestFile.getTestGroup(algorithmIndex, testGroupIndex).getInt("minOutLen")
                } else {
                    0
                }

                val drbgReturnedBitsLen = if (algorithmName.lowercase().contains("drbg")) {
                    cavpTestFile.getTestGroup(algorithmIndex, testGroupIndex).getInt("returnedBitsLen")
                } else {
                    0
                }

                val ecdsaCurve = if (algorithmName.lowercase().contains("ecdsa")) {
                    cavpTestFile.getTestGroup(algorithmIndex, testGroupIndex).getString("curve")
                } else {
                    ""
                }

                for (testCaseIndex in 0 until numberOfTestCases) {
                    val testCaseJson = cavpTestFile.getTestCase(algorithmIndex, testGroupIndex, testCaseIndex)
                    currentTestCount++
                    progress.value = currentTestCount.toFloat() / totalTestCount.toFloat()
                    val testId = "test$currentTestCount"
                    if (algorithmName.lowercase().contains("aes")) {
                        //TODO: run hardware AES
                        val stringToDevice = AESEngine.getHardwareTestInput(
                            testId,
                            testCaseJson,
                            aesDirection,
                            algorithmName,
                            aesKeyLength,
                            testType
                        )
                        serialCommunicator.sendTextToDevice(stringToDevice)
                        val resultXml = serialCommunicator.waitForResponse(testId, 10000) //wait for 10 seconds
                        println("[result] $resultXml")

                        val duplicatedTestCaseJson = JSONObject(testCaseJson.toString())
                        AESEngine.fillInHardwareTestOutput(testId, testCaseJson, resultXml, testType)

                        if(validateResult){
                            AESEngine.runAESWithTestCase(
                                duplicatedTestCaseJson,
                                aesDirection,
                                algorithmName,
                                aesKeyLength,
                                testType,
                                true
                            )
                            //compare result
                            if (!testCaseJson.similar(duplicatedTestCaseJson)) {
                                throw Exception("AES hardware test failed")
                            }
                        }

                    } else if (algorithmName.lowercase().contains("shake")) {  //"shake"裡面有"sha"，所以要先判斷"shake"
                        //TODO: run hardware SHAKE
                    } else if (algorithmName.lowercase().contains("sha")) {
                        //TODO: run hardware SHA
                    } else if (algorithmName.lowercase().contains("drbg")) {
                        //TODO: run hardware DRBG
                    } else if (algorithmName.lowercase().contains("ecdsa")) {
                        //TODO: run hardware ECDSA
                    } else if (algorithmName.lowercase().contains("rsa")) {
                        //TODO: run hardware RSA
                    }

                }

            }
        }
        if (save) {
            cavpTestFile.saveRspToFolder(saveToFolder)
        }

    }

}