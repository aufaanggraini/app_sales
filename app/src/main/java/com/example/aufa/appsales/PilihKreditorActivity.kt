package com.example.aufa.appsales

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.aufa.appsales.data.Kredit

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_list_kredit.*

import kotlinx.android.synthetic.main.activity_pilih_kreditor.*
import kotlinx.android.synthetic.main.kredit_item_checkbox.*
import kotlinx.android.synthetic.main.kredit_item_checkbox.view.*
import java.util.*

class PilihKreditorActivity : AppCompatActivity() {
    var listkredit: MutableList<Kredit> = mutableListOf()
    var selectedKredit: MutableSet<Kredit> = mutableSetOf()
    var selectedKreditKeys: MutableSet<String> = mutableSetOf()

    var adapter: SalesAdapter = SalesAdapter()
    lateinit var jobID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pilih_kreditor)

        title = "Penugasan Job"
        jobID = intent.getStringExtra("nama")
        if (jobID == null) return

        btnBatal.setOnClickListener {
            finish()
        }

        btnSimpan.setOnClickListener {
            simpanPenugasan()
        }

        loadDataKredit()
        loadPenugasan()
        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler.adapter = adapter
    }

    private fun simpanPenugasan(){
        val data = mutableMapOf<String, String>()
        selectedKredit.forEach {
            it.nama?.let {
                data.put("key","value")
            }
        }
        val myRef = FirebaseDatabase.getInstance().getReference("kredit").child(jobID)
        myRef.child("nama").setValue(data)
        finish()
    }

    fun loadPenugasan() {
        val myRef = FirebaseDatabase.getInstance().getReference("kredit").child(jobID)
        myRef.child("nama").addValueEventListener(
                object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {
                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        p0?.children?.forEach { ch ->
                            selectedKreditKeys.add(ch.key)
                        }
                        adapter.notifyDataSetChanged()
                    }
                }
        )
    }

    fun loadDataKredit() {
        val database = FirebaseDatabase.getInstance().getReference("kredit")

        database.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onDataChange(p0: DataSnapshot?) {
                listkredit = mutableListOf<Kredit>()
                p0?.children?.forEach {
                    val content = (it.value as? HashMap<String, Any>)

                    val k = Kredit()
                    k.idsales = content?.get(Kredit::idsales.name).toString()
                    k.alamat = content?.get(Kredit::alamat.name).toString()
                    k.nama = content?.get(Kredit::nama.name).toString()
                    k.dokumenLengkap = content?.get(Kredit::dokumenLengkap.name)?.toString()?.toInt() ?: 0
                    k.siapSurvey = content?.get(Kredit::siapSurvey.name)?.toString()?.toInt() ?: 0
                    k.photoUrl = content?.get(Kredit::photoUrl.name).toString()
                    k.key = it.key

                   /* if (System.currentTimeMillis() != null){
                        if (System.currentTimeMillis()>= )
                    }*/
                    if (k.idsales.equals(getUserLogin()))
                        listkredit.add(k)

                }

                adapter.notifyDataSetChanged()
            }
        })
    }

    inner class SalesAdapter: RecyclerView.Adapter<SalesAdapter.VH>() {
        override fun getItemCount(): Int = listkredit.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(LayoutInflater.from(this@PilihKreditorActivity).inflate(R.layout.kredit_item_checkbox, parent, false))
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder?.setKaryawan(listkredit[position])
        }

        inner class VH(val view: View): RecyclerView.ViewHolder(view) {
            fun setKaryawan(kredit: Kredit) {
                if(selectedKreditKeys.filter {
                    it == kredit.nama
                }.isNotEmpty()) {
                    selectedKredit.add(kredit)
                }

                view.namaKredit.text = kredit.nama
                view.checkbox.isChecked = selectedKreditKeys.filter {
                    it == kredit.nama
                }.isNotEmpty()

                view.checkbox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked)
                        selectedKredit.add(kredit)
                    else
                        selectedKredit.remove(kredit)
                }
            }
        }
    }
    private fun getUserLogin(): String? = getSharedPreferences("userlogin", Context.MODE_PRIVATE)
            .getString("email", null)
}
