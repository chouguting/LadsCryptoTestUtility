import cavp.CavpTestFile
import ciphers.SHAKEEngine
import kotlinx.coroutines.delay
import tw.edu.ntu.lads.chouguting.java.cipers.AESEngine
import tw.edu.ntu.lads.chouguting.java.cipers.SHAEngine




fun runAndSave(cavpTestFiles: ArrayList<CavpTestFile>, saveToFolder: String) {
//    delay(1000)
    println("runAndSave on ${Thread.currentThread().name}")
    for (cavpTestFile in cavpTestFiles) {
        val numberOfAlgorithm = cavpTestFile.numberOfAlgorithm
        for (algorithmIndex in 0 until numberOfAlgorithm) {
            val algorithmName = cavpTestFile.algorithmJsonLists[algorithmIndex].getString("algorithm")
            val numberOfTestGroup = cavpTestFile.numberOfTestGroups(algorithmIndex)
            for (testGroupIndex in 0 until numberOfTestGroup) {
                val numberOfTestCases = cavpTestFile.numberOfTestCases(algorithmIndex, testGroupIndex)
                val testType = cavpTestFile.getTestGroup(algorithmIndex, testGroupIndex).getString("testType")
                val direction =
                    if (algorithmName.lowercase().contains("aes")) {
                        cavpTestFile.getTestGroup(algorithmIndex, testGroupIndex).getString("direction")
                    } else {
                        ""
                    }
                val keyLength = if (algorithmName.lowercase().contains("aes")) {
                    cavpTestFile.getTestGroup(algorithmIndex, testGroupIndex).getInt("keyLen")
                } else {
                    0
                }
                for (testCaseIndex in 0 until numberOfTestCases) {
                    val testCaseJson = cavpTestFile.getTestCase(algorithmIndex, testGroupIndex, testCaseIndex)
                    if (algorithmName.lowercase().contains("aes")) {
                        AESEngine.runAESWithTestCase(testCaseJson, direction, algorithmName, keyLength, testType)
                    } else if(algorithmName.lowercase().contains("shake")){  //"shake"裡面有"sha"，所以要先判斷"shake"
                        SHAKEEngine.runSHAKEWithTestCase(testCaseJson, algorithmName, testType)
                    } else if (algorithmName.lowercase().contains("sha")) {
                        SHAEngine.runSHAWithTestCase(testCaseJson, algorithmName, testType)
                    }

                }

            }
        }
        cavpTestFile.saveRspToFolder(saveToFolder)
    }


}


