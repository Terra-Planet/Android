package app.terraplanet.terraplanet.screen.modal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.terraplanet.terraplanet.model.Coin
import app.terraplanet.terraplanet.ui.theme.MainBlue
import app.terraplanet.terraplanet.ui.theme.MainColor
import app.terraplanet.terraplanet.ui.theme.bgColor
import app.terraplanet.terraplanet.ui.theme.colorAware
import app.terraplanet.terraplanet.ui.util.*
import app.terraplanet.terraplanet.util.roundDecimal

enum class EarnTab {
    Deposit,
    Withdraw
}

@Composable
fun EarnModal(coin: Coin,
              earn: Double,
              isDeposit: Boolean,
              isLoading: Boolean,
              onSubmit: (Double, Boolean) -> Unit,
              modal: ModalTransitionDialogHelper
) {
    val scrollState = rememberScrollState()
    val earnSegment = remember { listOf(EarnTab.Deposit.name, EarnTab.Withdraw.name) }
    var selectedSegment by remember { mutableStateOf(if (isDeposit) EarnTab.Deposit.name else EarnTab.Withdraw.name) }
    var deposit by remember { mutableStateOf(isDeposit) }
    val ust by remember { mutableStateOf(coin) }

    var amount by remember { mutableStateOf(0.0) }

    val focusManager = LocalFocusManager.current

    BackHandler { modal.triggerAnimatedClose() }

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
            Text("Earn", fontSize = 40.sp, fontWeight = FontWeight.Bold)
            VSpacer(30)
            SegmentedControl(
                segments = earnSegment,
                selectedSegment = selectedSegment,
                onSegmentSelected = {
                    selectedSegment = it
                    deposit = selectedSegment == EarnTab.Deposit.name
                    amount = 0.0
                }
            ) {
                SegmentText(it)
            }
            VSpacer(30)
            Row {
                Text("Amount:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Expandable()
                Text(
                    "Balance: ${if (deposit) coin.amount.roundDecimal(4) else earn.roundDecimal(4)}",
                    fontSize = 18.sp
                )
                HSpacer(10)
                Text(
                    text = "Max.",
                    fontSize = 18.sp,
                    color = MainBlue,
                    modifier = Modifier.clickable {
                        amount = if (deposit)
                            if (coin.amount > 1.0) coin.amount - 1.0 else coin.amount
                            else earn })
            }
            VSpacer(4)
            Surface(
                border = BorderStroke(3.dp, MainColor),
                shape = RoundedCornerShape(10),
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent
            ) {
                SimpleCoinItem(amount, ust) {
                    val input = it.replace(",", "")
                    val coinAmount = if (coin.amount > 1.0) coin.amount - 1.0 else coin.amount
                    val earnAmount = if (earn > 1.0) earn - 1.0 else earn

                    if (isDeposit) {
                        val value = input.toDoubleOrNull()
                        value?.let { parsed ->
                            amount = if (parsed > coinAmount) {
                                coinAmount
                            } else {
                                parsed
                            }
                        }
                    } else {
                        val value = input.toDoubleOrNull()
                        value?.let { parsed ->
                            amount = if (parsed > earnAmount) {
                                earnAmount
                            } else {
                                parsed
                            }
                        }
                    }
                }
            }
            VSpacer(30)
            Button(
                onClick = {
                    focusManager.clearFocus()
                    onSubmit(amount, deposit)
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = MainColor),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25),
                enabled = if (deposit) amount >= 1.0 else amount >= 0.5 && coin.amount >= 0.5
            ) {
                Text(
                    text = selectedSegment.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (deposit) {
                VSpacer(10)
                Text(
                    "We suggest you to keep 1 UST at least to pay gas. Remember that in the Earn Section the gas will be paid always in UST",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            if (!deposit && coin.amount < 0.5) {
                VSpacer(10)
                Text(
                    "You don't have enough UST to pay fees and Withdraw your earnings. Please, deposit more funds or" +
                            " select a lower amount.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }

        if (isLoading) {
            LoadingOverlay(Color.White)
        }
    }
}

@Composable
private fun SimpleCoinItem(amount: Double, coin: Coin, onChangeValue: (String) -> Unit) {
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
        Expandable()
        BasicInput(
            value = if(amount == 0.0) "0" else "$amount",
            onValueChange = onChangeValue,
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
