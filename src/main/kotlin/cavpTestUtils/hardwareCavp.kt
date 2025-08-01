package cavpTestUtils

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import ciphers.ECDSAEngine
import ciphers.RSAEngine
import ciphers.SHAKEEngine
import kotlinx.coroutines.delay
import org.json.JSONObject
import tw.edu.ntu.lads.chouguting.java.cipers.AESEngine
import tw.edu.ntu.lads.chouguting.java.cipers.SHAEngine
import utils.SerialCommunicator
import java.io.FileWriter

suspend fun runHardwareCavp(
    serialCommunicator: SerialCommunicator,
    cavpTestFiles: ArrayList<CavpTestFile>,
    saveToFolder: String,
    save: Boolean = true,
    progress: MutableState<Float> = mutableStateOf(0f),
    validateResult: Boolean = false,
    startFromTestCase: Int = 0
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
                    if (currentTestCount < startFromTestCase) {
                        currentTestCount++
                        continue
                    }

                    val testCaseJson = cavpTestFile.getTestCase(algorithmIndex, testGroupIndex, testCaseIndex)

                    progress.value = currentTestCount.toFloat() / totalTestCount.toFloat()
                    val testId = "test$currentTestCount"
                    if (algorithmName.lowercase().contains("aes")) {
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

                        val questionTestCase = JSONObject(testCaseJson.toString())
                        val referenceTestCaseJson = JSONObject(testCaseJson.toString())
                        AESEngine.fillInHardwareTestOutput(testId, testCaseJson, resultXml, testType)

                        if (validateResult) {
                            AESEngine.runAESWithTestCase(
                                referenceTestCaseJson,
                                aesDirection,
                                algorithmName,
                                aesKeyLength,
                                testType,
                                true
                            )
                            //compare result
                            if (!testCaseJson.similar(referenceTestCaseJson)) {
                                checkMctResult(
                                    testCaseJson,
                                    referenceTestCaseJson,
                                    "AES",
                                    currentTestCount,
                                    saveToFolder
                                )
                                saveErrorTestCaseToFolder(
                                    saveToFolder,
                                    questionTestCase,
                                    referenceTestCaseJson,
                                    testCaseJson
                                )
                                throw Exception("AES hardware test failed on test case $currentTestCount,\nerror.txt saved to folder: $saveToFolder")
                            }
                        }

                    } else if (algorithmName.lowercase().contains("shake")) {  //"shake"裡面有"sha"，所以要先判斷"shake"
                        //TODO: run hardware SHAKE
                        val stringsToDevice = SHAKEEngine.getHardwareTestInput(
                            testId,
                            testCaseJson,
                            algorithmName,
                            testType,
                            shakeMaxOutLen,
                            shakeMinOutLen
                        )
                        serialCommunicator.sendTextToDevice(stringsToDevice)
                        var resultXml = ""
                        if (testType.lowercase().contains("mct")) {
                            resultXml = serialCommunicator.waitForResponse(testId, 50000000) //wait for 50000 seconds
                        } else {
                            resultXml = serialCommunicator.waitForResponse(testId, 50000) //wait for 50 seconds
                        }
                        println("[result] $resultXml")

                        val questionTestCase = JSONObject(testCaseJson.toString())
                        val referenceTestCaseJson = JSONObject(testCaseJson.toString())
                        SHAKEEngine.fillInHardwareTestOutput(testId, testCaseJson, resultXml, testType)
                        if (validateResult) {
                            SHAKEEngine.runSHAKEWithTestCase(
                                referenceTestCaseJson, algorithmName,
                                testType,
                                true,
                                shakeMaxOutLen,
                                shakeMinOutLen
                            )
                            //compare result
                            if (!testCaseJson.similar(referenceTestCaseJson)) {
                                checkMctResult(
                                    testCaseJson,
                                    referenceTestCaseJson,
                                    "SHAKE",
                                    currentTestCount,
                                    saveToFolder
                                )

                                saveErrorTestCaseToFolder(
                                    saveToFolder,
                                    questionTestCase,
                                    referenceTestCaseJson,
                                    testCaseJson
                                )

                                throw Exception("SHAKE hardware test failed on test case $currentTestCount,\nerror.txt saved to folder: $saveToFolder")
                            }
//                            println("SHAKE hardware test passed on test case $currentTestCount")
                        }


                    } else if (algorithmName.lowercase().contains("sha")) {
                        val stringsToDevice = SHAEngine.getHardwareTestInput(
                            testId,
                            testCaseJson,
                            algorithmName,
                            testType
                        )
                        //println("[send] $stringsToDevice")
                        serialCommunicator.sendTextToDevice(stringsToDevice)
                        val resultXml = serialCommunicator.waitForResponse(testId, 50000) //wait for 50 seconds
                        println("[result] $resultXml")

                        val questionTestCase = JSONObject(testCaseJson.toString())
                        val referenceTestCaseJson = JSONObject(testCaseJson.toString())
                        SHAEngine.fillInHardwareTestOutput(testId, testCaseJson, resultXml, testType)
                        if (validateResult) {
                            SHAEngine.runSHAWithTestCase(referenceTestCaseJson, algorithmName, testType, true)
                            //compare result
                            if (!testCaseJson.similar(referenceTestCaseJson)) {
                                checkMctResult(
                                    testCaseJson,
                                    referenceTestCaseJson,
                                    "SHA",
                                    currentTestCount,
                                    saveToFolder
                                )
                                saveErrorTestCaseToFolder(
                                    saveToFolder,
                                    questionTestCase,
                                    referenceTestCaseJson,
                                    testCaseJson
                                )
                                throw Exception("SHA hardware test failed on test case $currentTestCount,\nerror.txt saved to folder: $saveToFolder")
                            }
                        }


                    } else if (algorithmName.lowercase().contains("drbg")) {
                        //TODO: run hardware DRBG
                    } else if (algorithmName.lowercase().contains("ecdsa")) {
                        val stringsToDevice = ECDSAEngine.getHardwareTestInput(
                            testId, currentTestGroup, testCaseJson, ecdsaCurve, ecdsaOperationMode
                        )
                        serialCommunicator.sendTextToDevice(stringsToDevice, delayInterval = 100)
                        val questionTestCase = JSONObject(testCaseJson.toString())
                        val questionTestGroup = JSONObject(currentTestGroup.toString())
                        val resultXml = serialCommunicator.waitForResponse(testId, 30000) //wait for 10 seconds
                        ECDSAEngine.fillInHardwareTestOutput(testId, testCaseJson, resultXml, ecdsaOperationMode)
                        if(validateResult){
                            ECDSAEngine.validateHardwareResult(
                                saveToFolder,
                                currentTestCount,
                                questionTestGroup,
                                questionTestCase,
                                currentTestGroup,
                                testCaseJson,
                                ecdsaCurve,
                                ecdsaOperationMode
                            )
                            //println("validate ECDSA hardware test passed on test case $currentTestCount")
                        }

                    } else if (algorithmName.lowercase().contains("rsa")) {
                        //TODO: run hardware RSA
                        val stringToDevice = RSAEngine.getHardwareTestInput(
                            testId, currentTestGroup, testCaseJson, rsaOperationMode,
                        )
                        serialCommunicator.sendTextToDevice(stringToDevice, delayInterval = 1000)
                        val questionTestCase = JSONObject(testCaseJson.toString())
                        val questionTestGroup = JSONObject(currentTestGroup.toString())
                        val resultXml = serialCommunicator.waitForResponse(testId, 30000000)
                        //delay(2000)
                        RSAEngine.fillInHardwareTestOutput(testId, testCaseJson, resultXml, rsaOperationMode)
                        if(validateResult){
                            RSAEngine.validateHardwareResult(
                                saveToFolder,
                                currentTestCount,
                                questionTestCase,
                                questionTestGroup,
                                testCaseJson,
                                rsaOperationMode
                            )
                            //println("validate RSA hardware test passed on test case $currentTestCount")
                        }
                    }
                    currentTestCount++

                }

            }
        }
        if (save) {
            cavpTestFile.saveRspToFolder(saveToFolder)
        }

    }

}

