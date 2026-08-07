package com.emmikal.securedoselog.security

import android.content.Context
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import android.util.Base64
import com.emmikal.securedoselog.AppDatabase
import com.emmikal.securedoselog.MIGRATION_2_3
import com.emmikal.securedoselog.MIGRATION_3_4
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import java.security.SecureRandom

object DatabaseProvider {

    @Volatile
    private var instance: AppDatabase? = null

    private const val DB_NAME = "a7f3d2e19c4b"
    private const val PREFS_NAME = "b4e91ac308f2"
    private const val PASSPHRASE_KEY = "k9f2a1c7"

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

        return Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
            .openHelperFactory(factory)
            .allowMainThreadQueries()
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
            .build()
    }

    private fun getOrCreatePassphrase(context: Context): ByteArray {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val existing = prefs.getString(PASSPHRASE_KEY, null)
        if (existing != null) {
            return Base64.decode(existing, Base64.NO_WRAP)
        }

        val newPassphrase = ByteArray(32)
        SecureRandom().nextBytes(newPassphrase)

        prefs.edit()
            .putString(PASSPHRASE_KEY, Base64.encodeToString(newPassphrase, Base64.NO_WRAP))
            .apply()

        return newPassphrase
    }
}