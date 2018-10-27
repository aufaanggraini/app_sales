package com.example.aufa.appsales

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.aufa.appsales.data.Kerjaan
import com.example.aufa.appsales.data.Kredit
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_list_kredit.*
import kotlinx.android.synthetic.main.activity_pengerjaan_tugas.*
import kotlinx.android.synthetic.main.fragment_jobs.*
import kotlinx.android.synthetic.main.kreditor_layout.view.*

/**
 * Created by aufa on 23/08/18.
 */
class PengerjaanTugas : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    var kerjaan: Kerjaan? = null
    var listKredits = mutableListOf<Kredit>()
    /*private fun getUserKey(): String? = context?.getSharedPreferences("userlogin", Context.MODE_PRIVATE)
            ?.getString("email", null)?.replace(".", "")
*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pengerjaan_tugas)

        recyclerView = findViewById<RecyclerView>(R.id.list_kredit_peng).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@PengerjaanTugas)
        }


        val str = intent.getStringExtra("jobs")
        kerjaan = Gson().fromJson(str, Kerjaan::class.java)

        btnTambah.setOnClickListener {
            tambahKreditor()
        }


        listKredit()
    }

    override fun onResume() {
        super.onResume()
        listKredit()
    }

    fun listKredit() {
        val database = FirebaseDatabase.getInstance().getReference("kredit")

        database.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onDataChange(p0: DataSnapshot?) {
//                val item = mutableListOf<Kredit>()
                listKredits.clear()
                p0?.children?.forEach {
                    val content = (it.value as? HashMap<String, Any>)

                    val k = Kredit()
                    k.kerjaanID = content?.get(Kredit::kerjaanID.name).toString()
                    k.idsales = content?.get(Kredit::idsales.name).toString()
                    k.alamat = content?.get(Kredit::alamat.name).toString()
                    k.nama = content?.get(Kredit::nama.name).toString()
                    k.dokumenLengkap = content?.get(Kredit::dokumenLengkap.name)?.toString()?.toInt() ?: 0
                    k.siapSurvey = content?.get(Kredit::siapSurvey.name)?.toString()?.toInt() ?: 0
                    k.photoUrl = content?.get(Kredit::photoUrl.name).toString()
                    k.key = it.key

                    Log.e("list", "${content?.get("idsales")}")
                    Log.e("list", "${k.kerjaanID} == ${kerjaan?.key}")

                    if (k.idsales.equals(getUserLogin()) && k.kerjaanID == kerjaan?.key && k.kerjaanID != null)
                        this@PengerjaanTugas.listKredits.add(k)
                }
                recyclerView.adapter = PengerjaanTugas.MyAdapter(listKredits)

                kerjaan?.let { render(it, listKredits) }
            }
        })
    }

    class MyAdapter(private val myDataset: List<Kredit>) :
            RecyclerView.Adapter<PengerjaanTugas.MyAdapter.ViewHolder>() {

        class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            fun setJob(krd: Kredit) {
                view.txtNamaKreditor.text = krd.nama
                view.txtAlamat.setText(krd.alamat)
                if (!krd.photoUrl.isNullOrBlank()) {
                    Log.e("list", "${krd.photoUrl}")
                    Picasso.with(this.view.context).load(krd.photoUrl).into(view.foto)
                } else {
                    Picasso.with(this.view.context).load(R.drawable.placeholder_camera_green).into(view.foto)
                }
                krd.idsales?.let {
                    loadSales(it, view.txtNamaSales)
                    Log.e("list", "${it}")
                }
                /*view.setOnClickListener {
                    val intent = Intent(view.context, TambahKredit::class.java)
                    intent.putExtra("key", krd.key)
                    view.context.startActivity(intent)
                }*/
                view.setOnLongClickListener {
                    AlertDialog.Builder(view.context)

                            .setTitle(krd.nama)
                            .setMessage("Apa yang ingin anda lakukan dengan Database ini?")
                            .setPositiveButton("Edit", { dialog, which ->
                                val intent = Intent(view.context, TambahKredit::class.java)
                                intent.putExtra("key", krd.key)
                                view.context.startActivity(intent)
                            })
                            .setNegativeButton("Hapus", { dialog, which ->
                                FirebaseDatabase.getInstance().getReference("kredit").child(krd.key).setValue(null)
                            })

                            .show()
                    true
                }

                view.checkDocument.visibility = if (krd.dokumenLengkap == 1) View.VISIBLE else View.GONE
                view.checkSurvey.visibility = if (krd.siapSurvey == 1) View.VISIBLE else View.GONE
            }

            fun loadSales(salesID: String, listener: TextView) {
                listener.text = salesID
                val database = FirebaseDatabase.getInstance().getReference("sales").child(salesID.replace(".", ""))

                database.addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {
                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        val sales = p0?.child("nama")?.value?.toString()
//                        Log.e("sales", sales)
                        sales?.let {
                            listener.text = it
                        }
                    }
                })
            }


        }


        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): PengerjaanTugas.MyAdapter.ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.kreditor_layout, parent, false)
            return PengerjaanTugas.MyAdapter.ViewHolder(v)
        }

        override fun onBindViewHolder(holder: PengerjaanTugas.MyAdapter.ViewHolder, position: Int) {
            holder.setJob(myDataset[position])

            if (position % 2 == 1) {
                holder.view.setBackgroundColor(Color.rgb(240, 240, 240))
            } else {
                holder.view.setBackgroundColor(Color.rgb(255, 255, 255))

            }

        }

        override fun getItemCount() = myDataset.size
    }


    private fun render(k: Kerjaan, listKredits : MutableList<Kredit>) {
        namaTugas.text = k.nama
        namaTempat.text = k.lokasi
        tanggal.text = k.durasi
        val currentTime = System.currentTimeMillis()
        k.startTime?.let { st ->
            if (st > currentTime) {
                status.text = "BARU"
                status.setBackgroundResource(R.color.color_new)
                btnTambah.isEnabled = false
                btnTambah.visibility = View.GONE
            }

            k.endTime?.let { et ->
                if (et < currentTime) {
                    btnTambah.isEnabled = false
                    btnTambah.visibility = View.GONE
                    Log.e("LIST_KREDIT", Gson().toJson(listKredits))
                    if (listKredits.isEmpty()) {
                        status.text = "GAGAL"
                        status.setBackgroundResource(R.color.color_fail)
                    } else {
                        status.text = "BERHASIL"
                        status.setBackgroundResource(R.color.color_succes)

                    }
                }

                if (et > currentTime && st < currentTime) {
                    // IN PROGRESS
                    status.text = "IN PROGRESS"
                    status.setBackgroundResource(R.color.color_wip)

                }

            }

        }
    }

    fun tambahKreditor() {
        val intent = Intent(this@PengerjaanTugas, TambahKredit::class.java)
        intent.putExtra("id", kerjaan?.key)
        startActivity(intent)
    }
}