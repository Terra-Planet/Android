package app.terraplanet.terraplanet.secure

import android.content.Context
import android.content.SharedPreferences
import app.terraplanet.terraplanet.util.JSONConverter
import com.yakivmospan.scytale.Crypto
import com.yakivmospan.scytale.Options
import com.yakivmospan.scytale.Store
import java.lang.StringBuilder
import java.math.BigInteger
import java.security.SecureRandom
import javax.crypto.SecretKey

class Storage(val context: Context) {
    val sharedPref: SharedPreferences = context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)
    val store = Store(context)
    val crypto = Crypto(Options.TRANSFORMATION_SYMMETRIC)

    fun saveSecret(key: String, value: String) {
        val secretKey: SecretKey = if (!store.hasKey(key)) {
            store.generateSymmetricKey(key, null)
        } else {
            store.getSymmetricKey(key, null)
        }
        val encrypt = crypto.encrypt(value, secretKey)
        sharedPref.edit().apply {
            putString(key, encrypt)
            apply()
        }
    }

    fun save(key: String, value: String) {
        sharedPref.edit().apply {
            putString(key, value)
            apply()
        }
    }

    fun save(key: String, value: Int) {
        sharedPref.edit().apply {
            putInt(key, value)
            apply()
        }
    }

    fun saveSecret(key: String, value: Boolean) {
        sharedPref.edit().apply {
            putBoolean(key, value)
            apply()
        }
    }

    fun save(key: String, value: Float) {
        sharedPref.edit().apply {
            putFloat(key, value)
            apply()
        }
    }

    fun save(key: String, value: Long) {
        sharedPref.edit().apply {
            putLong(key, value)
            apply()
        }
    }

    fun getStringSecret(key: String): String? {
        val secretKey = store.getSymmetricKey(key, null)
        val encrypted = sharedPref.getString(key, null)
        return encrypted?.let { crypto.decrypt(it, secretKey) }
    }

    fun getString(key: String): String = sharedPref.getString(key, null) ?: ""
    fun getInt(key: String): Int = sharedPref.getInt(key, 0)
    fun getBoolean(key: String): Boolean = sharedPref.getBoolean(key, false)
    fun getFloat(key: String): Float = sharedPref.getFloat(key, 0F)
    fun getLong(key: String): Long = sharedPref.getLong(key, 0L)

    fun clear(key: String) {
        sharedPref.edit().clear().apply()
        store.deleteKey(key)
    }

    companion object {
        private const val STORE_NAME = "terraplanet"
        private const val MAX_SECURE = 16
        private const val REDIX = 16

        private val auth = Auth(getSecureAuth(), getSecureAuth())

        private fun getSecureAuth(): String {
            val secure = SecureRandom()
            val random = BigInteger(64, secure).toString(REDIX)
            val length = random.length
            val builder = StringBuilder()
            builder.append(random)
            if (length < MAX_SECURE) {
                repeat(MAX_SECURE - length) {
                    builder.append("0")
                }
            }
            return builder.toString()
        }

        fun getAuth(): Auth = auth
    }
}

data class Auth(val username: String, val password: String) : JSONConverter
