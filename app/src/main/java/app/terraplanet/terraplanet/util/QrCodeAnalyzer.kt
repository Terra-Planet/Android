package app.terraplanet.terraplanet.util

import android.graphics.ImageFormat
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.lang.Exception
import java.nio.ByteBuffer

class QrCodeAnalyzer(
    private val onCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val imageFormat = listOf(
        ImageFormat.YUV_420_888,
        ImageFormat.YUV_422_888,
        ImageFormat.YUV_444_888
    )

    override fun analyze(image: ImageProxy) {
        if (image.format in imageFormat) {
            val bytes = image.planes.first().buffer.toByteArray()
            val source = PlanarYUVLuminanceSource(
                bytes,
                image.width,
                image.height,
                0,
                0,
                image.width,
                image.height,
                false
            )

            val binaryBitmapLight = BinaryBitmap(HybridBinarizer(source))
            val binaryBitmapDark = BinaryBitmap(HybridBinarizer(source.invert()))

            try {
                val resultLight = MultiFormatReader().apply {
                    setHints(
                        mapOf(
                            DecodeHintType.POSSIBLE_FORMATS to arrayListOf(
                                BarcodeFormat.QR_CODE
                            )
                        )
                    )
                }.decode(binaryBitmapLight)
                onCodeScanned(resultLight.text)
            } catch (e: Exception) {
                try {
                    val resultDark = MultiFormatReader().apply {
                        setHints(
                            mapOf(
                                DecodeHintType.POSSIBLE_FORMATS to arrayListOf(
                                    BarcodeFormat.QR_CODE
                                )
                            )
                        )
                    }.decode(binaryBitmapDark)
                    onCodeScanned(resultDark.text)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    image.close()
                }
                e.printStackTrace()
            } finally {
                image.close()
            }
        }
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        return ByteArray(remaining()).also { get(it) }
    }
}