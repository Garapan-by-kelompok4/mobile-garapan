package com.app.garapan.di

import com.app.garapan.BuildConfig
import com.app.garapan.data.remote.api.ArtikelApi
import com.app.garapan.data.remote.api.AuthApi
import com.app.garapan.data.remote.api.JasaApi
import com.app.garapan.data.remote.api.KategoriApi
import com.app.garapan.data.remote.api.PembayaranApi
import com.app.garapan.data.remote.api.PesananApi
import com.app.garapan.data.remote.api.PortofolioApi
import com.app.garapan.data.remote.api.ProjectApi
import com.app.garapan.data.remote.api.SkillApi
import com.app.garapan.data.remote.api.SupportChatApi
import com.app.garapan.data.remote.api.TopWorkerApi
import com.app.garapan.data.remote.api.UsersApi
import com.app.garapan.data.remote.interceptor.AuthInterceptor
import com.app.garapan.data.remote.interceptor.AuthTokenAuthenticator
import com.app.garapan.data.remote.interceptor.MultipartUploadBufferInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        authenticator: AuthTokenAuthenticator
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .addInterceptor(MultipartUploadBufferInterceptor())
            .addInterceptor(authInterceptor)
            .authenticator(authenticator)

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                redactHeader("Authorization")
                // HEADERS avoids buffering large multipart upload bodies in debug logs.
                level = HttpLoggingInterceptor.Level.HEADERS
            }
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideUsersApi(retrofit: Retrofit): UsersApi =
        retrofit.create(UsersApi::class.java)

    @Provides
    @Singleton
    fun provideKategoriApi(retrofit: Retrofit): KategoriApi =
        retrofit.create(KategoriApi::class.java)

    @Provides
    @Singleton
    fun providePortofolioApi(retrofit: Retrofit): PortofolioApi =
        retrofit.create(PortofolioApi::class.java)

    @Provides
    @Singleton
    fun provideTopWorkerApi(retrofit: Retrofit): TopWorkerApi =
        retrofit.create(TopWorkerApi::class.java)

    @Provides
    @Singleton
    fun provideArtikelApi(retrofit: Retrofit): ArtikelApi =
        retrofit.create(ArtikelApi::class.java)

    @Provides
    @Singleton
    fun provideJasaApi(retrofit: Retrofit): JasaApi =
        retrofit.create(JasaApi::class.java)

    @Provides
    @Singleton
    fun provideProjectApi(retrofit: Retrofit): ProjectApi =
        retrofit.create(ProjectApi::class.java)

    @Provides
    @Singleton
    fun provideSkillApi(retrofit: Retrofit): SkillApi =
        retrofit.create(SkillApi::class.java)

    @Provides
    @Singleton
    fun provideSupportChatApi(retrofit: Retrofit): SupportChatApi =
        retrofit.create(SupportChatApi::class.java)

    @Provides
    @Singleton
    fun providePesananApi(retrofit: Retrofit): PesananApi =
        retrofit.create(PesananApi::class.java)

    @Provides
    @Singleton
    fun providePembayaranApi(retrofit: Retrofit): PembayaranApi =
        retrofit.create(PembayaranApi::class.java)
}
