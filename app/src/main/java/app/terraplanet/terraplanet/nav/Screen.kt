package app.terraplanet.terraplanet.nav

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import app.terraplanet.terraplanet.R

sealed class Screen(@StringRes val title: Int, @DrawableRes val icon: Int, val route: String) {
    object Wallet : Screen(R.string.bottom_navigation_wallet, icon = R.drawable.tab_wallet, route = "wallet_tab")
    object Transactions : Screen(R.string.bottom_navigation_transactions, icon = R.drawable.tab_transactions, route = "transactions_tab")
    object Settings : Screen(R.string.bottom_navigation_settings, icon = R.drawable.tab_settings, route = "settings_tab")
}