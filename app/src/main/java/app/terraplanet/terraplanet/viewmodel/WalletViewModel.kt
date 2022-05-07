package app.terraplanet.terraplanet.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.terraplanet.terraplanet.model.Coin
import app.terraplanet.terraplanet.model.Send
import app.terraplanet.terraplanet.model.Swap
import app.terraplanet.terraplanet.network.APIServiceImpl
import app.terraplanet.terraplanet.network.APIServiceImpl.Companion.LOG_E
import app.terraplanet.terraplanet.network.Denom
import app.terraplanet.terraplanet.network.Net
import app.terraplanet.terraplanet.util.roundDecimal
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.*

class WalletViewModel(val app: Application): AndroidViewModel(app) {
    private val _walletState = MutableStateFlow(
        WalletState(ResultState.LOADING, "0.00", 0.0, 0.0, Denom.UST, 0.0, listOf())
    )
    private var network = MutableStateFlow(api.getNetwork(app.applicationContext))

    val walletState: StateFlow<WalletState> get() = _walletState

    init {
        api.getWallet(app.applicationContext)
        network.onEach {
                fetchWallet()
            }.launchIn(viewModelScope)
    }

    fun getGas(): Denom {
        return api.getPayGas(app.applicationContext)
    }

    fun updateNetwork(net: Net) {
        network.value = net
    }

    fun fetchWallet() {
        val coins = mutableListOf<Coin>()
        var earn = 0.0
        var rate = 0.0
        var lunaPrice = 1.0
        var amount = "0.00"
        var gas = Denom.UST

        _walletState.update {
            WalletState(ResultState.LOADING, amount, earn, rate, gas, lunaPrice, coins).copy()
        }

        Single.zip(
            api.getBalance(),
            api.getEarnBalance(),
            api.getRate()
        ) { balance, earnBalance, market ->
            lunaPrice = api.getLunaPrice()
            coins.addAll(balance)
            var total = 0.0
            earn = earnBalance.amount.toDouble()
            coins.forEach { coin -> total += coin.amount }
            amount = (total + earnBalance.amount.toDouble()).roundDecimal(2)
            rate = market.rate
            gas = api.getPayGas(app.applicationContext)
        }.observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _walletState.update { WalletState(ResultState.SUCCESS, amount, earn, rate, gas, lunaPrice, coins).copy() }
            }, {
                _walletState.update { WalletState(ResultState.FAILED, amount, earn, rate, gas, lunaPrice, coins).copy() }
            })
    }

    fun userBalance() {
        api.getBalance()
    }

    fun anchorDeposit(amount: Double, onDone: () -> Unit, onError: () -> Unit) {
        api.anchorDeposit(amount)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onDone() }, {
                onError()
            })
    }

    fun anchorWithdraw(amount: Double, onDone: () -> Unit, onError: () -> Unit) {
        api.anchorWithdraw(amount)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onDone() }, {
                Log.e(LOG_E, "${it.message}")
                onError()
            })
    }

    fun swapPreview(swap: Swap, onDone: (Swap) -> Unit, onError: () -> Unit) {
        api.swapPreview(swap)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                onDone(it)
            }, {
                Log.e(LOG_E, "${it.message}")
                onError()
            })
    }

    fun swap(swap: Swap, onDone: (Swap) -> Unit, onError: () -> Unit) {
        api.swap(swap)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                onDone(it)
            }, {
                Log.e(LOG_E, "${it.message}")
                onError()
            })
    }

    fun sendPreview(send: Send, onDone: (Send) -> Unit, onError: () -> Unit) {
        api.sendPreview(send)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                onDone(it)
            }, {
                Log.e(LOG_E, "${it.message}")
                onError()
            })
    }

    fun send(send: Send, onDone: (Send) -> Unit, onError: () -> Unit) {
        api.send(send)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                onDone(it)
            }, {
                Log.e(LOG_E, "${it.message}")
                onError()
            })
    }

    fun validate(address: String, onResult: (Boolean) -> Unit, onError: () -> Unit) {
        api.validateAddress(address)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                onResult(it)
            }, {
                onError()
            })
    }

    companion object {
        private val api = APIServiceImpl()
    }
}

data class WalletState(
    val resultState: ResultState,
    val amount: String,
    val earn: Double,
    val rate: Double,
    val gas: Denom,
    val lunaPrice: Double,
    val coins: List<Coin>
)