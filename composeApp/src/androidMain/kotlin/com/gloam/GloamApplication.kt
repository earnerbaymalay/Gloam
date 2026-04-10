package com.gloam

import android.app.Application
import android.content.Context
import com.gloam.data.db.AndroidGloamDatabase
import com.gloam.data.db.AndroidGloamDatabaseAdapter
import com.gloam.data.db.GloamDatabase
import java.security.SecureRandom

class GloamApplication : Application() {
    
    val database: GloamDatabase by lazy {
        val passphrase = getOrCreatePassphrase()
        val db = AndroidGloamDatabase.getDatabase(this, passphrase)
        AndroidGloamDatabaseAdapter(db)
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    private fun getOrCreatePassphrase(): ByteArray {
        val prefs = getSharedPreferences("gloam_prefs", Context.MODE_PRIVATE)
        var passphraseHex = prefs.getString("db_passphrase", null)
        return if (passphraseHex != null) {
            decodeHex(passphraseHex)
        } else {
            val bytes = ByteArray(32).also { SecureRandom().nextBytes(it) }
            passphraseHex = encodeHex(bytes)
            prefs.edit().putString("db_passphrase", passphraseHex).apply()
            bytes
        }
    }

    private fun encodeHex(bytes: ByteArray): String =
        bytes.joinToString("") { "%02x".format(it) }

    private fun decodeHex(hex: String): ByteArray {
        val bytes = ByteArray(hex.length / 2)
        for (i in 0 until hex.length step 2) {
            bytes[i / 2] = hex.substring(i, i + 2).toInt(16).toByte()
        }
        return bytes
    }
    
    companion object {
        lateinit var instance: GloamApplication
            private set
    }
}
