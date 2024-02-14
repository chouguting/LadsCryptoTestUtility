package utils

import androidx.compose.runtime.MutableState
import cavp.CavpTestFile
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.filechooser.FileNameExtensionFilter



fun pickInputFile(
//    fileList: SnapshotStateList<String>,
//    fileAndItsPath: HashMap<String, String>,
    cavpTestFiles: ArrayList<CavpTestFile>,
    loadedFilesInfo: MutableState<String>,
    fileLoaded: MutableState<Boolean>,
    outputResult: MutableState<String>
) {

//            fileChooser.setFileFilter(new FileNameExtensionFilter("request file (*.req)", "req"));
    val fileChooser = JFileChooser()
    val filter = FileNameExtensionFilter(
        "*.json", "json"
    )

    fileChooser.setCurrentDirectory(File("."))
    fileChooser.setFileFilter(filter)
    fileChooser.setMultiSelectionEnabled(true)
    fileChooser.setAcceptAllFileFilterUsed(false) //disable all file type option
    fileChooser.setDialogTitle("Choose files")
    val userSelection = fileChooser.showOpenDialog(JFrame())
    if (userSelection == JFileChooser.APPROVE_OPTION) {
        val selectedFiles = fileChooser.selectedFiles
//        val selectedFilePath = ArrayList<String>()
        var testCaseCount = 0;
//        fileList.clear()
//        fileAndItsPath.clear()
        cavpTestFiles.clear()
        for (file in selectedFiles) {
//            if (fileAndItsPath.containsKey(file.getName())) {
//                JOptionPane.showMessageDialog(
//                    null,
//                    file.getName() + " already exists",
//                    "Duplicate File(s)",
//                    JOptionPane.ERROR_MESSAGE
//                )
//            } else {
//                fileList.add(file.getName())
//                fileAndItsPath.put(file.getName(), Path.of(file.absolutePath).toString())
//                cavpTestFiles.add(CavpTestFile(file.absolutePath))
//                testCaseCount += cavpTestFiles.last().getNumberOfAllTestCases()
//            }
            cavpTestFiles.add(CavpTestFile(file.absolutePath))
            testCaseCount += cavpTestFiles.last().getNumberOfAllTestCases()
        }
        loadedFilesInfo.value = "${cavpTestFiles.size} files loaded, found $testCaseCount test cases"
        if (cavpTestFiles.size > 0) {
            fileLoaded.value = true
        }
        outputResult.value = ""
    }

}


fun pickOutputFolder(
    saveToFolderInfo: MutableState<String>,
    saveToThisFolder: MutableState<String>,
    saveToFolderSelected: MutableState<Boolean>,
    outputResult: MutableState<String>
) {
    val fileChooser = JFileChooser()
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
    fileChooser.setCurrentDirectory(File("."))
    fileChooser.setDialogTitle("Choose a folder")
    fileChooser.setAcceptAllFileFilterUsed(false) //disable all file type option
    val userSelection = fileChooser.showOpenDialog(JFrame())
    if (userSelection == JFileChooser.APPROVE_OPTION) {
        val selectedDir = fileChooser.selectedFile.toString()
        saveToThisFolder.value = selectedDir
        saveToFolderInfo.value = "save to folder: $selectedDir"

        saveToFolderSelected.value = true
        outputResult.value = ""
    }
}