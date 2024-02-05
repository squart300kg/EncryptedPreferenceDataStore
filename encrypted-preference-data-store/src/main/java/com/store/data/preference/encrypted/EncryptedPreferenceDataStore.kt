package com.store.data.preference.encrypted

import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

class EncryptedPreferenceDataStore private constructor(
    corruptionHandler: ReplaceFileCorruptionHandler<Preferences>? = null,
    migrations: List<DataMigration<Preferences>> = listOf(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    produceFile: () -> File
): DataStore<Preferences> {

    private val aes256Util =
        AES256UtilImpl.instance

    private val preferencesDataStore =
        PreferenceDataStoreFactory.create(
            scope = scope,
            migrations = migrations,
            corruptionHandler = corruptionHandler,
            produceFile = produceFile
        )

    companion object {
        @Volatile
        var INSTANCE: DataStore<Preferences>? = null
        fun create(
            corruptionHandler: ReplaceFileCorruptionHandler<Preferences>? = null,
            migrations: List<DataMigration<Preferences>> = listOf(),
            scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile: () -> File
        ): DataStore<Preferences> {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = EncryptedPreferenceDataStore(
                        corruptionHandler = corruptionHandler,
                        migrations = migrations,
                        scope = scope,
                        produceFile = produceFile
                    )
                    INSTANCE = instance
                }
                return instance
            }
        }
    }

    override val data: Flow<Preferences>
        get() = preferencesDataStore.data

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        return preferencesDataStore.updateData(transform)
    }
}