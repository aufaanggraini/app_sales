package com.example.aufa.appsales

import android.location.Location
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.location.LocationListener
import android.location.LocationManager
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.support.v4.app.ActivityCompat
import android.content.DialogInterface
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.design.widget.NavigationView
import android.support.v7.app.AlertDialog
import android.util.Log.e
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.ActionBarDrawerToggle
import android.view.*
import kotlinx.android.synthetic.main.app_bar_halaman_utama.*
import kotlinx.android.synthetic.main.main_drawer.*


/**
 * Created by aufa on 23/11/17.
 */
class HalamanUtamaActivity : AppCompatActivity(), LocationListener, NavigationView.OnNavigationItemSelectedListener {


    lateinit var locationManager: LocationManager
    val MY_PERMISSIONS_REQUEST_LOCATION = 99
    var username: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_drawer)

        setSupportActionBar(toolbar)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        username = getUserLogin()?.replace(".", "")

        if (checkLocationPermission()) {
            e("permission", "true")
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 1f, this)

            cekLokasi()
        } else {
            e("permission", "false")
        }

        pager.adapter = CustomPagerAdapter(supportFragmentManager, this)
//        supportActionBar?.setDisplayShowHomeEnabled(true)
//        actionBar?.setDisplayShowHomeEnabled(true)
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.open_drawer, R.string.tutup_drawer)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        tabs.setupWithViewPager(pager)
        nav_view.setNavigationItemSelectedListener(this)
    }

    @SuppressLint("MissingPermission")
    private fun cekLokasi() {
        val lokasi = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        saveLokasi(lokasi)

    }

    private fun saveLokasi(lokasi: Location?) {
        e("Lokasi diterima", "${lokasi?.latitude} , ${lokasi?.longitude}")
        val database = FirebaseDatabase.getInstance().getReference("sales").child(username)
        val dbLocation = database.child("current_loc")
        val mapLoc = mutableMapOf<String, Double?>("latitude" to lokasi?.latitude, "longitude" to lokasi?.longitude)

        dbLocation.setValue(mapLoc)

        val dbHistory = FirebaseDatabase.getInstance().getReference("loc_his").child(username)

        val currentDate = Calendar.getInstance()
        val tanggal = SimpleDateFormat("yyyyMMdd")
        val jam = SimpleDateFormat("HHmmss")
        val tanggalDb = dbHistory.child(tanggal.format(currentDate.time)).child(jam.format(currentDate.time))

        tanggalDb.setValue(mapLoc)


    }


    fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                        .setTitle("Minta Izin Bro")
                        .setMessage("Izin minta request lokasi ente bro,, supaya bos tau... :p")
                        .setPositiveButton("Iya boleh", DialogInterface.OnClickListener { dialogInterface, i ->
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(this@HalamanUtamaActivity,
                                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                                    MY_PERMISSIONS_REQUEST_LOCATION)
                        })
                        .create()
                        .show()


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        MY_PERMISSIONS_REQUEST_LOCATION)
            }
            return false
        } else {
            return true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {

        e("PERMISION", "${grantResults}")
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 1f, this)
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return
            }
        }
    }


    override fun onLocationChanged(location: Location?) {
        saveLokasi(location)


    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.logout -> {
                dologout()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun dologout() {
        val database = FirebaseDatabase.getInstance().getReference("sales").child(username)
        val dblogin = database.child("login")
        dblogin.setValue(0)

        val editor = getSharedPreferences("userlogin", Context.MODE_PRIVATE).edit()
        editor.clear()
        editor.apply()

        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }


    internal inner class CustomPagerAdapter(fm: FragmentManager, var mContext: Context) : FragmentPagerAdapter(fm) {
        val titles = listOf<String>(
                "Belum Selesai",
                "Sudah Selesai",
                "Gagal",
                "Semua Task"
        )

        override fun getItem(position: Int): Fragment {
            val fragment = JobsFragment().apply {
                type = when (position) {
                    0 -> JobsFragment.TYPE.UNFINISHED
                    1 -> JobsFragment.TYPE.FINISHED
                    2 -> JobsFragment.TYPE.FAILED
                    else -> JobsFragment.TYPE.ALL
                }
            }
            return fragment
        }

        override fun getCount(): Int = titles.size

        override fun getPageTitle(position: Int): CharSequence {
            return titles[position]
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.nav_pekerjaan ->{
                halaman_Utama()
            }
            R.id.nav_kredit -> {
                list_kredit()
            }
        }

        return true
    }

    fun list_kredit(){
        val intent = Intent(this@HalamanUtamaActivity, ListKredit::class.java)
        startActivity(intent)
    }

    fun halaman_Utama(){
         val intent = Intent(this@HalamanUtamaActivity, HalamanUtamaActivity::class.java)
         startActivity(intent)
     }


}
