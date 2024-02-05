package com.sample.store.data.preference.encrypted.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.store.data.preference.encrypted.EncryptedPreferenceDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object PreferenceModule {

    @Provides
    @Singleton
    fun provideEncryptedDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> {
        return EncryptedPreferenceDataStore.create(
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            migrations = listOf()
        ) {
            context.preferencesDataStoreFile("SHARED_PREFS_NAME")
        }
    }
}