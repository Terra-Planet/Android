package app.terraplanet.terraplanet.screen.tab

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.terraplanet.terraplanet.R
import app.terraplanet.terraplanet.model.Coin
import app.terraplanet.terraplanet.model.Send
import app.terraplanet.terraplanet.model.Swap
import app.terraplanet.terraplanet.network.APIServiceImpl
import app.terraplanet.terraplanet.network.Denom
import app.terraplanet.terraplanet.network.Net
import app.terraplanet.terraplanet.screen.HomeActivity
import app.terraplanet.terraplanet.screen.modal.EarnModal
import app.terraplanet.terraplanet.screen.modal.ReceiveQrScreen
import app.terraplanet.terraplanet.screen.modal.SendModal
import app.terraplanet.terraplanet.screen.modal.SwapModal
import app.terraplanet.terraplanet.ui.theme.MainColor
import app.terraplanet.terraplanet.ui.theme.Orange
import app.terraplanet.terraplanet.ui.util.*
import app.terraplanet.terraplanet.util.roundDecimal
import app.terraplanet.terraplanet.viewmodel.SettingsViewModel
import app.terraplanet.terraplanet.viewmodel.State
import app.terraplanet.terraplanet.viewmodel.WalletViewModel

@SuppressLint("MutableCollectionMutableState")
@Composable
fun WalletTab(activity: ComponentActivity,
              model: WalletViewModel,
              settings: SettingsViewModel, navigateToSettings: () -> Unit
) {

    val wallet = model.walletState.collectAsState()
    val setting = settings.settingsState.collectAsState()

    val scrollState = rememberScrollState()
    var showEarnModal by remember { mutableStateOf(false) }
    var showReceiveModal by remember { mutableStateOf(false) }
    var showSwapModal by remember { mutableStateOf(false) }
    var showSendModal by remember { mutableStateOf(false) }

    var isEarnLoading by remember { mutableStateOf(false) }
    var isEarnDeposit: Boolean? by remember { mutableStateOf(null) }

    var isSwapLoading by remember { mutableStateOf(false) }
    var showSwapDialog by remember { mutableStateOf(false) }

    var isSendLoading by remember { mutableStateOf(false) }
    var showSendDialog by remember { mutableStateOf(false) }

    var swapData: Swap? by remember { mutableStateOf(null) }
    var sendData: Send? by remember { mutableStateOf(null) }

    val context = (activity as HomeActivity)

    model.updateNetwork(setting.value.network)

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        if (setting.value.network == Net.TEST) {
            Surface(modifier = Modifier
                .fillMaxWidth()
                .clickable { navigateToSettings() }, color = Orange) {
                Center {
                    Text(
                        text = Net.TEST.label.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }
        VSpacer(20)
        Row {
            Text("Total Balance", fontSize = 20.sp)
            HSpacer(5)
            Icon(
                painter = rememberVectorPainter(image = Icons.Default.Refresh),
                contentDescription = null,
                modifier = Modifier.clickable { model.fetchWallet() }
            )
        }
        VSpacer(6)
        Text("$${wallet.value.amount}", fontSize = 50.sp, fontWeight = FontWeight.Bold)
        VSpacer(30)
        Text("Your Coins", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        VSpacer(6)
        Surface(
            shape = RoundedCornerShape(10),
            color = MainColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                when(wallet.value.state) {
                    State.SUCCESS -> {
                        if (wallet.value.coins.isEmpty()) {
                            Text(text = "You don't own coins yet",
                                color = Color.LightGray,
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
                            )
                        } else {
                            wallet.value.coins.forEach {
                                VSpacer(4)
                                CoinItem(it)
                                VSpacer(4)
                            }
                        }
                    }
                    State.FAILED -> Text(text = "Error getting coins, try again",
                        color = Color.LightGray,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
                    )
                    State.LOADING -> Text(text = "Getting coins...",
                        color = Color.LightGray,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
                    )
                    State.CANCELLED -> {}
                }
            }
        }
        VSpacer(30)
        Text("Actions", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        VSpacer(8)
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            ReceiveButtonSection(
                onDismissRequest = { showReceiveModal = false },
                showReceiveModal = showReceiveModal
            ) { WalletAction(text = "Receive", icon = R.drawable.icon_receive, onClick = { showReceiveModal = true }) }
            HSpacer(10)
            SwapButtonSection(
                swapData,
                wallet.value.coins,
                wallet.value.lunaPrice,
                onDismissRequest = { showSwapModal = false },
                showSwapModal = showSwapModal,
                isLoading = isSwapLoading,
                showDialog = showSwapDialog,
                onSubmit = {
                    isSwapLoading = true
                    model.swapPreview(it, onDone = { swap ->
                        swapData = swap
                        isSwapLoading = false
                        showSwapDialog = true
                    }, onError = {
                        isSwapLoading = false
                    })
                },
                onConfirm = {
                    showSwapDialog = false
                    isSwapLoading = true
                    model.swap(swapData!!, onDone = { swap ->
                        swapData = swap
                        isSwapLoading = false
                        showSwapModal = false
                        model.fetchWallet()
                    }, onError = {
                        isSwapLoading = false
                    })
                },
                onDismiss = {
                    showSwapDialog = false
                    isSwapLoading = false
                }
            ) { WalletAction(text = "Swap", icon = R.drawable.icon_swap, onClick = { showSwapModal = true }) }
            HSpacer(10)
            SendButtonSection(
                context = context,
                model = model,
                onDismissRequest = { showSendModal = false },
                showSendModal = showSendModal,
                enabled = wallet.value.coins.isNotEmpty(),
                isLoading = isSendLoading,
                showDialog = showSendDialog,
                onSubmit = {
                    isSendLoading = true
                    model.sendPreview(it, onDone = { send ->
                        sendData = send
                        isSendLoading = false
                        showSendDialog = true
                    }, onError = {
                        isSendLoading = false
                    })
                },
                onConfirm = {
                    showSendDialog = false
                    isSendLoading = true
                    model.send(sendData!!, onDone = { send ->
                        sendData = send
                        isSendLoading = false
                        showSendModal = false
                        model.fetchWallet()
                    }, onError = {
                        isSendLoading = false
                    })
                },
                onDismiss = {
                    showSendDialog = false
                    isSendLoading = false
                }
            ) { WalletAction(text = "Send", icon = R.drawable.icon_send, onClick = { showSendModal = true }) }
        }
        VSpacer(40)
        Text("Earn (${wallet.value.rate.roundDecimal(2)}% APY)", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        VSpacer(8)
        Surface(
            border = BorderStroke(3.dp, MainColor),
            shape = RoundedCornerShape(10),
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "$${wallet.value.earn.roundDecimal(2)}",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
                VSpacer(13)

                EarnButtonSection(
                    coin = wallet.value.coins.find { it.denom == Denom.UST },
                    earn = wallet.value.earn,
                    isLoading = isEarnLoading,
                    isDeposit = isEarnDeposit ?: false,
                    onEarnDeposit = { isEarnDeposit = it },
                    onShowDialog = { showEarnModal = true },
                    onDismissRequest = { showEarnModal = false },
                    showEarnModal = showEarnModal,
                    onSubmit = { amount, deposit ->
                        isEarnLoading = true
                        if (deposit) {
                            model.anchorDeposit(
                                amount = amount,
                                onDone = {
                                    showEarnModal = false
                                    isEarnLoading = false
                                    model.fetchWallet()
                                },
                                onError = {
                                    isEarnLoading = false
                                    Toast.makeText(context, "Error. Please, try again.", Toast.LENGTH_SHORT).show()
                                })
                        } else {
                            model.anchorWithdraw(
                                amount = amount,
                                onDone = {
                                    showEarnModal = false
                                    isEarnLoading = false
                                    model.fetchWallet()
                                },
                                onError = {
                                    isEarnLoading = false
                                    Toast.makeText(context, "Error. Please, try again.", Toast.LENGTH_SHORT).show()
                                })
                        }
                    }
                )
            }
        }
        VSpacer(60)
    }

    if (wallet.value.state == State.LOADING) {
        LoadingOverlay(Color.White)
    }
}

@Composable
private fun ReceiveButtonSection(
    onDismissRequest: () -> Unit,
    showReceiveModal: Boolean,
    content: @Composable () -> Unit
) {
    val api = APIServiceImpl()
    val address = api.getWallet(LocalContext.current)?.address ?: ""

    content()

    if (showReceiveModal) {
        ShowReceiveDialog(onDismissRequest, address)
    }
}

@Composable
private fun SwapButtonSection(
    swap: Swap?,
    coins: List<Coin>,
    lunaPrice: Double,
    isLoading: Boolean,
    showDialog: Boolean,
    showSwapModal: Boolean,
    onSubmit: (Swap) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {

    content()

    if (showSwapModal) {
        ShowSwapDialog(swap, coins, lunaPrice, isLoading, showDialog, onSubmit, onConfirm, onDismiss, onDismissRequest)
    }
}

@Composable
private fun SendButtonSection(
    context: HomeActivity,
    model: WalletViewModel,
    onDismissRequest: () -> Unit,
    showSendModal: Boolean,
    enabled: Boolean,
    isLoading: Boolean,
    showDialog: Boolean,
    onSubmit: (Send) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {

    content()

    if (showSendModal && enabled) {
        ShowSendDialog(context, model, isLoading, showDialog, onSubmit, onConfirm, onDismiss, onDismissRequest)
    }
}

@Composable
private fun EarnButtonSection(
    coin: Coin?,
    earn: Double,
    isLoading: Boolean,
    isDeposit: Boolean,
    onEarnDeposit: (Boolean) -> Unit,
    onSubmit: (Double, Boolean) -> Unit,
    onShowDialog: () -> Unit,
    onDismissRequest: () -> Unit,
    showEarnModal: Boolean
) {

    Row {
        DepositEarn(onShowDialog) { onEarnDeposit(it) }
        HSpacer(16)
        WithdrawEarn(onShowDialog) { onEarnDeposit(it) }
    }

    if (showEarnModal) {
        coin?.let {
            ShowEarnDialog(it, earn, isDeposit, isLoading, onSubmit, onDismissRequest)
        }
    }
}

@Composable
fun CoinItem(coin: Coin) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            Text("${coin.denom.label}:", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        HSpacer(6)
        Text(
            coin.quantity.roundDecimal(if (coin.denom == Denom.LUNA) 4 else 2),
            color = Color.White,
            fontSize = 18.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Expandable()
        Text(
            "$${coin.amount.roundDecimal(2)}",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RowScope.WalletAction(text: String, icon: Int, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14),
        color = MainColor,
        modifier = Modifier.weight(1f),
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(painter = painterResource(id = icon), contentDescription = null, tint = Color.White)
            VSpacer(6)
            Text(text, fontSize = 18.sp, color = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DepositEarn(onShowDialog: () -> Unit, isDeposit: (Boolean) -> Unit) {
    Surface(
        shape = RoundedCornerShape(14),
        color = MainColor,
        onClick = {
            isDeposit(true)
            onShowDialog()
        }
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            HSpacer(16)
            Icon(painter = painterResource(id = R.drawable.icon_deposit), contentDescription = null, tint = Color.White)
            HSpacer(8)
            Text("Deposit", color = Color.White, fontSize = 20.sp)
            HSpacer(16)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WithdrawEarn(onShowDialog: () -> Unit, isDeposit: (Boolean) -> Unit) {
    Surface(
        shape = RoundedCornerShape(14),
        color = MainColor,
        onClick = {
            isDeposit(false)
            onShowDialog()
        }
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            HSpacer(16)
            Icon(
                painter = painterResource(id = R.drawable.icon_withdraw),
                contentDescription = null,
                tint = Color.White
            )
            HSpacer(8)
            Text("Withdraw", color = Color.White, fontSize = 20.sp)
            HSpacer(16)
        }
    }
}

@Composable
private fun ShowEarnDialog(
    coin: Coin,
    earn: Double,
    isDeposit: Boolean,
    isLoading: Boolean,
    onSubmit: (Double, Boolean) -> Unit,
    onDismissRequest: () -> Unit,
) {

    ModalTransitionDialog(onDismissRequest = onDismissRequest) {
        EarnModal(coin, earn, isDeposit, isLoading, onSubmit, it)
    }
}

@Composable
private fun ShowSwapDialog(
    swap: Swap?,
    coins: List<Coin>,
    lunaPrice: Double,
    isLoading: Boolean,
    showDialog: Boolean,
    onSubmit: (Swap) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalTransitionDialog(onDismissRequest = onDismissRequest) {
        SwapModal(swap, coins, lunaPrice, isLoading, showDialog, onSubmit, onConfirm, onDismiss, it)
    }
}

@Composable
private fun ShowReceiveDialog(
    onDismissRequest: () -> Unit,
    address: String
) {
    ModalTransitionDialog(onDismissRequest = onDismissRequest) {
        ReceiveQrScreen(it, address)
    }
}

@Composable
private fun ShowSendDialog(
    context: HomeActivity,
    model: WalletViewModel,
    isLoading: Boolean,
    showDialog: Boolean,
    onSubmit: (Send) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalTransitionDialog(onDismissRequest = onDismissRequest) {
        SendModal(context, model, isLoading, showDialog, onSubmit, onConfirm, onDismiss, it)
    }
}