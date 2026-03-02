package com.gloam.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val databasePath = File(System.getProperty("user.home"), ".gloam/gloam.db")
        databasePath.parentFile?.mkdirs()
        val url = "jdbc:sqlite:${databasePath.absolutePath}"
        val driver = JdbcSqliteDriver(url)
        val currentVersion = driver.getVersion()
        if (currentVersion == 0L) {
            GloamDatabase.Schema.create(driver)
            driver.setVersion(1L)
        }
        return driver
    }

    private fun SqlDriver.getVersion(): Long =
        executeQuery(null, "PRAGMA user_version", parameters = 0, binders = null).use {
            if (it.next().value) it.getLong(0) ?: 0L else 0L
        }

    private fun SqlDriver.setVersion(version: Long) {
        execute(null, "PRAGMA user_version = $version", parameters = 0, binders = null)
    }
}
