package com.app.kiranachoice.di

import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import androidx.room.Room
import com.app.kiranachoice.db.AppDatabase
import com.app.kiranachoice.network.DateTimeApi
import com.app.kiranachoice.network.SendNotificationAPI
import com.app.kiranachoice.utils.APP_DATABASE_NAME
import com.app.kiranachoice.utils.USER_REFERENCE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ): NotificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    @Qualifier  // define qualifier for DateTimeHttpClient
    @Retention(AnnotationRetention.BINARY)
    annotation class DateTimeHttpClient

    @Qualifier  // define qualifier for NotificationHttpClient
    @Retention(AnnotationRetention.BINARY)
    annotation class NotificationHttpClient

    @Singleton
    @NotificationHttpClient
    @Provides
    fun provideNotificationRetrofitClient(
        okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(SendNotificationAPI.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(gsonConverterFactory)
            .build()
    }

    @Singleton
    @Provides
    fun provideNotificationApiService(@NotificationHttpClient retrofit: Retrofit): SendNotificationAPI {
        return retrofit.create(SendNotificationAPI::class.java)
    }

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
    fun provideGsonConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create()
    }


    @Singleton
    @DateTimeHttpClient
    @Provides
    fun provideRetrofitClient(
        okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(DateTimeApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(gsonConverterFactory)
            .build()
    }

    @Singleton
    @Provides
    fun provideDateTimeApiService(@DateTimeHttpClient retrofit: Retrofit): DateTimeApi {
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

    @Singleton
    @Provides
    fun provideFireStoreInstance() = FirebaseFirestore.getInstance()

    @Singleton
    @Provides
    fun provideFirebaseAuthInstance() = FirebaseAuth.getInstance()

    @Singleton
    @Provides
    fun provideFirebaseDatabase() = FirebaseDatabase.getInstance()

    @Singleton
    @Provides
    fun provideUserFireStoreCollectionReference(firebaseReference: FirebaseFirestore): CollectionReference {
        return firebaseReference.collection(USER_REFERENCE)
    }
}