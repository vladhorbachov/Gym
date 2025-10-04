package com.gymshark.di

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import org.koin.dsl.module

val firebaseModule = module {
    single { Firebase.auth }
}