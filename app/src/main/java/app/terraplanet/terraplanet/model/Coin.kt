package app.terraplanet.terraplanet.model

import android.os.Parcelable
import app.terraplanet.terraplanet.network.Denom
import app.terraplanet.terraplanet.util.JSONConverter
import kotlinx.parcelize.Parcelize

@Parcelize
data class Coin(
    val denom: Denom,
    val icon: Int,
    var quantity: Double,
    var amount: Double,
    var isLuna: Boolean = denom == Denom.LUNA
    ) : JSONConverter, Parcelable