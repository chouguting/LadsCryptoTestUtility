package views.headerFooter

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
@Preview
fun LadsTobBar(
    title:String
){
    //Top bar
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        //.background(Color.Cyan)
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            title,
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
}