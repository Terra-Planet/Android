package app.terraplanet.terraplanet

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import app.terraplanet.terraplanet.network.APIServiceImpl
import app.terraplanet.terraplanet.screen.HomeActivity
import app.terraplanet.terraplanet.screen.InitActivity
import app.terraplanet.terraplanet.ui.theme.MainColor
import app.terraplanet.terraplanet.ui.theme.TerraPlanetTheme
import app.terraplanet.terraplanet.ui.util.Expandable
import app.terraplanet.terraplanet.ui.util.VSpacer
import app.terraplanet.terraplanet.ui.util.clearStack
import app.terraplanet.terraplanet.util.Biometrics.Companion.checkBiometricSupport
import app.terraplanet.terraplanet.util.bitmapDrawable
import app.terraplanet.terraplanet.viewmodel.ResultState
import com.google.accompanist.systemuicontroller.rememberSystemUiController

data class LoginState(val resultState: ResultState, val status: String, val loading: Boolean)

class MainActivity : FragmentActivity() {
    var status = mutableStateOf(LoginState(ResultState.LOADING, "Server status...", true))
    val app by lazy { (applicationContext as BaseApplication) }
    val api = APIServiceImpl()

    private var serverResult = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoadingScreen(status.value) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    launchBiometric(this, authenticationCallback)
                }
            }
        }

        app.checkServerStatus({
            status.value = LoginState(ResultState.LOADING, it, true)
        }, { result ->
            serverResult.value = result
            if (result) {
                if (getWallet()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        launchBiometric(this, authenticationCallback)
                    } else {
                        goToHomeScreen()
                    }
                } else {
                    goToInitScreen()
                }
            } else {
                status.value = LoginState(ResultState.FAILED, "Server failed. Close the app and try again", false)
            }
        })
    }

    private fun getWallet(): Boolean = api.getWallet(applicationContext) != null

    private fun goToInitScreen() {
        val intent = Intent(this, InitActivity::class.java).apply { flags = clearStack }
        startActivity(intent)
    }

    private fun goToHomeScreen() {
        val intent = Intent(this, HomeActivity::class.java).apply { flags = clearStack }
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private val authenticationCallback: BiometricPrompt.AuthenticationCallback =
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                status.value = LoginState(ResultState.SUCCESS, "Auth Successful", false)
                goToHomeScreen()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                status.value = LoginState(ResultState.CANCELLED, "Auth Canceled", false)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                status.value = LoginState(ResultState.CANCELLED, "Error. Try Again", false)
            }
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun launchBiometric(context: Context, authenticationCallback: BiometricPrompt.AuthenticationCallback) {
        if (checkBiometricSupport(context)) {
            val biometricPromptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Terra Planet")
                .setDescription("Authenticate with Biometrics")
                .setNegativeButtonText("Cancel")
                .setConfirmationRequired(true)
                .build()

            val biometricPrompt = BiometricPrompt(this, mainExecutor, authenticationCallback)
            biometricPrompt.authenticate(biometricPromptInfo)
        } else {
            status.value = LoginState(ResultState.SUCCESS, "Auth Successful", false)
            goToHomeScreen()
        }
    }
}

@Composable
private fun LoadingScreen(state: LoginState, onBiometrics: () -> Unit) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(color = MainColor)

    val context = LocalContext.current

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
                    bitmap = context.bitmapDrawable(R.drawable.app_logo)!!,
                    modifier = Modifier.size(width = 200.dp, height = 200.dp),
                    contentDescription = null,
                )
                Expandable()
                if (state.loading) {
                    CircularProgressIndicator(color = Color.White)
                } else if (state.resultState == ResultState.CANCELLED) {
                    IconButton(onClick = { onBiometrics() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_finger),
                            tint = Color.White,
                            contentDescription = null,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }
                VSpacer(20)
                Text(state.status, color = Color.White, textAlign = TextAlign.Center)
                VSpacer(50)
            }
        }
    }
}
