package app.terraplanet.terraplanet.util

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

class Biometrics {
    companion object {
        private var cancellationSignal: CancellationSignal? = null

        fun getCancellationSignal(onCancel: () -> Unit): CancellationSignal {
            cancellationSignal = CancellationSignal()
            cancellationSignal?.setOnCancelListener { onCancel() }
            return cancellationSignal as CancellationSignal
        }

        @RequiresApi(Build.VERSION_CODES.M)
        fun checkBiometricSupport(context: Context): Boolean {
            val keyGuardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

            if (!keyGuardManager.isDeviceSecure) { return true }

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.USE_BIOMETRIC
                ) != PackageManager.PERMISSION_GRANTED
            ) { return false }

            return context.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
        }
    }
}