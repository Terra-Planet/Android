package app.terraplanet.terraplanet.nav

import app.terraplanet.terraplanet.R

sealed class Screen(val title: String, val icon: Int, val route: String) {
    object Wallet : Screen("Wallet", icon = R.drawable.tab_wallet, route = "wallet_tab")
    object Transactions : Screen("Transactions", icon = R.drawable.tab_transactions, route = "transactions_tab")
    object Settings : Screen("Settings", icon = R.drawable.tab_settings, route = "settings_tab")
}