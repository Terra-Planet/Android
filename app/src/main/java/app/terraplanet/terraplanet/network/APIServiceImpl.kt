package app.terraplanet.terraplanet.network

import android.content.Context
import app.terraplanet.terraplanet.R
import app.terraplanet.terraplanet.model.*
import app.terraplanet.terraplanet.secure.Storage
import app.terraplanet.terraplanet.util.AppUtil.Companion.toList
import app.terraplanet.terraplanet.util.parseToDouble
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject

class APIServiceImpl {

    fun getStatus(): Single<ServerStatus> {
        return Network.getClient(auth).status().subscribeOn(Schedulers.io())
    }

    fun create(context: Context): Single<Wallet> {
        val storage = Storage(context)
        val network = storage.getStringSecret(NETWORK)
        return Network.getClient(auth)
            .create(network ?: Net.TEST.id)
            .subscribeOn(Schedulers.io())
            .map {
                wallet = it
                storage.saveSecret(WALLET, it.toJSON())
                wallet!!
            }
    }

    fun restore(mnemonic: String, context: Context): Single<Wallet> {
        val storage = Storage(context)
        val decryptedWallet = storage.getStringSecret(WALLET)

        return if (decryptedWallet != null) {
            Single.create {
                wallet = gson.fromJson(decryptedWallet, Wallet::class.java)
                wallet
            }
        } else {
            val map = mutableMapOf<String, String>()
            map[MNEMONIC] = mnemonic
            val params = RequestBody.create(MediaType.parse(CONTENT_TYPE_JSON), gson.toJson(map))
            Network.getClient(auth)
                .restore(params)
                .subscribeOn(Schedulers.io())
                .map { data ->
                    wallet = Wallet(data.address, mnemonic)
                    storage.saveSecret(WALLET, wallet!!.toJSON())
                    wallet!!
                }
        }
    }

    fun getBalance(): Single<List<Coin>> {
        val client = Network.getClient(auth)
        return client
            .lunaRate()
            .flatMap {
                lunaPrice = it.amount.parseToDouble()
                client.balance(wallet!!.address, network.id)
            }
            .map { data ->
                val coins = JSONArray(data.native[0] as String)
                val supportedCoins = mutableListOf<Coin>()
                coins.toList().forEach {
                    val coinData = JSONObject("$it")
                    if (supported.contains(coinData[DENOM] as String)) {
                        val isLuna = coinData[DENOM] == Denom.LUNA.id
                        val quantity = ("${coinData[AMOUNT]}".parseToDouble() / 1000000)
                        val coin = Coin(
                            if (isLuna) Denom.LUNA else Denom.UST,
                            if (isLuna) R.drawable.coin_luna else  R.drawable.coin_ust,
                            quantity,
                            if (isLuna) quantity * lunaPrice else quantity
                        )
                        supportedCoins.add(coin)
                    }
                }
                supportedCoins.toList()
            }.subscribeOn(Schedulers.io())
    }

    fun getEarnBalance(): Single<EarnBalance> {
        val map = mutableMapOf<String, String>()
        map[MNEMONIC] = wallet!!.mnemonic
        map[NETWORK] = network.id
        val params = RequestBody.create(MediaType.parse(CONTENT_TYPE_JSON), gson.toJson(map))
        return Network.getClient(auth)
            .earnBalance(params)
            .subscribeOn(Schedulers.io())
    }

    fun getRate(): Single<Market> {
        val map = mutableMapOf<String, String>()
        map[MNEMONIC] = wallet!!.mnemonic
        map[NETWORK] = network.id
        val params = RequestBody.create(MediaType.parse(CONTENT_TYPE_JSON), gson.toJson(map))
        return Network.getClient(auth)
            .rate(params)
            .subscribeOn(Schedulers.io())
    }

    fun anchorDeposit(amount: Double): Single<Any> {
        val map = mutableMapOf<String, String>()
        map[TOKEN] = Denom.UST.id
        map[AMOUNT] = "$amount"
        map[MNEMONIC] = wallet!!.mnemonic
        map[NETWORK] = network.id
        val params = RequestBody.create(MediaType.parse(CONTENT_TYPE_JSON), gson.toJson(map))
        return Network.getClient(auth)
            .anchorDeposit(params)
            .subscribeOn(Schedulers.io())
    }

