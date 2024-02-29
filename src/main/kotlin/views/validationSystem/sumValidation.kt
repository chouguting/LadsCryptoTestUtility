package views.validationSystem

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.ExperimentalComposeUiApi
import com.fazecast.jSerialComm.SerialPort
import kotlinx.coroutines.delay

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)
@Composable
@Preview
fun sumValidationScreen() {

}


suspend fun sumValidation(
    currentCommPort: SerialPort,
    displayLog: MutableState<String>,
//    stopTest: MutableState<Boolean>
) {
    currentCommPort.openPort()
    currentCommPort.setBaudRate(115200)
    for (i in 0..2) {
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
        var sendByte = sendString.toByteArray()
        currentCommPort.writeBytes(sendByte, sendByte.size)

        sendString = randomString2 + "\n"
        sendByte = sendString.toByteArray()
        currentCommPort.writeBytes(sendByte, sendByte.size)

        //receive
        //wait for 5 seconds max
        // if no response, then stop the test
        val startTime = System.currentTimeMillis()
        var resultString = ""
        val waitTime = 3000
        printlnTestLog("waiting for response", displayLog)
        responses@ while (System.currentTimeMillis() - startTime < waitTime) {
            printlnTestLog("time left: ${waitTime - (System.currentTimeMillis() - startTime)}", displayLog)
            if (currentCommPort.bytesAvailable() > 0) {
                val readBuffer = ByteArray(currentCommPort.bytesAvailable())
                val numRead = currentCommPort.readBytes(readBuffer, readBuffer.size)
//                for (i in readBuffer.indices) print(Char(readBuffer[i].toUShort()))

                for (i in readBuffer.indices) {
                    if (readBuffer[i].toUShort() == 0x0A.toUShort()) {
                        resultString += "\n"
                        continue
                    } else if (readBuffer[i].toUShort() == 0x0D.toUShort()) {
                        continue
                    }
                    resultString += Char(readBuffer[i].toUShort())
                }
                if (resultString.trim().replace("\n", "") == answerString) {
                    break@responses
                }
//                break@responses
            }
            delay(1000)
        }
        resultString = resultString.trim().replace("\n", "")
        printlnTestLog("received: ${resultString}", displayLog)
        if (resultString == answerString) {
            printlnTestLog("PASS", displayLog)
        } else {
            printlnTestLog("FAIL , expected: ${answerString}, received: ${resultString}", displayLog)
        }
        printlnTestLog("=====================================\n", displayLog)
        delay(1000)
    }

    currentCommPort.closePort()

}