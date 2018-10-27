package com.example.aufa.appsales

import android.app.Activity
import android.content.Context
import com.example.aufa.appsales.data.Sales
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.sql.Time
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by aufa on 24/04/18.
 */

fun Context.getUserLogin(): String? = getSharedPreferences("userlogin", Context.MODE_PRIVATE)
        .getString("email", null)

fun Context.getSales(id: String? = getUserLogin(), onSalesResult:(Sales?) -> Unit) {
    val database = FirebaseDatabase.getInstance()
    val db = database.getReference("sales")
    db.child(id?.replace(".", "")).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError?) {
        }

        override fun onDataChange(p0: DataSnapshot?) {
            val sales = p0?.getValue(Sales::class.java)
            onSalesResult.invoke(sales)
        }
    })
}


