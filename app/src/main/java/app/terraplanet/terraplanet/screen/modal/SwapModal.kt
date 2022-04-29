package app.terraplanet.terraplanet.screen.modal

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import app.terraplanet.terraplanet.model.Swap
import app.terraplanet.terraplanet.network.APIServiceImpl
import app.terraplanet.terraplanet.network.Denom
import app.terraplanet.terraplanet.screen.HomeActivity
import app.terraplanet.terraplanet.ui.theme.MainBlue
import app.terraplanet.terraplanet.ui.theme.MainColor
import app.terraplanet.terraplanet.ui.theme.bgColor
import app.terraplanet.terraplanet.ui.theme.colorAware
import app.terraplanet.terraplanet.ui.util.*
import app.terraplanet.terraplanet.util.ValidInput
import app.terraplanet.terraplanet.util.bitmapDrawable
import app.terraplanet.terraplanet.util.isNumeric
import app.terraplanet.terraplanet.util.parseToDouble

@Composable
fun SwapModal(
    context: HomeActivity,
    swap: Swap?,
    coins: List<Coin>,
    lunaPrice: Double,
    isLoading: Boolean,
    showDialog: Boolean,
    onSubmit: (Swap) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modal: ModalTransitionDialogHelper
) {
    val api = APIServiceImpl()

    val scrollState = rememberScrollState()
    val denoms = listOf(
        Coin(Denom.LUNA, R.drawable.coin_luna, 0.0, 0.0),
        Coin(Denom.UST, R.drawable.coin_ust, 0.0, 0.0),
    )
    var swapCoins by remember { mutableStateOf(listOf(denoms.first(), denoms.last())) }

    val lunaQuantity = coins.find { it.denom == Denom.LUNA }?.quantity ?: 0.0
    val uusdQuantity = coins.find { it.denom == Denom.UST }?.quantity ?: 0.0

    var luna by remember { mutableStateOf(ValidInput("0", true)) }
    var uusd by remember { mutableStateOf(ValidInput("0", true)) }

    var lunaRate by remember { mutableStateOf(lunaPrice) }

    val focusManager = LocalFocusManager.current

    BackHandler { modal.triggerAnimatedClose() }

    api.getLunaRate(rate = { lunaRate = it })

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
            Text(stringResource(R.string.swap_title), fontSize = 40.sp, fontWeight = FontWeight.Bold)
            VSpacer(30)
            Surface(
                border = BorderStroke(3.dp, MainColor),
                shape = RoundedCornerShape(10),
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent
            ) {
                SimpleCoinItem(
                    coin = swapCoins.first(),
                    amount = if (swapCoins.first().denom == Denom.UST) uusd else luna,
                    onValueChange = { value ->
                        val parsed = value.take(15)
                            .trimStart('0')
                            .replace(",", "")
                            .replace("-", "")
                            .replace(" ", "")
                        val isNumber = isNumeric(parsed)
                        if (swapCoins.first().denom == Denom.UST) {
                            val fix = if (parsed.startsWith(".")) "0$parsed" else parsed
                            if (isNumber) {
                                val number = parsed.parseToDouble()
                                uusd = ValidInput(fix, number <= uusdQuantity)
                                luna = ValidInput("${number / lunaRate}", true)
                            } else {
                                uusd = ValidInput(fix, false)
                            }
                        } else {
                            val fix = if (parsed.startsWith(".")) "0$parsed" else parsed
                            if (isNumber) {
                                val number = parsed.parseToDouble()
                                luna = ValidInput(fix, number <= lunaQuantity)
                                uusd = ValidInput("${number * lunaRate}", true)
                            } else {
                                luna = ValidInput(fix, false)
                            }
                        }
                    }
                )
            }
            VSpacer(20)
            IconButton(onClick = {
                luna = ValidInput("0", true)
                uusd = ValidInput("0", true)
                focusManager.clearFocus()
                swapCoins = if (swapCoins.first().denom == denoms.first().denom) {
                    listOf(denoms.last(), denoms.first())
                } else {
                    listOf(denoms.first(), denoms.last())
                }
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_swap),
                    contentDescription = null,
                    modifier = Modifier
                        .rotate(90f)
                        .size(35.dp),
                    tint = MainBlue
                )
            }
            VSpacer(20)
            Surface(
                border = BorderStroke(3.dp, MainColor),
                shape = RoundedCornerShape(10),
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent
            ) {
                SimpleCoinItem(
                    coin = swapCoins.last(),
                    amount = if (swapCoins.last().denom == Denom.UST) uusd else luna,
                    onValueChange = { value ->
                        val parsed = value.take(15)
                            .trimStart('0')
                            .replace(",", "")
                            .replace("-", "")
                            .replace(" ", "")
                        val isNumber = isNumeric(parsed)
                        if (swapCoins.last().denom == Denom.UST) {
                            val fix = if (parsed.startsWith(".")) "0$parsed" else parsed
                            if (isNumber) {
                                val number = fix.parseToDouble()
                                uusd = ValidInput(fix, number <= uusdQuantity)
                                luna = ValidInput("${number / lunaRate}", true)
                            } else {
                                uusd = ValidInput(fix, false)
                            }
                        } else {
                            val fix = if (parsed.startsWith(".")) "0$parsed" else parsed
                            if (isNumber) {
                                val number = parsed.parseToDouble()
                                luna = ValidInput(fix, number <= lunaQuantity)
                                uusd = ValidInput("${number * lunaRate}", true)
                            } else {
                                luna = ValidInput(fix, false)
                            }
                        }
                    }
                )
            }
            VSpacer(30)
            Button(
                onClick = {
                    focusManager.clearFocus()

                    val swapData = Swap(
                        swapCoins.first(),
                        swapCoins.last(),
                        if (swapCoins.first().denom == Denom.UST) uusd.input.parseToDouble() else luna.input.parseToDouble(),
                        0.0,
                        api.getPayGas(context)
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        context.launchBiometric(
                            context,
                            context.getString(R.string.swap_authenticate),
                            context.authenticationCallback(onSuccess = {
                                onSubmit(swapData)
                            }), unsupportedCallback = {
                                onSubmit(swapData)
                            })
                    } else {
                        onSubmit(swapData)
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = MainColor),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25),
                enabled = (uusd.valid && uusd.input.parseToDouble() > 0.0) && (luna.valid && luna.input.parseToDouble() > 0.0)
            ) {
                Text(
                    stringResource(R.string.swap_dialog_title),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(5.dp)
                )
            }
        }

        if (isLoading) {
            LoadingOverlay(Color.White)
        }

        ShowSwapDialog(swap, showDialog, lunaRate, onConfirm, onDismiss)
    }
}

