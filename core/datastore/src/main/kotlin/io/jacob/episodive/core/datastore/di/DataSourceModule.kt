package io.jacob.episodive.core.datastore.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.jacob.episodive.core.datastore.datasource.UserPreferencesDataSource
import io.jacob.episodive.core.datastore.datasource.UserPreferencesDataSourceImpl
import io.jacob.episodive.core.datastore.store.UserPreferencesStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {
    @Provides
    @Singleton
    fun provideUserPreferencesDataSource(
        store: UserPreferencesStore
    ): UserPreferencesDataSource {
        return UserPreferencesDataSourceImpl(
            store = store
        )
    }
}