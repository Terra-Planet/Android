package app.terraplanet.terraplanet.model

import app.terraplanet.terraplanet.network.Denom
import app.terraplanet.terraplanet.util.JSONConverter

data class Send(
    val gas: Denom,
    val token: Denom,
    val amount: String,
    var fee: Double,
    val address: String,
    val memo: String?
) : JSONConverter