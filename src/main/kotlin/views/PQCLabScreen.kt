package views

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.fazecast.jSerialComm.SerialPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Year
import java.util.*


@OptIn(
    ExperimentalFoundationApi::class, ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)
@Composable
@Preview
fun PQCLabScreen() {

    val commList = remember { mutableStateListOf<SerialPort>() }
    val selectedCommPortIndex = remember { mutableStateOf(0) }


    val coroutineScope = rememberCoroutineScope() //coroutine scope
    val serialCoroutineScope = rememberCoroutineScope() //coroutine scope

    val dropDownMenuExpanded = remember { mutableStateOf(false) }

    val sendText = remember { mutableStateOf("") }
    val sendStart = remember { mutableStateOf(false) }

    val receivedText = remember { mutableStateOf("") }

    val receivedScrollState = rememberScrollState()



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
        LadsTobBar("PQC Lab")

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
                GordonFooter()
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