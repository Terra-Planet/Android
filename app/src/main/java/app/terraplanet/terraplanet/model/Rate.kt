package app.terraplanet.terraplanet.model

import app.terraplanet.terraplanet.util.JSONConverter
import com.google.gson.annotations.SerializedName

data class Rate(
    @SerializedName("token") val token: String,
    @SerializedName("amount") val amount: String,
) : JSONConverter