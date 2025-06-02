package com.example.proyecto.data

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

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
}

object FirestoreProvider {
    val instance: FirebaseFirestore = Firebase.firestore
}