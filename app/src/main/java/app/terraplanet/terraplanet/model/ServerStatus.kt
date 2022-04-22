package app.terraplanet.terraplanet.model

import app.terraplanet.terraplanet.util.JSONConverter
import com.google.gson.annotations.SerializedName

data class ServerStatus(
    @SerializedName("status") val status: String
) : JSONConverter