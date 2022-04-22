package app.terraplanet.terraplanet.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.terraplanet.terraplanet.nav.Screen
import app.terraplanet.terraplanet.nav.SetupNavGraph
import app.terraplanet.terraplanet.ui.theme.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class HomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HomeScreen(this) }
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
                icon = { Icon(painter = painterResource(id = tab.icon), tab.title) },
                label = { Text(tab.title, fontSize = 12.sp) },
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