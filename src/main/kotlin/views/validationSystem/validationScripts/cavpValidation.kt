package views.validationSystem.validationScripts

import androidx.compose.runtime.MutableState
import cavp.CavpTestFile
import cavp.runCavp
import ciphers.DRBGEngine
import ciphers.ECDSAEngine
import ciphers.RSAEngine
import ciphers.SHAKEEngine
import com.fazecast.jSerialComm.SerialPort
import kotlinx.coroutines.delay
import tw.edu.ntu.lads.chouguting.java.cipers.AESEngine
import tw.edu.ntu.lads.chouguting.java.cipers.SHAEngine
import views.validationSystem.ValidationTestCase
import views.validationSystem.printlnTestLog
import java.util.Objects


suspend fun cavpValidation(
    displayLog: MutableState<String>,
    sendText: MutableState<String>,
    sendStart: MutableState<Boolean>,
    arguments: HashMap<String,Any>
) {
    val cavpTestFiles = arguments["cavpTestFiles"] as ArrayList<CavpTestFile>
    for(cavpFile in cavpTestFiles){
        println(cavpFile.numberOfAlgorithm)

    }
    printlnTestLog("=====================================\ncalculation CAVP Golden Result", displayLog)
    runCavp(cavpTestFiles, "", false, false);
    printlnTestLog("calculation Finished", displayLog)
    printlnTestLog("=====================================\n\n", displayLog)
//


    var testCounter = 1;

    for (cavpTestFile in cavpTestFiles) {

        val numberOfAlgorithm = cavpTestFile.numberOfAlgorithm
        for (algorithmIndex in 0 until numberOfAlgorithm) {
            val algorithmName = cavpTestFile.algorithmJsonLists[algorithmIndex].getString("algorithm")

            //check if the algorithm is supported
            var supported = false
            checkSupported@for(supportedAlgorithm in ValidationTestCase.supportedAlgorithmList){
                if(algorithmName.lowercase().contains(supportedAlgorithm)){
                    for(supportedMode in ValidationTestCase.supportedMode[supportedAlgorithm]!!){
                        if(algorithmName.lowercase().contains(supportedMode)){
                            supported = true
                            break@checkSupported
                        }
                    }
                }
            }
            if(!supported){
                printlnTestLog("================================", displayLog)
                printlnTestLog("Test $algorithmName is not supported", displayLog)
                printlnTestLog("================================\n", displayLog)

                continue
            }

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
                    var validationTestCase = ValidationTestCase()
                    if (algorithmName.lowercase().contains("aes")) {

                        AESEngine.extractValidationTestCase(
                            validationTestCase,
                            testCaseJson,
                            aesDirection,
                            algorithmName,
                            aesKeyLength,
                            testType
                        )
                    } else if (algorithmName.lowercase().contains("shake")) {  //"shake"裡面有"sha"，所以要先判斷"shake"
//                        SHAKEEngine.runSHAKEWithTestCase(
//                            testCaseJson,
//                            algorithmName,
//                            testType,
//                            mctEnabled,
//                            shakeMaxOutLen,
//                            shakeMinOutLen
//                        )
                    } else if (algorithmName.lowercase().contains("sha")) {
//                        SHAEngine.runSHAWithTestCase(testCaseJson, algorithmName, testType, mctEnabled)
                    } else if (algorithmName.lowercase().contains("drbg")) {
//                        DRBGEngine.runDrbgWithTestCase(testCaseJson, drbgReturnedBitsLen)
                    }else if (algorithmName.lowercase().contains("ecdsa")) {
//                        ECDSAEngine.runEcdsaWithTestCase(currentTestGroup,testCaseJson, ecdsaCurve, ecdsaOperationMode)
                    }else if(algorithmName.lowercase().contains("rsa")){
//                        RSAEngine.runRsaWithTestCase(currentTestGroup, testCaseJson, rsaOperationMode)
                    }
                    printlnTestLog("=========================", displayLog)
                    printlnTestLog("Test $testCounter", displayLog)

                    for(input in validationTestCase.inputs){
                        printlnTestLog("input: $input", displayLog)
                        sendText.value = input
                        sendStart.value = true
                        while (sendStart.value){
                            delay(100)
                        }
//                        delay(2000)
                    }
                    for (expectedOutput in validationTestCase.expectedOutput){
                        printlnTestLog("expectedOutput: $expectedOutput", displayLog)
                        println(expectedOutput)
                    }
                    testCounter++
                    printlnTestLog("===========================", displayLog)
                    delay(1000)

                }

            }
        }


    }

//    currentCommPort.closePort()

}