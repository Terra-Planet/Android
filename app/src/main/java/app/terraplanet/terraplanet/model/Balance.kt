package app.terraplanet.terraplanet.model

import app.terraplanet.terraplanet.util.JSONConverter
import com.google.gson.annotations.SerializedName

data class Balance(
    @SerializedName("native") val native: List<Any>
) : JSONConverter