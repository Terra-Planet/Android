package app.terraplanet.terraplanet.nav

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import app.terraplanet.terraplanet.screen.tab.SettingsTab
import app.terraplanet.terraplanet.screen.tab.TransactionsTab
import app.terraplanet.terraplanet.screen.tab.WalletTab
import app.terraplanet.terraplanet.viewmodel.SettingsViewModel
import app.terraplanet.terraplanet.viewmodel.WalletViewModel

@Composable
fun SetupNavGraph(context: ComponentActivity, navController: NavHostController) {
    val walletViewModel = ViewModelProvider(context).get(WalletViewModel::class.java)
    val settingsViewModel = ViewModelProvider(context).get(SettingsViewModel::class.java)
    NavHost(
        navController = navController,
        startDestination = Screen.Wallet.route
    ) {
        composable(route = Screen.Wallet.route) { WalletTab(walletViewModel, settingsViewModel) {
                navController.navigate(route = Screen.Settings.route)
            }
        }
        composable(route = Screen.Transactions.route) { TransactionsTab() }
        composable(route = Screen.Settings.route) {
            SettingsTab(settingsViewModel)
        }
    }
}