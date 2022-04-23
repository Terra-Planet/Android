package app.terraplanet.terraplanet.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import app.terraplanet.terraplanet.network.APIServiceImpl
import app.terraplanet.terraplanet.network.Denom
import app.terraplanet.terraplanet.network.Net
import app.terraplanet.terraplanet.screen.InitActivity
import app.terraplanet.terraplanet.ui.util.resetStack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel(val app: Application): AndroidViewModel(app) {
    private val api = APIServiceImpl()
    private val _settingsState = MutableStateFlow(
        SettingsState(state = State.LOADING, Net.TEST, Denom.UST)
    )

    val settingsState: StateFlow<SettingsState> get() = _settingsState

    private val _showSeedDialog = MutableStateFlow(false)
    val showSeedDialog: StateFlow<Boolean> = _showSeedDialog.asStateFlow()

    private val _showLogoutDialog = MutableStateFlow(false)
    val showLogoutDialog: StateFlow<Boolean> = _showLogoutDialog.asStateFlow()

    init { fetchSettings() }

    private fun fetchSettings() {
        _settingsState.update { SettingsState(state = State.LOADING, Net.TEST, Denom.UST).copy() }
        val network = api.getNetwork(app.applicationContext)
        val payGas = api.getPayGas(app.applicationContext)
        _settingsState.update {
            SettingsState(State.SUCCESS, network, payGas).copy()
        }
    }

    fun saveNetwork(net: String) {
        val network = api.putNetwork(app.applicationContext, net)
        val payGas = api.getPayGas(app.applicationContext)
        _settingsState.update {
            SettingsState(State.SUCCESS, network, payGas).copy()
        }
    }

    fun savePayGas(gas: String) {
        val payGas = api.putPayGas(app.applicationContext, gas)
        val network = api.getNetwork(app.applicationContext)
        _settingsState.update {
            SettingsState(State.SUCCESS, network, payGas).copy()
        }
    }

    fun openSeedDialog() {
        _showSeedDialog.value = true
    }

    fun onSeedDialogConfirm() {
        _showSeedDialog.value = false
    }

    fun openLogoutDialog() {
        _showLogoutDialog.value = true
    }

    fun dismissLogoutDialog() {
        _showLogoutDialog.value = false
    }

    fun resetInitial(context: Context) {
        val intent = Intent(context, InitActivity::class.java).apply { flags = resetStack }
        context.startActivity(intent)
    }
}

data class SettingsState(val state: State, val network: Net, val gas: Denom)