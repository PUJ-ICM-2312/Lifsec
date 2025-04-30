package com.example.proyecto.data.location

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder

class RoutesService {
    //Colocar API directions
    private val apiKey = "API_MAPS_KEY"


    suspend fun getRoutePoints(origin: LatLng, destination: LatLng): List<LatLng> {
        return withContext(Dispatchers.IO) {
            try {
                val urlString = "https://maps.googleapis.com/maps/api/directions/json?" +
                        "origin=${origin.latitude},${origin.longitude}" +
                        "&destination=${destination.latitude},${destination.longitude}" +
                        "&key=$apiKey" +
                        "&mode=driving"

                Log.d("DirectionsAPI", "Requesting route from $origin to $destination")
                val jsonResponse = URL(urlString).readText()
                Log.d("DirectionsAPI", "Response received: ${jsonResponse.take(100)}...")
                parseRouteFromJson(jsonResponse)

            } catch (e: Exception) {
                Log.e("DirectionsAPI", "Error fetching directions", e)
                e.printStackTrace()
                emptyList()
            }
        }
    }

    private fun parseRouteFromJson(jsonResponse: String): List<LatLng> {
        val routePoints = mutableListOf<LatLng>()

        try {
            val jsonObject = JSONObject(jsonResponse)

            // Verificar si la respuesta es exitosa
            val status = jsonObject.getString("status")
            if (status != "OK") {
                return emptyList()
            }

            // Obtener la primera ruta
            val routes = jsonObject.getJSONArray("routes")
            if (routes.length() == 0) {
                return emptyList()
            }

            val route = routes.getJSONObject(0)
            val legs = route.getJSONArray("legs")

            // Procesar cada "leg" (tramo) de la ruta
            for (i in 0 until legs.length()) {
                val leg = legs.getJSONObject(i)
                val steps = leg.getJSONArray("steps")

                // Procesar cada paso del tramo
                for (j in 0 until steps.length()) {
                    val step = steps.getJSONObject(j)

                    // Añadir el punto de inicio del paso
                    val startLocation = step.getJSONObject("start_location")
                    val startLat = startLocation.getDouble("lat")
                    val startLng = startLocation.getDouble("lng")
                    routePoints.add(LatLng(startLat, startLng))

                    // Decodificar los puntos de polyline para obtener todos los puntos intermedios
                    val polyline = step.getJSONObject("polyline").getString("points")
                    routePoints.addAll(decodePolyline(polyline))

                    // Añadir el punto final del paso
                    val endLocation = step.getJSONObject("end_location")
                    val endLat = endLocation.getDouble("lat")
                    val endLng = endLocation.getDouble("lng")
                    routePoints.add(LatLng(endLat, endLng))
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return routePoints
    }

    // Función para decodificar el formato polyline de Google Maps
    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0

            do {
                b = encoded[index++].toInt() - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)

            val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lat += dlat

            shift = 0
            result = 0

            do {
                b = encoded[index++].toInt() - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)

            val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lng += dlng

            val p = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
            poly.add(p)
        }

        return poly
    }
}