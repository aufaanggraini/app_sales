package com.example.aufa.appsales

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

/**
 * Created by aufa on 17/04/18.
 */
class MyApplications: Application() {

    override fun onCreate() {
        super.onCreate()
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}