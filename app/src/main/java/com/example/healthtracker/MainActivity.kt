package com.example.healthtracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.healthtracker.database.AppDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "health-tracker-db"
        ).allowMainThreadQueries().build()

        findViewById<Button>(R.id.profileButton).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<Button>(R.id.startWorkoutButton).setOnClickListener {
            if (db.userDao().getUser() == null) {
                // Быкануть, если профиль не заполнен
                Toast.makeText(this, "Заполните профиль перед началом тренировки",
                    Toast.LENGTH_LONG).show()
            } else {
                startActivity(Intent(this, WorkoutActivity::class.java))
            }
        }
    }
}
