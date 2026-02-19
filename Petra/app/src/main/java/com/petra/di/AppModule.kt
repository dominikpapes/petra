package com.petra.di

import androidx.room.Room
import com.petra.data.AppDatabase
import com.petra.viewmodel.PetViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // 1. Provide the Room Database as a Singleton
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "pet_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    // 2. Provide the Daos (extracting them from the database)
    single { get<AppDatabase>().petDao() }
    single { get<AppDatabase>().petActivityDao() }

    // 3. Provide the ViewModel
    viewModel { PetViewModel(
        petDao = get(),
        petActivityDao = get(),
        context = androidContext()
    ) }
}
