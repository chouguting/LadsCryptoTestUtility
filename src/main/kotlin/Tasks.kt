import cavp.CavpTestFile
import tw.edu.ntu.lads.chouguting.java.cipers.AESEngine
import org.json.JSONObject
import tw.edu.ntu.lads.chouguting.java.JsonUtils
import tw.edu.ntu.lads.chouguting.java.cipers.SHAEngine
import java.io.FileWriter
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

fun convertToJsonAndSave(fileList: List<String>, filePathMap: HashMap<String, String>, saveToFolder: String) {

    for (filename in fileList) {
//        val filename: String = currentSelectedFileList.get(index)
        val filenameWithoutFileExtension =
            filename.trim { it <= ' ' }.split("\\.".toRegex(), limit = 2).toTypedArray()[0]
        val newFilename = "$filenameWithoutFileExtension.json"
        if (filename.trim { it <= ' ' }.endsWith(".req")) {
            val filepath: String = filePathMap[filename] ?: throw RuntimeException("file path not found")
            val jsonobject = JsonUtils.reqFileToJson(filepath)
            JsonUtils.saveJsonToFile(saveToFolder, newFilename, jsonobject)
        } else if (filename.trim { it <= ' ' }.endsWith(".json")) {
            val filepath: String = filePathMap.get(filename) ?: throw RuntimeException("file path not found")
            //if the file is json, just copy it
            try {
                val content = Files.readString(Path.of(filepath), StandardCharsets.UTF_8)
                val fileWriter: FileWriter = FileWriter(saveToFolder + "\\" + newFilename)
                fileWriter.write(content)
                fileWriter.close()
            } catch (ex: IOException) {
                throw RuntimeException(ex)
            }
        }
    }
}

fun aesAndSave(mode: Int, fileList: List<String>, filePathMap: HashMap<String, String>, saveToFolder: String) {
    //mode 0: aes to json
    //mode 1: aes to rsp
    println("do AES")
//    val selectedIndex: IntArray = loadedFileJList.getSelectedIndices() //get selected index
//    currentConvertedFileList = ArrayList<String>() //clear current converted file list
    for (filename in fileList) {
//        val filename: String = currentSelectedFileList.get(index) //get filename
        val filenameWithoutFileExtension =
            filename.trim { it <= ' ' }.split("\\.".toRegex(), limit = 2)
                .toTypedArray()[0] //get filename without extension
        var jsonobject: JSONObject? = null
        if (filename.trim { it <= ' ' }.endsWith(".req")) {
            val filepath: String = filePathMap.get(filename) ?: throw RuntimeException("file path not found")
            jsonobject = JsonUtils.reqFileToJson(filepath)
        } else if (filename.trim { it <= ' ' }.endsWith(".json")) {
            val filepath: String = filePathMap.get(filename) ?: throw RuntimeException("file path not found")
            jsonobject = try {
                val content = Files.readString(Path.of(filepath), StandardCharsets.UTF_8)
                JSONObject(content)
            } catch (ex: IOException) {
                throw java.lang.RuntimeException(ex)
            }
        }
        val resultJson = AESEngine.runAESWithJson(jsonobject) //run AES
        var newFilename = ""
        if (mode == 0) {
            newFilename = filenameWithoutFileExtension + "_result" + ".json" //new file extension is json
            JsonUtils.saveJsonToFile(saveToFolder, newFilename, resultJson) //save to file
        } else if (mode == 1) {
            newFilename = "$filenameWithoutFileExtension.rsp" //new file extension is rsp
            JsonUtils.saveJsonToRspFile(saveToFolder, newFilename, resultJson) //save to rsp file
        }
    }

}


fun runAndSave(cavpTestFiles: ArrayList<CavpTestFile>, saveToFolder: String) {
    for (cavpTestFile in cavpTestFiles) {
        val numberOfAlgorithm = cavpTestFile.numberOfAlgorithm
        for (algorithmIndex in 0 until numberOfAlgorithm) {
            val algorithmName = cavpTestFile.algorithmJsonLists[algorithmIndex].getString("algorithm")
            val numberOfTestGroup = cavpTestFile.numberOfTestGroups(algorithmIndex)
            for (testGroupIndex in 0 until numberOfTestGroup) {
                val numberOfTestCases = cavpTestFile.numberOfTestCases(algorithmIndex, testGroupIndex)
                val direction =
                    if (algorithmName.lowercase().contains("aes")) {
                        cavpTestFile.getTestGroup(algorithmIndex, testGroupIndex).getString("direction")
                    } else {
                        ""
                    }
                for (testCaseIndex in 0 until numberOfTestCases) {
                    val testCaseJson = cavpTestFile.getTestCase(algorithmIndex, testGroupIndex, testCaseIndex)
                    if (algorithmName.lowercase().contains("aes")) {
                        AESEngine.runAESWithTestCase(testCaseJson, direction, algorithmName)
                    } else if (algorithmName.lowercase().contains("sha")) {
                        SHAEngine.runSHAWithTestCase(testCaseJson, algorithmName)
                    }

                }

            }
        }
        cavpTestFile.saveRspToFolder(saveToFolder)
    }


}


