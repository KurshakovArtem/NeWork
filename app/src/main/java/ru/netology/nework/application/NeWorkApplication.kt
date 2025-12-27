package ru.netology.nework.application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@HiltAndroidApp
class NeWorkApplication : Application() {
    private val appScope = CoroutineScope(Dispatchers.Default)


}