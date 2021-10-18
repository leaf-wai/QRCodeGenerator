package com.leaf.qrcodegenerator

import android.app.Application
import com.kongzue.dialog.util.DialogSettings

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        ApplicationHolder.init(this)
        initDialog()
    }

    private fun initDialog(){
        DialogSettings.style = DialogSettings.STYLE.STYLE_MIUI
        DialogSettings.theme = DialogSettings.THEME.LIGHT
        DialogSettings.init()
    }
}