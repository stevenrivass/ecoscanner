package com.example.ecoscanner.data.repository

import com.example.ecoscanner.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    fun logout() {
        auth.signOut()
    }

    // ---------- Nombre del usuario (Firestore) ----------

    // Devuelve el nombre guardado, o null si no tiene
    suspend fun getDisplayName(): String? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val doc = db.collection("users").document(uid).get().await()
            doc.getString("displayName")
        } catch (e: Exception) {
            null
        }
    }

    // Guarda o actualiza el nombre del usuario
    suspend fun setDisplayName(name: String): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(Exception("No hay sesión activa."))
        return try {
            db.collection("users")
                .document(uid)
                .set(mapOf("displayName" to name), com.google.firebase.firestore.SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}