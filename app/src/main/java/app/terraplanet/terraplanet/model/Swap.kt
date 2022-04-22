package app.terraplanet.terraplanet.model

import app.terraplanet.terraplanet.network.Denom
import app.terraplanet.terraplanet.util.JSONConverter

data class Swap(val from: Coin, val to: Coin, val amount: Double, var fee: Double, val pay: Denom) : JSONConverter