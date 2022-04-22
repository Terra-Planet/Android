package app.terraplanet.terraplanet.model

import app.terraplanet.terraplanet.network.Denom
import app.terraplanet.terraplanet.util.JSONConverter

data class Send(
    var gas: Denom,
    val gasCoin: Coin?,
    val coin: Coin,
    val amount: Double,
    var fee: Double,
    val address: String,
    val memo: String?
) : JSONConverter