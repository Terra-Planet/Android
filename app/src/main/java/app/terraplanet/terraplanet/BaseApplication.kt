package app.terraplanet.terraplanet

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import app.terraplanet.terraplanet.network.APIServiceImpl
import app.terraplanet.terraplanet.network.APIServiceImpl.Companion.LOG_E
import app.terraplanet.terraplanet.secure.Auth
import app.terraplanet.terraplanet.secure.Storage
import app.terraplanet.terraplanet.util.AppUtil
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.exceptions.UndeliverableException
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit

class BaseApplication : Application() {
    private external fun startNodeWithArguments(arguments: Array<String>): Int
    val uiThread = Handler(Looper.getMainLooper())
    var ioThread: Thread? = null

    val api = APIServiceImpl()

    override fun onCreate() {
        super.onCreate()
        rxErrorHandler()
    }

    private fun rxErrorHandler() {
        RxJavaPlugins.setErrorHandler { e ->
            if (e is UndeliverableException) {
                Log.e(LOG_E, "${e.message}")
            } else {
                Thread.currentThread().also { thread ->
                    thread.uncaughtExceptionHandler?.uncaughtException(thread, e)
                }
            }
        }
    }

    fun startServer(auth: Auth, status: (Int) -> Unit, result: (Boolean) -> Unit) {
        status(R.string.server_status_starting)
        ioThread = Thread {
            val nodeDir = applicationContext.filesDir.absolutePath + "/nodejs-project"
            val nodeDirReference = File(nodeDir)
            if (nodeDirReference.exists()) {
                AppUtil.deleteFolderRecursively(File(nodeDir))
            }
            AppUtil.copyAssetFolder(applicationContext.assets, "nodejs-project", nodeDir)
            uiThread.post { status(R.string.server_status_connecting) }
            try {
                startNodeWithArguments(
                    arrayOf("node", "$nodeDir/bin/www", auth.username, auth.password)
                )
            } catch (e: Exception) {
                ioThread?.interrupt()
                startServer(auth, status, result)
            }
        }
        ioThread!!.start()

        var ping = false
        var disposable: Disposable? = null

        disposable = api.getStatus().toObservable()
            .map { ping = it.status == OK }
            .repeatUntil { ping }
            .retryWhen { Observable.interval(100, TimeUnit.MILLISECONDS) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                disposable?.dispose()
                status(R.string.server_status_connected)
                result(ping)
            }, {
                disposable?.dispose()
                status(R.string.server_status_error)
                result(ping)
                Log.e(LOG_E, "${it.message}")
            })
    }

    fun checkServerStatus(status: (Int) -> Unit, result: (Boolean) -> Unit) {
        val auth = Storage.getAuth()
        var value: Boolean
        status(R.string.server_status)
        api.getStatus()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ data ->
                value = data.status == OK
                if (value) status(R.string.server_status_running)
                result(value)
            }, {
                value = false
                ioThread?.isAlive?.let { isAlive ->
                    if (isAlive) ioThread!!.interrupt()
                }
                startServer(auth, status, result)
            })
    }

    companion object {
        const val OK = "ok"
        const val STATUS = "status"

        init {
            System.loadLibrary("native-lib")
            System.loadLibrary("node")
        }
    }
}