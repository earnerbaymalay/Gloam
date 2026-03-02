package com.gloam

import android.app.Application
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.gloam.data.db.DatabaseDriverFactory
import com.gloam.data.db.GloamDatabase
import com.gloam.data.repository.GloamRepositoryImpl

class GloamApplication : Application() {

    val database: GloamDatabase by lazy {
        val driver = DatabaseDriverFactory(this).createDriver()
        GloamDatabase(driver)
    }

    val repository: GloamRepositoryImpl by lazy {
        GloamRepositoryImpl(database)
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
