package app.terraplanet.terraplanet.network

import android.annotation.SuppressLint
import android.util.Base64
import app.terraplanet.terraplanet.secure.Auth
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class Network {
    companion object {
        private const val BASE_URL = "http://localhost:4938/"
        private val sslCert = certificate()

        private const val SSL = "SSL"
        private const val AUTH_HEADER = "Authorization"
        private const val BASIC_AUTH = "Basic"

        private fun client(auth: Auth): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .sslSocketFactory(sslCert.sslContext.socketFactory, sslCert.x509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .addInterceptor(Interceptor { chain ->
                val builder = chain.request().newBuilder()
                builder.addHeader(
                    AUTH_HEADER,
                    "$BASIC_AUTH ${genAuthKey(auth)}")
                return@Interceptor chain.proceed(builder.build())
            })
            .build()

        fun getClient(auth: Auth): APIService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client(auth))
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()
                .create(APIService::class.java)
        }

        private fun genAuthKey(auth: Auth): String {
            val key = "${auth.username}:${auth.password}"
            val encodedBytes = Base64.encode(key.toByteArray(), Base64.NO_WRAP)
            return String(encodedBytes, Charsets.UTF_8)
        }

        private fun certificate(): SSLCert {
            val x509TrustManager = @SuppressLint("CustomX509TrustManager") object : X509TrustManager {
                @SuppressLint("TrustAllX509TrustManager")
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                @SuppressLint("TrustAllX509TrustManager")
                override fun checkServerTrusted(chain: Array<X509Certificate?>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            }
            val trustAllCerts: Array<TrustManager> = arrayOf(x509TrustManager)
            val sslContext: SSLContext = SSLContext.getInstance(SSL)
            sslContext.init(null, trustAllCerts, SecureRandom())
            return SSLCert(sslContext, x509TrustManager)
        }
    }
}

data class SSLCert(val sslContext: SSLContext, val x509TrustManager: X509TrustManager)
