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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.fazecast.jSerialComm.SerialPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(
    ExperimentalFoundationApi::class, ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)
@Composable
@Preview
fun PQCLabScreen() {




    //for hiding keyboard and clear focus when click outside of text field
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() } // create an instance of interaction source

    var selectedPQCAlgorithm by remember { mutableStateOf(PQCAlgorithmsLabScreen.KyberKEMLab) }

    val dropDownMenuExpanded = remember { mutableStateOf(false) }

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

        //select PQC algorithm
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Selected algorithm: ", style = MaterialTheme.typography.bodyLarge)
            Text(selectedPQCAlgorithm.title,style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.width(20.dp))
            Box(
                modifier = Modifier
                    .wrapContentSize()
            ) {
                IconButton(onClick = {
                    dropDownMenuExpanded.value = true
                }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Localized description")
                }

                DropdownMenu(
                    expanded = dropDownMenuExpanded.value,
                    onDismissRequest = { dropDownMenuExpanded.value = false }
                ) {
                    PQCAlgorithmsLabScreen.values().forEach { pqcAlgorithm ->
                        DropdownMenuItem(
                            text = { Text(pqcAlgorithm.title) },
                            onClick = {
                                dropDownMenuExpanded.value = false
                                selectedPQCAlgorithm = pqcAlgorithm
                            }
                        )
                    }
                }
            }
        }

        //input file selection
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            selectedPQCAlgorithm.screen()
        }
        GordonFooter()

    }


}