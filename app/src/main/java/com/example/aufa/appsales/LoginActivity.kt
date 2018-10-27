package com.example.aufa.appsales

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.aufa.appsales.R.id.txtPassword
import com.example.aufa.appsales.data.Sales
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    val database = FirebaseDatabase.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (!getUserLogin().isNullOrBlank()) {
            startMain()
            return
        }

        txtemail.setBorderColor("#FFD42626")
        // set the border radius
        txtemail.setBorderRadius(20f)
        // set the border stroke
        txtemail.setBorderStroke(4)

        txtPassword.setBorderColor("#FFD42626")
        // set the border radius
        txtPassword.setBorderRadius(20f)
        // set the border stroke
        txtPassword.setBorderStroke(4)

        btnLogin.setBorderColor("#FFD42626")
        // set the border radius
        btnLogin.setBorderRadius(20f)
        // set the border stroke
        btnLogin.setBorderStroke(4)


        btnLogin.setOnClickListener {
            login()
        }

    }

    fun login() {
        val db = database.getReference("sales")
        val email = txtemail.text.toString()
        val password = txtPassword.text.toString()



        db.child(email.replace(".", "")).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
            }


            override fun onDataChange(p0: DataSnapshot?) {
                val sales = p0?.getValue(Sales::class.java)

                Log.e("sales", "$email")


                if (!sales?.email.isNullOrEmpty()) {
                    Toast.makeText(this@LoginActivity, "User dengan email : ${sales?.email}, ditemukan =${sales?.password}", Toast.LENGTH_LONG).show()
                    if (sales?.password.equals(password)) {
                        Toast.makeText(this@LoginActivity, "Passwordnya benar", Toast.LENGTH_LONG).show()
                        saveStatus(email)
                        startMain()

                    } else {
                        Toast.makeText(this@LoginActivity, "Passwordnya salah", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "User dengan username : ${email} tidak ditemukan", Toast.LENGTH_LONG).show()
                }
            }


        })

    }

    private fun startMain() {
        val intent = Intent(this@LoginActivity, HalamanUtamaActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun saveStatus(email: String) {
        val database = FirebaseDatabase.getInstance().getReference("sales").child(email.replace(".", ""))
        val dblogin = database.child("login")
        val editor = getSharedPreferences("userlogin", Context.MODE_PRIVATE).edit()
        editor.putString("email", email)
        editor.apply()
        dblogin.setValue(1)

    }


    private fun getUserLogin(): String? = getSharedPreferences("userlogin", Context.MODE_PRIVATE)
            .getString("email", null)
}
