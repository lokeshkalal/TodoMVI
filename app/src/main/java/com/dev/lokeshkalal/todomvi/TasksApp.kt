package com.dev.lokeshkalal.todomvi

import android.app.Application
import com.dev.lokeshkalal.todomvi.di.ToothpickActivityLifecycleCallbacks
import com.dev.lokeshkalal.todomvi.model.backend.TasksRestApiModule
import timber.log.Timber
import toothpick.Scope
import toothpick.Toothpick
import toothpick.smoothie.module.SmoothieApplicationModule

class TasksApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Logger init
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.i("%s %d", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)

        // DI Root Scope init
        Toothpick.inject(this, openApplicationScope(this))
        registerActivityLifecycleCallbacks(ToothpickActivityLifecycleCallbacks())
    }

    /**
     * A very basic Application scope.
     */
    private fun openApplicationScope(app: Application): Scope {
        return Toothpick.openScope(app).apply {
            installModules(
                SmoothieApplicationModule(app),
                        TasksRestApiModule
            )
        }
    }
}