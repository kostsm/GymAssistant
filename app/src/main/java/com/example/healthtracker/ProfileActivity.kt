package com.example.healthtracker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.room.Room
import com.example.healthtracker.database.AppDatabase
import com.example.healthtracker.database.User
import com.example.healthtracker.ui.theme.HealthTrackerTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class ProfileActivity : ComponentActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false) // This line enables drawing behind the status bar
        setContent {
            HealthTrackerTheme {
                db = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java, "health-tracker-db"
                ).allowMainThreadQueries().build()

                ProfileScreen(db)
            }
        }
    }
}

@Composable
fun ProfileScreen(db: AppDatabase) {
    val context = LocalContext.current
    val view = LocalView.current
    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color(0xFF4A90E2)
        )
    }

    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var trainingLevel by remember { mutableStateOf(0) }
    var smoking by remember { mutableStateOf(false) }
    var drinking by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val user = db.userDao().getUser()
        user?.let {
            name = it.name
            age = it.age.toString()
            height = it.height.toString()
            weight = it.weight.toString()
            trainingLevel = it.trainingLevel
            smoking = it.smoking
            drinking = it.drinking
        }
    }

    fun saveUserProfile() {
        if (name.isBlank() || age.isBlank() || height.isBlank() || weight.isBlank()) {
            Toast.makeText(context, "Все поля должны быть заполнены", Toast.LENGTH_LONG).show()
        } else {
            val user = User(
                name = name,
                age = age.toInt(),
                height = height.toInt(),
                weight = weight.toInt(),
                trainingLevel = trainingLevel,
                smoking = smoking,
                drinking = drinking
            )
            db.userDao().insert(user)
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF4A90E2), Color(0xFF50E3C2))
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(48.dp)) // Смещаем название ниже

            val infiniteTransition = rememberInfiniteTransition()
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000),
                    repeatMode = RepeatMode.Reverse
                ), label = ""
            )

            AnimatedTitle(text = "Health Tracker", scale)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Имя") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Возраст") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Рост") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Вес") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            Text(
                text = "Тренированность: $trainingLevel",
                style = MaterialTheme.typography.bodyLarge
            )
            Slider(
                value = trainingLevel.toFloat(),
                onValueChange = { trainingLevel = it.toInt() },
                valueRange = 0f..3f,
                steps = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = smoking, onCheckedChange = { smoking = it })
                    Text("Курение")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = drinking, onCheckedChange = { drinking = it })
                    Text("Алкоголь")
                }
            }

            GradientButton(
                text = "Сохранить профиль",
                onClick = { saveUserProfile() },
                isIconColored = true,
                icon = R.drawable.ic_save,
            )
        }
    }
}
