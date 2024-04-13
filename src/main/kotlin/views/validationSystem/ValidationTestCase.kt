package views.validationSystem

class ValidationTestCase(val inputMap:HashMap<String, String> = HashMap(), val inputList:MutableList<String> = mutableListOf(), val expectedOutputList:MutableList<String> = mutableListOf(), val expectedOutputMap:HashMap<String, String> = HashMap()){
    companion object{
        val supportedAlgorithmList = hashSetOf(
            "aes",
        )
        val supportedMode:HashMap<String, HashSet<String>> = hashMapOf(
            "aes" to hashSetOf("ecb", "cbc", "cfb8", "cfb128","ctr")
        )

    }


}