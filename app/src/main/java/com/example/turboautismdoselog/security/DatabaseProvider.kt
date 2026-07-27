package com.example.turboautismdoselog.security

import android.content.Context
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import android.util.Base64
import com.example.turboautismdoselog.AppDatabase
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import java.security.SecureRandom

object DatabaseProvider {

    @Volatile
    private var instance: AppDatabase? = null

    @JvmStatic
    fun getDatabase(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: buildDatabase(context.applicationContext).also { instance = it }
        }
    }

    private fun buildDatabase(context: Context): AppDatabase {
        System.loadLibrary("sqlcipher")

        val passphrase = getOrCreatePassphrase(context)
        val factory = SupportOpenHelperFactory(passphrase)

        return Room.databaseBuilder(context, AppDatabase::class.java, "drug_database")
            .openHelperFactory(factory)
            .allowMainThreadQueries()
            .build()
    }

    private fun getOrCreatePassphrase(context: Context): ByteArray {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val prefs = EncryptedSharedPreferences.create(
            context,
            "db_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val existing = prefs.getString("db_passphrase", null)
        if (existing != null) {
            return Base64.decode(existing, Base64.NO_WRAP)
        }

        val newPassphrase = ByteArray(32)
        SecureRandom().nextBytes(newPassphrase)

        prefs.edit()
            .putString("db_passphrase", Base64.encodeToString(newPassphrase, Base64.NO_WRAP))
            .apply()

        return newPassphrase
    }
}