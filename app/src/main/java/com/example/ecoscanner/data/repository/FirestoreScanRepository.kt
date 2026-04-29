package com.example.ecoscanner.data.repository

import com.example.ecoscanner.model.ScanRecord
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreScanRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    // Devuelve el UID del usuario actual o null si no hay sesión
    private fun uid(): String? = auth.currentUser?.uid

    // Guarda un escaneo en users/{uid}/scans
    suspend fun saveScan(record: ScanRecord): Result<Unit> {
        val userId = uid()
            ?: return Result.failure(Exception("No hay sesión activa."))

        return try {
            // Forzamos el timestamp del servidor
            val toSave = record.copy(scannedAt = Timestamp.now())

            db.collection("users")
                .document(userId)
                .collection("scans")
                .add(toSave)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Devuelve TODOS los escaneos del usuario actual, del más reciente al más antiguo
    suspend fun getAllScans(): Result<List<ScanRecord>> {
        val userId = uid()
            ?: return Result.failure(Exception("No hay sesión activa."))

        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("scans")
                .orderBy("scannedAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val list = snapshot.documents.mapNotNull { doc ->
                doc.toObject(ScanRecord::class.java)
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}