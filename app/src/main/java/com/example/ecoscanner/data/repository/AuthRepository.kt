package com.example.ecoscanner.data.repository


import com.example.ecoscanner.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    // Devuelve el usuario actual o null si no hay sesión
    fun currentUser(): User? {
        val fbUser = auth.currentUser ?: return null
        return User(
            uid = fbUser.uid,
            email = fbUser.email ?: ""
        )
    }

    // Iniciar sesión
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val fbUser = result.user
                ?: return Result.failure(Exception("Usuari no trobat"))
            Result.success(User(fbUser.uid, fbUser.email ?: ""))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Registro de usuario nuevo
    suspend fun register(email: String, password: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val fbUser = result.user
                ?: return Result.failure(Exception("No s'ha pogut crear l'usuari"))
            Result.success(User(fbUser.uid, fbUser.email ?: ""))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cerrar sesión
    fun logout() {
        auth.signOut()
    }
}