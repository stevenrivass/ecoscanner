package com.example.ecoscanner.data.repository

import com.example.ecoscanner.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun currentUser(): User? {
        val fbUser = auth.currentUser ?: return null
        return User(
            uid = fbUser.uid,
            email = fbUser.email ?: ""
        )
    }

    // Comprueba si el usuario actual tiene el email verificado
    fun isEmailVerified(): Boolean {
        return auth.currentUser?.isEmailVerified == true
    }

    // Recarga los datos del usuario desde Firebase (importante para detectar verificación)
    suspend fun reloadUser(): Result<Boolean> {
        return try {
            auth.currentUser?.reload()?.await()
            Result.success(auth.currentUser?.isEmailVerified == true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    // Registro: crea cuenta + envía email de verificación automáticamente
    suspend fun register(email: String, password: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val fbUser = result.user
                ?: return Result.failure(Exception("No s'ha pogut crear l'usuari"))

            // Enviamos email de verificación inmediatamente
            fbUser.sendEmailVerification().await()

            Result.success(User(fbUser.uid, fbUser.email ?: ""))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Reenvía el email de verificación (por si el primero no llegó o caducó)
    suspend fun resendVerificationEmail(): Result<Unit> {
        return try {
            val user = auth.currentUser
                ?: return Result.failure(Exception("No hi ha sessió activa"))
            user.sendEmailVerification().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun getDisplayName(): String? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val doc = db.collection("users").document(uid).get().await()
            doc.getString("displayName")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun setDisplayName(name: String): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(Exception("No hay sesión activa."))
        return try {
            db.collection("users")
                .document(uid)
                .set(mapOf("displayName" to name), SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}