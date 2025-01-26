package com.luislezama.customloyaltycards

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout


class CustomerListActivity : AppCompatActivity() {
    lateinit var database: CustomerDatabase
    lateinit var customerList: List<Customer>
    lateinit var customerListUnaltered: List<Customer>

    lateinit var customerListEmptyMsg: LinearLayout
    lateinit var customerListScrollView: ScrollView
    lateinit var customerListAdapter: RecyclerView
    lateinit var customerlistFieldSearch: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_list)

        // Initialize database
        database = CustomerDatabase(this@CustomerListActivity)
        customerList = database.getAllCustomers()
        customerListUnaltered = customerList.toList()

        // Set and show toolbar
        val toolbar: Toolbar = findViewById(R.id.customerlist_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Setup recycler view
        customerListEmptyMsg = findViewById(R.id.customerlist_empty)
        customerListScrollView = findViewById(R.id.customerlist_scrollview)
        fun showHideEmptyMsg() {
            if (customerList.isNotEmpty()) {
                customerListEmptyMsg.visibility = View.GONE
                customerListScrollView.visibility = View.VISIBLE
            } else {
                customerListEmptyMsg.visibility = View.VISIBLE
                customerListScrollView.visibility = View.GONE
            }
        }
        showHideEmptyMsg()
        if (customerListUnaltered.isNotEmpty()) {
            customerListAdapter = findViewById(R.id.customerlist_recycler)
            customerListAdapter.adapter = CustomerListRecycler(customerListUnaltered) { customer ->
                openCustomerCard(customer)
            }
            customerListAdapter.layoutManager = LinearLayoutManager(this)
        }

        // Setup search field
        customerlistFieldSearch = findViewById(R.id.customerlist_field_search)
        customerlistFieldSearch.editText!!.doOnTextChanged { text, _, _, _ ->
            val searchText = text?.trim() ?: ""
            customerList = if (searchText.isNotEmpty()) {
                customerList.filter { customer ->
                    customer.name.contains(searchText, ignoreCase = true) || customer.phone.contains(searchText)
                }
            } else {
                customerListUnaltered
            }

            if (this::customerListAdapter.isInitialized) (customerListAdapter.adapter as CustomerListRecycler).filterList(customerList)
            showHideEmptyMsg()
        }

        // Open customer card if requested
        intent.getStringExtra("customerCardOpen").apply {
            if (this != null) {
                val foundCustomer = database.getCustomerByUid(this)
                if (foundCustomer is Customer) {
                    openCustomerCard(foundCustomer)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (this::database.isInitialized) {
            customerList = database.getAllCustomers()
            customerListUnaltered = customerList.toList()
        }
        if (this::customerListAdapter.isInitialized) customerlistFieldSearch.editText!!.setText("${customerlistFieldSearch.editText!!.text}")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    // Menu (delete all)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.customerlist_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.customerlist_deleteall -> {
                MaterialAlertDialogBuilder(this).apply {
                    setTitle(R.string.customerlist_deleteall_confirm_title)
                    setMessage(R.string.customerlist_deleteall_confirm_message)
                    setNegativeButton(R.string.action_cancel, null)
                    setPositiveButton(R.string.customerlist_deleteall) { dialog, which ->
                        database.deleteAllCustomers()

                        Toast.makeText(
                            applicationContext,
                            getString(R.string.customerlist_deleteall_success),
                            Toast.LENGTH_LONG
                        ).show()

                        this@CustomerListActivity.onResume()
                    }
                }.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Show customer card
    fun openCustomerCard(customer: Customer) {
        val customerCardFragment = CustomerCardFragment.newInstance(customer.uid)
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top, R.anim.slide_in_top, R.anim.slide_out_bottom)
            .replace(R.id.listactivity_container, customerCardFragment, "CUSTOMER_CARD")
            .addToBackStack("CUSTOMER_CARD")
            .commit();
    }
}