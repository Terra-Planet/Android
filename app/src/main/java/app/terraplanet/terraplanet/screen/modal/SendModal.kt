package app.terraplanet.terraplanet.screen.modal

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import app.terraplanet.terraplanet.R
import app.terraplanet.terraplanet.model.Coin
import app.terraplanet.terraplanet.model.Send
import app.terraplanet.terraplanet.network.APIServiceImpl
import app.terraplanet.terraplanet.network.Denom
import app.terraplanet.terraplanet.screen.CameraActivity
import app.terraplanet.terraplanet.screen.HomeActivity
import app.terraplanet.terraplanet.ui.theme.*
import app.terraplanet.terraplanet.ui.util.*
import app.terraplanet.terraplanet.util.*
import app.terraplanet.terraplanet.viewmodel.WalletViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SendModal(
    context: HomeActivity,
    model: WalletViewModel,
    isLoading: Boolean,
    showDialog: Boolean,
    onSubmit: (Send) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modal: ModalTransitionDialogHelper
) {
    val scrollState = rememberScrollState()
    val state = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()
    val wallet = model.walletState.collectAsState()

    var coin by remember { mutableStateOf(wallet.value.coins.first()) }
    var totalBalance by remember { mutableStateOf(if (coin.denom == Denom.LUNA) coin.quantity else coin.amount) }
    fun show() = scope.launch { state.show() }
    fun hide() = scope.launch { state.hide() }

    val configuration = LocalConfiguration.current
    val focusManager = LocalFocusManager.current
    val screenHeight = configuration.screenHeightDp.dp + 30.dp

    var address by remember { mutableStateOf("") }
    var amount: ValidInput by remember { mutableStateOf(ValidInput("0", true)) }
    var memo: String? by remember { mutableStateOf(null) }
    var send: Send? by remember { mutableStateOf(null) }

    BackHandler { modal.triggerAnimatedClose() }

    val startForResult = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                intent?.let {
                    val data = it.getStringExtra(CameraActivity.RESULT) ?: ""
                    model.validate(data, onResult = { valid ->
                        if (valid) {
                            address = data
                        } else {
                            Toast.makeText(context, "Not a valid Terra address", Toast.LENGTH_SHORT).show()
                        }
                    }, {
                        Toast.makeText(context, "Not a valid Terra address", Toast.LENGTH_SHORT).show()
                    })
                }
            }
        }

    BoxWithConstraints(
        modifier = Modifier.height(screenHeight)
    ) {
        ModalBottomSheetLayout(
            sheetState = state,
            sheetElevation = 0.dp,
            sheetBackgroundColor = Color.Transparent,
            sheetContent = {
                Surface(color = bgColor(), modifier = Modifier.padding(top = 32.dp)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Select Coin", fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                        LazyColumn {
                            items(wallet.value.coins.size) {
                                ListItem(
                                    text = { Text(wallet.value.coins[it].denom.label, fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.clickable {
                                        coin = wallet.value.coins[it]
                                        totalBalance = if (coin.denom == Denom.LUNA) coin.quantity else coin.amount
                                        if (amount.valid) {
                                            if (amount.input.toDouble() > totalBalance) {
                                                amount = ValidInput("$totalBalance", true)
                                            }
                                        }
                                        hide()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        ) {
            Surface(
                color = bgColor(),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(topStartPercent = 4, topEndPercent = 4)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Container(modifier = Modifier.align(Alignment.End)) {
                        IconButton(onClick = modal::triggerAnimatedClose) {
                            Icon(
                                painter = rememberVectorPainter(image = Icons.Default.Close),
                                contentDescription = null,
                                tint = colorAware()
                            )
                        }
                    }
                    Text("Send To", fontSize = 40.sp, fontWeight = FontWeight.Bold)
                    VSpacer(30)
                    Container(modifier = Modifier.align(Alignment.Start)) {
                        Text("Address:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    VSpacer(10)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, if (isDark()) Color.DarkGray else Color.LightGray),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDark()) Color.Black else Color.White
                        ) {
                            BasicInput(
                                value = address,
                                onValueChange = { address = it }
                            )
                        }
                        HSpacer(5)
                        Button(
                            onClick = {
                                context.pasteFromClipboard { data ->
                                    model.validate(data, onResult = { valid ->
                                        if (valid) {
                                            address = data
                                        } else {
                                            Toast.makeText(context, "Not a valid Terra address", Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    }, {
                                        Toast.makeText(context, "Not a valid Terra address", Toast.LENGTH_SHORT).show()
                                    })
                                }
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                            shape = RoundedCornerShape(10),
                            elevation = ButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
                        ) {
                            Text(
                                text = "Paste",
                                color = MainBlue,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = {
                            startForResult.launch(Intent(context, CameraActivity::class.java))
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_qr_code),
                                tint = MainBlue,
                                contentDescription = null
                            )
                        }
                    }
                    VSpacer(30)
                    Container(modifier = Modifier.align(Alignment.Start)) {
                        Text("Coin:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    VSpacer(10)
                    Surface(
                        border = BorderStroke(3.dp, MainColor),
                        shape = RoundedCornerShape(10),
                        color = Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                focusManager.clearFocus()
                                show()
                            }
                    ) {
                        CoinSelector(coin)
                    }
                    VSpacer(40)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Amount:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Expandable()
                        Text("Balance:")
                        HSpacer(4)
                        Text(totalBalance.roundDecimal(if (coin.denom == Denom.UST) 2 else 4))
                        HSpacer(15)
                        Text("Max", fontSize = 18.sp, color = MainBlue, modifier = Modifier.clickable {
                            amount = ValidInput("$totalBalance", true)
                        })
                    }
                    VSpacer(10)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, if (isDark()) Color.DarkGray else Color.LightGray),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDark()) Color.Black else Color.White
                        ) {
                            Column {
                                Container(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                                    BasicInput(
                                        value = amount.input,
                                        onValueChange = { value ->
                                            val parsed = value.take(15)
                                                .trimStart('0')
                                                .replace(",", "")
                                                .replace("-", "")
                                                .replace(" ", "")
                                            val fix = if (parsed.startsWith(".")) "0$parsed" else parsed
                                            val isNumber = isNumeric(parsed)
                                            amount = if (isNumber) {
                                                val maxValue = if (coin.denom == Denom.LUNA) coin.quantity else coin.amount
                                                val number = fix.parseToDouble()
                                                ValidInput(fix, number <= maxValue)
                                            } else {
                                                ValidInput(fix, false)
                                            }
                                        },
                                        color = if (amount.valid) colorAware() else Color.Red,
                                        keyboardType = KeyboardType.Decimal,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        HSpacer(10)
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                send = Send(
                                    model.getGas(),
                                    wallet.value.coins.find { it.denom == model.getGas() },
                                    coin,
                                    amount.input.toDoubleOrNull() ?: amount.input.toInt().toDouble(),
                                    0.0,
                                    address,
                                    memo
                                )
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    context.launchBiometric(context, "Authenticate to send transaction",
                                        context.authenticationCallback(onSuccess = {
                                            onSubmit(send!!)
                                        }), unsupportedCallback = {
                                            onSubmit(send!!)
                                        })
                                } else {
                                    onSubmit(send!!)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = MainColor),
                            shape = RoundedCornerShape(20),
                            enabled = amount.valid &&
                                    (amount.input.parseToDouble()) > 0.0 &&
                                    address.isNotEmpty()
                        ) {
                            Text(
                                text = "SEND",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(5.dp)
                            )
                        }
                    }
                    VSpacer(20)
                    Container(modifier = Modifier.align(Alignment.Start)) {
                        Text("Memo (optional):")
                    }
                    VSpacer(10)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, if (isDark()) Color.DarkGray else Color.LightGray),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDark()) Color.Black else Color.White
                    ) {
                        BasicInput(
                            value = memo ?: "",
                            onValueChange = { memo = it },
                        )
                    }
                }
            }
        }

        if (isLoading) {
            LoadingOverlay(Color.White)
        }

        ShowSwapDialog(send, showDialog, onConfirm, onDismiss)
    }
}

@Composable
private fun BasicInput(
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = colorAware()
) {
    val focusManager = LocalFocusManager.current

    BoxWithConstraints(
        modifier = Modifier
            .clipToBounds()
            .widthIn(1.dp)
            .padding(vertical = 14.dp, horizontal = 8.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = textAlign
            ),
            cursorBrush = SolidColor(colorAware()),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            maxLines = 1
        )
    }
}

@Composable
private fun CoinSelector(coin: Coin) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            bitmap = context.bitmapDrawable(coin.icon)!!,
            contentDescription = null,
            modifier = Modifier
                .width(35.dp)
                .height(35.dp)
        )
        HSpacer(10)
        Box(
            modifier = Modifier.width(55.dp)
        ) {
            Text(coin.denom.label, color = colorAware(), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Expandable()
        Icon(
            painter = rememberVectorPainter(image = Icons.Default.ArrowDropDown),
            contentDescription = null,
            tint = MainColor
        )
    }
}

@Composable
private fun ShowSwapDialog(
    sendData: Send?,
    show: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val api = APIServiceImpl()
    val wallet = api.getWallet(context)

    if (show) {
        sendData?.let { send ->
            var haveBalance = false
            send.gasCoin?.let {  fee ->
                haveBalance = if (send.coin.denom == fee.denom) {
                    val left = if (fee.denom == Denom.LUNA) fee.quantity - send.amount else fee.amount - send.amount
                    left >= send.fee
                } else {
                    val left = if (fee.denom == Denom.LUNA) fee.quantity - send.fee else fee.amount - send.fee
                    left >= send.fee
                }
            }

            AlertDialog(
                onDismissRequest = onConfirm,
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
                confirmButton = {
                    if (haveBalance) {
                        ConfirmButton(onConfirm)
                    } else {
                        DismissButton(onDismiss)
                    }
                },
                dismissButton = if (haveBalance) (
                        { DismissButton(onDismiss) }
                        ) else null,
                title = {
                    Text(
                        if (haveBalance) "SEND" else "ATTENTION",
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = if (haveBalance) "From: ${wallet?.address}\n\n" +
                                "To: ${send.address}\n\n" +
                                "Amount: ${send.amount}\n\n" +
                                "Fee: ${send.fee} ${send.gas.label}"
                        else "You don't have enough ${send.gas.label} balance to pay fees and perform this action.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 18.sp
                    )
                }
            )
        }
    }
}

@Composable
private fun ConfirmButton(onConfirm: () -> Unit) {
    TextButton(onClick = onConfirm)
    { Text(text = "Sign it!", fontSize = 18.sp) }
}

@Composable
private fun DismissButton(onDismiss: () -> Unit) {
    TextButton(onClick = onDismiss)
    { Text(text = "Cancel", fontSize = 18.sp) }
}
