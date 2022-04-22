package app.terraplanet.terraplanet.screen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.terraplanet.terraplanet.model.Wallet
import app.terraplanet.terraplanet.network.APIServiceImpl
import app.terraplanet.terraplanet.network.APIServiceImpl.Companion.LOG_E
import app.terraplanet.terraplanet.ui.theme.MainColor
import app.terraplanet.terraplanet.ui.theme.TerraPlanetTheme
import app.terraplanet.terraplanet.ui.theme.bgColor
import app.terraplanet.terraplanet.ui.util.Expandable
import app.terraplanet.terraplanet.ui.util.LoadingOverlay
import app.terraplanet.terraplanet.ui.util.VSpacer
import app.terraplanet.terraplanet.ui.util.clearStack
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class InitWalletActivity : ComponentActivity() {
    val api = APIServiceImpl()
    var wallet = mutableStateOf<Wallet?>(null)
    var loading = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YourWallet(loading.value, wallet.value, onContinue = {
                api.initSettings(applicationContext)
                initHomeScreen()
            })
        }

        createWallet()
    }

    private fun createWallet() {
        loading.value = true
        api.create(applicationContext)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                wallet.value = it
                loading.value = false
            }, { error ->
                error.message?.let { Log.e(LOG_E, it) }
                loading.value = false
                finish()
            })
    }

    private fun initHomeScreen() {
        val intent = Intent(this, HomeActivity::class.java).apply { flags = clearStack }
        startActivity(intent)
    }
}

@Composable
private fun YourWallet(loading: Boolean, wallet: Wallet?, onContinue: () -> Unit) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(color = bgColor())

    TerraPlanetTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            if (loading) {
                LoadingOverlay()
            } else {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    VSpacer(30)
                    Text(
                        "Your Wallet",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    VSpacer(20)
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Address:",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    VSpacer(6)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MainColor
                    ) {
                        Text(
                            wallet?.address ?: "",
                            maxLines = 1,
                            color = Color.White,
                            modifier = Modifier.padding(
                                horizontal = 16.dp, vertical = 10.dp
                            )
                        )
                    }
                    VSpacer(20)
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Seed Phrase:",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    VSpacer(6)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MainColor
                    ) {
                        Text(
                            wallet?.mnemonic ?: "",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                    VSpacer(10)
                    Text(
                        "Please store your seed phrase securely. We would not be able to recover your wallet if you lose it. We suggest copying it on paper.",
                    )
                    Expandable()
                    Button(
                        onClick = { onContinue() },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(25),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp
                        )
                    ) {
                        Text(
                            text = "CONTINUE",
                            color = MainColor,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    VSpacer(20)
                }
            }
        }
    }
}