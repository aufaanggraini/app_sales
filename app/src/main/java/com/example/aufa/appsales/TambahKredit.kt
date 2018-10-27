package com.example.aufa.appsales

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.aufa.appsales.data.Kredit
import com.example.aufa.appsales.data.Sales
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_kredit.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class TambahKredit : AppCompatActivity() {
    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE = arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    var kredit: Kredit? = null
    var selectedPhoto: Bitmap? = null
    var defaultPhotoURL: String? = null
    var storage = FirebaseStorage.getInstance()
    val database = FirebaseDatabase.getInstance()

    var sales: Sales? = null
    var idKerjaan: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kredit)
        verifyStoragePermissions(this)


        var key = intent.getStringExtra("key")
        idKerjaan = intent.getStringExtra("id")
        key?.let {
            getkredit(it)
        }


        ambilPhoto.setOnClickListener {
            val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(i, 99)
        }
        resetPhoto.setOnClickListener {
            ambilPhoto.setImageResource(R.drawable.placeholder_camera_green)
            selectedPhoto = null
        }

        btnTambah.setOnClickListener {
            tambahKredit()
        }

        btnCancel.setOnClickListener {
            finish()
        }

        disableAllViews(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 99 && resultCode == Activity.RESULT_OK && null != data) {
            val selectedImage = data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(selectedImage!!, filePathColumn, null, null, null)
            cursor!!.moveToFirst()
            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            val picturePath = cursor.getString(columnIndex)
            cursor.close()
            selectedPhoto = BitmapFactory.decodeFile(picturePath)
            Log.e("list", "${selectedPhoto}")
            ambilPhoto.setImageBitmap(selectedPhoto)
        }
    }


 /*   fun getKredit(key: String) {
        val database = FirebaseDatabase.getInstance().getReference("kredit").child(key)

        database.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onDataChange(p0: DataSnapshot?) {
                kredit = p0?.getValue(Kredit::class.java)
            }
        })
    }*/

    private fun disableAllViews(enable: Boolean = false) {
        ambilPhoto.isEnabled = enable
        resetPhoto.isEnabled = enable
        txtnama.isEnabled = enable
        txtalamat.isEnabled = enable
        txtIdSales.isEnabled = enable
        btnTambah.isEnabled = enable
        btnCancel.isEnabled = enable
    }

    fun verifyStoragePermissions(activity: Activity) {
        // Check if we have write permission
        val permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            )
        }
    }

    var uploading = false

    fun tambahKredit() {
        val key = database.getReference("kredit").push().key

        if (selectedPhoto != null) {
            // upload photo
            uploading = true
            uploadPhoto(kredit?.key ?: key)
        } else {
            simpanData(kredit?.key ?: key)
        }

    }


    private fun uploadPhoto(salesid: String) {
        disableAllViews()
        progress.visibility = View.VISIBLE
        ambilPhoto.visibility = View.GONE
        val baos = ByteArrayOutputStream()
        selectedPhoto?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val imageRef = storage.reference.child("sales/${salesid}.jpg")

        val uploadTask = imageRef.putBytes(data)
        uploadTask.addOnFailureListener(OnFailureListener {
            // Handle unsuccessful uploads
            progress.visibility = View.GONE
            ambilPhoto.visibility = View.VISIBLE
            Toast.makeText(this, "gagal upload", Toast.LENGTH_LONG).show()
            uploading = false
        }).addOnSuccessListener {
            uploading = false
            progress.visibility = View.GONE
            ambilPhoto.visibility = View.VISIBLE
            defaultPhotoURL = it.downloadUrl.toString()
            simpanData(salesid)
        }
    }

    private fun getkredit(key: String) {
        val db = database.getReference("kredit")

        db.child(key).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(sl: DataSnapshot?) {
                val data = (sl?.value as? HashMap<String, Any>)

                Log.e("sales", "$data")

                kredit = Kredit().apply {
                    nama = data?.get("nama")?.toString() ?: ""
                    alamat = data?.get("alamat")?.toString() ?: ""
                    idsales = data?.get("idsales")?.toString() ?: ""
                    dokumenLengkap = data?.get("dokumenLengkap")?.toString()?.toIntOrNull() ?: 0
                    photoUrl = data?.get("photoUrl")?.toString() ?: ""
                    siapSurvey = data?.get("siapSurvey")?.toString()?.toIntOrNull() ?: 0
                    this.key = sl?.key ?: ""
                    //key = sl?.key
                }

                txtalamat.setText(kredit?.alamat)
                txtnama.setText(kredit?.nama)
                txtIdSales.setText(kredit?.idsales)

                if (kredit?.dokumenLengkap == 1) {
                    chDokumenLengkap.isChecked = true;
                }
                if (kredit?.siapSurvey == 1) {
                    chSiapSurvey.isChecked = true

                }
                btnTambah.text = "Ubah"
                defaultPhotoURL = kredit?.photoUrl

                if (kredit?.photoUrl?.isNotBlank() == true) {
                    Log.e("photoUrl", "$defaultPhotoURL")
                    Picasso.with(this@TambahKredit).load(defaultPhotoURL).into(ambilPhoto)

                }
                else {
                    Picasso.with(this@TambahKredit).load(R.drawable.placeholder_camera_green).into(ambilPhoto)

                }

            }

        })
    }

    private fun simpanData(key: String) {
        if (uploading) return
        val nama = txtnama.text.toString()
        val alamat = txtalamat.text.toString()


        val myRef = database.getReference("kredit").child(kredit?.key ?: key)

        kredit?.let {
        } ?: kotlin.run {
            kredit = Kredit()
        }

        kredit?.let {
            it.nama = nama
            it.idsales = getUserLogin()
            it.alamat = alamat
            it.siapSurvey = if (chSiapSurvey.isChecked) 1 else 0
            it.dokumenLengkap = if (chDokumenLengkap.isChecked) 1 else 0
            it.photoUrl = defaultPhotoURL
            it.kerjaanID = idKerjaan


            myRef.setValue(it)


        }

        finish()

    }
}