    fun anchorWithdraw(amount: Double): Single<Any> {
        val map = mutableMapOf<String, String>()
        map[TOKEN] = Denom.UST.id
        map[AMOUNT] = "$amount"
        map[MNEMONIC] = wallet!!.mnemonic
        map[NETWORK] = network.id
        val params = RequestBody.create(MediaType.parse(CONTENT_TYPE_JSON), gson.toJson(map))
        return Network.getClient(auth)
            .anchorWithdraw(params)
            .subscribeOn(Schedulers.io())
    }

    fun swapPreview(swap: Swap): Single<Swap> {
        val map = mutableMapOf<String, String>()
        map[FEE_TOKEN] = gas.id
        map[SRC] = swap.from.denom.id
        map[DST] = swap.to.denom.id
        map[AMOUNT] = "${swap.amount}"
        map[ADDRESS] = wallet!!.address
        map[MNEMONIC] = wallet!!.mnemonic
        map[NETWORK] = network.id

        val params = RequestBody.create(MediaType.parse(CONTENT_TYPE_JSON), gson.toJson(map))
        val client = Network.getClient(auth)
        return client
            .lunaRate()
            .flatMap {
                lunaPrice = it.amount.parseToDouble()
                client.swapPreview(params)
            }
            .map {
                val json = JSONObject(it)
                val authInfo = json[AUTH_INFO] as JSONObject
                val feeInfo = JSONObject(authInfo[FEE] as String)
                val fee = (feeInfo[AMOUNT] as JSONArray)[0] as JSONObject
                val amount = fee[AMOUNT] as String
                swap.fee = (amount.parseToDouble()) / 1000000
                swap
            }
            .subscribeOn(Schedulers.io())
    }

    fun swap(swap: Swap): Single<Swap> {
        val map = mutableMapOf<String, String>()
        map[FEE_TOKEN] = gas.id
        map[SRC] = swap.from.denom.id
        map[DST] = swap.to.denom.id
        map[AMOUNT] = "${swap.amount}"
        map[MNEMONIC] = wallet!!.mnemonic
        map[NETWORK] = network.id

        val params = RequestBody.create(MediaType.parse(CONTENT_TYPE_JSON), gson.toJson(map))
        val client = Network.getClient(auth)
        return client
            .lunaRate()
            .flatMap {
                lunaPrice = it.amount.parseToDouble()
                client.swap(params)
            }
            .map { swap }
            .subscribeOn(Schedulers.io())
    }

    fun sendPreview(send: Send): Single<Send> {
        val map = mutableMapOf<String, String>()
        map[FEE_TOKEN] = gas.id
        map[TOKEN] = send.coin.denom.id
        map[AMOUNT] = "${send.amount}"
        map[DST_ADDR] = send.address
        map[MNEMONIC] = wallet!!.mnemonic
        map[NETWORK] = network.id

        val params = RequestBody.create(MediaType.parse(CONTENT_TYPE_JSON), gson.toJson(map))
        val client = Network.getClient(auth)
        return client
            .lunaRate()
            .flatMap {
                lunaPrice = it.amount.parseToDouble()
                client.sendPreview(params)
            }
            .map {
                val json = JSONObject(it)
                val authInfo = json[AUTH_INFO] as JSONObject
                val feeInfo = JSONObject(authInfo[FEE] as String)
                val fee = (feeInfo[AMOUNT] as JSONArray)[0] as JSONObject
                val amount = fee[AMOUNT] as String
                send.fee = (amount.parseToDouble()) / 1000000
                send
            }
            .subscribeOn(Schedulers.io())
    }

