package com.udacity.project4.authentication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class AuthViewModel : ViewModel() {
    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, ERROR_STATE
    }

    val authenticationState = FirebaseUserLiveData().map{ user->
        if(user != null){
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }
}