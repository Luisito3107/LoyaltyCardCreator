package com.luislezama.customloyaltycards

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.doOnTextChanged
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import java.text.DateFormat
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.max
import kotlin.math.min


class RegisterActivity : AppCompatActivity() {
    private lateinit var database: CustomerDatabase

    private var existentCustomer: Customer? = null

    private lateinit var register_field_name: TextInputLayout
    private lateinit var register_field_phone: TextInputLayout
    private lateinit var register_field_uid: TextInputLayout
    private lateinit var register_field_visits: TextInputLayout

    private lateinit var register_field_expiration: TextInputLayout
    private lateinit var register_field_expiration_datepicker: MaterialDatePicker<Long>
    private var register_field_expiration_value: Calendar = Calendar.getInstance().apply {
        add(Calendar.MONTH, Customer.DEFAULT_EXPIRATION_MONTHS);
    }

    private lateinit var register_save: ExtendedFloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize storage
        database = CustomerDatabase(this@RegisterActivity)

        // Set and show toolbar
        val toolbar: Toolbar = findViewById(R.id.register_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Initialize variables
        register_field_name = findViewById(R.id.register_field_name)
        register_field_name.editText!!.doOnTextChanged { text, _, _, _ ->
            register_field_name.error = if (text.toString().trim().length < 3) getString(R.string.register_field_name_error) else null
        }
        register_field_phone = findViewById(R.id.register_field_phone)
        register_field_phone.editText!!.doOnTextChanged { text, _, _, _ ->
            register_field_phone.error = if (text.toString().trim().length != 10) getString(R.string.register_field_phone_error) else null
        }
        register_field_uid = findViewById(R.id.register_field_uid)
        register_field_uid.editText!!.setText(Customer.genUid())
        register_field_visits = findViewById(R.id.register_field_visits)
        register_field_visits.editText!!.setText("0")

        // Find if updating existent customer
        intent.getStringExtra("updateCustomerUid").apply {
            if (this != null) {
                existentCustomer = database.getCustomerByUid(this)
                if (existentCustomer is Customer) {
                    toolbar.setTitle(R.string.register_title_update)
                    register_field_name.editText!!.setText(existentCustomer!!.name)
                    register_field_phone.editText!!.setText(existentCustomer!!.phone)
                    register_field_uid.editText!!.setText(existentCustomer!!.uid)
                    register_field_visits.editText!!.setText(existentCustomer!!.visits.toString())
                    register_field_expiration_value.time = existentCustomer!!.expirationDate!!
                } else {
                    finish()
                }
            }
        }

        // Expiration date picker
        register_field_expiration = findViewById(R.id.register_field_expiration)
        register_field_expiration.editText!!.inputType = InputType.TYPE_NULL;
        register_field_expiration.editText!!.keyListener = null
        register_field_expiration.editText!!.setOnFocusChangeListener { _, focus ->
            if (focus) register_field_expiration_datepicker!!.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
        }
        fun setSelectedDateText() {
            register_field_expiration.editText!!.setText(DateFormat.getDateInstance(DateFormat.LONG).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(register_field_expiration_value.timeInMillis))
        }
        fun initializeDatePicker() {
            register_field_expiration_datepicker = MaterialDatePicker.Builder.datePicker().apply {
                setSelection(register_field_expiration_value.timeInMillis)
                setCalendarConstraints(CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now()).build())
            }.build().apply {
                addOnPositiveButtonClickListener {
                    register_field_expiration_value.timeInMillis = it
                    setSelectedDateText()
                }
                addOnDismissListener {
                    register_field_expiration.clearFocus()
                    initializeDatePicker()
                }
            }
            setSelectedDateText()
        }
        initializeDatePicker()

        // Customer visits
        register_field_visits.editText!!.inputType = InputType.TYPE_NULL;
        register_field_visits.editText!!.keyListener = null
        register_field_visits.setStartIconOnClickListener {
            register_field_visits.editText!!.setText((max(register_field_visits.editText!!.text.toString().toInt() - 1, 0)).toString())
        }
        register_field_visits.setEndIconOnClickListener {
            register_field_visits.editText!!.setText((min(register_field_visits.editText!!.text.toString().toInt() + 1, Customer.VISITS_MAX)).toString())
        }

        // Save
        register_save = findViewById(R.id.register_save)
        register_save.setOnClickListener {
            register_field_name.error = if (register_field_name.editText!!.text.toString().trim().length < 3) getString(R.string.register_field_name_error) else null
            register_field_phone.error = if (existentCustomer == null && database.doesPhoneExist(register_field_phone.editText!!.text.toString())) getString(R.string.register_field_phone_error_duplicate) else null


            var valid = (register_field_name.editText!!.text.toString().trim().length >= 3
                    && register_field_phone.editText!!.text.toString().trim().length == 10
                    && !(existentCustomer == null && database.doesPhoneExist(register_field_phone.editText!!.text.toString())))


            if (valid) {
                var success = true

                if (existentCustomer is Customer) {
                    existentCustomer!!.apply {
                        name = register_field_name.editText!!.text.toString()
                        phone = register_field_phone.editText!!.text.toString()
                        uid = register_field_uid.editText!!.text.toString()
                        expirationDate = register_field_expiration_value.time
                        visits = register_field_visits.editText!!.text.toString().toInt()
                    }
                    success = database.insertOrUpdateCustomer(existentCustomer!!) > 0

                    if (success) setResult(Activity.RESULT_OK)
                } else {
                    val customer = Customer().apply {
                        name = register_field_name.editText!!.text.toString()
                        phone = register_field_phone.editText!!.text.toString()
                        uid = register_field_uid.editText!!.text.toString()
                        expirationDate = register_field_expiration_value.time
                        visits = register_field_visits.editText!!.text.toString().toInt()
                    }
                    success = database.insertOrUpdateCustomer(customer) > 0

                    if (success) startActivity(Intent(this@RegisterActivity, CustomerListActivity::class.java).apply {
                        putExtra("customerCardOpen", customer.uid)
                    })
                }

                if (success) {
                    Toast.makeText(
                        this@RegisterActivity,
                        getString(R.string.register_save_success),
                        Toast.LENGTH_LONG
                    ).show()

                    this.finish()
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        getString(R.string.register_save_error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        // Delete
        val register_delete = findViewById<Button>(R.id.register_delete)
        if (existentCustomer is Customer) {
            register_delete.setOnClickListener {
                MaterialAlertDialogBuilder(this).apply {
                    setTitle(R.string.register_delete_confirm_title)
                    setMessage(R.string.register_delete_confirm_message)
                    setNegativeButton(R.string.action_cancel, null)
                    setPositiveButton(R.string.register_delete_confirm_ok) { dialog, which ->
                        if (database.deleteCustomer(existentCustomer!!.uid)) {
                            Toast.makeText(
                                this@RegisterActivity,
                                getString(R.string.register_delete_success),
                                Toast.LENGTH_LONG
                            ).show()

                            setResult(Activity.RESULT_OK)

                            this@RegisterActivity.finish()
                        }
                    }
                }.show()
            }
        } else {
            register_delete.visibility = View.GONE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        MaterialAlertDialogBuilder(this).apply {
            setTitle(R.string.register_onback_confirm_title)
            setMessage(R.string.register_onback_confirm_message)
            setNegativeButton(R.string.action_cancel, null)
            setPositiveButton(R.string.register_onback_confirm_ok) { dialog, which ->
                super.onBackPressed()
            }
        }.show()
    }
}