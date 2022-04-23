package app.terraplanet.terraplanet.screen

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import app.terraplanet.terraplanet.ui.theme.TerraPlanetTheme
import app.terraplanet.terraplanet.ui.util.Center
import app.terraplanet.terraplanet.ui.util.Expandable
import app.terraplanet.terraplanet.ui.util.VSpacer
import app.terraplanet.terraplanet.util.QrCodeAnalyzer
import java.lang.Exception

class CameraActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CameraScreen { getResult(it) }
        }
    }

    fun getResult(result: String) {
        val intent = Intent()
        intent.putExtra(RESULT, result)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    companion object {
        const val RESULT = "result"
    }
}

@Composable
fun CameraScreen(onResult: (String) -> Unit) {
    TerraPlanetTheme {

        val context = LocalContext.current
        val lifeCycleOwner = LocalLifecycleOwner.current

        val cameraProvider = remember {
            ProcessCameraProvider.getInstance(context)
        }
        var hasCameraPermission by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            )
        }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { granted -> hasCameraPermission = granted }
        )

        var size by remember { mutableStateOf(IntSize.Zero) }

        LaunchedEffect(key1 = true) {
            launcher.launch(Manifest.permission.CAMERA)
        }

        Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {

            if (hasCameraPermission) {
                AndroidView(factory = { context ->
                    val previewView = PreviewView(context)
                    val preview = Preview.Builder().build()
                    val selector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(
                        ContextCompat.getMainExecutor(context),
                        QrCodeAnalyzer{ result -> onResult(result) }
                    )

                    try {
                        cameraProvider.get().bindToLifecycle(lifeCycleOwner, selector, preview, imageAnalysis)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    previewView
                }, modifier = Modifier.fillMaxSize())

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { size = it },
                    color = Color.Black.copy(alpha = 0.25f)
                ) {
                    Surface(modifier =
                    Modifier
                        .fillMaxSize()
                        .drawWithCache {
                            onDrawWithContent {
                                drawRect(SolidColor(Color.Black), blendMode = BlendMode.SrcOut)
                            }
                        }
                    ) {}
                    Center {
                        Column {
                            VSpacer(20)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = size.width.dp * 0.035f)
                                    .aspectRatio(1f)
                                    .drawWithCache {
                                        onDrawWithContent {
                                            drawRoundRect(
                                                SolidColor(Color.White),
                                                blendMode = BlendMode.DstOut,
                                                cornerRadius = CornerRadius(30.dp.toPx(), 30.dp.toPx()),
                                            )
                                        }
                                    },
                            ) {}
                            VSpacer(20)
                            Text(
                                text = "Scan a Terra address QR Code",
                                textAlign = TextAlign.Center,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.Start
            ) {
                Surface(modifier = Modifier
                    .width(60.dp)
                    .height(60.dp)
                    .padding(top = 20.dp, start = 20.dp),
                    color = Color.Black,
                    shape = RoundedCornerShape(50),
                ) {
                    IconButton(onClick = { onResult("") }, modifier = Modifier.align(Alignment.Start)) {
                        Icon(
                            painter = rememberVectorPainter(image = Icons.Default.ArrowBack),
                            tint = Color.White,
                            contentDescription = null,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
                Expandable()
            }
        }
    }
}
