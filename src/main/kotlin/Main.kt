import androidx.compose.animation.AnimatedVisibility
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JFrame

@Composable
@Preview
fun App() {


    val fileList = remember { mutableStateListOf<String>() }
    val fileAndItsPath = remember { HashMap<String, String>()}

    val loadedFolderInfo = remember { mutableStateOf("no folder loaded yet") }
    val folderLoaded = remember { mutableStateOf(false) }

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
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                //.background(Color.Cyan)
                modifier = Modifier.fillMaxWidth()) {
                Text("LaDS CryptoTest Utility", fontFamily = FontFamily.SansSerif,style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.weight(1f))
                Image(
                    painter = painterResource("images/LaDS_logo.png"),
                    contentDescription = "LaDS Logo",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.size(width = 200.dp, height = 100.dp)
                )
            }

            Row (verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)){
                Button(onClick = {
                    val fileChooser = JFileChooser()
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
                    fileChooser.setCurrentDirectory(File("."))
                    fileChooser.setDialogTitle("Choose a directory")
                    fileChooser.setAcceptAllFileFilterUsed(false) //disable all file type option

                    val userSelection = fileChooser.showOpenDialog(JFrame())
                    if (userSelection == JFileChooser.APPROVE_OPTION) {
                        val selectedDir = fileChooser.selectedFile.toString()
                        val fileLists = Utils.findSupportedFilesInDir(selectedDir);

                        for (filename in fileLists) {
                            fileList.add(filename)
                            fileAndItsPath.put(filename, selectedDir + "\\" + filename)
                        }

                        loadedFolderInfo.value = "loaded folder: $selectedDir , ${fileList.size} files loaded"
                        folderLoaded.value = true
                        outputResult.value = ""
                    }

                }) {
                    Text("input file folder")
                }

                Text(loadedFolderInfo.value)

            }
            //spacer
            Spacer(modifier = Modifier.fillMaxWidth().height(20.dp))

            AnimatedVisibility(visible = folderLoaded.value) {
                Row (verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)){
                    Button(onClick = {
                        val fileChooser = JFileChooser()
                        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
                        fileChooser.setCurrentDirectory(File("."))
                        fileChooser.setDialogTitle("Choose a directory")
                        fileChooser.setAcceptAllFileFilterUsed(false) //disable all file type option
                        val userSelection = fileChooser.showOpenDialog(JFrame())
                        if (userSelection == JFileChooser.APPROVE_OPTION) {
                            val selectedDir = fileChooser.selectedFile.toString()
                            saveToThisFolder.value = selectedDir
                            saveToFolderInfo.value = "save to dir: $selectedDir"
                            saveToFolderSelected.value = true
                            outputResult.value = ""
                        }



                    }) {
                        Text("output folder")
                    }
                    Text(saveToFolderInfo.value)

                }


            }

            //spacer
            Spacer(modifier = Modifier.fillMaxWidth().height(20.dp))

            AnimatedVisibility(visible = saveToFolderSelected.value) {
                Column(modifier = Modifier.fillMaxWidth()){
                    Text("Choose the task you want to run: ")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick={
                            convertToJsonAndSave(fileList, fileAndItsPath, saveToThisFolder.value)
                            outputResult.value = "convertToJsonAndSave done"
                        }){
                            Text("Convert to Json")
                        }
                        Button(onClick={
                            aesAndSave(mode = 0,fileList, fileAndItsPath, saveToThisFolder.value)
                            outputResult.value = "AES done, result(json) saved to folder"
                        }){
                            Text("AES result to Json")
                        }
                        Button(onClick={
                            aesAndSave(mode = 1,fileList, fileAndItsPath, saveToThisFolder.value)
                            outputResult.value = "AES done, result(rsp) saved to folder"
                        }){
                            Text("AES result to RSP")
                        }
                    }
                    Text(outputResult.value)
                }



            }





            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .background(Color.White)

            ) {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    ) {

                }
                Column(modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    ) {

                }
                Column(modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    ) {
//                    Icon(
//                        Icons.Rounded.ShoppingCart,
//                        contentDescription = ""
//                    )
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


