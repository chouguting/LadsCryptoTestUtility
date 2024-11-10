package views.serialTool

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.fazecast.jSerialComm.SerialPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import views.headerFooter.GordonFooter
import views.headerFooter.LadsTobBar
import java.util.*

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)
@Composable
@Preview
fun SerialToolScreen() {

    val commList = remember { mutableStateListOf<SerialPort>() }
    val selectedCommPortIndex = remember { mutableStateOf(0) }


    val coroutineScope = rememberCoroutineScope() //coroutine scope
    val serialCoroutineScope = rememberCoroutineScope() //coroutine scope

    val dropDownMenuExpanded = remember { mutableStateOf(false) }

    val sendText = remember { mutableStateOf("") }
    val sendStart = remember { mutableStateOf(false) }
    val changePort = remember { mutableStateOf(false) }

    val receivedText = remember { mutableStateOf("") }

    val receivedScrollState = rememberScrollState()


    //serial port coroutine
//    LaunchedEffect(Unit) {
//        serialCoroutineScope.launch(Dispatchers.IO) {
//
//            println("coroutine started")
//            delay(200)
//
//            var commPorts = SerialPort.getCommPorts()
//            while (commPorts.size == 0 || dropDownMenuExpanded.value) {
//                println("getting port")
//                delay(1000)
//                commPorts = SerialPort.getCommPorts()
//
//            }
//
//            for (port in commPorts) {
//                commList.add(port)
//            }
//            for (i in commPorts.indices) println("comPorts[" + i + "] = " + commPorts[i].descriptivePortName)
//
//            while (isActive) {
//                var currentCommPort = commList[selectedCommPortIndex.value]
//                currentCommPort.openPort()
//                println("open port comPorts[" + selectedCommPortIndex.value + "]  " + currentCommPort.descriptivePortName)
//                currentCommPort.setBaudRate(115200)
//                try {
//                    serialService@ while (isActive) {
//
//                        if (sendStart.value) {
//                            sendStart.value = false
//                            println("send: ${sendText.value}")
//                            val sendString = sendText.value + "\n"
//                            val sendByte = sendString.toByteArray()
//                            currentCommPort.writeBytes(sendByte, sendByte.size)
//                            sendText.value = ""
//                            sendStart.value = false
//                            println("send start false")
//                        }
//
//                        // read serial port and display data
//                        while (currentCommPort.bytesAvailable() > 0) {
//                            val readBuffer = ByteArray(currentCommPort.bytesAvailable())
//                            val numRead = currentCommPort.readBytes(readBuffer, readBuffer.size)
//                            //System.out.print("Read " + numRead + " bytes from COM port: ");
//                            for (i in readBuffer.indices) print(Char(readBuffer[i].toUShort()))
//
//                            for (i in readBuffer.indices) {
//                                if (readBuffer[i].toUShort() == 0x0A.toUShort()) {
//                                    receivedText.value += "\n"
//                                    continue
//                                } else if (readBuffer[i].toUShort() == 0x0D.toUShort()) {
//                                    continue
//                                }
//                                receivedText.value += Char(readBuffer[i].toUShort())
//                                receivedText.value = receivedText.value.takeLast(10000)
//                            }
//                        }
//                        if (changePort.value) {
//                            println("change port")
//                            changePort.value = false
//                            break@serialService
//                        }
//                    }
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//                println("port closed")
//                currentCommPort.closePort()
//            }
//
//
//            println("coroutine ended")
//        }
//    }

    //use DisposableEffect instead of LaunchedEffect can cancel the coroutine when the composable is disposed
    //whenever the composable is disposed or the key changes, onDispose will be called
    DisposableEffect(Unit) {
        val job = serialCoroutineScope.launch(Dispatchers.IO) {
            println("coroutine started")
            delay(200)

            var commPorts = SerialPort.getCommPorts()
            while (commPorts.isEmpty() || dropDownMenuExpanded.value) {
                println("getting port")
                delay(1000)
                commPorts = SerialPort.getCommPorts()
            }

            commList.clear()
            commList.addAll(commPorts)
            commPorts.forEachIndexed { i, port -> println("comPorts[$i] = ${port.descriptivePortName}") }

            while (isActive) {
                val currentCommPort = commList[selectedCommPortIndex.value]
                currentCommPort.openPort()
                println("open port comPorts[${selectedCommPortIndex.value}]  ${currentCommPort.descriptivePortName}")
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
                            for (i in readBuffer.indices) print(Char(readBuffer[i].toUShort()))

                            for (i in readBuffer.indices) {
                                if (readBuffer[i].toUShort() == 0x0A.toUShort()) {
                                    receivedText.value += "\n"
                                    continue
                                } else if (readBuffer[i].toUShort() == 0x0D.toUShort()) {
                                    continue
                                }
                                receivedText.value += Char(readBuffer[i].toUShort())
                                receivedText.value = receivedText.value.takeLast(10000)
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

        onDispose( {
            job.cancel() //although coroutineScope is cancelled when the composable is disposed, it is a good practice to cancel the job explicitly
            commList.forEach { it.closePort() } // close all ports
            println("DisposableEffect disposed") // print a message when the DisposableEffect is disposed
        })
    }

    //when receivedText changed, scroll to bottom
    LaunchedEffect(receivedText.value) {
        receivedScrollState.scrollTo(receivedScrollState.maxValue)
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
        LadsTobBar("Serial Tool")

        //comm port select and send text
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
                        IconButton(onClick = {
                            dropDownMenuExpanded.value = true
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
                            expanded = dropDownMenuExpanded.value,
                            onDismissRequest = { dropDownMenuExpanded.value = false }
                        ) {
                            commList.forEachIndexed { index, comm ->
                                DropdownMenuItem(
                                    text = { Text(comm.systemPortName) },
                                    onClick = {
                                        dropDownMenuExpanded.value = false
                                        selectedCommPortIndex.value = index
                                        changePort.value = true
                                    }
                                )
                                if (index != commList.size - 1) Divider()
                            }
                        }
                    }


                }

                TextField(
                    value = sendText.value,

                    onValueChange = { sendText.value = it },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(20.dp).height(300.dp).weight(1f).fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp)).onKeyEvent { keyEvent ->
                        if (keyEvent.key != Key.Enter && keyEvent.key != Key.NumPadEnter) return@onKeyEvent false //如果不是Enter鍵就不處理，回傳false代表事件傳遞給下一個處理者
                        if (commList.size == 0) return@onKeyEvent true
                        if (sendText.value.isBlank()) return@onKeyEvent true
                        sendStart.value = true //按下Enter鍵時，觸發sendStart

                        true //回傳true表示事件已經被處理
                        //因為預設按下enter時，focus會被清除，所以這裡要回傳true，表示事件已經被處理，不要再傳遞給下一個處理者
                    }

                )


                //send button
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = {
                        if (commList.size == 0) return@Button
                        if (sendText.value.isBlank()) return@Button
                        sendStart.value = true
                    }, modifier = Modifier.padding(20.dp, 0.dp)) {
                        Text("Send", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(Icons.Outlined.Send, contentDescription = "Localized description")
                    }
                }


                //bottom part
                GordonFooter()
            }


            Row(
                modifier = Modifier.weight(1.8f).clip(RoundedCornerShape(20.dp)).background(Color.LightGray)
                    .padding(10.dp).fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f).fillMaxSize().verticalScroll(receivedScrollState),
                ) {
                    SelectionContainer() {
                        Text(receivedText.value, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                IconButton(onClick = {
                    receivedText.value = ""
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "clear received text")
                }
                VerticalScrollbar(
                    rememberScrollbarAdapter(receivedScrollState),
                    Modifier.fillMaxHeight()
                )

            }


        }


    }


}

fun main() {
    //test serial port
    val serialPorts = SerialPort.getCommPorts();
    for (port in serialPorts) {
        println(port.systemPortName)
    }
}