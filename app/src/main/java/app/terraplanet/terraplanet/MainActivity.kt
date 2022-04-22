package app.terraplanet.terraplanet

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.terraplanet.terraplanet.screen.InitActivity
import app.terraplanet.terraplanet.ui.theme.MainColor
import app.terraplanet.terraplanet.ui.theme.TerraPlanetTheme
import app.terraplanet.terraplanet.ui.util.Expandable
import app.terraplanet.terraplanet.ui.util.VSpacer
import app.terraplanet.terraplanet.ui.util.clearStack
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {
    var status = mutableStateOf("")
    val app by lazy { (applicationContext as BaseApplication) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LoadingScreen(status.value) }

        app.checkServerStatus({ status.value = it }, { result ->
           if (result) goToInitScreen()
        })
    }

    private fun goToInitScreen() {
        val intent = Intent(baseContext, InitActivity::class.java).apply { flags = clearStack }
        startActivity(intent)
    }
}

@Composable
private fun LoadingScreen(status: String) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(color = MainColor)

    TerraPlanetTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MainColor,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                VSpacer(50)
                Expandable()
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    modifier = Modifier.size(width = 200.dp, height = 200.dp),
                    contentDescription = null,
                )
                Expandable()
                CircularProgressIndicator(color = Color.White)
                VSpacer(20)
                Text(status, color = Color.White, textAlign = TextAlign.Center)
                VSpacer(50)
            }
        }
    }
}
