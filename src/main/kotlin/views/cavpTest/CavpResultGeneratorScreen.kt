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
import androidx.compose.ui.unit.dp
import cavpTestUtils.CavpTestFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import utils.pickInputFile
import utils.pickOutputFolder
import cavpTestUtils.runCavp
import kotlinx.coroutines.swing.Swing
import views.headerFooter.GordonFooter
import views.headerFooter.LadsTobBar
import java.util.ArrayList

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
@Preview
fun CavpResultGeneratorScreen() {


    val testAlgorithmList = remember { mutableStateListOf<String>() } //list of test algorithms
    var cavpTestFiles = remember { ArrayList<CavpTestFile>() }  //list of cavp test files


    val loadedFilesInfo = remember { mutableStateOf("no file loaded yet") } //info of loaded files
    val inputFileLoaded = remember { mutableStateOf(false) } //whether input file is loaded

    val saveToFolderInfo = remember { mutableStateOf("please select the output folder") } //info of save to folder
    val saveToThisFolder = remember { mutableStateOf("") }  //save to this folder
    val saveToFolderSelected = remember { mutableStateOf(false) }  //whether save to folder is selected

    val outputResult = remember { mutableStateOf("") }  //task result output

    val coroutineScope = rememberCoroutineScope() //coroutine scope

    val mctTestEnable = remember { mutableStateOf(true) } //whether to run mct test

    val runningTask = remember { mutableStateOf(false) } //whether is running tasks



    Column(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
            .padding(16.dp)
    ) {

        //Top bar
        LadsTobBar("CAVP Result Generator")

        //input file selection
        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(2f).fillMaxSize()) {
                //choose input file
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Button(enabled = !runningTask.value, onClick = {
                        coroutineScope.launch(Dispatchers.Swing) {
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
                //spacer
                Spacer(modifier = Modifier.fillMaxWidth().height(20.dp))

                //choose output folder
                AnimatedVisibility(visible = inputFileLoaded.value) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
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

                //options
                AnimatedVisibility(visible = saveToFolderSelected.value && inputFileLoaded.value) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("options: ")
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilterChip(
                                onClick = {
                                    if (!runningTask.value) {
                                        mctTestEnable.value = !(mctTestEnable.value)
                                    }
                                },
                                label = {
                                    Text("with Monte Carlo Test")
                                },
                                selected = mctTestEnable.value,
                                leadingIcon = if (mctTestEnable.value) {
                                    {
                                        Icon(
                                            imageVector = Icons.Filled.Done,
                                            contentDescription = "Done icon",
                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                        )
                                    }
                                } else {
                                    null
                                }
                            )
                        }


                    }
                }


                //spacer
                Spacer(modifier = Modifier.fillMaxWidth().height(20.dp))


                //do tasks part: run button
                AnimatedVisibility(visible = saveToFolderSelected.value && inputFileLoaded.value) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Choose the task you want to run: ")
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
                                        runCavp(cavpTestFiles, saveFolderPath, mctTestEnable.value)
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

                //spacer fill max size
                Spacer(modifier = Modifier.fillMaxSize().weight(1f))

                //bottom part
                GordonFooter()
            }

            //display file list
            Column(
                modifier = Modifier.weight(1.2f).clip(RoundedCornerShape(20.dp)).background(Color.LightGray)
                    .padding(10.dp).fillMaxSize().verticalScroll(rememberScrollState())
            ) {
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


        }


    }


}