package views.validationSystem.validationScripts

import cavpTestUtils.CavpTestFile
import cavpTestUtils.runCavp
import kotlinx.coroutines.delay
import tw.edu.ntu.lads.chouguting.java.cipers.AESEngine
import views.validationSystem.OutputValidator
import views.validationSystem.TestControlBundle
import views.validationSystem.ValidationTestCase
import views.validationSystem.printlnTestLog


suspend fun cavpValidation(
//    displayLog: MutableState<String>,
//    sendText: MutableState<String>,
//    sendStart: MutableState<Boolean>,
//    arguments: HashMap<String,Any>,
    testControlBundle: TestControlBundle,
    outputValidator: OutputValidator
) {
    val displayLog = testControlBundle.displayLog
    val sendText = testControlBundle.sendText
    val sendStart = testControlBundle.sendStart
    val arguments = testControlBundle.arguments
    val cavpTestFiles = arguments["cavpTestFiles"] as ArrayList<CavpTestFile> //從arguments裡面取出cavpTestFiles
    val testcaseStartIndex = arguments["testcaseStartIndex"] as Int //從arguments裡面取出testcaseStartIndex

    for(cavpFile in cavpTestFiles){
        printlnTestLog("there are ${cavpFile.numberOfAlgorithm} algorithms in ${cavpFile.filename}", displayLog)  //印出有幾個algorithm
    }
    printlnTestLog("=====================================\ncalculating CAVP Golden Result", displayLog)
    runCavp(cavpTestFiles, "", false, false);  //計算CAVP的結果 (GOlden Result)
    printlnTestLog("calculation Finished", displayLog)
    printlnTestLog("=====================================\n\n", displayLog)
//


    var testCounter = 1; //測試的次數
    outputValidator.reset() //把outputValidator的資料清空

    for (cavpTestFile in cavpTestFiles) { //對每一個CAVP的檔案做測試

        val numberOfAlgorithm = cavpTestFile.numberOfAlgorithm //取得有幾個algorithm
        for (algorithmIndex in 0 until numberOfAlgorithm) { //對每一個algorithm做測試
            val algorithmName = cavpTestFile.algorithmJsonLists[algorithmIndex].getString("algorithm") //取得algorithm的名字

            //檢查是否支援這個algorithm
            var supported = false //預設是不支援的
            checkSupported@for(supportedAlgorithm in ValidationTestCase.supportedAlgorithmList){
                if(algorithmName.lowercase().contains(supportedAlgorithm)){  //check if the algorithm is supported
                    for(supportedMode in ValidationTestCase.supportedMode[supportedAlgorithm]!!){
                        if(algorithmName.lowercase().contains(supportedMode)){  //check if the mode is supported
                            supported = true
                            break@checkSupported
                        }
                    }
                }
            }
            //如果不支援就跳過
            if(!supported){
                printlnTestLog("================================", displayLog)
                printlnTestLog("Test $algorithmName is not supported", displayLog)
                printlnTestLog("================================\n", displayLog)

                continue
            }

            //取得有幾個test group
            val numberOfTestGroup = cavpTestFile.numberOfTestGroups(algorithmIndex)

            //取得各個演算法的專用參數

            //ECDSA的mode
            val ecdsaOperationMode = if (algorithmName.lowercase().contains("ecdsa")) {
                cavpTestFile.algorithmJsonLists[algorithmIndex].getString("mode")
            } else {
                ""
            }

            //RSA的mode
            val rsaOperationMode = if (algorithmName.lowercase().contains("rsa")) {
                cavpTestFile.algorithmJsonLists[algorithmIndex].getString("mode")
            } else {
                ""
            }

            //對每一個test group做測試
            for (testGroupIndex in 0 until numberOfTestGroup) {
                val numberOfTestCases = cavpTestFile.numberOfTestCases(algorithmIndex, testGroupIndex) //取得有幾個test case
                val testType = cavpTestFile.getTestGroup(algorithmIndex, testGroupIndex).getString("testType") //取得test type
                val currentTestGroup = cavpTestFile.getTestGroup(algorithmIndex, testGroupIndex) //取得目前的test group
                if(testType=="MCT"){
                    continue
                    //目前還不支援MCT
                    //TODO: 也許之後可以加入MCT的測試
                }

                //AES的direction
                val aesDirection =
                    if (algorithmName.lowercase().contains("aes")) {
                        cavpTestFile.getTestGroup(algorithmIndex, testGroupIndex).getString("direction")
                    } else {
                        ""
                    }
                //AES的key length
                val aesKeyLength = if (algorithmName.lowercase().contains("aes")) {
                    cavpTestFile.getTestGroup(algorithmIndex, testGroupIndex).getInt("keyLen")
                } else {
                    0
                }
                //SHAKE的maxOutLen
                val shakeMaxOutLen = if (algorithmName.lowercase().contains("shake") && cavpTestFile.getTestGroup(
                        algorithmIndex,
                        testGroupIndex
                    ).has("maxOutLen")
                ) {
                    cavpTestFile.getTestGroup(algorithmIndex, testGroupIndex).getInt("maxOutLen")
                } else {
                    0
                }
                //SHAKE的minOutLen
                val shakeMinOutLen = if (algorithmName.lowercase().contains("shake") && cavpTestFile.getTestGroup(
                        algorithmIndex,
                        testGroupIndex
                    ).has("minOutLen")
                ) {
                    cavpTestFile.getTestGroup(algorithmIndex, testGroupIndex).getInt("minOutLen")
                } else {
                    0
                }
                //DRBG的returnedBitsLen
                val drbgReturnedBitsLen = if (algorithmName.lowercase().contains("drbg")) {
                    cavpTestFile.getTestGroup(algorithmIndex, testGroupIndex).getInt("returnedBitsLen")
                } else {
                    0
                }

                //ECDSA的curve
                val ecdsaCurve = if (algorithmName.lowercase().contains("ecdsa")) {
                    cavpTestFile.getTestGroup(algorithmIndex, testGroupIndex).getString("curve")
                } else {
                    ""
                }

                //對每一個test case做測試
                for (testCaseIndex in 0 until numberOfTestCases) {
                    val testCaseJson = cavpTestFile.getTestCase(algorithmIndex, testGroupIndex, testCaseIndex) //取得目前的test case的json
                    var validationTestCase = ValidationTestCase() //建立一個ValidationTestCase(會記錄input,expected output)
                    if (algorithmName.lowercase().contains("aes")) { //如果是AES
                        if(aesDirection=="decrypt"){
                            continue //skip decrypt test case for now
                            //TODO: implement decrypt test case  目前還不知道MCU能不能做解密
                        }
                        AESEngine.extractValidationTestCase( //把要傳給硬體及接收的資料整理成ValidationTestCase
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
                    if(testCounter<testcaseStartIndex){ //如果測試次數還沒到testcaseStartIndex，就跳過
                        testCounter++
                        continue
                    }

                    printlnTestLog("=========================", displayLog)
                    printlnTestLog("Test $testCounter", displayLog)

                    outputValidator.reset()  //把outputValidator的資料清空(確保每次測試都是從頭開始)
                    for (expectedOutput in validationTestCase.expectedOutputList){
                        outputValidator.addValidationTask(expectedOutput)  //把需要驗證的答案放進outputValidator
                    }

                    printlnTestLog("[inputs]=>", displayLog)
                    for((key, value) in validationTestCase.inputMap){  //把input參數印出來
                        printlnTestLog("$key: $value", displayLog);
                    }

                    printlnTestLog("[expectedOutputs]=>", displayLog)
                    for ((key, value) in validationTestCase.expectedOutputMap){  //把期望的答案印出來
                        printlnTestLog("$key: $value", displayLog)
                    }

                    for(input in validationTestCase.inputList){  //把input傳給硬體
                        sendText.value = input
                        sendStart.value = true
                        while (sendStart.value){
                            delay(100) //如果還沒傳完，就等待
                        }
                        delay(200)
                    }



                    var waitCounter = 0
                    while (!outputValidator.foundAnswer){  //等待硬體回傳答案
                        delay(500)
                        waitCounter++
                        if(waitCounter>10){ //如果等待超過10次就跳出
                            printlnTestLog("\nError: wrong outputs or timeout ", displayLog)
                            return
                        }
                    }
                    printlnTestLog("Test $testCounter: Pass", displayLog)  //如果還沒有跳出，就代表硬體回傳的答案是對的
                    printlnTestLog("===========================", displayLog)
                    outputValidator.reset()
                    testCounter++  //測試次數+1
                    delay(200)

                }

            }
        }


    }

//    currentCommPort.closePort()

}