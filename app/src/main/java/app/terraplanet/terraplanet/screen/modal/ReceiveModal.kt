package app.terraplanet.terraplanet.screen.modal

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.terraplanet.terraplanet.ui.theme.*
import app.terraplanet.terraplanet.ui.util.Container
import app.terraplanet.terraplanet.ui.util.ModalTransitionDialogHelper
import app.terraplanet.terraplanet.ui.util.VSpacer
import app.terraplanet.terraplanet.util.copyToClipboard
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun ReceiveQrScreen(modal: ModalTransitionDialogHelper, address: String) {
    val context = LocalContext.current

    BackHandler { modal.triggerAnimatedClose() }

    Surface(
        color = bgColor(),
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        shape = RoundedCornerShape(topStartPercent = 4, topEndPercent = 4)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Container(modifier = Modifier.align(Alignment.End)) {
                IconButton(onClick = modal::triggerAnimatedClose) {
                    Icon(
                        painter = rememberVectorPainter(image = Icons.Default.Close),
                        contentDescription = null,
                        tint = colorAware()
                    )
                }
            }
            generateQRBitmap(address, isDark())?.let {
                Image(
                    bitmap = it,
                    contentDescription = null,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxWidth(),
                    contentScale = ContentScale.FillBounds
                )
            }
            VSpacer(20)
            Container(modifier = Modifier.align(Alignment.Start)) {
                Text("Address:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            VSpacer(8)
            Container(modifier = Modifier.align(Alignment.Start)) {
                Text(address, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            VSpacer(20)
            Button(
                onClick = { context.copyToClipboard("Wallet Address", address) {
                    Toast.makeText(context, "Address copied", Toast.LENGTH_SHORT).show()
                }},
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                )
            ) {
                Text(
                    text = "Copy Address",
                    color = MainBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }
    }
}

fun generateQRBitmap(content: String?, isDark: Boolean): ImageBitmap? {
    var bitmap: Bitmap? = null
    val width = 320
    val height = 320
    val writer = QRCodeWriter()
    try {
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height)
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y])
                            if (isDark) BgLight.toArgb() else BgDark.toArgb()
                            else (if (isDark) BgDark.toArgb() else BgLight.toArgb()))
            }
        }
    } catch (e: WriterException) {
        e.printStackTrace()
    }
    return bitmap?.asImageBitmap()
}