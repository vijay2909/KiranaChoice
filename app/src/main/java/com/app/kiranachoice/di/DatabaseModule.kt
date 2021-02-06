package com.app.kiranachoice.di

import android.content.Context
import androidx.room.Room
import com.app.kiranachoice.data.db.AppDatabase
import com.app.kiranachoice.network.DateTimeApi
import com.app.kiranachoice.utils.APP_DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /*@Singleton
    @Provides
    fun provideCategory(): String {
        return "Aata"
    }*/

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Singleton
    @Provides
    fun provideGsonConverterFactory() : GsonConverterFactory {
        return GsonConverterFactory.create()
    }


    @Singleton
    @Provides
    fun provideRetrofitClient(
        okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ) : Retrofit{
        return Retrofit.Builder()
            .baseUrl(DateTimeApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(gsonConverterFactory)
            .build()
    }

    @Singleton
    @Provides
    fun provideDateTimeApiService(retrofit: Retrofit): DateTimeApi {
        return retrofit.create(DateTimeApi::class.java)
    }

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext app: Context): AppDatabase =
        Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            APP_DATABASE_NAME
        ).build()

    @Singleton
    @Provides
    fun provideDatabaseDao(db: AppDatabase) = db.databaseDao

}