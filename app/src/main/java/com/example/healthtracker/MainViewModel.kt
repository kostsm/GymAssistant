package com.example.healthtracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.database.AppDatabase
import com.example.healthtracker.database.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)

    var user: User? = null
        private set

    init {
        viewModelScope.launch {
            user = loadUser()
        }
    }

    private suspend fun loadUser(): User? {
        return withContext(Dispatchers.IO) {
            db.userDao().getUser()
        }
    }
}
