package views.headerFooter

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import java.time.Year

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
@Preview
fun GordonFooter(){
    Text(
        "developed by Gordon Chou @ NTU LaDS ${Year.now().value}",
        fontFamily = FontFamily.SansSerif,
        style = MaterialTheme.typography.labelSmall
    )
}