package com.example.healthtracker.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey val id: Int = 0,
    val name: String,
    val age: Int,
    val height: Int,
    val weight: Int,
    val trainingLevel: Int,
    val smoking: Boolean,
    val drinking: Boolean
)