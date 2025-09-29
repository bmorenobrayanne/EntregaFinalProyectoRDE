package com.example.aplicacioncamara.viewmodel

import androidx.lifecycle.*
import com.example.aplicacioncamara.model.PhotoEntity
import com.example.aplicacioncamara.repository.PhotoRepository
import kotlinx.coroutines.launch

class PhotoViewModel(private val repository: PhotoRepository) : ViewModel() {

    // 🔹 LiveData con la lista de fotos
    val photos: LiveData<List<PhotoEntity>> = repository.getAllPhotos().asLiveData()

    // 🔹 Método para guardar una foto en segundo plano
    fun savePhoto(photo: PhotoEntity) {
        viewModelScope.launch {
            repository.insertPhoto(photo)
        }
    }
}