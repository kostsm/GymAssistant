package com.example.healthtracker

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.healthtracker.database.AppDatabase
import com.example.healthtracker.database.User

class ProfileActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var ageEditText: EditText
    private lateinit var heightEditText: EditText
    private lateinit var weightEditText: EditText
    private lateinit var trainingLevelSeekBar: SeekBar
    private lateinit var trainingLevelTextView: TextView
    private lateinit var smokingCheckBox: CheckBox
    private lateinit var drinkingCheckBox: CheckBox

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        nameEditText = findViewById(R.id.nameEditText)
        ageEditText = findViewById(R.id.ageEditText)
        heightEditText = findViewById(R.id.heightEditText)
        weightEditText = findViewById(R.id.weightEditText)
        trainingLevelSeekBar = findViewById(R.id.trainingLevelSeekBar)
        trainingLevelTextView = findViewById(R.id.trainingLevelTextView)
        smokingCheckBox = findViewById(R.id.smokingCheckBox)
        drinkingCheckBox = findViewById(R.id.drinkingCheckBox)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "health-tracker-db"
        ).allowMainThreadQueries().build()

        loadUserProfile()

        trainingLevelSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                trainingLevelTextView.text = "Тренированность: $progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        findViewById<Button>(R.id.saveProfileButton).setOnClickListener {
            saveUserProfile()
        }
    }

    private fun loadUserProfile() {
        val user = db.userDao().getUser()
        user?.let {
            nameEditText.setText(it.name)
            ageEditText.setText(it.age.toString())
            heightEditText.setText(it.height.toString())
            weightEditText.setText(it.weight.toString())
            trainingLevelSeekBar.progress = it.trainingLevel
            trainingLevelTextView.text = "Тренированность: ${it.trainingLevel}"
            smokingCheckBox.isChecked = it.smoking
            drinkingCheckBox.isChecked = it.drinking
        }
    }

    private fun saveUserProfile() {
        val user = User(
            name = nameEditText.text.toString(),
            age = ageEditText.text.toString().toInt(),
            height = heightEditText.text.toString().toInt(),
            weight = weightEditText.text.toString().toInt(),
            trainingLevel = trainingLevelSeekBar.progress,
            smoking = smokingCheckBox.isChecked,
            drinking = drinkingCheckBox.isChecked
        )
        db.userDao().insert(user)
        finish()
    }
}
