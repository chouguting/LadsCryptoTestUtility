import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.ArrayList

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
@Preview
fun AppMaincreen(){
    var selectedItem = remember { mutableStateOf(0) }
    val items = listOf("CAVP", "Serial")
    val icons = listOf(Icons.Filled.Home, Icons.Default.Build)
    MaterialTheme {
        Row {
            NavigationRail {
                Spacer(Modifier.height(30.dp))
                items.forEachIndexed { index, item ->
                    NavigationRailItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem.value == index,
                        onClick = { selectedItem.value = index }
                    )
                }
            }
            when (selectedItem.value) {
                0 -> CavpResultGeneratorScreen()
                1 -> SerialToolScreen()
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
        AppMaincreen()
    }
}


