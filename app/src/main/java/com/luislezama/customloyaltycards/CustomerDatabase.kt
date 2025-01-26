package com.luislezama.customloyaltycards

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Date

class CustomerDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "CustomLoyaltyCards"

        // Table and column names
        private const val TABLE_NAME = "customers"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_EXPIRATION_DATE = "expiration_date"
        private const val COLUMN_VISITS = "visits"
        private const val COLUMN_UID = "uid"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT,
                $COLUMN_PHONE TEXT,
                $COLUMN_EXPIRATION_DATE INTEGER,
                $COLUMN_VISITS INTEGER,
                $COLUMN_UID TEXT UNIQUE
            )
        """.trimIndent()

        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Handle upgrades if needed
    }

    fun getAllCustomers(): List<Customer> {
        val customerList = mutableListOf<Customer>()

        val db = readableDatabase
        val projection = arrayOf(
            COLUMN_ID,
            COLUMN_NAME,
            COLUMN_PHONE,
            COLUMN_EXPIRATION_DATE,
            COLUMN_VISITS,
            COLUMN_UID
        )

        val cursor: Cursor = db.query(
            TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            "$COLUMN_NAME ASC"
        )

        with(cursor) {
            while (moveToNext()) {
                val name = getString(getColumnIndexOrThrow(COLUMN_NAME))
                val phone = getString(getColumnIndexOrThrow(COLUMN_PHONE))
                val expirationDate = getLong(getColumnIndexOrThrow(COLUMN_EXPIRATION_DATE))
                val visits = getInt(getColumnIndexOrThrow(COLUMN_VISITS))
                val uid = getString(getColumnIndexOrThrow(COLUMN_UID))

                val customer = Customer().apply {
                    this.name = name
                    this.phone = phone
                    this.expirationDate = Date(expirationDate)
                    this.visits = visits
                    this.uid = uid
                }

                customerList.add(customer)
            }
        }

        cursor.close()
        return customerList.toList()
    }

    fun getCustomerByUid(uid: String): Customer? {
        val db = readableDatabase
        val projection = arrayOf(
            COLUMN_ID,
            COLUMN_NAME,
            COLUMN_PHONE,
            COLUMN_EXPIRATION_DATE,
            COLUMN_VISITS,
            COLUMN_UID
        )

        val selection = "$COLUMN_UID = ?"
        val selectionArgs = arrayOf(uid)

        val cursor: Cursor = db.query(
            TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            val phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE))
            val expirationDate = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EXPIRATION_DATE))
            val visits = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VISITS))

            val customer = Customer().apply {
                this.name = name
                this.phone = phone
                this.expirationDate = Date(expirationDate)
                this.visits = visits
                this.uid = uid
            }

            cursor.close()
            customer
        } else {
            cursor.close()
            null
        }
    }

    fun getCustomersBySearch(query: String): List<Customer> {
        val customerList = mutableListOf<Customer>()

        val db = readableDatabase
        val projection = arrayOf(
            COLUMN_ID,
            COLUMN_NAME,
            COLUMN_PHONE,
            COLUMN_EXPIRATION_DATE,
            COLUMN_VISITS,
            COLUMN_UID
        )

        val selection = "$COLUMN_NAME LIKE ? OR $COLUMN_PHONE LIKE ?"
        val selectionArgs = arrayOf("%$query%", "%$query%")

        val cursor: Cursor = db.query(
            TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            "$COLUMN_NAME ASC"
        )

        with(cursor) {
            while (moveToNext()) {
                val name = getString(getColumnIndexOrThrow(COLUMN_NAME))
                val phone = getString(getColumnIndexOrThrow(COLUMN_PHONE))
                val expirationDate = getLong(getColumnIndexOrThrow(COLUMN_EXPIRATION_DATE))
                val visits = getInt(getColumnIndexOrThrow(COLUMN_VISITS))
                val uid = getString(getColumnIndexOrThrow(COLUMN_UID))

                val customer = Customer().apply {
                    this.name = name
                    this.phone = phone
                    this.expirationDate = Date(expirationDate)
                    this.visits = visits
                    this.uid = uid
                }

                customerList.add(customer)
            }
        }
        cursor.close()
        return customerList.toList()
    }

    fun insertOrUpdateCustomer(customer: Customer): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, customer.name)
            put(COLUMN_PHONE, customer.phone)
            put(COLUMN_EXPIRATION_DATE, customer.expirationDate?.time ?: 0)
            put(COLUMN_VISITS, customer.visits)
            put(COLUMN_UID, customer.uid)
        }

        return db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun deleteCustomer(uid: String): Boolean {
        val db = writableDatabase
        val whereClause = "$COLUMN_UID = ?"
        val whereArgs = arrayOf(uid)
        val deletedRows = db.delete(TABLE_NAME, whereClause, whereArgs)

        return deletedRows > 0
    }

    fun deleteAllCustomers() {
        val db = writableDatabase
        db.delete(TABLE_NAME, null, null)
    }

    fun doesPhoneExist(phone: String): Boolean {
        val db = readableDatabase
        val projection = arrayOf(
            COLUMN_ID
        )

        val selection = "$COLUMN_PHONE = ?"
        val selectionArgs = arrayOf(phone)

        val cursor: Cursor = db.query(
            TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        val exists = cursor.count > 0
        cursor.close()
        return exists
    }
}