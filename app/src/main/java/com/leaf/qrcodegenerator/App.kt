package com.leaf.qrcodegenerator

import android.app.Application

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        ApplicationHolder.init(this)
    }
}