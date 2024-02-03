import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.fazecast.jSerialComm.SerialPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
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

    val receivedText = remember { mutableStateOf("") }

    val receivedScrollState = rememberScrollState()


    LaunchedEffect(true) {
        val serialPorts = SerialPort.getCommPorts();
        for (port in serialPorts) {
            commList.add(port)
        }
    }

    //serial port coroutine
    LaunchedEffect(serialCoroutineScope){
        serialCoroutineScope.launch(Dispatchers.IO) {
            val console = Scanner(System.`in`)
            println("List COM ports")
            delay(200)
            var commPorts = SerialPort.getCommPorts()
            while (commPorts.size == 0) {
                delay(1000)
                commPorts = SerialPort.getCommPorts()
            }
            for (i in commPorts.indices) println("comPorts[" + i + "] = " + commPorts[i].descriptivePortName)
            val port = 0 // array index to select COM port
            var currentCommPort = commPorts[port]
            currentCommPort.openPort()
            println("open port comPorts[" + port + "]  " + commPorts[port].descriptivePortName)
            currentCommPort.setBaudRate(115200)
            try {
                while (true) {
                    // if keyboard token entered read it
                    if (System.`in`.available() > 0) {
                        //System.out.println("enter chars ");
                        val s = console.nextLine() + "\n" // read token
                        val writeBuffer = s.toByteArray()
                        currentCommPort.writeBytes(writeBuffer, writeBuffer.size)
                        //System.out.println("write " + writeBuffer.length);
                    }
                    if(sendStart.value){
                        val sendString = sendText.value
                        val sendByte = sendString.toByteArray()
                        println("send: ${String(sendByte)}")
                        currentCommPort.writeBytes(sendByte, sendByte.size)
                        sendText.value = ""
                        sendStart.value = false
                    }

                    // read serial port  and display data
                    while (currentCommPort.bytesAvailable() > 0) {
                        val readBuffer = ByteArray(currentCommPort.bytesAvailable())
                        val numRead = currentCommPort.readBytes(readBuffer, readBuffer.size)
                        //System.out.print("Read " + numRead + " bytes from COM port: ");
                        for (i in readBuffer.indices) print(Char(readBuffer[i].toUShort()))

                        for (i in readBuffer.indices){
                            if(readBuffer[i].toUShort() == 0x0A.toUShort()) {
                                receivedText.value += "\n"
                                continue
                            }else if(readBuffer[i].toUShort() == 0x0D.toUShort()){
                                continue
                            }
                            receivedText.value += Char(readBuffer[i].toUShort())
                            receivedText.value = receivedText.value.takeLast(1000)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            commPorts[port].closePort()
        }
    }

    //when receivedText changed, scroll to bottom
    LaunchedEffect(receivedText.value){
        receivedScrollState.scrollTo(receivedScrollState.maxValue)
    }


    Column(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
            .padding(16.dp)
    ) {

        //Top bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            //.background(Color.Cyan)
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Serial Tool",
                fontFamily = FontFamily.SansSerif,
                style = MaterialTheme.typography.headlineLarge
            )


            Spacer(Modifier.weight(1f))
            Image(
                painter = painterResource("images/LaDS_logo.png"),
                contentDescription = "LaDS Logo",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.size(width = 200.dp, height = 100.dp)
            )
        }

        //input file selection
        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(2f).fillMaxSize()) {

                //select comm port
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text("Selected Port: ", style = MaterialTheme.typography.bodyLarge)
                    Text(if (commList.size > 0) commList[selectedCommPortIndex.value].systemPortName else "No port connected",style = MaterialTheme.typography.bodyLarge)
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
                                    }
                                   )
                                if(index != commList.size - 1) Divider()
                            }
                        }
                    }


                }

                TextField(
                    value = sendText.value,

                    onValueChange = { sendText.value = it },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(20.dp).height(300.dp).weight(1f).fillMaxWidth().clip(RoundedCornerShape(20.dp))

                )


                //send button
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()){
                    Button(onClick = {
                        if(commList.size == 0) return@Button
                        if(sendText.value.isBlank()) return@Button
//                        val sendByte = sendText.value.toByteArray()
//                        coroutineScope.launch(Dispatchers.IO) {
//                            commList[selectedCommPortIndex.value].openPort()
//                            commList[selectedCommPortIndex.value].setBaudRate(115200)
//                            commList[selectedCommPortIndex.value].writeBytes(sendByte, sendByte.size)
//                            println("send: ${String(sendByte)}")
//                            commList[selectedCommPortIndex.value].closePort()
//                            sendText.value = ""
//                        }
                        sendStart.value = true


                    }, modifier = Modifier.padding(20.dp,0.dp)){
                        Text("Send", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(Icons.Outlined.Send, contentDescription = "Localized description")
                    }
                }


                //bottom part
                Text(
                    "developed by Gordon Chou @ NTU LaDS 2023",
                    fontFamily = FontFamily.SansSerif,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Column(
                modifier = Modifier.weight(1.8f).clip(RoundedCornerShape(20.dp)).background(Color.LightGray)
                    .padding(10.dp).fillMaxSize().verticalScroll(receivedScrollState)
            ) {
                Text(receivedText.value, style = MaterialTheme.typography.bodyLarge)
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