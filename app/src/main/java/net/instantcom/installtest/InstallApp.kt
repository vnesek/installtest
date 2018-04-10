package net.instantcom.installtest

import android.app.Application
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import timber.log.Timber.DebugTree


class InstallApp : Application() {

    val log = BehaviorSubject.create<String>()

    override fun onCreate() {
        super.onCreate()
        Timber.plant(DebugTree())
        Timber.plant(object : Timber.Tree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                log.onNext(message)
            }
        })
    }

}