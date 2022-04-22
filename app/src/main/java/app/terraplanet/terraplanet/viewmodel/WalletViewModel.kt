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
import app.terraplanet.terraplanet.network.Net
import app.terraplanet.terraplanet.util.roundDecimal
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.*

class WalletViewModel(app: Application): AndroidViewModel(app) {
    private val api = APIServiceImpl()
    private val _walletState = MutableStateFlow(
        WalletState(state = State.LOADING, "0.00", 0.0, 0.0, 0.0, listOf())
    )
    private lateinit var disposable: Disposable
    private var network = MutableStateFlow(api.getNetwork(app.applicationContext))

    var fee = api.getPayGas(app.applicationContext)

    val walletState: StateFlow<WalletState> get() = _walletState

    init {
        network.onEach {
                fetchWallet()
            }.launchIn(viewModelScope)
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

        _walletState.update {
            WalletState(State.LOADING, amount, earn, rate, lunaPrice, coins).copy()
        }

        disposable = Observable.fromSingle<Any> {
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
            }.observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _walletState.update {WalletState(State.SUCCESS, amount, earn, rate, lunaPrice, coins).copy() }
                }, {
                    _walletState.update {WalletState(State.FAILED, amount, earn, rate, lunaPrice, coins).copy() }
                })
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({}, {})
    }

    fun anchorDeposit(amount: Double, onDone: () -> Unit, onError: () -> Unit) {
        api.anchorDeposit(amount)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onDone() }, {
                println("ERROR: ${it.message}")
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
}

data class WalletState(
    val state: State,
    val amount: String,
    val earn: Double,
    val rate: Double,
    val lunaPrice: Double,
    val coins: List<Coin>
)