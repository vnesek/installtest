package net.instantcom.installtest

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageInstaller
import android.os.Bundle
import android.view.View
import android.widget.Button
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.install_activity.*
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.File


class InstallActivity : Activity() {

    private lateinit var disposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.install_activity)
    }

    override fun onResume() {
        super.onResume()
        disposable = (application as InstallApp).log
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    info.text = it
                }
        Timber.d("Ready")
        intent?.dumpExtras()
    }

    private fun Intent.dumpExtras() {
        val extras = this.extras
        if (extras != null) {
            for (key in extras.keySet()) {
                val value = extras.get(key)
                Timber.d("$key=$value")
                if (value is Intent) {
                    value.dumpExtras()
                }
            }
        }
    }

    override fun onPause() {
        disposable.dispose()
        super.onPause()
    }

    private fun downloadApk(name: String): File {
        val file = cacheDir.resolve(name)
        if (!file.exists()) {
            Timber.i("Downloading $name")
            val url = "http://afms.instantcom.net/download/$name"
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            Timber.i("$name HTTP response ${response.code()}")
            if (!response.isSuccessful) throw RuntimeException("Failed to download APK ${response.message()}")
            file.outputStream().use { response.body()!!.byteStream().copyTo(it) }
            Timber.i("Downloaded ${file.absolutePath} size ${file.length()}")
        } else {
            Timber.i("Already downloaded $name")
        }
        return file
    }

    private fun createIntentSender(sessionId: Int): IntentSender {
        val intent = Intent(this, this::class.java)
        intent.putExtra("session", sessionId)

        val pendingIntent = PendingIntent.getActivity(this, sessionId, intent, 0)
        return pendingIntent.intentSender
    }

    private fun installApk(packageName: String, file: File) {
        Timber.i("Installing ${file.name}")
        val packageInstaller = this.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        params.setAppPackageName(packageName)

        try {
            val sessionId = packageInstaller.createSession(params)
            val session = packageInstaller.openSession(sessionId)
            file.inputStream().use { input ->
                session.openWrite("sessionName", 0, -1).use { output ->
                    input.copyTo(output, 4096)
                    session.fsync(output)
                }

                session.commit(createIntentSender(sessionId))
            }
            Timber.i("Session ${file.name} committed")
        } catch (t: Throwable) {
            Timber.e(t, "Install ${file.name} failed")
        }
    }

    fun onButtonClick(view: View) {
        Observable.fromArray((view as Button).text.toString())
                .observeOn(Schedulers.io())
                .map(this::downloadApk)
                .map { installApk("net.instantcom.fms.android.mybigapk", it) }
                .subscribe()
    }
}
