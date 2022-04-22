package app.terraplanet.terraplanet.screen.tab

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import app.terraplanet.terraplanet.network.APIServiceImpl

@Composable
fun TransactionsTab() {
    val api = APIServiceImpl()
    val context = LocalContext.current
    val address = api.getWallet(context)?.address ?: ""

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        TransactionWebView(url = "https://finder.terra.money/${api.getNetwork(context).id}net/address/$address")
    }
}

@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
fun TransactionWebView(url: String) {
    val context = LocalContext.current

    AndroidView(
        factory = {
            WebView(context).apply {
                webViewClient = WebViewClient()
                loadUrl(url)
                settings.javaScriptEnabled = true
                addJavascriptInterface(WebAppInterface(context), "Android")
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

class WebAppInterface(private val context: Context)
