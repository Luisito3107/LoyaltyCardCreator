package com.luislezama.customloyaltycards

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        //DynamicColors.applyToActivitiesIfAvailable(this, DynamicColorsOptions.Builder().setThemeOverlay(R.style.Theme_CustomLoyaltyCards).build())
    }
}