package com.cidadeativa.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

data class Usuario(
    val nome: String,
    val email: String,
    val senha: String
)

// Repositório em memória usado na simulação; os dados são perdidos ao fechar o app
object UsuarioRepository {
    private val usuarios = mutableListOf(
        Usuario(nome = "Usuário Teste", email = "teste@cidadeativa.com", senha = "123456")
    )

    fun buscarPorEmail(email: String): Usuario? =
        usuarios.firstOrNull { it.email.equals(email.trim(), ignoreCase = true) }

    fun adicionar(usuario: Usuario) {
        usuarios.add(usuario)
    }
}

class AuthViewModel : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    var usuarioAtual: Usuario? = null
        private set

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.update { it.copy(error = "Preencha e-mail e senha") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            delay(600)
            val usuario = UsuarioRepository.buscarPorEmail(email)
            if (usuario != null && usuario.senha == password) {
                usuarioAtual = usuario
                _state.update { it.copy(isLoading = false, isLoggedIn = true) }
            } else {
                _state.update { it.copy(isLoading = false, error = "E-mail ou senha inválidos") }
            }
        }
    }

    fun register(nome: String, email: String, password: String, confirmPassword: String) {
        val erro = when {
            nome.isBlank() || email.isBlank() || password.isBlank() -> "Preencha todos os campos"
            !email.contains("@") -> "E-mail inválido"
            password.length < 6 -> "A senha deve ter no mínimo 6 caracteres"
            password != confirmPassword -> "As senhas não coincidem"
            UsuarioRepository.buscarPorEmail(email) != null -> "Este e-mail já está cadastrado"
            else -> null
        }
        if (erro != null) {
            _state.update { it.copy(error = erro) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            delay(600)
            val usuario = Usuario(nome.trim(), email.trim(), password)
            UsuarioRepository.adicionar(usuario)
            usuarioAtual = usuario
            _state.update { it.copy(isLoading = false, isLoggedIn = true) }
        }
    }

    fun logout() {
        usuarioAtual = null
        _state.update { AuthUiState(isLoggedIn = false) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}