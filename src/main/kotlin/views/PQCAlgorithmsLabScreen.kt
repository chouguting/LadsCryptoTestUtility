package views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.fazecast.jSerialComm.SerialPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import tw.edu.ntu.lads.chouguting.java.cipers.CipherUtils
import tw.edu.ntu.lads.chouguting.java.cipers.KyberEngine

enum class PQCAlgorithmsLabScreen(val title: String, val screen: @Composable () -> Unit) {
    KyberLab("Kyber", screen = { KyberLabScreen() }),

}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun InputWithLabel(
//    label:String, textFieldValue: MutableState<String>){
//    Row(
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.spacedBy(20.dp)
//    ) {
//        Text(label, style = MaterialTheme.typography.bodyLarge, fontFamily = FontFamily.SansSerif)
//        OutlinedTextField(
//            value = textFieldValue.value,
//            onValueChange = { textFieldValue.value = it },
//            modifier = Modifier.wrapContentHeight(),
//            label = { Text("Label") }
//        )
//    }
//}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)

@Composable
fun AlertMessageDialog(
    message: String,
    showAlert: MutableState<Boolean>,

    ) {
    AlertDialog(
        onDismissRequest = {
            showAlert.value = false
        },
        title = {
            Text(text = "Error")
        },
        text = {
            Column() {
                Text(text = message)
                Spacer(modifier = Modifier.height(10.dp))
            }

        },
        confirmButton = {
            TextButton(
                onClick = {
                    showAlert.value = false
                }
            ) {
                Text("OK")
            }
        },
//            dismissButton = {
//                TextButton(
//                    onClick = {
//                        showAlert = false
//                    }
//                ) {
//                    Text("Cancel")
//                }
//            }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
@Preview
fun KyberLabScreen() {

    val coroutineScope = rememberCoroutineScope()

    /*要用by的話，要import
    * import androidx.compose.runtime.getValue
    * import androidx.compose.runtime.setValue
    * */
    var publicKey by remember { mutableStateOf("") }
    var privateKey by remember { mutableStateOf("") }

    var publicKeyEnc by remember { mutableStateOf("") }
    var privateKeyDec by remember { mutableStateOf("") }


    var seed by remember { mutableStateOf("") }
    var ciphertextEnc by remember { mutableStateOf("") }
    var ciphertextDec by remember { mutableStateOf("") }
    var sharedSecretEnc by remember { mutableStateOf("") }
    var sharedSecretDec by remember { mutableStateOf("") }

    val kyberEngine = remember { KyberEngine() }
    val publickeyBytes = remember { ByteArray(kyberEngine.KYBER_PUBLICKEYBYTES) }
    val privatekeyBytes = remember { ByteArray(kyberEngine.KYBER_PRIVATEKEYBYTES) }
    val seedBytes = remember { ByteArray(kyberEngine.KYBER_SEEDBYTES) }

    val ciphertextEncBytes = remember { ByteArray(kyberEngine.KYBER_CIPHERTEXTBYTES) }
    val ciphertextDecBytes = remember { ByteArray(kyberEngine.KYBER_CIPHERTEXTBYTES) }
    val sharedSecretEncBytes = remember { ByteArray(kyberEngine.KYBER_SSBYTES) }
    val sharedSecretDecBytes = remember { ByteArray(kyberEngine.KYBER_SSBYTES) }

    var showAlert = remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }

    if (showAlert.value) {
        AlertMessageDialog(alertMessage, showAlert)
    }



    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(20.dp))
        Text("Key generation", style = MaterialTheme.typography.headlineMedium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Button(onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    kyberEngine.keypair(publickeyBytes, privatekeyBytes)
                    publicKey = CipherUtils.bytesToHexString(publickeyBytes)
                    privateKey = CipherUtils.bytesToHexString(privatekeyBytes)
                    publicKeyEnc = publicKey
                    privateKeyDec = privateKey
                }
            }) {
                Text("Generate")
            }
            OutlinedTextField(
                value = publicKey,
                onValueChange = { },
                modifier = Modifier.weight(1f),
                label = { Text("public key") },
                singleLine = true
            )
            OutlinedTextField(
                value = privateKey,
                onValueChange = { },
                modifier = Modifier.weight(1f),
                label = { Text("private key") },
                singleLine = true
            )

        }
        Spacer(modifier = Modifier.height(20.dp))

        AnimatedVisibility(visible = publicKey.isNotEmpty() && privateKey.isNotEmpty()) {
            Column {
                Text("Encapsulation", style = MaterialTheme.typography.headlineMedium)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    OutlinedTextField(
                        value = seed,
                        onValueChange = { seed = it },
                        modifier = Modifier.wrapContentHeight().width(200.dp),
                        label = { Text("seed") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = publicKeyEnc,
                        onValueChange = { publicKeyEnc = it },
                        modifier = Modifier.wrapContentHeight().width(200.dp),
                        label = { Text("publicKey") },
                        singleLine = true
                    )
                    Button(onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                CipherUtils.copyByteArray(seedBytes, CipherUtils.hexStringToBytes(seed))

                            } catch (e: Exception) {
                                println(e)
                                alertMessage = "Please input valid seed (length must be even)"
                                showAlert.value = true
                                return@launch
                            }
                            try {
                                CipherUtils.copyByteArray(publickeyBytes, CipherUtils.hexStringToBytes(publicKeyEnc))
                            } catch (e: Exception) {
                                println(e)
                                alertMessage = "Please input valid public key(length must be even)"
                                showAlert.value = true
                                return@launch
                            }

                            kyberEngine.encapsulate(ciphertextEncBytes, sharedSecretEncBytes, seedBytes, publickeyBytes)
                            ciphertextEnc = CipherUtils.bytesToHexString(ciphertextEncBytes)
                            sharedSecretEnc = CipherUtils.bytesToHexString(sharedSecretEncBytes)
                        }

                    }) {
                        Text("Encapsulate")
                    }
                    OutlinedTextField(
                        value = ciphertextEnc,
                        onValueChange = { },
                        modifier = Modifier.weight(1f),
                        label = { Text("ciphertext") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = sharedSecretEnc,
                        onValueChange = { },
                        modifier = Modifier.weight(1f),
                        label = { Text("shared secret") },
                        singleLine = true
                    )


                }

                Spacer(modifier = Modifier.height(20.dp))

                Text("Decapsulation", style = MaterialTheme.typography.headlineMedium)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    OutlinedTextField(
                        value = ciphertextDec,
                        onValueChange = { ciphertextDec = it },
                        modifier = Modifier.wrapContentHeight().width(200.dp),
                        label = { Text("ciphertext") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = privateKeyDec,
                        onValueChange = { privateKeyDec = it },
                        modifier = Modifier.wrapContentHeight().width(200.dp),
                        label = { Text("privateKey") },
                        singleLine = true
                    )
                    Button(onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                CipherUtils.copyByteArray(
                                    ciphertextDecBytes,
                                    CipherUtils.hexStringToBytes(ciphertextDec)
                                )
                            } catch (e: Exception) {
                                println(e)
                                alertMessage = "Please input valid ciphertext (length must be even)"
                                showAlert.value = true
                                return@launch
                            }

                            try {
                                CipherUtils.copyByteArray(privatekeyBytes, CipherUtils.hexStringToBytes(privateKeyDec))
                            } catch (e: Exception) {
                                println(e)
                                alertMessage = "Please input valid private key (length must be even)"
                                showAlert.value = true
                                return@launch
                            }
                            kyberEngine.decapsulate(sharedSecretDecBytes, ciphertextDecBytes, privatekeyBytes)
                            sharedSecretDec = CipherUtils.bytesToHexString(sharedSecretDecBytes)
                        }

                    }) {
                        Text("Decapsulate")
                    }
                    OutlinedTextField(
                        value = sharedSecretDec,
                        onValueChange = { },
                        modifier = Modifier.weight(1f),
                        label = { Text("shared secret") },
                        singleLine = true
                    )
                    Text("Same? ${sharedSecretEnc == sharedSecretDec}", modifier = Modifier.width(100.dp))

                }

            }

        }


    }

}