package app.terraplanet.terraplanet.screen.tab

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import app.terraplanet.terraplanet.network.APIServiceImpl
import app.terraplanet.terraplanet.network.Denom
import app.terraplanet.terraplanet.network.Net
import app.terraplanet.terraplanet.ui.theme.MainColor
import app.terraplanet.terraplanet.ui.util.*
import app.terraplanet.terraplanet.viewmodel.SettingsViewModel

@Composable
fun SettingsTab(settings: SettingsViewModel) {
    val api = APIServiceImpl()
    val scrollState = rememberScrollState()
    val showDialog: Boolean by settings.showDialog.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VSpacer(30)
        Text("Settings", fontSize = 40.sp, fontWeight = FontWeight.Bold)
        VSpacer(30)
        Surface(
            border = BorderStroke(3.dp, MainColor),
            shape = RoundedCornerShape(10),
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pay gas with:", fontSize = 18.sp)
                Expandable()
                PayGasSegmented(settings)
            }
        }
        VSpacer(24)
        Surface(
            border = BorderStroke(3.dp, MainColor),
            shape = RoundedCornerShape(10),
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Network:", fontSize = 18.sp)
                Expandable()
                NetworkSegmented(settings)
            }
        }
        VSpacer(40)
        Button(
            onClick = { settings.openDialog() },
            colors = ButtonDefaults.buttonColors(backgroundColor = MainColor),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(25)
        ) {
            Text(
                text = "See my Seed Phrase",
                color = Color.White,
                fontSize = 18.sp
            )
        }
        VSpacer(10)
        Button(
            onClick = {
                api.nukeWallet(context)
                settings.resetInitial(context)
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(25),
            elevation = ButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            )
        ) {
            Text(
                text = "Log Out",
                color = Color.Red,
                fontSize = 18.sp
            )
        }
        VSpacer(60)
    }

    ShowSeedDialog(
        showDialog,
        api.getWallet(context)?.mnemonic ?: "",
        settings::onDialogConfirm
    )
}

@Composable
private fun ShowSeedDialog(
    show: Boolean,
    seedPhrase: String,
    onConfirm: () -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onConfirm,
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
            confirmButton = {
                TextButton(onClick = onConfirm)
                { Text(text = "OK") }
            },
            title = { Text("Your seed phrase is:") },
            text = { Text(seedPhrase) }
        )
    }
}

@Composable
private fun PayGasSegmented(settings: SettingsViewModel) {
    val setting = settings.settingsState.collectAsState()
    val paySegment = remember { listOf(Denom.UST.label, Denom.LUNA.label) }
    var selectedSegment by remember { mutableStateOf(setting.value.gas.label) }

    Box(
        modifier = Modifier.width(150.dp)
    ) {
        SegmentedControl(
            segments = paySegment,
            selectedSegment = selectedSegment,
            onSegmentSelected = {
                settings.savePayGas(it)
                selectedSegment = it
            }
        ) {
            SegmentText(it)
        }
    }
}

@Composable
private fun NetworkSegmented(settings: SettingsViewModel) {
    val setting = settings.settingsState.collectAsState()
    val networkSegment = remember { listOf(Net.TEST.label, Net.MAIN.label) }
    var selectedSegment by remember { mutableStateOf(setting.value.network.label) }

    Box(
        modifier = Modifier.width(150.dp)
    ) {
        SegmentedControl(
            segments = networkSegment,
            selectedSegment = selectedSegment,
            onSegmentSelected = {
                settings.saveNetwork(it)
                selectedSegment = it
            }
        ) {
            SegmentText(it)
        }
    }
}
