package com.dev.lokeshkalal.todomvi.model.backend

import com.dev.lokeshkalal.todomvi.model.Task
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import toothpick.ProvidesSingletonInScope
import toothpick.config.Module
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Simple typing for DI bindings.
 */
typealias BaseUrl = String

/**
 * Module defining bindings for the TasksRestApi
 */
object TasksRestApiModule : Module() {
    init {
        bind(BaseUrl::class.java).toInstance("https://casterdemoendpoints.firebaseio.com/")
        bind(TasksRestApi::class.java).toProvider(TasksRestApiProvider::class.java)
    }
}

interface TasksRestApi {
    /**
     * Get a Map of (read only) tasks from our demo Firebase sample.
     */
    @GET("tasks.json")
    fun getTasks(): Observable<Map<String, Task>>
}

@Singleton @ProvidesSingletonInScope
class TasksRestApiProvider @Inject constructor(baseUrl:BaseUrl) : Provider<TasksRestApi> {
    override fun get(): TasksRestApi = retrofit.create(TasksRestApi::class.java)

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}