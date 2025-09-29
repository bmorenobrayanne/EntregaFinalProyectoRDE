package com.example.aplicacioncamara.view

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicacioncamara.R
import com.example.aplicacioncamara.model.AppDatabase
import com.example.aplicacioncamara.model.PhotoEntity
import com.example.aplicacioncamara.repository.PhotoRepository
import com.example.aplicacioncamara.viewmodel.PhotoViewModel
import com.example.aplicacioncamara.viewmodel.PhotoViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var photoViewModel: PhotoViewModel
    private lateinit var cameraLauncher: ActivityResultLauncher<Void?>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var photoAdapter: PhotoAdapter
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 🔹 Inicializar ViewModel
        val dao = AppDatabase.getDatabase(applicationContext).photoDao()
        val repository = PhotoRepository(dao)
        val factory = PhotoViewModelFactory(repository)
        photoViewModel = factory.create(PhotoViewModel::class.java)

        // 🔹 Inicializar FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 🔹 Configurar RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewPhotos)
        photoAdapter = PhotoAdapter(emptyList())
        recyclerView.adapter = photoAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 🔹 Observar LiveData del ViewModel
        photoViewModel.photos.observe(this) { photos ->
            photoAdapter.setPhotos(photos)
        }

        // 🔹 Configurar el ActivityResultLauncher
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                val uri = saveBitmapToInternalStorage(it)

                // Obtener ubicación y solo después guardar la foto
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.

                }
                fusedLocationClient.getCurrentLocation(
                    com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY,
                    null
                ).addOnSuccessListener { location ->
                    val dateTime = obtenerFechaHoraActual()
                    val lat = location?.latitude ?: 0.0
                    val lon = location?.longitude ?: 0.0

                    val photoEntity = PhotoEntity(
                        uri = uri.toString(),
                        latitude = lat,
                        longitude = lon,
                        dateTime = dateTime
                    )
                    photoViewModel.savePhoto(photoEntity)
                }
            }
        }

        // 🔹 Botón para tomar foto
        val btnTakePhoto = findViewById<Button>(R.id.btnTakePhoto)
        btnTakePhoto.setOnClickListener { openCamera() }

        // 🔹 Solicitar permisos al iniciar la app
        solicitarPermisos()
    }

    // 🔹 Método para abrir cámara
    private fun openCamera() {
        cameraLauncher.launch(null)
    }

    // 🔹 Guardar Bitmap en almacenamiento interno y devolver URI
    private fun saveBitmapToInternalStorage(bitmap: Bitmap): Uri {
        val filename = "IMG_${System.currentTimeMillis()}.jpg"
        val file = File(filesDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
        return Uri.fromFile(file)
    }

    // 🔹 Obtener ubicación actual
    private fun obtenerUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            solicitarPermisos()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                latitude = it.latitude
                longitude = it.longitude
            }
        }
    }

    // 🔹 Obtener fecha y hora actual
    private fun obtenerFechaHoraActual(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }

    // 🔹 Solicitar permisos en tiempo de ejecución
    private fun solicitarPermisos() {
        val permisos = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val permisosNoOtorgados = permisos.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permisosNoOtorgados.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permisosNoOtorgados.toTypedArray(), 100)
        }
    }

    // 🔹 Manejar la respuesta de permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            // Puedes agregar lógica si algún permiso fue denegado
        }
    }
}
