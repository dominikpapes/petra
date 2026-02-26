package com.petra.di

import androidx.room.Room
import com.petra.data.AppDatabase
import com.petra.viewmodel.PetViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "pet_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    single { get<AppDatabase>().petDao() }
    single { get<AppDatabase>().petActivityDao() }

    viewModel { PetViewModel(
        petDao = get(),
        petActivityDao = get(),
        context = androidContext()
    ) }
}
