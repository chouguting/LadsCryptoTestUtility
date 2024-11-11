package views.cavpTest

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cavpTestUtils.CavpTestFile
import cavpTestUtils.runCavp
import cavpTestUtils.runHardwareCavp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import utils.SerialCommunicator
import utils.pickInputFile
import utils.pickOutputFolder
import views.headerFooter.GordonFooter
import views.headerFooter.LadsTobBar
import java.util.ArrayList


@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
@Preview
fun CavpHardwareTestScreen() {

    val coroutineScope = rememberCoroutineScope()
    val serialCommunicator = remember { SerialCommunicator(coroutineScope) }
    serialCommunicator.setUpCommPort()
    serialCommunicator.createCommunication()
    val sendData = remember { mutableStateOf("") }

    //CAVP stuff
    val loadingInputFile = remember { mutableStateOf(false) } //whether is loading input file
    val testAlgorithmList = remember { mutableStateListOf<String>() } //list of test algorithms
    var cavpTestFiles = remember { ArrayList<CavpTestFile>() }  //list of cavp test files


    val loadedFilesInfo = remember { mutableStateOf("no file loaded yet") } //info of loaded files
    val inputFileLoaded = remember { mutableStateOf(false) } //whether input file is loaded

    val saveToFolderInfo = remember { mutableStateOf("please select the output folder") } //info of save to folder
    val saveToThisFolder = remember { mutableStateOf("") }  //save to this folder
    val saveToFolderSelected = remember { mutableStateOf(false) }  //whether save to folder is selected

    val outputResult = remember { mutableStateOf("") }  //task result output

    val runningTask = remember { mutableStateOf(false) } //whether is running tasks


    //scroll state
    val testListScrollState = rememberScrollState()
    val sentToDeviceScrollState = rememberScrollState()
    val receivedFromDeviceScrollState = rememberScrollState()

    LaunchedEffect(serialCommunicator.sentText.value) {
        sentToDeviceScrollState.animateScrollTo(sentToDeviceScrollState.maxValue)
    }

    LaunchedEffect(serialCommunicator.receivedText.value) {
        receivedFromDeviceScrollState.animateScrollTo(receivedFromDeviceScrollState.maxValue)
    }

    //progress
    val testProgress = remember { mutableStateOf(0f) }



    Column(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
            .padding(16.dp)
    ) {

        //Top bar
        LadsTobBar("CAVP Hardware Test")

        //input file selection
        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(1.2f).fillMaxSize()) {
                serialCommunicator.commPortSelector(dropDownMenuEnable = !runningTask.value)
                serialCommunicator.connectingToCommPortIndicator()

                AnimatedVisibility(visible = serialCommunicator.commPortSelected.value) {
                    //choose input file
                    Column {
                        Button(enabled = !runningTask.value && !loadingInputFile.value, onClick = {
                            coroutineScope.launch(Dispatchers.Swing) {

                                pickInputFile(
                                    cavpTestFiles,
                                    loadedFilesInfo,
                                    inputFileLoaded,
                                    outputResult,
                                    loadingInputFile
                                )
                                testAlgorithmList.clear()
                                for (cavpTestFile in cavpTestFiles) {
                                    for (i in 0 until cavpTestFile.numberOfAlgorithm) {
                                        val numberOfTestGroup = cavpTestFile.numberOfTestGroups(i)
                                        var algorithm_name = cavpTestFile.algorithmJsonLists[i].getString("algorithm")
                                        if (algorithm_name.lowercase().contains("ecdsa") || algorithm_name.lowercase()
                                                .contains("rsa")
                                        ) {
                                            val ecdsaOperationMode =
                                                cavpTestFile.algorithmJsonLists[i].getString("mode")
                                            algorithm_name += "-$ecdsaOperationMode"
                                        }
                                        val testCasesCount = cavpTestFile.getNumberOfAllTestCasesOfAlgorithm(i)
                                        testAlgorithmList.add("$algorithm_name ($numberOfTestGroup test groups, $testCasesCount test cases)")
                                    }
                                }
                                loadingInputFile.value = false
                            }
                        }) {
                            Text("input file")
                        }
                        Text(loadedFilesInfo.value)
                    }
                }
                AnimatedVisibility(visible = loadingInputFile.value) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.Black
                        )
                        Text("Loading input file(s)...")
                    }
                }


                //spacer
                Spacer(modifier = Modifier.fillMaxWidth().height(20.dp))

                //choose output folder
                AnimatedVisibility(visible = inputFileLoaded.value) {
                    Column {
                        Button(enabled = !runningTask.value, onClick = {
                            coroutineScope.launch(Dispatchers.Swing) {
                                pickOutputFolder(saveToFolderInfo, saveToThisFolder, saveToFolderSelected, outputResult)
                            }
                        }) {
                            Text("output folder")
                        }
                        Text(saveToFolderInfo.value)
                    }
                }

                //spacer
                Spacer(modifier = Modifier.fillMaxWidth().height(20.dp))

                //spacer
                Spacer(modifier = Modifier.fillMaxWidth().height(20.dp))


                //do tasks part: run button
                AnimatedVisibility(visible = saveToFolderSelected.value && inputFileLoaded.value) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Click the run button to start: ")
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(enabled = !(runningTask.value), onClick = {
                                println("run button press on ${Thread.currentThread().name}")
                                //多執行緒
                                coroutineScope.launch(Dispatchers.Default) {
                                    runningTask.value = true
                                    val saveFolderPath = saveToThisFolder.value
                                    outputResult.value = "Running... Please wait"
//                                        delay(2000)
                                    try {
                                        runHardwareCavp(
                                            serialCommunicator,
                                            cavpTestFiles,
                                            saveFolderPath,
                                            true,
                                            testProgress
                                        )
                                        outputResult.value =
                                            "Execution success! \nOutput files are saved to $saveFolderPath"
                                    } catch (e: Exception) {
                                        outputResult.value = "Error: $e"
                                    }
                                    runningTask.value = false
                                }
                            }) {
                                if (runningTask.value) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.Black
                                    )

                                } else {
                                    Text("Run")
                                }

                            }
                        }
                        AnimatedContent(targetState = outputResult) { text ->
                            Text(text = text.value)
                        }

                    }
                }
                AnimatedVisibility(visible = runningTask.value) {
                    Column {
                        Text("Progress: ${String.format("%.3f", testProgress.value * 100)}%")
                        LinearProgressIndicator(
                            progress = testProgress.value,
                            modifier = Modifier.fillMaxWidth().height(10.dp)
                        )
                    }

                }

                //spacer fill max size
                Spacer(modifier = Modifier.fillMaxSize().weight(1f))

                //bottom part
                GordonFooter()
            }

            Spacer(modifier = Modifier.fillMaxHeight().width(10.dp))

            //display Test list
            Column(
                modifier = Modifier.weight(1.0f).padding(5.dp).clip(RoundedCornerShape(20.dp))
                    .background(Color.LightGray)
                    .padding(10.dp).fillMaxSize()
            ) {
                Text("Test list", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Row(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
                        Column(Modifier.fillMaxSize().verticalScroll(testListScrollState)) {
                            for (testAlgorithm in testAlgorithmList) {
                                Text(testAlgorithm, modifier = Modifier.onClick {
                                    if (runningTask.value) return@onClick //if running task, return
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
                        VerticalScrollbar(
                            rememberScrollbarAdapter(testListScrollState),
                            Modifier.fillMaxHeight()
                        )
                    }
                }


            }


            // Spacer(modifier = Modifier.fillMaxHeight().width(10.dp))

            //display IO
            Column(
                modifier = Modifier.weight(1.0f)
            ) {
                Column(
                    modifier = Modifier.weight(0.4f).padding(5.dp).clip(RoundedCornerShape(20.dp))
                        .background(Color.LightGray)
                        .padding(10.dp).fillMaxSize()
                ) {
                    Text("To Device", fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().verticalScroll(sentToDeviceScrollState)
                        ) {
                            Text(serialCommunicator.sentText.value)
                        }
                        VerticalScrollbar(
                            rememberScrollbarAdapter(sentToDeviceScrollState),
                            Modifier.fillMaxHeight()
                        )
                    }

                }

                Column(
                    modifier = Modifier.weight(0.6f).padding(5.dp).clip(RoundedCornerShape(20.dp))
                        .background(Color.LightGray)
                        .padding(10.dp).fillMaxSize()
                ) {
                    Text("From Device", fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().verticalScroll(receivedFromDeviceScrollState)
                        ) {
                            Text(serialCommunicator.receivedText.value)
                        }
                        VerticalScrollbar(
                            rememberScrollbarAdapter(receivedFromDeviceScrollState),
                            Modifier.fillMaxHeight()
                        )
                    }

                }

            }


        }


    }
}