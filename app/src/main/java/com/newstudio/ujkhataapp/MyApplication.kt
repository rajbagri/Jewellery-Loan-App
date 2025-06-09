package com.newstudio.ujkhataapp

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.firestore.persistentCacheSettings

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val settings = firestoreSettings {
            setLocalCacheSettings(persistentCacheSettings {
                setSizeBytes(100 * 1024 * 1024)
            })
        }

        FirebaseFirestore.getInstance().firestoreSettings = settings
    }
}
