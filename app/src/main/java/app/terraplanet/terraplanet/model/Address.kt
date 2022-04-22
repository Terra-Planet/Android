package app.terraplanet.terraplanet.model

import app.terraplanet.terraplanet.util.JSONConverter
import com.google.gson.annotations.SerializedName

data class Address(
    @SerializedName("acc_address") val address: String
) : JSONConverter