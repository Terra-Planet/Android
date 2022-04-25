package app.terraplanet.terraplanet.screen.tab

import android.os.Build
import androidx.activity.ComponentActivity
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import app.terraplanet.terraplanet.network.APIServiceImpl
import app.terraplanet.terraplanet.network.Denom
import app.terraplanet.terraplanet.network.Net
import app.terraplanet.terraplanet.screen.HomeActivity
import app.terraplanet.terraplanet.ui.theme.MainColor
import app.terraplanet.terraplanet.ui.util.*
import app.terraplanet.terraplanet.viewmodel.SettingsViewModel

@Composable
fun SettingsTab(activity: ComponentActivity, settings: SettingsViewModel) {
    val api = APIServiceImpl()
    val scrollState = rememberScrollState()
    val showSeedDialog: Boolean by settings.showSeedDialog.collectAsState()
    val showLogoutDialog: Boolean by settings.showLogoutDialog.collectAsState()
    val context = (activity as HomeActivity)

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
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    context.launchBiometric(context, "Authenticate to show Seed Phrase",
                        context.authenticationCallback(onSuccess = {
                            settings.openSeedDialog()
                        }), unsupportedCallback = {
                            settings.openSeedDialog()
                        })
                } else {
                    settings.openSeedDialog()
                }
            },
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
            onClick = { settings.openLogoutDialog() },
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
        showSeedDialog,
        api.getWallet(context)?.mnemonic ?: "",
        settings::onSeedDialogConfirm
    )

    ShowLogoutDialog(
        showLogoutDialog,
        onConfirm = {
            api.nukeWallet(context)
            settings.dismissLogoutDialog()
            settings.resetInitial(context)
        }, onDismiss = {
            settings.dismissLogoutDialog()
        }
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
                { Text(text = "OK", fontWeight = FontWeight.Bold) }
            },
            title = { Text("Your seed phrase is:", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = { Text(seedPhrase, fontSize = 16.sp) }
        )
    }
}

@Composable
private fun ShowLogoutDialog(
    show: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onConfirm,
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
            confirmButton = {
                TextButton(onClick = onConfirm)
                { Text(text = "Confirm", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = onDismiss)
                { Text(text = "Cancel", fontWeight = FontWeight.Bold) }
            },
            title = { Text("ATTENTION", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = { Text("Make sure your Seed Phrase is saved. If you don't have your Seed Phrase you will lose " +
                    "access to your Wallet forever.", fontSize = 16.sp) }
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
