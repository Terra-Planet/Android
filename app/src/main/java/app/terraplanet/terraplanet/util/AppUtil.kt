package app.terraplanet.terraplanet.util

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import app.terraplanet.terraplanet.MainActivity
import app.terraplanet.terraplanet.ui.util.resetStack
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*


class AppUtil {

    companion object {
        private const val MIN_ADDRESS_LENGTH = 16
        private const val ADDRESS_THRESHOLD = 8

        fun deleteFolderRecursively(file: File): Boolean {
            return try {
                var res = true
                for (childFile in file.listFiles()!!) {
                    res = if (childFile.isDirectory) {
                        res and deleteFolderRecursively(childFile)
                    } else {
                        res and childFile.delete()
                    }
                }
                res = res and file.delete()
                res
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        fun copyAssetFolder(assetManager: AssetManager, fromAssetPath: String, toPath: String): Boolean {
            return try {
                val files = assetManager.list(fromAssetPath)
                var res = true
                if (files!!.isEmpty()) {
                    //If it's a file, it won't have any assets "inside" it.
                    res = res and copyAsset(
                        assetManager,
                        fromAssetPath,
                        toPath
                    )
                } else {
                    File(toPath).mkdirs()
                    for (file in files) res = res and copyAssetFolder(
                        assetManager,
                        "$fromAssetPath/$file",
                        "$toPath/$file"
                    )
                }
                res
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        private fun copyAsset(assetManager: AssetManager, fromAssetPath: String, toPath: String): Boolean {
            var `in`: InputStream?
            var out: OutputStream?
            return try {
                `in` = assetManager.open(fromAssetPath)
                File(toPath).createNewFile()
                out = FileOutputStream(toPath)
                copyFile(`in`, out)
                `in`.close()
                `in` = null
                out.flush()
                out.close()
                out = null
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        @Throws(IOException::class)
        fun copyFile(`in`: InputStream, out: OutputStream) {
            val buffer = ByteArray(1024)
            var read: Int
            while (`in`.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }
        }

        @Throws(JSONException::class)
        fun JSONObject.toMap(): Map<String, Any> {
            val map = mutableMapOf<String, Any>()
            val keysItr: Iterator<String> = this.keys()
            while (keysItr.hasNext()) {
                val key = keysItr.next()
                var value: Any = this.get(key)
                when (value) {
                    is JSONArray -> value = value.toList()
                    is JSONObject -> value = value.toMap()
                }
                map[key] = value
            }
            return map
        }

        @Throws(JSONException::class)
        fun JSONArray.toList(): List<Any> {
            val list = mutableListOf<Any>()
            for (i in 0 until this.length()) {
                var value: Any = this[i]
                when (value) {
                    is JSONArray -> value = value.toList()
                    is JSONObject -> value = value.toMap()
                }
                list.add(value)
            }
            return list
        }

        fun maskAddress(address: String): String {
            var masked = address
            if (address.length >= MIN_ADDRESS_LENGTH) {
                masked = address.substring(0, ADDRESS_THRESHOLD) +
                        "..." +
                        address.subSequence(address.length - ADDRESS_THRESHOLD, address.length)
            }
            return masked
        }

        fun restart(context: Context) {
            val intent = Intent(context, MainActivity::class.java).apply { flags = resetStack }
            context.startActivity(intent)
            (context as Activity).finishAffinity()
        }
    }
}

fun Double.roundDecimal(digit: Int) = "%.${digit}f".format(this)

interface JSONConverter {
    fun toJSON(): String = Gson().toJson(this)
}

inline fun <reified T: JSONConverter> String.toObject(): T = Gson().fromJson(this, T::class.java)

fun Context.copyToClipboard(clipLabel: String, text: CharSequence, done: () -> Unit) {
    val clipboard = ContextCompat.getSystemService(this, ClipboardManager::class.java)
    clipboard?.setPrimaryClip(ClipData.newPlainText(clipLabel, text))
    done()
}

fun Context.pasteFromClipboard(done: (String) -> Unit) {
    val clipboardManager = ContextCompat.getSystemService(this, ClipboardManager::class.java)
    clipboardManager?.let { clipboard ->
        clipboard.primaryClip?.let {
            done(it.getItemAt(0).text.toString())
        }
    }
}

fun isNumeric(toCheck: String): Boolean {
    val regex = """^[0-9]*((\.)[0-9]+)?$""".toRegex()
    return if (toCheck.isEmpty()) false else regex.matches(toCheck)
}

fun String.parseToDouble(): Double {
    return this.toDoubleOrNull() ?: this.toInt().toDouble()
}

fun Context.bitmapDrawable(drawable: Int): ImageBitmap? {
    return ContextCompat.getDrawable(this, drawable)?.toBitmap()?.asImageBitmap()
}

data class ValidInput(val input: String, val valid: Boolean)
