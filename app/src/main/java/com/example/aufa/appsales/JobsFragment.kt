package com.example.aufa.appsales

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.example.aufa.appsales.data.Kerjaan
import com.example.aufa.appsales.data.Kredit
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import kotlinx.android.synthetic.main.dialog_input.view.*
import kotlinx.android.synthetic.main.fragment_jobs.*
import kotlinx.android.synthetic.main.item_layout.view.*


class JobsFragment : Fragment() {
    var type: TYPE = TYPE.ALL
    var mapListKreditByKerjaan = mutableMapOf<String?, MutableList<Kredit?>?>()
    var listKerjaan = mutableListOf<Kerjaan?>()
    var kerjaanLoaded = false
    var kreditLoaded = false

    private fun getUserKey(): String? = context?.getSharedPreferences("userlogin", Context.MODE_PRIVATE)
            ?.getString("email", null)?.replace(".", "")


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadKerjaanKredit()
        recycleViewJobs.layoutManager = LinearLayoutManager(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_jobs, container, false)
    }

    fun listPekerjaan() {
        val database = FirebaseDatabase.getInstance().getReference("list_kerja")

        database.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot?) {
                listKerjaan.clear()
                p0?.children?.forEach {
                    kerjaanLoaded = true
                    val content = (it.value as? HashMap<String, Any>)
                    if (it.child("penugasan").hasChild(getUserKey())) {

//                    val content = data?.values?.firstOrNull() as? HashMap<String, Any>
                        val kerjaan = Kerjaan(
                                content?.get(Kerjaan::nama.name)?.toString() ?: "",
                                content?.get(Kerjaan::durasi.name)?.toString() ?: "",
                                content?.get(Kerjaan::startTime.name)?.toString()?.toLong() ?: -1,
                                content?.get(Kerjaan::endTime.name)?.toString()?.toLong() ?: -1,
                                content?.get(Kerjaan::lokasi.name)?.toString() ?: "",
                                it.key
                        )
                        listKerjaan.add(kerjaan)

//                        val al = it.child("penugasan").child(getUserKey()).value.toString()
//                        kerjaan.alasan = al
//                        when (type) {
//                            TYPE.ALL -> listKerjaan.add(kerjaan)
//                            TYPE.FINISHED -> if (al == "1") {
//                                listKerjaan.add(kerjaan)
//                            }
//                            TYPE.UNFINISHED -> if (al == "0") {
//                                listKerjaan.add(kerjaan)
//                            }
//                            else -> if (al != "1" && al != "0") {
//                                listKerjaan.add(kerjaan)
//                            }
//
//                        }

                        Log.e("list", "${content?.get("nama")}")
                    }
                }
                //recycleViewJobs?.adapter = MyAdapter(listKerjaan)

                recheck()
            }
        })
    }

    private fun recheck() {
        Log.e("MAP_LIST", Gson().toJson(mapListKreditByKerjaan))
        if (kreditLoaded && kerjaanLoaded) {
            val items: List<Kerjaan?> = when (type) {
                TYPE.ALL -> listKerjaan
                TYPE.FAILED -> listKerjaan.filter { System.currentTimeMillis() > it?.endTime!! && mapListKreditByKerjaan.get(it?.key) == null }
                TYPE.FINISHED -> listKerjaan.filter { System.currentTimeMillis() > it?.endTime!! && mapListKreditByKerjaan.get(it?.key)?.isNotEmpty() == true }
                TYPE.UNFINISHED -> listKerjaan.filter {
                    val et = it?.endTime
                    val st = it?.startTime

                    (et != null && st != null && et > System.currentTimeMillis() && st < System.currentTimeMillis())
                }
            }
            recycleViewJobs?.adapter = MyAdapter(items)
        }
    }

    private fun loadKerjaanKredit() {
        val db = FirebaseDatabase.getInstance().getReference("kredit").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(data: DataSnapshot?) {
                data?.children?.forEach {
                    kreditLoaded = true
                    val kredit = it.getValue(Kredit::class.java)
                    Log.e("KREDIT_KEY", Gson().toJson(it?.key))
                    val list = mapListKreditByKerjaan.get(kredit?.kerjaanID) ?: mutableListOf()
                    list.add(kredit)
                    mapListKreditByKerjaan[kredit?.kerjaanID] = list
                }

                listPekerjaan()
            }
        })
    }


    inner class MyAdapter(private val myDataset: List<Kerjaan?>) :
            RecyclerView.Adapter<MyAdapter.ViewHolder>() {

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            fun setJob(job: Kerjaan) {


                /*val et = job.endTime
                val st = job.startTime
                if (et != null && st != null) {
                     if(System.currentTimeMillis() > et ) {
                        view.txtPenugasan.text = "SELESAI"
                        view.txtNama.setTextColor(ContextCompat.getColor(view.context, R.color.colorFinished))
                        view.txtPenugasan.setTextColor(ContextCompat.getColor(view.context, R.color.colorFinished))
                    }
                 *//*   "0" -> {
                        view.txtPenugasan.text = "BELUM SELESAI"
                        view.txtNama.setTextColor(ContextCompat.getColor(view.context, R.color.colorUNFinished))
                        view.txtPenugasan.setTextColor(ContextCompat.getColor(view.context, R.color.colorUNFinished))
                    }
                    else -> {
                        view.txtNama.setTextColor(ContextCompat.getColor(view.context, R.color.colorFailed))
                        view.txtPenugasan.setTextColor(ContextCompat.getColor(view.context, R.color.colorFailed))
                        view.txtPenugasan.text = "Alasan Gagal: ${job.alasan}"
                    }*//*
                }*/
                view.txtNama.text = job.nama
                view.txtLokasi.text = job.lokasi
                view.txtDurasi.text = job.durasi
                view.setOnClickListener {
                    val intent = Intent(this@JobsFragment.context, PengerjaanTugas::class.java)
                    intent.putExtra("jobs", Gson().toJson(job))
                    startActivity(intent)
                }
            }

            private fun showAlasanDialogInput(job: Kerjaan) {
                val v = LayoutInflater.from(context).inflate(R.layout.dialog_input, null, false)
                val alert = AlertDialog.Builder(view.context)
                        .setTitle("Alasan Gagal job ${job.nama}")
                        .setView(v)
                        .create()


                v.txtBatal.setOnClickListener {
                    alert.dismiss()
                }
                v.txtSimpan.isEnabled = false

                v.txtSimpan.setOnClickListener {
                    val alasan = v.editAlasan.text.toString()
                    job.alasan = alasan
                    updateStatus(job)
                    alert.dismiss()
                }

                v.hint.text = "Kurang 20 karakter"
                v.editAlasan.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {

                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                        v.txtSimpan.isEnabled = (s?.length ?: 0) >= 20
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        v.hint.text = "Kurang ${20 - (s?.length ?: 0)} karakter"
                        if ((s?.length ?: 0) >= 20) v.hint.text = ""
                    }

                })

//                alert.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = false

                alert.show()
            }

            fun updateStatus(job: Kerjaan) {
                Log.e("update status", "status: ${job.alasan}")
                val database = FirebaseDatabase.getInstance()
                val ref = database.getReference("list_kerja")
                        .child(job.key)
                        .child("penugasan")
                        .child(getUserKey())
                ref.setValue(job.alasan)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): MyAdapter.ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            myDataset[position]?.let {
                holder.setJob(it)
                if (position % 2 == 0) {
                    holder.view.setBackgroundColor(Color.rgb(240, 240, 240))
                } else {
                    holder.view.setBackgroundColor(Color.rgb(255, 255, 255))

                }
            }

        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = myDataset.size
    }


    enum class TYPE {
        ALL, FINISHED, UNFINISHED, FAILED
    }

}// Required empty public constructor
