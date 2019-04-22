package com.dev.lokeshkalal.todomvi.view

import io.reactivex.Observable

interface EventObservable<E> {
    fun events(): Observable<E>
}