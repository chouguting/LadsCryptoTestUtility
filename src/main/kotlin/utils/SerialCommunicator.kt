package utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fazecast.jSerialComm.SerialPort
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SerialCommunicator(val coroutineScope: CoroutineScope) {
    val commList = mutableStateListOf<SerialPort>()
    val commListNames = mutableStateListOf<String>()

    val selectedCommPortIndex = mutableStateOf(0)
    val dropDownMenuExpanded = mutableStateOf(false)
    val commPortSelected = mutableStateOf(false)
    var currentCommPort: SerialPort? = null
    val sendDataFIFO = mutableStateListOf<String>()
    val sentText = mutableStateOf("")
    val fifoMutex = Mutex()
    val portMutex = Mutex()
    val receivedText = mutableStateOf("")


    fun getCommList() {
        commList.clear()
        commListNames.clear()
        val ports = SerialPort.getCommPorts()
        for (port in ports) {
            commList.add(port)
            commListNames.add(port.systemPortName)
        }
    }

    fun changeCommPort() {
        currentCommPort?.closePort().also {
            println("port closed")
        }
        if (commList.size == 0) return
        currentCommPort = commList[selectedCommPortIndex.value]
        currentCommPort?.openPort().also {
            currentCommPort?.setBaudRate(115200)
        }.also {
            println("port opened")
        }
    }


    fun sendTextToDevice(text: String) {
        coroutineScope.launch(Dispatchers.Default) {
            fifoMutex.withLock {
                //critical section，代表只有一個coroutine可以進入
                sentText.value += text
                sentText.value = sentText.value.takeLast(1000)
                sendDataFIFO.add(text)
            }
        }
    }


    @Composable
    fun setUpCommPort() {
        DisposableEffect(Unit) { //initial get ports
            coroutineScope.launch(Dispatchers.IO) {
                while (isActive && commList.size == 0) {
                    println("getting ports")
                    getCommList()
                    delay(1000)
                }
                currentCommPort = commList[selectedCommPortIndex.value]
                currentCommPort?.openPort().also {
                    currentCommPort?.setBaudRate(115200)
                }.also {
                    commPortSelected.value = true
                    println("port opened")
                }


            }
            onDispose {
                currentCommPort?.closePort().also {  //close port when component is disposed
                    println("port closed")
                }
            }
        }
    }


    @Composable
    fun createCommunication() {
        LaunchedEffect(Unit) {
            //send data
            coroutineScope.launch(Dispatchers.IO) {
                while (isActive && currentCommPort == null) {
                    delay(1000)
                }
                while (isActive) {
                    if (sendDataFIFO.size > 0) {
                        var sendString = ""
                        fifoMutex.withLock {
                            val data = sendDataFIFO.removeAt(0)
                            sendString = data + "\n"
                        }
                        val sendByte = sendString.toByteArray()
                        portMutex.withLock {
                            currentCommPort?.writeBytes(sendByte, sendByte.size)
                        }
                    }
                }
            }


            //receive data
            coroutineScope.launch(Dispatchers.IO) {
                while (isActive && currentCommPort == null) {
                    delay(1000)
                }
                // read serial port and display data
                while (isActive) {
                    while (currentCommPort != null && currentCommPort!!.bytesAvailable() > 0) {
                        val readBuffer = ByteArray(currentCommPort!!.bytesAvailable())
                        portMutex.withLock {
                            val numRead = currentCommPort!!.readBytes(readBuffer, readBuffer.size)
                        }
                        for (i in readBuffer.indices) print(Char(readBuffer[i].toUShort()))
                        var currentReceivedText = ""
                        for (i in readBuffer.indices) {
                            if (readBuffer[i].toUShort() == 0x0A.toUShort()) {
                                currentReceivedText += "\n"
                                continue
                            } else if (readBuffer[i].toUShort() == 0x0D.toUShort()) {
                                continue
                            }
                            currentReceivedText += Char(readBuffer[i].toUShort())
                        }
                        receivedText.value += currentReceivedText
                        receivedText.value = receivedText.value.takeLast(10000)
                    }
                }
            }
        }
    }


    @Composable
    fun commPortSelector(
        verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
        horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(20.dp),
        modifier: Modifier = Modifier,
    ) {

        //select comm port
        Row(
            verticalAlignment = verticalAlignment,
            horizontalArrangement = horizontalArrangement,
            modifier = modifier
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
                                changeCommPort()
                            }
                        )
                        if (index != commList.size - 1) Divider()
                    }
                }
            }


        }
    }

    @Composable
    fun connectingToCommPortIndicator() {
        AnimatedVisibility(visible = !commPortSelected.value){
            Column(modifier = Modifier, horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    modifier = Modifier.size(30.dp),
                    strokeWidth = 3.dp,
                    color = Color.Black
                )
                Text("Connecting to the device...")
            }
        }
    }

}