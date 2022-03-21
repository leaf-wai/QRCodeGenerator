package com.leaf.qrcodegenerator

import android.app.Application
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogx.style.MIUIStyle

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        ApplicationHolder.init(this)
        initDialog()
    }

    private fun initDialog() {
        DialogX.init(this)
        DialogX.globalStyle = MIUIStyle()
    }
}