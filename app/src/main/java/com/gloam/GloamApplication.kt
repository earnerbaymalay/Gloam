package com.gloam

import android.app.Application
import com.gloam.data.db.GloamDatabase

class GloamApplication : Application() {
    
    val database: GloamDatabase by lazy {
        GloamDatabase.getDatabase(this)
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    
    companion object {
        lateinit var instance: GloamApplication
            private set
    }
}