    fun send(send: Send): Single<Send> {
        val map = mutableMapOf<String, String>()
        map[FEE_TOKEN] = gas.id
        map[TOKEN] = send.coin.denom.id
        map[AMOUNT] = "${send.amount}"
        map[DST_ADDR] = send.address
        map[MNEMONIC] = wallet!!.mnemonic
        map[NETWORK] = network.id

        send.memo?.let { memo ->
            map[MEMO] = memo
        }

        val params = RequestBody.create(MediaType.parse(CONTENT_TYPE_JSON), gson.toJson(map))
        val client = Network.getClient(auth)
        return client
            .lunaRate()
            .flatMap {
                lunaPrice = it.amount.parseToDouble()
                client.send(params)
            }
            .map { send }
            .subscribeOn(Schedulers.io())
    }

    fun validateAddress(address: String): Single<Boolean> {
        return Network
            .getClient(auth)
            .validate(address)
            .map {
                val result = JSONObject(it)
                result[VALID] as Boolean
            }
            .subscribeOn(Schedulers.io())
    }

    fun getWallet(context: Context): Wallet? {
        val storage = Storage(context)
        val data = storage.getStringSecret(WALLET)
        data?.let {
            wallet = gson.fromJson(data, Wallet::class.java)
            return wallet
        }
        return null
    }

    fun nukeWallet(context: Context) {
        val storage = Storage(context)
        storage.clear(WALLET)
    }

    fun putNetwork(context: Context, net: String): Net {
        val storage = Storage(context)
        val value = if (net == Net.TEST.label) Net.TEST.id else Net.MAIN.id
        network = if (net == Net.TEST.label) Net.TEST else Net.MAIN
        storage.save(CURRENT_NETWORK, value)
        return network
    }

    fun getNetwork(context: Context): Net {
        val storage = Storage(context)
        val value = storage.getString(CURRENT_NETWORK)
        network = if (value == Net.TEST.id) Net.TEST else Net.MAIN
        return network
    }

    fun putPayGas(context: Context, payGas: String): Denom {
        val storage = Storage(context)
        gas = if (payGas == Denom.UST.label) Denom.UST else Denom.LUNA
        storage.save(PAY_GAS, if (payGas == Denom.UST.label) Denom.UST.id else Denom.LUNA.id)
        return gas
    }

    fun getPayGas(context: Context): Denom {
        val storage = Storage(context)
        val value = storage.getString(PAY_GAS)
        gas = if (value == Denom.UST.id) Denom.UST else Denom.LUNA
        return gas
    }

    fun getLunaPrice(): Double = lunaPrice

    fun getLunaRate(rate: (Double) -> Unit) {
        Network.getClient(auth)
            .lunaRate()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                lunaPrice = it.amount.parseToDouble()
                rate(lunaPrice)
            }, {
                rate(lunaPrice)
            })
    }

    fun initSettings(context: Context) {
        val storage = Storage(context)
        storage.save(CURRENT_NETWORK, network.id)
        storage.save(PAY_GAS, gas.id)
    }

    companion object {
        const val LOG_E = "[Terra-Planet]"

        // Params
        private const val CONTENT_TYPE_JSON = "application/json; charset=utf-8"
        private const val MNEMONIC = "mnemonic"
        private const val TOKEN = "token"
        private const val FEE_TOKEN = "fee_token"
        private const val SRC = "src"
        private const val DST = "dst"
        private const val DST_ADDR = "dst_addr"
        private const val ADDRESS = "address"
        private const val MEMO = "memo"
        private const val VALID = "valid"

        // Keys
        private const val WALLET = "wallet"
        private const val NETWORK = "network"
        private const val DENOM = "denom"
        private const val AMOUNT = "amount"
        private const val PAY_GAS = "pay_gas"
        private const val CURRENT_NETWORK = "current_network"
        private const val AUTH_INFO = "auth_info"
        private const val FEE = "fee"

        // Util
        private val gson = Gson()

        // Wallet
        private var wallet: Wallet? = null
        private var lunaPrice: Double = 1.0
        private var network = Net.TEST
        private var gas = Denom.UST

        val auth = Storage.getAuth()
        val supported = listOf(Denom.UST.id, Denom.LUNA.id)
    }
}

enum class Net(val id: String, val label: String) {
    TEST("test", "testnet"),
    MAIN("main", "mainnet")
}

enum class Denom(val id: String, val label: String) {
    UST("uusd", "UST"),
    LUNA("uluna", "LUNA")
}