package com.example.aplicacioncamara.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uri: String,
    val latitude: Double,
    val longitude: Double,
    val dateTime: String
)