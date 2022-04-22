package app.terraplanet.terraplanet.screen.modal

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
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
import app.terraplanet.terraplanet.ui.theme.MainBlue
import app.terraplanet.terraplanet.ui.theme.MainColor
import app.terraplanet.terraplanet.ui.theme.bgColor
import app.terraplanet.terraplanet.ui.theme.colorAware
import app.terraplanet.terraplanet.ui.util.*
import app.terraplanet.terraplanet.util.roundDecimal

@Composable
fun SwapModal(
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
    val context = LocalContext.current

    val scrollState = rememberScrollState()
    val denoms = listOf(
        Coin(Denom.LUNA, R.drawable.coin_luna, 0.0, 0.0),
        Coin(Denom.UST, R.drawable.coin_ust, 0.0, 0.0),
    )
    var swapCoins by remember { mutableStateOf(listOf(denoms.first(), denoms.last())) }

    val lunaQuantity = coins.find { it.denom == Denom.LUNA }?.quantity ?: 0.0
    val uusdQuantity = coins.find { it.denom == Denom.UST }?.quantity ?: 0.0

    var luna by remember { mutableStateOf(0.0) }
    var uusd by remember { mutableStateOf(0.0) }

    val focusManager = LocalFocusManager.current

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
            Text("Swap", fontSize = 40.sp, fontWeight = FontWeight.Bold)
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
                    onValueChange = {
                        val input = it.replace(",", "")
                        if (swapCoins.first().denom == Denom.UST) {
                            val value = input.toDoubleOrNull()
                            value?.let {
                                uusd = if (value > uusdQuantity) uusdQuantity else value
                                luna = uusd / lunaPrice
                            }
                        } else {
                            val value = input.toDoubleOrNull()
                            value?.let {
                                luna = if (value > lunaQuantity) lunaQuantity else value
                                uusd = luna * lunaPrice
                            }
                        }
                    }
                )
            }
            VSpacer(20)
            IconButton(onClick = {
                luna = 0.0
                uusd = 0.0
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
                    onValueChange = {
                        val input = it.replace(",", "")
                        if (swapCoins.last().denom == Denom.UST) {
                            val value = input.toDoubleOrNull()
                            value?.let {
                                uusd = if (value > uusdQuantity) uusdQuantity else value
                                luna = uusd / lunaPrice
                            }
                        } else {
                            val value = input.toDoubleOrNull()
                            value?.let {
                                luna = if (value > lunaQuantity) lunaQuantity else value
                                uusd = luna * lunaPrice
                            }
                        }
                    }
                )
            }
            VSpacer(30)
            Button(
                onClick = {
                    focusManager.clearFocus()
                    onSubmit(
                        Swap(
                            swapCoins.first(),
                            swapCoins.last(),
                            if (swapCoins.first().denom == Denom.UST) uusd else luna,
                            0.0,
                            api.getPayGas(context)
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = MainColor),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25),
                enabled = uusd > 0.0 && luna > 0.0
            ) {
                Text(
                    text = "SWAP",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(5.dp)
                )
            }
        }

        if (isLoading) {
            LoadingOverlay(Color.White)
        }

        ShowSwapDialog(swap, showDialog, lunaPrice, onConfirm, onDismiss)
    }
}

@Composable
private fun SimpleCoinItem(
    coin: Coin,
    amount: Double,
    onValueChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = coin.icon),
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
        BasicInput(
            value = if(amount == 0.0) "0" else amount.roundDecimal(if (coin.denom == Denom.UST) 2 else 6),
            onValueChange = onValueChange,
            keyboardType = KeyboardType.Decimal,
            color = colorAware()
        )
    }
}

@Composable
private fun BasicInput(
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    color: Color = Color.White
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
            modifier = Modifier.width(IntrinsicSize.Min),
            textStyle = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color,
            ),
            cursorBrush = SolidColor(colorAware()),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
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
                    { Text(text = "Sign it!", fontSize = 18.sp) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss)
                    { Text(text = "Cancel", fontSize = 18.sp) }
                },
                title = {
                    Text(
                        "SWAP",
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text =  "From: ${it.from.denom.label}\n" +
                                "To: ${it.to.denom.label}\n\n" +
                                "Amount: ${if (it.from.denom == Denom.LUNA) it.amount * lunaPrice else it.amount / lunaPrice}\n\n" +
                                "Fee: ${swap.fee} ${swap.pay.label}",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 18.sp
                    )
                }
            )
        }
    }
}