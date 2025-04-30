package com.example.proyecto.ui.elderlyScreens


import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.camera.core.Preview

import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat


import androidx.navigation.NavController
import com.example.proyecto.ui.viewmodel.SharedImageViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CamaraScreen(navController: NavController, sharedImageViewModel: SharedImageViewModel){
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    LaunchedEffect(Unit) {
        cameraPermissionState.launchPermissionRequest()

    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Camara")

        Spacer(modifier = Modifier.padding(15.dp))

        if (!cameraPermissionState.status.isGranted) {
            Text("Se necesita permiso de la cámara para continuar.")
            return
        }else{
            CameraFoto(cameraPermissionState,navController,sharedImageViewModel)

        }

    }
}

//////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////

//FOTO

//////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraFoto(cameraPermissionState: PermissionState, navController: NavController, sharedImageViewModel: SharedImageViewModel){
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current


    // Estado para almacenar la imagen capturada
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }

    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraExecutor = remember { ContextCompat.getMainExecutor(context) }

    LaunchedEffect(previewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraXScreen", "Error al inicializar CameraX", e)
            }
        }, cameraExecutor)
    }
    LaunchedEffect(capturedImage) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraXScreen", "Error al inicializar CameraX", e)
            }
        }, cameraExecutor)
    }
    fun takePhoto(executor: Executor) {
        val photoFile = File.createTempFile(
            "photo_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())}",
            ".jpg",
            context.externalCacheDir
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    capturedImage = BitmapFactory.decodeFile(photoFile.absolutePath)
                    saveImageToGallery(context, capturedImage!!) //se usa !! porque capturedImage puede ser nulo, y al poner esto obligara a que haya algo
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraXScreen", "Error al capturar la foto", exc)
                }
            }
        )
    }

    val pickMedia = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                capturedImage = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                Log.d("PhotoPicker", "Selected URI: $uri")
            } catch (e: Exception) {
                Log.e("PhotoPicker", "Error decoding image: ${e.message}", e)
            }
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }
    Column(
        modifier = Modifier.size(width = 250.dp, height = 450.dp),
        verticalArrangement = Arrangement.SpaceBetween) { // Use a Column to manage vertical layout
        Spacer(modifier = Modifier.padding(15.dp))
        if(capturedImage != null){
            Image(
                bitmap = capturedImage!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            )
        }else{
            Box(
                modifier = Modifier
                    .weight(1f) //
                    .fillMaxSize()//
            ) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.padding(35.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp), // Add some bottom padding to the Row
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            if (capturedImage == null) {
                Button(
                    onClick = { takePhoto(cameraExecutor) },
                ) {
                    Text(text = "Tomar Foto")
                }
            }else{

                Button(
                    onClick = { capturedImage = null },
                ) {
                    Text(text = "tomar foto")
                }
            }
            Spacer(modifier = Modifier.padding(15.dp))
            Button(
                onClick = {

                    pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))

                }
            ) {
                Text(text = "foto de galeria")

            }

        }
        Row(
            modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp), // Add some bottom padding to the Row
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom) {

                Button(
                onClick = {

                    sharedImageViewModel.capturedImage = capturedImage
                    navController.popBackStack()

                }) { Text(text = "Cargar la foto") }
        }

    }
}

// Función para guardar la imagen en la galería
fun saveImageToGallery(context: Context, bitmap: Bitmap) {
    val imageUri = MediaStore.Images.Media.insertImage(
        context.contentResolver,
        bitmap,
        "Photo_${System.currentTimeMillis()}",
        "Foto tomada con CameraX"
    )

    if (imageUri != null) {
        Log.d("CameraXScreen", "Imagen guardada en la galería: $imageUri")
    } else {
        Log.e("CameraXScreen", "Error al guardar la imagen en la galería")
    }


}


