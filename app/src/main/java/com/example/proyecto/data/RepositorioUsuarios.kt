package com.example.proyecto.data

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import android.util.Log

// Carga a ancianos y cuidadores de firestore
class RepositorioUsuarios (
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val cuidadores = firestore.collection("cuidadores")
    private val ancianos = firestore.collection("ancianos")

    suspend fun getCuidadores(): List<Usuario> {
        return cuidadores.get().await().documents.mapNotNull { it.toObject(Usuario::class.java) }
    }

    suspend fun getAncianos(): List<Usuario> {
        return ancianos.get().await().documents.mapNotNull { it.toObject(Usuario::class.java) }
    }

    /*
    Un Flow<T> es una secuencia de valores de tipo T que se emiten
    de forma asíncrona y solo se ejecuta cuando alguien lo colecciona
    (collect) en una corrutina
    */

    // Obtener la lista de cuidadores asociados a un anciano
    fun getCuidadoresPorAncianoIdFlow(ancianoId: String): Flow<List<Cuidador>> = callbackFlow {
        // Observar el documento del anciano
        val ancianoListener = ancianos.document(ancianoId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                // Obtener la lista actualizada de IDs de cuidadores
                val cuidadoresIds = snapshot?.toObject(Anciano::class.java)?.cuidadoresIds ?: emptyList()
                val cuidadoresLista = mutableListOf<Cuidador>()

                // Limpiar listeners anteriores si existieran
                trySend(emptyList())

                // Crear nuevos listeners para cada cuidador
                cuidadoresIds.forEach { id ->
                    cuidadores.document(id)
                        .get()
                        .addOnSuccessListener { documento ->
                            documento.toObject(Cuidador::class.java)?.let { cuidador ->
                                cuidadoresLista.add(cuidador)
                                trySend(cuidadoresLista.toList())
                            }
                        }
                }
            }

        awaitClose {
            ancianoListener.remove()
        }
    }

    // Obtener la lista de ancianos asociados a un cuidador
    fun getAncianosByIdsFlow(ids: List<String>): Flow<List<Usuario>> = callbackFlow {
        val ancianosLista = mutableListOf<Usuario>()
        val listeners = mutableListOf<ListenerRegistration>()

        ids.forEach { id ->
            val listener = ancianos.document(id).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error) // Cierra el Flow si hay un error
                    return@addSnapshotListener
                }
                snapshot?.toObject(Usuario::class.java)?.let { anciano ->
                    ancianosLista.add(anciano)
                    trySend(ancianosLista.toList()) // Emite la lista actualizada
                }
            }
            listeners.add(listener)
        }

        awaitClose {
            listeners.forEach { it.remove() } // Remueve todos los listeners al cerrar el Flow
        }
    }

    suspend fun buscarCuidadorPorEmail(email: String): String {
        return try {
            val snapshot = cuidadores
                .whereEqualTo("email", email)
                .get()
                .await()

            snapshot.documents.firstOrNull()?.id ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun agregarCuidadorAnciano(ancianoId: String, cuidadorId: String): Boolean {
        return try {
            val ancianoRef = ancianos.document(ancianoId)
            val cuidadorRef = cuidadores.document(cuidadorId)

            // Obtener documentos
            val ancianoDoc = ancianoRef.get().await()
            val cuidadorDoc = cuidadorRef.get().await()

            if (ancianoDoc.exists() && cuidadorDoc.exists()) {
                val anciano = ancianoDoc.toObject(Anciano::class.java)
                val cuidador = cuidadorDoc.toObject(Cuidador::class.java)

                val cuidadoresIds = anciano?.cuidadoresIds?.toMutableList() ?: mutableListOf()
                val ancianosIds = cuidador?.ancianosIds?.toMutableList() ?: mutableListOf()

                // Verificar si la relación ya existe
                if (!cuidadoresIds.contains(cuidadorId) && !ancianosIds.contains(ancianoId)) {
                    // Actualizar ambas listas
                    cuidadoresIds.add(cuidadorId)
                    ancianosIds.add(ancianoId)

                    // Actualizar ambos documentos en una transacción
                    firestore.runTransaction { transaction ->
                        transaction.update(ancianoRef, "cuidadoresIds", cuidadoresIds)
                        transaction.update(cuidadorRef, "ancianosIds", ancianosIds)
                    }.await()

                    true
                } else {
                    false // La relación ya existe
                }
            } else {
                false // Alguno de los documentos no existe
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun actualizarEstadoConexion(usuarioId: String, esAnciano: Boolean, conectado: Boolean) {
        val coleccion = if (esAnciano) ancianos else cuidadores
        try {
            coleccion.document(usuarioId)
                .update("conectado", conectado)
                .await()
        } catch (e: Exception) {
            throw Exception("Error al actualizar estado de conexión: ${e.message}")
        }
    }

    suspend fun actualizarUbicacionAnciano(uid: String, latLng: LatLng) {
        val geo = GeoPoint(latLng.latitude, latLng.longitude)
        try {
            ancianos.document(uid)
                .update(
                    mapOf(
                        "latLng" to geo,
                        "timestamp" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Log.i("RepositorioUsuarios", "Ubicación actualizada correctamente en Firebase: $latLng")
        } catch (e: Exception) {
            Log.e("RepositorioUsuarios", "Error al actualizar ubicación en Firebase: ${e.message}")
            throw Exception("Error al actualizar ubicación: ${e.message}")
        }
    }

    suspend fun actualizarUbicacionCuidador(uid: String, latLng: LatLng) {
        val geo = GeoPoint(latLng.latitude, latLng.longitude)
        try {
            cuidadores.document(uid)
                .update(
                    mapOf(
                        "latLng" to geo,
                        "timestamp" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Log.i("RepositorioUsuarios", "Ubicación del cuidador actualizada correctamente en Firebase: $latLng")
        } catch (e: Exception) {
            Log.e("RepositorioUsuarios", "Error al actualizar ubicación del cuidador en Firebase: ${e.message}")
            throw Exception("Error al actualizar ubicación del cuidador: ${e.message}")
        }
    }

    fun getCuidadoresConectadosPorAncianoIdFlow(ancianoId: String): Flow<List<Cuidador>> = callbackFlow {
        var cuidadoresListener: ListenerRegistration? = null

        val ancianoListener = ancianos.document(ancianoId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("RepositorioUsuarios", "Error al observar anciano: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                // Obtener la lista de IDs de cuidadores del anciano
                val cuidadoresIds = snapshot?.toObject(Anciano::class.java)?.cuidadoresIds ?: emptyList()

                if (cuidadoresIds.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                // Remover el listener anterior si existe
                cuidadoresListener?.remove()

                // Crear una consulta para observar los cuidadores conectados
                val cuidadoresQuery = cuidadores
                    .whereIn("userID", cuidadoresIds)
                    .whereEqualTo("conectado", true)

                // Crear nuevo listener para cuidadores
                cuidadoresListener = cuidadoresQuery.addSnapshotListener { cuidadoresSnapshot, cuidadoresError ->
                    if (cuidadoresError != null) {
                        Log.e("RepositorioUsuarios", "Error al observar cuidadores: ${cuidadoresError.message}")
                        return@addSnapshotListener
                    }

                    val cuidadoresConectados = cuidadoresSnapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Cuidador::class.java)
                    } ?: emptyList()

                    Log.d("RepositorioUsuarios", "Cuidadores conectados encontrados: ${cuidadoresConectados.size}")
                    trySend(cuidadoresConectados)
                }
            }

        // Limpiar todos los listeners cuando se cierre el Flow
        awaitClose {
            Log.d("RepositorioUsuarios", "Cerrando listeners")
            ancianoListener.remove()
            cuidadoresListener?.remove()
        }
    }

    /**
     * Obtiene los ancianos conectados relacionados con un cuidador específico.
     */
    fun getAncianosConectadosPorCuidadorIdFlow(cuidadorId: String): Flow<List<Usuario>> = callbackFlow {
        var ancianosListener: ListenerRegistration? = null

        val cuidadorListener = cuidadores.document(cuidadorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("RepositorioUsuarios", "Error al observar cuidador: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                // Obtener la lista de IDs de ancianos del cuidador
                val ancianosIds = snapshot?.toObject(Cuidador::class.java)?.ancianosIds ?: emptyList()

                if (ancianosIds.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                // Remover el listener anterior si existe
                ancianosListener?.remove()

                // Crear una consulta para observar los ancianos conectados
                val ancianosQuery = ancianos
                    .whereIn("userID", ancianosIds)
                    .whereEqualTo("conectado", true)

                // Crear nuevo listener para ancianos
                ancianosListener = ancianosQuery.addSnapshotListener { ancianosSnapshot, ancianosError ->
                    if (ancianosError != null) {
                        Log.e("RepositorioUsuarios", "Error al observar ancianos: ${ancianosError.message}")
                        return@addSnapshotListener
                    }

                    val ancianosConectados = ancianosSnapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Usuario::class.java)
                    } ?: emptyList()

                    Log.d("RepositorioUsuarios", "Ancianos conectados encontrados: ${ancianosConectados.size}")
                    trySend(ancianosConectados)
                }
            }

        // Limpiar todos los listeners cuando se cierre el Flow
        awaitClose {
            Log.d("RepositorioUsuarios", "Cerrando listeners")
            cuidadorListener.remove()
            ancianosListener?.remove()
        }
    }
}

object FirestoreProvider {
    val instance: FirebaseFirestore = Firebase.firestore
}
