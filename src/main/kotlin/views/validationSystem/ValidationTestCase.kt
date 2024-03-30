package views.validationSystem

class ValidationTestCase(val parameters:HashMap<String, String> = HashMap(), val inputs:MutableList<String> = mutableListOf(), val expectedOutput:MutableList<String> = mutableListOf()){
    companion object{
        val supportedAlgorithmList = hashSetOf(
            "aes",
        )
        val supportedMode:HashMap<String, HashSet<String>> = hashMapOf(
            "aes" to hashSetOf("ecb")
        )

    }


}