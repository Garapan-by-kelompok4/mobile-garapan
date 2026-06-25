package com.app.garapan.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.app.garapan.data.repository.ArtikelRepositoryImpl
import com.app.garapan.data.repository.AuthRepositoryImpl
import com.app.garapan.data.repository.JasaRepositoryImpl
import com.app.garapan.data.repository.KategoriRepositoryImpl
import com.app.garapan.data.repository.PembayaranRepositoryImpl
import com.app.garapan.data.repository.PesananRepositoryImpl
import com.app.garapan.data.repository.PortofolioRepositoryImpl
import com.app.garapan.data.repository.ProjectRepositoryImpl
import com.app.garapan.data.repository.ReviewRepositoryImpl
import com.app.garapan.data.repository.SessionRepositoryImpl
import com.app.garapan.data.repository.SkillRepositoryImpl
import com.app.garapan.data.repository.SupportChatRepositoryImpl
import com.app.garapan.data.repository.TopWorkerRepositoryImpl
import com.app.garapan.data.repository.UsersRepositoryImpl
import com.app.garapan.domain.repository.UsersRepository
import com.app.garapan.domain.repository.ArtikelRepository
import com.app.garapan.domain.repository.AuthRepository
import com.app.garapan.domain.repository.JasaRepository
import com.app.garapan.domain.repository.KategoriRepository
import com.app.garapan.domain.repository.PembayaranRepository
import com.app.garapan.domain.repository.PesananRepository
import com.app.garapan.domain.repository.PortofolioRepository
import com.app.garapan.domain.repository.ProjectRepository
import com.app.garapan.domain.repository.ReviewRepository
import com.app.garapan.domain.repository.SessionRepository
import com.app.garapan.domain.repository.SkillRepository
import com.app.garapan.domain.repository.SupportChatRepository
import com.app.garapan.domain.repository.TopWorkerRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "garapan_prefs")

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds
    @Singleton
    abstract fun bindKategoriRepository(impl: KategoriRepositoryImpl): KategoriRepository

    @Binds
    @Singleton
    abstract fun bindPortofolioRepository(impl: PortofolioRepositoryImpl): PortofolioRepository

    @Binds
    @Singleton
    abstract fun bindTopWorkerRepository(impl: TopWorkerRepositoryImpl): TopWorkerRepository

    @Binds
    @Singleton
    abstract fun bindUsersRepository(impl: UsersRepositoryImpl): UsersRepository

    @Binds
    @Singleton
    abstract fun bindArtikelRepository(impl: ArtikelRepositoryImpl): ArtikelRepository

    @Binds
    @Singleton
    abstract fun bindJasaRepository(impl: JasaRepositoryImpl): JasaRepository

    @Binds
    @Singleton
    abstract fun bindProjectRepository(impl: ProjectRepositoryImpl): ProjectRepository

    @Binds
    @Singleton
    abstract fun bindSkillRepository(impl: SkillRepositoryImpl): SkillRepository

    @Binds
    @Singleton
    abstract fun bindSupportChatRepository(impl: SupportChatRepositoryImpl): SupportChatRepository

    @Binds
    @Singleton
    abstract fun bindPesananRepository(impl: PesananRepositoryImpl): PesananRepository

    @Binds
    @Singleton
    abstract fun bindPembayaranRepository(impl: PembayaranRepositoryImpl): PembayaranRepository

    @Binds
    @Singleton
    abstract fun bindReviewRepository(impl: ReviewRepositoryImpl): ReviewRepository
}
