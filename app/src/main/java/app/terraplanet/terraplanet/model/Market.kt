package app.terraplanet.terraplanet.model

import app.terraplanet.terraplanet.util.JSONConverter
import com.google.gson.annotations.SerializedName

data class Market(@SerializedName("APY") val rate: Double) : JSONConverter