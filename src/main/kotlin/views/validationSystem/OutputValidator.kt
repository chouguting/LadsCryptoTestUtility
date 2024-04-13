package views.validationSystem

class OutputValidator {

    private val answerPool = HashSet<String>()
    var hasValidationTask = false
    var foundAnswer = false

    fun addValidationTask(answer: String) {
        val answerLowercase = answer.lowercase()
        answerPool.add(answerLowercase)
        hasValidationTask = true
//        println("(setValidationTask) set validation task: $answer")
    }

    fun validate(value: String) {
        if(!hasValidationTask) return
        val lowerCaseValue = value.lowercase()
        if(lowerCaseValue in answerPool){
            answerPool.remove(lowerCaseValue)
            println("(validator) found answer: $lowerCaseValue")
            if(answerPool.isEmpty()){
                foundAnswer = true
            }
        }
        if(lowerCaseValue.contains(":")){
            val valueList = lowerCaseValue.split(":")
            findMatch@for(split in valueList){
                for(answer in answerPool){
                    if(split.trim().contains(answer)){
                        answerPool.remove(answer)
                        println("(validator) found answer: $answer")
                        if(answerPool.isEmpty()){
                            foundAnswer = true
                            break@findMatch
                        }
                    }
                }

            }
        }
    }

    fun reset(){
        hasValidationTask = false
        foundAnswer = false
        answerPool.clear()
//        println("(reset) output validator")
    }
}
