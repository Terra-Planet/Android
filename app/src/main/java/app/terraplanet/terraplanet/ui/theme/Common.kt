package app.terraplanet.terraplanet.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import app.terraplanet.terraplanet.R

@Composable
fun isDark(): Boolean = isSystemInDarkTheme()

@Composable
fun bgColor(): Color = if (isDark()) BgDark else BgLight

@Composable
fun bottomNavBgColor(): Color = if (isDark()) BnvDark else BnvLight

@Composable
fun colorAware(): Color = if (isDark()) Color.White else Color.Black

@Preview
@Composable
fun Title() {
    Text(
        stringResource(R.string.app_name),
        color = Color.White,
        fontSize = 40.sp,
        fontWeight = FontWeight.Bold,
    )
}