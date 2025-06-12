package com.example.gamevault.ui.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamevault.isValidEmail
import com.example.gamevault.network.FirebaseAuthHelper
import com.example.gamevault.ui.screens.login.LoginState
import com.example.gamevault.validatePassword
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}

class RegisterViewModel : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    fun register(email: String, password: String) {
        if (!isValidEmail(email)) {
            _registerState.value = RegisterState.Error("Please enter a valid email address")
            return
        }

        validatePassword(password)?.let { error ->
            _registerState.value = RegisterState.Error(error)
            return
        }

        _registerState.value = RegisterState.Loading
        viewModelScope.launch {
            val result = FirebaseAuthHelper.register(email, password)
            _registerState.value = if (result.isSuccess) {
                RegisterState.Success
            } else {
                val errorMessage = when (result.exceptionOrNull()?.message) {
                    "The email address is already in use by another account." ->
                        "This email is already registered"
                    "A network error (such as timeout, interrupted connection or unreachable host) has occurred." ->
                        "Network error. Please check your connection"
                    else -> "Registration failed. Please try again"
                }
                RegisterState.Error(errorMessage)
            }
        }
    }

    fun resetState() {
        _registerState.value = RegisterState.Idle
    }
}