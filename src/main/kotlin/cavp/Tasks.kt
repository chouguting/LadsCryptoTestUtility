package cavp

import ciphers.DRBGEngine
import ciphers.ECDSAEngine
import ciphers.RSAEngine
import ciphers.SHAKEEngine
import tw.edu.ntu.lads.chouguting.java.cipers.AESEngine
import tw.edu.ntu.lads.chouguting.java.cipers.SHAEngine


fun runAndSave(cavpTestFiles: ArrayList<CavpTestFile>, saveToFolder: String, mctEnabled: Boolean) {
//    delay(1000)
    println("runAndSave on ${Thread.currentThread().name}")
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
                    if (algorithmName.lowercase().contains("aes")) {
                        AESEngine.runAESWithTestCase(
                            testCaseJson,
                            aesDirection,
                            algorithmName,
                            aesKeyLength,
                            testType,
                            mctEnabled
                        )
                    } else if (algorithmName.lowercase().contains("shake")) {  //"shake"裡面有"sha"，所以要先判斷"shake"
                        SHAKEEngine.runSHAKEWithTestCase(
                            testCaseJson,
                            algorithmName,
                            testType,
                            mctEnabled,
                            shakeMaxOutLen,
                            shakeMinOutLen
                        )
                    } else if (algorithmName.lowercase().contains("sha")) {
                        SHAEngine.runSHAWithTestCase(testCaseJson, algorithmName, testType, mctEnabled)
                    } else if (algorithmName.lowercase().contains("drbg")) {
                        DRBGEngine.runDrbgWithTestCase(testCaseJson, drbgReturnedBitsLen)
                    }else if (algorithmName.lowercase().contains("ecdsa")) {
                        ECDSAEngine.runEcdsaWithTestCase(currentTestGroup,testCaseJson, ecdsaCurve, ecdsaOperationMode)
                    }else if(algorithmName.lowercase().contains("rsa")){
                        RSAEngine.runRsaWithTestCase(currentTestGroup, testCaseJson, rsaOperationMode)
                    }

                }

            }
        }
        cavpTestFile.saveRspToFolder(saveToFolder)
    }
}


