package views.validationSystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import cavp.CavpTestFile
import com.fazecast.jSerialComm.SerialPort
import kotlinx.coroutines.*
import utils.pickInputFile
import views.PQCLabs.AlertMessageDialog
import views.headerFooter.GordonFooter
import views.headerFooter.LadsTobBar
import views.validationSystem.validationScripts.cavpValidation
import views.validationSystem.validationScripts.sumValidation
import java.util.ArrayList
import kotlin.coroutines.cancellation.CancellationException


enum class SerialTestItem(val testName: String, val testFunction: suspend ( MutableState<String>,MutableState<String>,MutableState<Boolean>, HashMap<String,Any>? ) -> Unit) {
    SUM("sum", { displayLog,sendText,sendStart , _   -> sumValidation(displayLog, sendText, sendStart) }),
    CAVP("CAVP", { displayLog,sendText, sendStart, arguments -> if(arguments!=null) cavpValidation( displayLog,sendText,sendStart,arguments) }),
}

fun printlnTestLog(log: String, displayLog: MutableState<String>) {
    displayLog.value += (log + "\n")
    displayLog.value = displayLog.value.takeLast(1000)
    println(log)
}

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)
@Composable
@Preview
fun SerialValidationSystemScreen() {

    val commList = remember { mutableStateListOf<SerialPort>() }
    val selectedCommPortIndex = remember { mutableStateOf(0) }
    val selectedTestItem = remember { mutableStateOf(SerialTestItem.CAVP) }


    val coroutineScope = rememberCoroutineScope() //coroutine scope
    val serialCoroutineScope = rememberCoroutineScope() //coroutine scope


    val portDropDownMenuExpanded = remember { mutableStateOf(false) }
    val testDropDownMenuExpanded = remember { mutableStateOf(false) }

    var validationRunning by remember { mutableStateOf(false) }
    val displayLog = remember { mutableStateOf("") }
    val deviceOutput = remember { mutableStateOf("") }

    val displayLogScrollState = rememberScrollState()
    val deviceOutputScrollState = rememberScrollState()
    val testSelectScrollState = rememberScrollState()


//    var currentCommPort: SerialPort = commList[selectedCommPortIndex.value]

    var showAlert = remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }

    val changePort = remember { mutableStateOf(false) }
    val sendText = remember { mutableStateOf("") }
    val sendStart = remember { mutableStateOf(false) }



    //CAVP files
    var cavpTestFiles = remember { ArrayList<CavpTestFile>() }  //list of cavp test files
    val loadedFilesInfo = remember { mutableStateOf("no file loaded yet") } //info of loaded files
    val inputFileLoaded = remember { mutableStateOf(false) } //whether input file is loaded
    val outputResult = remember { mutableStateOf("") }  //task result output
    val testAlgorithmList = remember { mutableStateListOf<String>() } //list of test algorithms


    if (showAlert.value) {
        AlertMessageDialog(alertMessage, showAlert)
    }

    //serial port coroutine
    LaunchedEffect(Unit) {
        serialCoroutineScope.launch(Dispatchers.IO) {
            println("coroutine started")
            delay(200)
            var commPorts = SerialPort.getCommPorts()
            while (commPorts.size == 0 || portDropDownMenuExpanded.value) {
                println("getting port")
                delay(1000)
                commPorts = SerialPort.getCommPorts()

            }
            for (port in commPorts) {
                commList.add(port)
            }
            for (i in commPorts.indices) println("comPorts[" + i + "] = " + commPorts[i].descriptivePortName)

            while (isActive) {
                var currentCommPort = commList[selectedCommPortIndex.value]
                currentCommPort.openPort()
                println("open port comPorts[" + selectedCommPortIndex.value + "]  " + currentCommPort.descriptivePortName)
                currentCommPort.setBaudRate(115200)
                try {
                    serialService@ while (isActive) {

                        if (sendStart.value) {
                            sendStart.value = false
                            println("send: ${sendText.value}")
                            val sendString = sendText.value + "\n"
                            val sendByte = sendString.toByteArray()
                            currentCommPort.writeBytes(sendByte, sendByte.size)
                            sendText.value = ""
                            sendStart.value = false
                            println("send start false")
                        }

                        // read serial port and display data
                        while (currentCommPort.bytesAvailable() > 0) {
                            val readBuffer = ByteArray(currentCommPort.bytesAvailable())
                            val numRead = currentCommPort.readBytes(readBuffer, readBuffer.size)
                            //System.out.print("Read " + numRead + " bytes from COM port: ");
                            for (i in readBuffer.indices) print(Char(readBuffer[i].toUShort()))

                            for (i in readBuffer.indices) {
                                if (readBuffer[i].toUShort() == 0x0A.toUShort()) {
                                    deviceOutput.value += "\n"
                                    continue
                                } else if (readBuffer[i].toUShort() == 0x0D.toUShort()) {
                                    continue
                                }
                                deviceOutput.value += Char(readBuffer[i].toUShort())
                                deviceOutput.value = deviceOutput.value.takeLast(1000)
                            }
                        }
                        if (changePort.value) {
                            println("change port")
                            changePort.value = false
                            break@serialService
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                println("port closed")
                currentCommPort.closePort()
            }
            println("coroutine ended")
        }
    }

    //when receivedText changed, scroll to bottom
    LaunchedEffect(deviceOutput.value) {
        deviceOutputScrollState.scrollTo(deviceOutputScrollState.maxValue)
    }


    LaunchedEffect(Unit) {
        val serialPorts = SerialPort.getCommPorts();
        for (port in serialPorts) {
            commList.add(port)
        }
        println("List COM ports")
        delay(200)
        var commPorts = SerialPort.getCommPorts()
        while (commPorts.size == 0) {
            delay(1000)
            commPorts = SerialPort.getCommPorts()
        }
    }


    //when receivedText changed, scroll to bottom
    LaunchedEffect(displayLog.value) {
        displayLogScrollState.scrollTo(displayLogScrollState.maxValue)
    }

    LaunchedEffect(Unit) {
        val job = coroutineScope.coroutineContext.job // this fails if there is no job
        job.invokeOnCompletion { cause ->
            if (cause is CancellationException) {
                // that's a normal cancellation
                if (validationRunning) {
                    println("Job cancelled")
                    println("close port")
                    commList[selectedCommPortIndex.value].closePort() //close the port if the job is cancelled
                }
            }
        }
    }


    //for hiding keyboard and clear focus when click outside of text field
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() } // create an instance of interaction source

    Column(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
            .clickable(
                interactionSource = interactionSource, // pass the interaction source to the clickable
                indication = null // disable the highlight(ripple) on click
            ) {
                focusManager.clearFocus() // clear focus on click
                keyboardController?.hide() // hide the keyboard on click
            }
            .padding(16.dp)
    ) {

        //Top bar
        LadsTobBar("Serial Validation Platform")

        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(2f).fillMaxSize()) {

                //select comm port
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text("Selected Port: ", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        if (commList.size > 0) commList[selectedCommPortIndex.value].systemPortName else "No port connected",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                    ) {
                        IconButton(
                            enabled = !validationRunning,
                            onClick = {
                                if (validationRunning) return@IconButton
                                portDropDownMenuExpanded.value = true
                                coroutineScope.launch(Dispatchers.IO) {
                                    val serialPorts = SerialPort.getCommPorts()
                                    commList.clear()
                                    for (port in serialPorts) {
                                        commList.add(port)
                                    }
                                }
                            }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Localized description")
                        }

                        DropdownMenu(
                            expanded = portDropDownMenuExpanded.value,
                            onDismissRequest = { portDropDownMenuExpanded.value = false }
                        ) {
                            commList.forEachIndexed { index, comm ->
                                DropdownMenuItem(
                                    text = { Text(comm.systemPortName) },
                                    onClick = {
                                        portDropDownMenuExpanded.value = false
                                        selectedCommPortIndex.value = index
                                    }
                                )
                                if (index != commList.size - 1) Divider()
                            }
                        }
                    }


                }

                //select test
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text("Selected test: ", style = MaterialTheme.typography.bodyLarge)
                    Text(selectedTestItem.value.testName, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.width(20.dp))
                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                    ) {
                        IconButton(
                            enabled = !validationRunning,
                            onClick = {
                                if (validationRunning) return@IconButton
                                testDropDownMenuExpanded.value = true
                            }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Localized description")
                        }

                        DropdownMenu(
                            expanded = testDropDownMenuExpanded.value,
                            onDismissRequest = { testDropDownMenuExpanded.value = false }
                        ) {
                            SerialTestItem.values().forEachIndexed { index, item ->
                                DropdownMenuItem(
                                    text = { Text(item.testName) },
                                    onClick = {
                                        testDropDownMenuExpanded.value = false
                                        selectedTestItem.value = item
                                    }
                                )
                                if (index != SerialTestItem.values().size - 1) Divider()
                            }
                        }
                    }


                }



                //select CAVP file
                AnimatedVisibility(
                    selectedTestItem.value == SerialTestItem.CAVP,
                ){
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        Button(enabled = !validationRunning, onClick = {
                            coroutineScope.launch(Dispatchers.Default) {
                                pickInputFile(
                                    cavpTestFiles,
                                    loadedFilesInfo,
                                    inputFileLoaded,
                                    outputResult
                                )
                                //update loadedFileInfo
                                testAlgorithmList.clear()
                                for (cavpTestFile in cavpTestFiles) {
                                    for (i in 0 until cavpTestFile.numberOfAlgorithm) {
                                        val numberOfTestGroup = cavpTestFile.numberOfTestGroups(i)
                                        var algorithm_name = cavpTestFile.algorithmJsonLists[i].getString("algorithm")
                                        if (algorithm_name.lowercase().contains("ecdsa") || algorithm_name.lowercase().contains("rsa")) {
                                            val ecdsaOperationMode = cavpTestFile.algorithmJsonLists[i].getString("mode")
                                            algorithm_name += "-$ecdsaOperationMode"
                                        }
                                        val testCasesCount = cavpTestFile.getNumberOfAllTestCasesOfAlgorithm(i)
                                        testAlgorithmList.add("$algorithm_name ($numberOfTestGroup test groups, $testCasesCount test cases)")
                                    }
                                }
                            }
                        }) {
                            Text("input file")
                        }

                        Text(loadedFilesInfo.value)

                    }
                }



                //File Selection for CAVP
                AnimatedVisibility(
                    selectedTestItem.value == SerialTestItem.CAVP && testAlgorithmList.size > 0,
                    modifier = Modifier.weight(0.8f)
                ){
                    Spacer(modifier = Modifier.height(20.dp))
                    Column(
                        modifier = Modifier.padding(end = 10.dp, bottom = 10.dp,top = 10.dp).fillMaxSize().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(10.dp).verticalScroll(testSelectScrollState)
                    ) {
                        for (testAlgorithm in testAlgorithmList) {
                            Text(testAlgorithm, modifier = Modifier.onClick {
                                if (validationRunning) return@onClick //if running task, return
                                testAlgorithmList.remove(testAlgorithm)
                                for (cavpTestFile in cavpTestFiles) {
                                    val originalName = testAlgorithm.split("(").first().trim()
                                    cavpTestFile.removeAlgorithmWithNameOf(originalName)
                                }
                                //recalculate
                                var allTestCasesCount = 0
                                for (cavpTestFile in cavpTestFiles) {
                                    allTestCasesCount += cavpTestFile.getNumberOfAllTestCases()
                                }
                                //update loadedFileInfo
                                loadedFilesInfo.value = loadedFilesInfo.value.replace(
                                    "found \\d+ test cases".toRegex(),
                                    "found $allTestCasesCount test cases"
                                )
                                outputResult.value = ""
                                if (allTestCasesCount == 0) {
                                    inputFileLoaded.value = false
                                }
                            })
                        }

                    }
                }


                //run button
                AnimatedVisibility(
                    selectedTestItem.value != SerialTestItem.CAVP ||
                            (
                                    selectedTestItem.value == SerialTestItem.CAVP && testAlgorithmList.size > 0
                                    )
                ){
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        enabled = !validationRunning,
                        onClick = {
                            if (validationRunning) return@Button
                            if (commList.size == 0) {
                                alertMessage = "No port connected"
                                showAlert.value = true
                                return@Button
                            }
                            coroutineScope.launch(Dispatchers.IO) {

                                validationRunning = true
                                when(selectedTestItem.value) {
                                    SerialTestItem.SUM -> selectedTestItem.value.testFunction(
                                        displayLog,
                                        sendText,
                                        sendStart,
                                        null
                                    )
                                    SerialTestItem.CAVP -> {
                                        val arguments = HashMap<String, Any>()
                                        arguments["cavpTestFiles"] = cavpTestFiles
                                        selectedTestItem.value.testFunction(
                                            displayLog,
                                            sendText,
                                            sendStart,
                                            arguments
                                        )
                                    }
                                }
                                validationRunning = false
                            }
                        }
                    ) {
                        if (validationRunning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.Black
                            )
                        } else {
                            Text("Start Test")
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(0.2f))

                //bottom part
                GordonFooter()
            }

            Column(modifier = Modifier.weight(1.8f)) {
                Column(
                    modifier = Modifier.weight(0.5f).padding(10.dp).clip(RoundedCornerShape(20.dp)).background(Color.LightGray)
                        .padding(10.dp).fillMaxSize().verticalScroll(displayLogScrollState)
                ) {
                    Text(displayLog.value, style = MaterialTheme.typography.bodyLarge)
                }
                Column(
                    modifier = Modifier.weight(0.5f).padding(10.dp).clip(RoundedCornerShape(20.dp)).background(Color.LightGray)
                        .padding(10.dp).fillMaxSize().verticalScroll(deviceOutputScrollState)
                ) {
                    Text(deviceOutput.value, style = MaterialTheme.typography.bodyLarge)
                }

            }



        }


    }


}