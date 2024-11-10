package views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import views.PQCLabs.PQCLabScreen
import views.cavpTest.CavpResultGeneratorScreen
import views.serialTool.SerialToolScreen
import views.validationSystem.SerialValidationSystemScreen
import views.cavpTest.CavpHardwareTestScreen

enum class MainNavigationRailItem(val title:String,val icon: ImageVector,val screen:@Composable () -> Unit) {
    CAVPScreen("CAVP", Icons.Filled.Home, screen = { CavpResultGeneratorScreen() }),
    CavpHardwareTestScreen("CAVP HW", Icons.Default.Build, screen = { CavpHardwareTestScreen() } ),
    SerialScreen("Serial", Icons.Default.Build, screen = { SerialToolScreen() } ),
    PQCLabScreen("PQC Lab", Icons.Default.Build, screen = { PQCLabScreen() } ),
    SerialValidatorScreen("validation", Icons.Default.Build, screen = { SerialValidationSystemScreen() } ),


}
