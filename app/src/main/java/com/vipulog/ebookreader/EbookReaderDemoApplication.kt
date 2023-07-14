package com.vipulog.ebookreader

import android.app.Application
import com.google.android.material.color.DynamicColors

class EbookReaderDemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}