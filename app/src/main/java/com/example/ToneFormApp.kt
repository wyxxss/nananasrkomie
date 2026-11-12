package com.example

import android.app.Application
import com.example.media.MediaRepository

class ToneFormApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MediaRepository.getInstance(this)
    }
}
