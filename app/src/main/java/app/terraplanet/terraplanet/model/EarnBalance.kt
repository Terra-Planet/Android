package app.terraplanet.terraplanet.model

import app.terraplanet.terraplanet.util.JSONConverter
import com.google.gson.annotations.SerializedName

data class EarnBalance(@SerializedName("total_deposit_balance_in_ust") val amount: String) : JSONConverter