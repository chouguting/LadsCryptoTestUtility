package cavp

import org.json.JSONArray
import org.json.JSONObject
import java.io.FileWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class CavpTestFile() {
//    lateinit var topJsonArray: JSONArray
    lateinit var headerJSONObject: JSONObject
    lateinit var filename: String
    val algorithmJsonLists = ArrayList<JSONObject>()
    val numberOfAlgorithm:Int
        get() = algorithmJsonLists.size




    constructor(filePath: String) : this() {
        val jsonString = Files.readString(Path.of(filePath), StandardCharsets.UTF_8)
        val filename = filePath.trim().split("\\").last()
        this.filename = filename

        //top is an array
        //first element is the meta data
        val topJsonArray = JSONArray(jsonString)
        if(topJsonArray.length() < 2){
            throw Exception("invalid CAVP test file: $filename")
        }
        headerJSONObject = topJsonArray.getJSONObject(0)
        for(i in 1 until topJsonArray.length()){
            val taskJson = topJsonArray.getJSONObject(i)
            algorithmJsonLists.add(taskJson)
        }
    }

    //decompose test group
    fun numberOfTestGroups(algorithmIndex:Int):Int{
        val algorithmJson = algorithmJsonLists[algorithmIndex]
        val testGroupsJson = algorithmJson.getJSONArray("testGroups")
        return testGroupsJson.length()
    }

    fun getTestGroup(algorithmIndex:Int, testGroupIndex:Int):JSONObject{
        val algorithmJson = algorithmJsonLists[algorithmIndex]
        val testGroupsJson = algorithmJson.getJSONArray("testGroups")
        return testGroupsJson.getJSONObject(testGroupIndex)
    }

    //decompose test cases
    fun numberOfTestCases(algorithmIndex:Int, testGroupIndex:Int):Int{
        val testGroupJson = getTestGroup(algorithmIndex, testGroupIndex)
        val testSetsJson = testGroupJson.getJSONArray("tests")
        return testSetsJson.length()
    }

    fun getTestCase(algorithmIndex:Int, testGroupIndex:Int, testSetIndex:Int):JSONObject{
        val testGroupJson = getTestGroup(algorithmIndex, testGroupIndex)
        val testSetsJson = testGroupJson.getJSONArray("tests")
        return testSetsJson.getJSONObject(testSetIndex)
    }

    fun getNumberOfAllTestCasesOfAlgorithm(algorithmIndex:Int):Int{
        var numberOfAllTestCases = 0
        for(j in 0 until numberOfTestGroups(algorithmIndex)){
            numberOfAllTestCases += numberOfTestCases(algorithmIndex, j)
        }
        return numberOfAllTestCases
    }
    fun getNumberOfAllTestCases():Int{
        var numberOfAllTestCases = 0
        for(i in 0 until numberOfAlgorithm){
            for(j in 0 until numberOfTestGroups(i)){
                numberOfAllTestCases += numberOfTestCases(i, j)
            }
        }
        return numberOfAllTestCases
    }

    fun removeAlgorithmWithNameOf(name:String){
        for(i in 0 until numberOfAlgorithm){
            val algorithmName = algorithmJsonLists[i].getString("algorithm")
            if(name == algorithmName){
                algorithmJsonLists.removeAt(i)
                break
            }
        }
    }

    fun saveRspToFolder(saveToThisFolder:String){
        var fileNameWithoutExtension = filename
        if(fileNameWithoutExtension.contains(".json")){
            fileNameWithoutExtension= fileNameWithoutExtension.replace(".json", "")
        }
        if(fileNameWithoutExtension.contains("_req")){
            fileNameWithoutExtension = fileNameWithoutExtension.replace("_req", "")
        }
        val newFileName = "${fileNameWithoutExtension}_rsp.json"
        val fileWriter = FileWriter("$saveToThisFolder\\$newFileName")
        val outputJsonArray = JSONArray()
        outputJsonArray.put(headerJSONObject)
        for(i in 0 until numberOfAlgorithm){
            outputJsonArray.put(algorithmJsonLists[i])
        }
        fileWriter.write(outputJsonArray.toString(8))
        fileWriter.close()

    }

}

