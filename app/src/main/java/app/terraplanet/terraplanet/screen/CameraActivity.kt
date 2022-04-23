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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import app.terraplanet.terraplanet.ui.theme.TerraPlanetTheme
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
                        QrCodeAnalyzer { result -> onResult(result) }
                    )

                    try {
                        cameraProvider.get().bindToLifecycle(lifeCycleOwner, selector, preview, imageAnalysis)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    previewView
                }, modifier = Modifier.fillMaxSize())
            }
        }
    }
}