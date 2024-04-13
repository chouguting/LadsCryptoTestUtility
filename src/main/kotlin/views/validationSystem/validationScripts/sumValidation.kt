package views.validationSystem.validationScripts

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.ExperimentalComposeUiApi
import com.fazecast.jSerialComm.SerialPort
import kotlinx.coroutines.delay
import views.validationSystem.TestControlBundle
import views.validationSystem.printlnTestLog

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)
@Composable
@Preview
fun sumValidationScreen() {

}


suspend fun sumValidation(
//    displayLog: MutableState<String>,
//    sendText: MutableState<String>,
    testControlBundle: TestControlBundle,
//    sendStart: MutableState<Boolean>,
//    stopTest: MutableState<Boolean>
) {
    val displayLog = testControlBundle.displayLog
    val sendText = testControlBundle.sendText
    val sendStart = testControlBundle.sendStart

    for (i in 0..20) {
        printlnTestLog("=====================================", displayLog)
        printlnTestLog("Test ${i + 1}", displayLog)
        //two random number
        val random1 = (0..100).random()
        val randomString1 = random1.toString()

        val random2 = (0..100).random()
        val randomString2 = random2.toString()

        val answer = random1 + random2
        val answerString = answer.toString()


        //send
        printlnTestLog("send: ${randomString1} + ${randomString2}", displayLog)
        var sendString = randomString1 + "\n"
        sendText.value = randomString1
        sendStart.value = true
        while (sendStart.value) {
            delay(100)
        }


        sendString = randomString2 + "\n"
        sendText.value = randomString2
        sendStart.value = true
        while (sendStart.value) {
            delay(100)
        }

        printlnTestLog("expected output: ${answer}", displayLog)

        //receive
        //wait for 5 seconds max
        // if no response, then stop the test


//        resultString = resultString.trim().replace("\n", "")
//        printlnTestLog("received: ${resultString}", displayLog)
//        if (resultString == answerString) {
//            printlnTestLog("PASS", displayLog)
//        } else {
//            printlnTestLog("FAIL , expected: ${answerString}, received: ${resultString}", displayLog)
//        }
        printlnTestLog("=====================================\n", displayLog)
        delay(1000)
    }



}