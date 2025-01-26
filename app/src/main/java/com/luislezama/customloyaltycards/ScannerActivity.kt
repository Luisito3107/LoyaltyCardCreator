package com.luislezama.customloyaltycards

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView


class ScannerActivity : AppCompatActivity() {
    // Scanner options
    companion object {
        const val SCANNER_PROMPT = ""
        const val SCANNER_BEEP = true
        const val SCANNER_FORMATS = "CODE_39"
        const val SCANNER_ORIENTATION_LOCK = true
    }

    private var capture: CaptureManager? = null
    private var barcodeScannerView: DecoratedBarcodeView? = null
    private var torch = false


    private val defaultBrightness = 0.9f
    private var systemBrightness = 0.5f
    private var systemLayoutAttributes: WindowManager.LayoutParams? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        // Get top color
        val windowBgColor = TypedValue()
        theme.resolveAttribute(android.R.attr.windowBackground, windowBgColor, true)

        // Set and show toolbar without title
        val toolbar: Toolbar = findViewById(R.id.scanner_toolbar)
        toolbar.title = ""
        toolbar.setBackgroundColor(Color.argb(128, Color.red(windowBgColor.data), Color.green(windowBgColor.data), Color.blue(windowBgColor.data)))
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Prepare barcode scanner
        barcodeScannerView = findViewById<DecoratedBarcodeView>(R.id.zxing_barcode_scanner)
        capture = CaptureManager(this, barcodeScannerView)
        capture!!.initializeFromIntent(intent, savedInstanceState)
        capture!!.decode()

        adjustScreenBrightness()
    }

    // Menu (flash toggle)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.scanner_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.scanner_toggle_flash -> {
                torch = !torch
                item.icon = ContextCompat.getDrawable(applicationContext, if (!torch) R.drawable.outline_flashlight_on_24 else R.drawable.outline_flashlight_off_24)
                barcodeScannerView!!.onKeyDown(if (!torch) 24 else 25, null)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Brightness
    private fun adjustScreenBrightness() {
        systemLayoutAttributes = this.window?.attributes
        systemLayoutAttributes?.screenBrightness?.let { currentBrightness ->
            systemBrightness = currentBrightness
            if (currentBrightness < defaultBrightness) {
                systemLayoutAttributes?.screenBrightness = defaultBrightness
                this.window?.attributes = systemLayoutAttributes
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        capture!!.onDestroy()

        // Restore brightness at system level
        systemLayoutAttributes?.screenBrightness?.let { currentBrightness ->
            if (currentBrightness > systemBrightness) {
                systemLayoutAttributes?.screenBrightness = systemBrightness
                this.window?.attributes = systemLayoutAttributes
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onResume() {
        super.onResume()
        capture!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture!!.onPause()
    }

    /*override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // return barcodeScannerView!!.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
        return false
    }*/
}