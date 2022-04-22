package app.terraplanet.terraplanet.model

import app.terraplanet.terraplanet.util.JSONConverter
import com.google.gson.annotations.SerializedName

data class Wallet(
    @SerializedName("acc_address") val address: String,
    @SerializedName("mnemonic") val mnemonic: String
) : JSONConverter