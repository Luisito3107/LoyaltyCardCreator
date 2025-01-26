package com.luislezama.customloyaltycards

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlin.math.min


class MainActivity : AppCompatActivity() {
    private lateinit var database: CustomerDatabase

    private lateinit var btn_scan: Button
    private lateinit var btn_register: Button
    private lateinit var btn_list: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = CustomerDatabase(this)

        // Start scanner from shortcut
        if (intent.getBooleanExtra("openScanner", false)) scannerLaunch()

        btn_scan = findViewById(R.id.btn_scan)
        btn_scan.setOnClickListener {
            scannerLaunch()
        }

        btn_register = findViewById(R.id.btn_register)
        btn_register.setOnClickListener {
            startActivity(Intent(this@MainActivity, RegisterActivity::class.java))
        }

        btn_list = findViewById(R.id.btn_list)
        btn_list.setOnClickListener {
            startActivity(Intent(this@MainActivity, CustomerListActivity::class.java))
        }
    }

    // Launch scanner
    private val barcodeLauncher = registerForActivityResult<ScanOptions, ScanIntentResult>(ScanContract()) { result: ScanIntentResult -> scannerCallback(result) }
    private fun scannerLaunch() {
        barcodeLauncher.launch(ScanOptions()
            .setPrompt(ScannerActivity.SCANNER_PROMPT)
            .setBeepEnabled(ScannerActivity.SCANNER_BEEP)
            .setDesiredBarcodeFormats(ScannerActivity.SCANNER_FORMATS)
            .setOrientationLocked(ScannerActivity.SCANNER_ORIENTATION_LOCK)
            .setCaptureActivity(ScannerActivity::class.java))
    }
    private fun scannerCallback(result: ScanIntentResult) {
        if (result.contents == null) {
            val originalIntent = result.originalIntent
            if (originalIntent == null) {

            } else if (originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
                MaterialAlertDialogBuilder(this@MainActivity).apply {
                    setTitle(R.string.scanner_error_permissions)
                    setPositiveButton(R.string.action_ok) { dialog, which -> null }
                }.show()
            }
        } else {
            var customerUid = (result.contents?.trim() ?: "")
            customerUid = customerUid.substring(0, min(6, customerUid.length))

            if (customerUid.isNotEmpty()) {
                val customer = database.getCustomerByUid(customerUid)

                if (customer != null) {
                    startActivity(Intent(this@MainActivity, CustomerListActivity::class.java).apply {
                        putExtra("customerCardOpen", customer.uid)
                    })
                } else {
                    MaterialAlertDialogBuilder(this@MainActivity).apply {
                        setTitle(R.string.scanner_error_customer_notfound)
                        setPositiveButton(R.string.action_ok) { dialog, which -> null }
                    }.show()
                }
            }
        }
    }
}