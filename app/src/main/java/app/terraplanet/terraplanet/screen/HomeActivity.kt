package app.terraplanet.terraplanet.screen

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.terraplanet.terraplanet.nav.Screen
import app.terraplanet.terraplanet.nav.SetupNavGraph
import app.terraplanet.terraplanet.network.APIServiceImpl
import app.terraplanet.terraplanet.ui.theme.*
import app.terraplanet.terraplanet.util.Biometrics
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class HomeActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HomeScreen(this) }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun authenticationCallback(onSuccess: () -> Unit, onError: (() -> Unit)? = null): BiometricPrompt.AuthenticationCallback =
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError?.invoke()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError?.invoke()
            }
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun launchBiometric(
        context: Context,
        description: String = "Authenticate with Biometrics",
        authenticationCallback: BiometricPrompt.AuthenticationCallback,
        unsupportedCallback: () -> Unit
    ) {
        if (Biometrics.checkBiometricSupport(context)) {
            val biometricPromptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Terra Planet")
                .setDescription(description)
                .setNegativeButtonText("Cancel")
                .setConfirmationRequired(true)
                .build()

            val biometricPrompt = BiometricPrompt(this, mainExecutor, authenticationCallback)
            biometricPrompt.authenticate(biometricPromptInfo)
        } else {
            unsupportedCallback()
        }
    }

    override fun onResume() {
        super.onResume()
        api.getWallet(this)
    }

    companion object {
        val api = APIServiceImpl()
    }
}

@Composable
private fun HomeScreen(context: ComponentActivity) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setStatusBarColor(color = bgColor())

    val navController = rememberNavController()

    TerraPlanetTheme {
        Scaffold(
            bottomBar = { BottomNavigation(navController = navController) }
        ) {
            SetupNavGraph(context, navController = navController)
        }
    }
}

@Composable
private fun BottomNavigation(navController: NavController) {
    val tabs = listOf(Screen.Wallet, Screen.Transactions, Screen.Settings)

    BottomNavigation(
        backgroundColor = bottomNavBgColor()
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        tabs.forEach { tab ->
            BottomNavigationItem(
                icon = { Icon(painter = painterResource(id = tab.icon), stringResource(tab.title)) },
                label = { Text(text = stringResource(tab.title), fontSize = 12.sp) },
                selected = currentRoute == tab.route,
                onClick = {
                    navController.navigate(tab.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route)
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}