package com.luislezama.customloyaltycards

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.zxing.BarcodeFormat
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.DateFormat
import java.util.TimeZone


class CustomerCardFragment : Fragment() {
    private lateinit var database: CustomerDatabase
    private var customerUid: String? = ""
    private var existentCustomer: Customer? = null

    private lateinit var fragmentInflater: LayoutInflater
    private lateinit var fragmentView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = CustomerDatabase(requireContext())
        arguments?.let {
            customerUid = it.getString(CUSTOMER_UID)
            if (customerUid is String) {
                existentCustomer = database.getCustomerByUid(customerUid!!)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_customer_card, container, false)

        // Hide keyboard
        requireActivity().currentFocus?.let { view ->
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }

        // Set and show toolbar
        val toolbar: MaterialToolbar = view.findViewById(R.id.customercard_toolbar)
        toolbar.apply {
            title = ""
            setNavigationOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }
            inflateMenu(R.menu.customercard_menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.customercard_share -> {
                        val imageBitmap = viewToBitmap(view.findViewById(R.id.customercard_all))
                        shareBitmap(imageBitmap)
                    }
                    R.id.customercard_edit -> {
                        val intent = Intent(requireContext(), RegisterActivity::class.java).apply {
                            putExtra("updateCustomerUid", existentCustomer!!.uid)
                        }
                        resultLauncher.launch(intent)
                    }
                }
                true
            }
        }

        this@CustomerCardFragment.fragmentInflater = inflater
        this@CustomerCardFragment.fragmentView = view


        showCustomerData()

        return view
    }

    companion object {
        private const val CUSTOMER_UID = "customer_uid"

        @JvmStatic
        fun newInstance(uid: String) =
            CustomerCardFragment().apply {
                arguments = Bundle().apply {
                    putString(CUSTOMER_UID, uid)
                }
            }
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (customerUid is String) {
                existentCustomer = database.getCustomerByUid(customerUid!!)
                showCustomerData()
            }
        }
    }

    private fun showCustomerData() {
        if (existentCustomer != null) {
            fragmentView.findViewById<TextView>(R.id.customercard_name).text = existentCustomer!!.name
            fragmentView.findViewById<TextView>(R.id.customercard_uid).text = existentCustomer!!.uid
            val visitGrid = fragmentView.findViewById<GridLayout>(R.id.customercard_visits)

            visitGrid.removeAllViews()
            for (visit in 1..Customer.VISITS_MAX) {
                var isMilestone = Customer.VISITS_MILESTONES.containsKey(visit)
                val visitView = fragmentInflater.inflate(R.layout.customercard_visit, visitGrid, false).apply {
                    this.findViewById<RelativeLayout>(R.id.background).backgroundTintList = requireContext().getColorStateList(if (isMilestone) { R.color.customercard_color_primary } else { R.color.white })

                    this.findViewById<TextView>(R.id.hint).apply {
                        text = if (isMilestone) { Customer.VISITS_MILESTONES.get(visit)!! } else { "" }
                        visibility = if (isMilestone) { View.VISIBLE } else { View.GONE }
                    }

                    this.findViewById<ImageView>(R.id.active_icon).apply {
                        visibility = if (visit <= existentCustomer!!.visits) { View.VISIBLE } else { View.GONE }
                        imageTintList = requireContext().getColorStateList(if (isMilestone) { R.color.white } else { R.color.customercard_color_primary })
                    }
                }

                visitGrid.addView(visitView)
            }

            try {
                val bitmap = CustomBarcodeEncoder().apply {
                    setBackgroundColor(requireContext().getColor(R.color.transparent))
                    setForegroundColor(requireContext().getColor(R.color.customercard_color_primary))
                }.encodeBitmap(existentCustomer!!.uid, BarcodeFormat.CODE_39, 1200, 288)
                fragmentView.findViewById<ImageView>(R.id.customercard_barcode).setImageBitmap(bitmap)
            } catch (_: Exception) { }

            fragmentView.findViewById<TextView>(R.id.customercard_expiration).text = getString(
                R.string.customercard_expiration,
                DateFormat.getDateInstance(DateFormat.LONG).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(existentCustomer!!.expirationDate!!)
            )
        } else {
            requireActivity().supportFragmentManager.beginTransaction().remove(this@CustomerCardFragment).commit()
        }
    }

    // Screen to image and share
    private fun viewToBitmap(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)

        val background: Drawable? = view.background
        background?.draw(canvas)

        view.draw(canvas)

        return bitmap
    }

    private fun shareBitmap(bitmap: Bitmap) {
        try {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val bytes = stream.toByteArray()

            val file = File(requireActivity().cacheDir, "temp_image.jpg")
            FileOutputStream(file).apply {
                write(bytes)
                flush()
                close()
            }

            val uri = FileProvider.getUriForFile(requireContext(), "${requireActivity().packageName}.provider", file)

            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = ClipData.newRawUri("", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }, ""))

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}