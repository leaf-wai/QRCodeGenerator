package com.leaf.qrcodegenerator

import android.app.Application
import com.tencent.mmkv.MMKV

object ApplicationHolder {
    lateinit var app: Application

    fun init(application: Application) {
        app = application
        MMKV.initialize(application)
    }
}