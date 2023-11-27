import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import cavp.CavpTestFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.ArrayList

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
@Preview
fun App() {


    val testAlgorithmList = remember { mutableStateListOf<String>() } //list of test algorithms
    var cavpTestFiles = remember { ArrayList<CavpTestFile>() }  //list of cavp test files


    val loadedFilesInfo = remember { mutableStateOf("no file loaded yet") } //info of loaded files
    val inputFileLoaded = remember { mutableStateOf(false) } //whether input file is loaded

    val saveToFolderInfo = remember { mutableStateOf("please select the output folder") }
    val saveToThisFolder = remember { mutableStateOf("") }
    val saveToFolderSelected = remember { mutableStateOf(false) }

    val outputResult = remember { mutableStateOf("") }


    MaterialTheme {
        Column(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                //.background(Color.Cyan)
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "LaDS CryptoTest Utility",
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

            Row(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.weight(2f).fillMaxSize()) {
                    //choose input folder
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Button(onClick = {
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
                                    val algorithm_name = cavpTestFile.algorithmJsonLists[i].getString("algorithm")
                                    val testCasesCount = cavpTestFile.getNumberOfAllTestCasesOfAlgorithm(i)
                                    testAlgorithmList.add("$algorithm_name ($numberOfTestGroup test groups, $testCasesCount test cases)")
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
                            Button(onClick = {
                                pickOutputFolder(saveToFolderInfo, saveToThisFolder, saveToFolderSelected, outputResult)
                            }) {
                                Text("output folder")
                            }
                            Text(saveToFolderInfo.value)
                        }
                    }

                    //spacer
                    Spacer(modifier = Modifier.fillMaxWidth().height(20.dp))


                    //choose task
                    val running = remember { mutableStateOf(false) }
                    val runningScope = rememberCoroutineScope()

                    AnimatedVisibility(visible = saveToFolderSelected.value && inputFileLoaded.value) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Choose the task you want to run: ")
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(enabled = !(running.value), onClick = {
                                    //多執行緒
                                    runningScope.launch {
                                        running.value = true
                                        val saveFolderPath = saveToThisFolder.value
                                        outputResult.value = "Running... Please wait"
                                        delay(2000)
                                        try {
                                            runAndSave(cavpTestFiles, saveFolderPath)
                                            outputResult.value =
                                                "Execution success! \nOutput files are saved to $saveFolderPath"
                                        } catch (e: Exception) {
                                            outputResult.value = "Error: $e"
                                        }
                                        running.value = false
                                    }
                                }) {
                                    if (running.value) {
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

                    Text(
                        "developed by Gordon Chou @ NTU LaDS 2023",
                        fontFamily = FontFamily.SansSerif,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                //display file list
                Column(
                    modifier = Modifier.weight(1.2f).clip(RoundedCornerShape(20.dp)).background(Color.LightGray)
                        .padding(10.dp).fillMaxSize().verticalScroll(rememberScrollState())
                ) {
                    for (testAlgorithm in testAlgorithmList) {
                        Text(testAlgorithm, modifier = Modifier.onClick {
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
}

fun main() = application {
//    Window(onCloseRequest = ::exitApplication) {
//        App()
//    }
    val state = rememberWindowState(
        placement = WindowPlacement.Floating,
        position = WindowPosition(Alignment.Center),
        isMinimized = false,
        width = 1280.dp,
        height = 720.dp
    )
    var windowTitle by remember { mutableStateOf("LaDS CryptoTest Utility") }

    Window(
        title = windowTitle,
        resizable = true,
        state = state,
        icon = painterResource("images/LaDS_logo_square.png"),
        onCloseRequest = ::exitApplication
    ) {
        App()
    }
}