@Composable
private fun SimpleCoinItem(
    coin: Coin,
    amount: ValidInput,
    onValueChange: (String) -> Unit,
) {
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
        Box(modifier = Modifier.weight(1f))
        HSpacer(10)
        BasicInput(
            value = amount.input,
            onValueChange = onValueChange,
            keyboardType = KeyboardType.Decimal,
            color = if (amount.valid) colorAware() else Color.Red
        )
    }
}

@Composable
private fun BasicInput(
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    textAlign: TextAlign = TextAlign.End,
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
            maxLines = 1,
        )
    }
}

@Composable
private fun ShowSwapDialog(
    swap: Swap?,
    show: Boolean,
    lunaPrice: Double,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (show) {
        swap?.let {
            AlertDialog(
                onDismissRequest = onConfirm,
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
                confirmButton = {
                    TextButton(onClick = onConfirm)
                    { Text(stringResource(R.string.swap_dialog_sign), fontSize = 18.sp) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss)
                    { Text(stringResource(R.string.swap_dialog_cancel), fontSize = 18.sp) }
                },
                title = {
                    Text(
                        stringResource(R.string.swap_dialog_title),
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.swap_dialog_description_from, it.from.denom.label)+ "\n" +
                                stringResource(R.string.swap_dialog_description_to, it.to.denom.label)+ "\n\n" +
                                stringResource(R.string.swap_dialog_description_amount, if (it.from.denom == Denom.LUNA) it.amount * lunaPrice else it.amount / lunaPrice)+ "\n\n" +
                                stringResource(R.string.swap_dialog_description_fee, swap.pay.label),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 18.sp
                    )
                }
            )
        }
    }
}