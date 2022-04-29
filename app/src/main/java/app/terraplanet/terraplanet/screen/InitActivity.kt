package app.terraplanet.terraplanet.screen

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.terraplanet.terraplanet.R
import app.terraplanet.terraplanet.network.APIServiceImpl
import app.terraplanet.terraplanet.ui.theme.MainColor
import app.terraplanet.terraplanet.ui.theme.TerraPlanetTheme
import app.terraplanet.terraplanet.ui.theme.Title
import app.terraplanet.terraplanet.ui.util.Expandable
import app.terraplanet.terraplanet.ui.util.VSpacer
import app.terraplanet.terraplanet.util.bitmapDrawable
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class InitActivity : ComponentActivity() {
    val api = APIServiceImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InitScreen(
                onImport = { initImportWalletScreen() },
                onWallet = { initYourWalletScreen() }
            )
        }
    }

    private fun initYourWalletScreen() {
        startActivity(Intent(this, InitWalletActivity::class.java))
    }

    private fun initImportWalletScreen() {
        startActivity(Intent(this, ImportWalletActivity::class.java))
    }


}

@Composable
private fun InitScreen(onWallet: () -> Unit, onImport: () -> Unit) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(color = MainColor)

    val context = LocalContext.current

    TerraPlanetTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MainColor,
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Title()
                VSpacer(30)
                Image(
                    bitmap = context.bitmapDrawable(R.drawable.app_logo)!!,
                    modifier = Modifier.size(width = 150.dp, height = 150.dp),
                    contentDescription = null,
                )
                VSpacer(30)
                Text(
                    text = stringResource(R.string.init_app_description),
                    color = Color.White,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                Expandable()
                Button(
                    onClick = { onWallet() },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(25)
                ) {
                    Text(
                        text = stringResource(R.string.init_create_wallet),
                        color = MainColor,
                        fontWeight = FontWeight.Bold,
                    )
                }
                VSpacer(10)
                Button(
                    onClick = { onImport() },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(25),
                    elevation = ButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    Text(
                        text = stringResource(R.string.init_import_wallet),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}