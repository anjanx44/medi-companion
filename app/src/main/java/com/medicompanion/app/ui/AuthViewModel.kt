package com.medicompanion.app.ui

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.medicompanion.app.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface AuthUiState {
    data object Loading : AuthUiState
    data object LoggedOut : AuthUiState
    data class LoggedIn(val uid: String) : AuthUiState
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = Firebase.auth

    private val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(
        application,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(application.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    )

    private val _authState = MutableStateFlow<AuthUiState>(
        auth.currentUser?.let { AuthUiState.LoggedIn(it.uid) } ?: AuthUiState.Loading
    )
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _authState.value = firebaseAuth.currentUser?.let { AuthUiState.LoggedIn(it.uid) }
                ?: AuthUiState.LoggedOut
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun googleSignInIntent(): Intent = googleSignInClient.signInIntent

    fun signOutGoogle() {
        googleSignInClient.signOut()
        auth.signOut()
    }

    fun handleGoogleSignInResult(resultIntent: Intent?) {
        _error.value = null
        _busy.value = true
        try {
            val account: GoogleSignInAccount = GoogleSignIn.getSignedInAccountFromIntent(resultIntent)
                .getResult(ApiException::class.java)
            authenticateWithGoogle(account)
        } catch (e: ApiException) {
            _busy.value = false
            when (e.statusCode) {
                12501 -> _error.value = "Google sign-in cancelled"
                10 -> _error.value = "Google sign-in not configured. Check google-services.json"
                else -> _error.value = "Google sign-in failed (${e.statusCode})"
            }
        }
    }

    private fun authenticateWithGoogle(account: GoogleSignInAccount) {
        val idToken = account.idToken
        if (idToken == null) {
            _busy.value = false
            _error.value = "Missing ID token from Google"
            return
        }
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential).await()
            } catch (e: Exception) {
                Log.w("AuthViewModel", "Google credential sign-in failed", e)
                _error.value = authErrorMessage(e.message)
            } finally {
                _busy.value = false
            }
        }
    }

    fun login(email: String, password: String) {
        val cleanEmail = email.trim()
        if (cleanEmail.isEmpty() || password.isEmpty()) {
            _error.value = "Please enter email and password"
            return
        }
        _error.value = null
        _busy.value = true
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(cleanEmail, password).await()
            } catch (e: Exception) {
                _error.value = authErrorMessage(e.message)
            } finally {
                _busy.value = false
            }
        }
    }

    fun signup(email: String, password: String) {
        val cleanEmail = email.trim()
        if (cleanEmail.isEmpty() || password.length < 6) {
            _error.value = "Password must be at least 6 characters"
            return
        }
        _error.value = null
        _busy.value = true
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(cleanEmail, password).await()
            } catch (e: Exception) {
                _error.value = authErrorMessage(e.message)
            } finally {
                _busy.value = false
            }
        }
    }

    fun logout() {
        auth.signOut()
    }

    private fun authErrorMessage(raw: String?): String = when {
        raw == null -> "Something went wrong"
        raw.contains("invalid-email", ignoreCase = true) -> "Invalid email address"
        raw.contains("wrong-password", ignoreCase = true) ||
            raw.contains("invalid-login-credentials", ignoreCase = true) ||
            raw.contains("invalid-credential", ignoreCase = true) ->
            "Incorrect email or password"
        raw.contains("user-not-found", ignoreCase = true) -> "No account found with this email"
        raw.contains("email-already-in-use", ignoreCase = true) -> "An account already exists with this email"
        raw.contains("weak-password", ignoreCase = true) -> "Password must be at least 6 characters"
        raw.contains("too-many-requests", ignoreCase = true) -> "Too many attempts. Try again later"
        else -> raw
    }
}
