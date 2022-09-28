package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.map
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.FragmentAuthBinding
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle

class AuthenticationFragment: Fragment() {
    //private lateinit var binding: FragmentAuthBinding
    companion object{
        const val TAG = "AuthenticationActivity"
        const val SIGN_IN_RESULT_CODE = 1001
    }

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, ERROR_STATE
    }

    val authenticationState = FirebaseUserLiveData().map{
        if(it != null){
            AuthenticationState.AUTHENTICATED
            Log.i(TAG,"Authentication State - Auth")
        } else {
            AuthenticationState.UNAUTHENTICATED
            Log.i(TAG,"Authentication State - Unauth")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       // binding = DataBindingUtil.inflate(inflater, R.layout.fragment_auth, container, false)
       val binding = DataBindingUtil.inflate<FragmentAuthBinding>(inflater, R.layout.fragment_auth , container, false)
        binding.loginRegBttn.setOnClickListener{ login() }
       return binding.root
        //return super.onCreateView(inflater, container, savedInstanceState)
    }

    fun login(){
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers)
                .build(), SIGN_IN_RESULT_CODE
        )

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in user.
                Log.i(
                    TAG,
                    "Successfully signed in user " +
                            "${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )
            } else {
                // Sign in failed. If response is null the user canceled the sign-in flow using
                // the back button. Otherwise check response.getError().getErrorCode() and handle
                // the error.
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }
}