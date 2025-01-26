package com.luislezama.customloyaltycards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CustomerListRecycler (private var customerList: List<Customer>, val itemClickListener: (Customer) -> Unit) : RecyclerView.Adapter<CustomerListRecycler.ViewHolder>() {
    // Filtering customer list
    fun filterList(filterList: List<Customer>) {
        customerList = filterList
        notifyDataSetChanged()
    }

    // Define recycle_customer views
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val customerName: TextView = itemView.findViewById(R.id.customer_name)
        val customerPhone: TextView = itemView.findViewById(R.id.customer_phone)
    }

    // Inflate layout of recycle_customer
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerListRecycler.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val recycleCustomer = inflater.inflate(R.layout.recycle_customer, parent, false)
        return ViewHolder(recycleCustomer)
    }

    // Set each element
    override fun onBindViewHolder(viewHolder: CustomerListRecycler.ViewHolder, position: Int) {
        val customer: Customer = customerList[position]

        viewHolder.customerName.text = customer.name
        viewHolder.customerPhone.text = customer.phone

        viewHolder.itemView.setOnClickListener {
            itemClickListener(customer)
        }
    }

    override fun getItemCount(): Int {
        return customerList.size
    }
}