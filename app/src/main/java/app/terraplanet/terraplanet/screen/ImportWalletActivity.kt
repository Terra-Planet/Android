package app.terraplanet.terraplanet.screen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.terraplanet.terraplanet.R
import app.terraplanet.terraplanet.network.APIServiceImpl
import app.terraplanet.terraplanet.ui.theme.MainColor
import app.terraplanet.terraplanet.ui.theme.TerraPlanetTheme
import app.terraplanet.terraplanet.ui.theme.bgColor
import app.terraplanet.terraplanet.ui.util.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class ImportWalletActivity : ComponentActivity() {
    val api = APIServiceImpl()
    val loading = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImportWallet(loading.value, onContinue = { restoreWallet(it) })
        }
    }

    private fun restoreWallet(mnemonic: String) {
        loading.value = true
        api.restore(mnemonic, applicationContext)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                api.initSettings(applicationContext)
                initHomeScreen()
            }, { error ->
                loading.value = false
                error.message?.let { Log.e(APIServiceImpl.LOG_E, it) }
                Toast.makeText(this, "Input a correct Seed Phrase", Toast.LENGTH_SHORT).show()
            })
    }

    private fun initHomeScreen() {
        val intent = Intent(this, HomeActivity::class.java).apply { flags = clearStack }
        startActivity(intent)
        loading.value = false
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ImportWallet(loading: Boolean, onContinue: (String) -> Unit) {
    val systemUiController = rememberSystemUiController()
    var phrase by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    systemUiController.setSystemBarsColor(color = bgColor())

    if (!loading) {
        phrase = ""
    }

    TerraPlanetTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                VSpacer(30)
                Text(
                    text = stringResource(R.string.import_wallet_import_wallet),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                )
                VSpacer(30)
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.import_wallet_seed_phrase),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                SimpleTextField(
                    value = phrase,
                    onValueChange = { phrase = it.replace("\n", "") },
                    modifier = Modifier.fillMaxWidth(),
                    onDone = {
                        focusManager.clearFocus()
                        if (phrase.isNotEmpty()) onContinue(phrase)
                    }
                )
                VSpacer(20)
                Expandable()
                Button(
                    onClick = { if (phrase.isNotEmpty()) onContinue(phrase) },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(25),
                    elevation = ButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    Text(
                        text = stringResource(R.string.import_wallet_continue),
                        color = MainColor,
                        fontWeight = FontWeight.Bold,
                    )
                }
                VSpacer(20)
            }
        }

        if (loading) LoadingOverlay()
    }
}