fun saveErrorTestCaseToFolder(
    saveToThisFolder: String,
    questionTestCase: JSONObject,
    goldenReferenceTestCase: JSONObject,
    hardwareResultTestCase: JSONObject
) {
    val errorFileName = "error.txt"
    val fileWriter = FileWriter("$saveToThisFolder\\$errorFileName")
    fileWriter.write("question test case:\n")
    fileWriter.write(questionTestCase.toString(8))
    fileWriter.write("\n\n==================================================\n\n")
    fileWriter.write("golden reference test case:\n")
    fileWriter.write(goldenReferenceTestCase.toString(8))
    fileWriter.write("\n\n==================================================\n\n")
    fileWriter.write("hardware result test case:\n")
    fileWriter.write(hardwareResultTestCase.toString(8))
    fileWriter.close()
}

fun JSONObject.hasResultsArray() = this.has("resultsArray")

fun checkMctResult(
    testCaseJson: JSONObject,
    referenceTestCaseJson: JSONObject,
    cryptoAlgorithm: String,
    currentTestCount: Int,
    saveToFolder: String
) {
    if (testCaseJson.hasResultsArray() && referenceTestCaseJson.hasResultsArray()) {
        val testResultsArray = testCaseJson.getJSONArray("resultsArray")
        val referenceResultsArray = referenceTestCaseJson.getJSONArray("resultsArray")
        for (i in 0 until testResultsArray.length()) {
            val testResult = testResultsArray.getJSONObject(i)
            val referenceResult = referenceResultsArray.getJSONObject(i)
            if (!testResult.similar(referenceResult)) {
                throw Exception("$cryptoAlgorithm hardware test failed on test case $currentTestCount, MCT failed on round $i,\nerror.txt saved to folder: $saveToFolder")
            }
        }
    }
}