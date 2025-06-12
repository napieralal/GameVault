package com.example.gamevault.ui.screens.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamevault.isValidEmail
import com.example.gamevault.network.FirebaseAuthHelper
import com.example.gamevault.validatePassword
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun signIn(email: String, password: String) {
        if (!isValidEmail(email)) {
            _loginState.value = LoginState.Error("Please enter a valid email address")
            return
        }

        validatePassword(password)?.let { error ->
            _loginState.value = LoginState.Error(error)
            return
        }

        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            val result = FirebaseAuthHelper.signIn(email, password)
            _loginState.value = if (result.isSuccess) {
                LoginState.Success
            } else {
                LoginState.Error("Login failed: ${result.exceptionOrNull()?.message ?: "Unknown error"}")
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }

    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank()) return

        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = FirebaseAuthHelper.sendPasswordResetEmail(email)
            _loginState.value = if (result.isSuccess) {
                LoginState.Error("Password reset email sent")
            } else {
                LoginState.Error("Failed to send reset email: ${result.exceptionOrNull()?.message}")
            }
        }
    }

}

