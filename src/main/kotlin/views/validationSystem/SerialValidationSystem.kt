package views.validationSystem

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
import com.fazecast.jSerialComm.SerialPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import views.PQCLabs.AlertMessageDialog
import views.headerFooter.GordonFooter
import views.headerFooter.LadsTobBar
import kotlin.coroutines.cancellation.CancellationException


enum class SerialTestItem(val testName: String, val testFunction: suspend (SerialPort, MutableState<String>) -> Unit) {
    SUM("sum", { serialPort: SerialPort, displayLog: MutableState<String> -> sumValidation(serialPort, displayLog) }),
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
    val selectedTestItem = remember { mutableStateOf(SerialTestItem.SUM) }


    val coroutineScope = rememberCoroutineScope() //coroutine scope
//    val serialCoroutineScope = rememberCoroutineScope() //coroutine scope


    val portDropDownMenuExpanded = remember { mutableStateOf(false) }
    val testDropDownMenuExpanded = remember { mutableStateOf(false) }

    var validationRunning by remember { mutableStateOf(false) }
    val displayLog = remember { mutableStateOf("") }

    val receivedScrollState = rememberScrollState()

//    var currentCommPort: SerialPort = commList[selectedCommPortIndex.value]

    var showAlert = remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }

    if (showAlert.value) {
        AlertMessageDialog(alertMessage, showAlert)
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
        receivedScrollState.scrollTo(receivedScrollState.maxValue)
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
                Spacer(modifier = Modifier.height(20.dp))


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
                            selectedTestItem.value.testFunction(commList[selectedCommPortIndex.value], displayLog)
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



                Spacer(modifier = Modifier.weight(1f))


                //bottom part
                GordonFooter()
            }

            Column(
                modifier = Modifier.weight(1.8f).clip(RoundedCornerShape(20.dp)).background(Color.LightGray)
                    .padding(10.dp).fillMaxSize().verticalScroll(receivedScrollState)
            ) {
                Text(displayLog.value, style = MaterialTheme.typography.bodyLarge)
            }

        }


    }


}