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
    fun getCuidadoresByIdsFlow(ids: List<String>): Flow<List<Cuidador>> = callbackFlow {
        val cuidadoresLista = mutableListOf<Cuidador>()
        val listeners = mutableListOf<ListenerRegistration>()

        ids.forEach { id ->
            val listener = cuidadores.document(id).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error) // Cierra el Flow si hay un error
                    return@addSnapshotListener
                }
                snapshot?.toObject(Cuidador::class.java)?.let { cuidador ->
                    cuidadoresLista.add(cuidador)
                    trySend(cuidadoresLista.toList()) // Emite la lista actualizada
                }
            }
            listeners.add(listener)
        }

        awaitClose {
            listeners.forEach { it.remove() } // Remueve todos los listeners al cerrar el Flow
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
}

object FirestoreProvider {
    val instance: FirebaseFirestore = Firebase.firestore
}