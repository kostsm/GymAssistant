package com.example.healthtracker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.example.healthtracker.database.AppDatabase
import com.example.healthtracker.ui.theme.HealthTrackerTheme

class MainActivity : ComponentActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Устанавливаем цвет статус бара
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.parseColor("#4A90E2")

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "health-tracker-db"
        ).build()

        val viewModelFactory = MainViewModelFactory(application)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        setContent {
            HealthTrackerTheme {
                MainScreen(viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
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
        Image(
            painter = painterResource(id = R.drawable.background_athlete),
            contentDescription = "Background Athlete",
            modifier = Modifier
                .fillMaxSize()
                .scale(1.2f),  // Увеличиваем размер фона
            alpha = 0.1f
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(48.dp)) // Смещаем название ниже

            AnimatedTitle("Health Tracker")

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Пульсирующее сердце
                PulsingHeart()

                // Пузырьки воздуха с иконкой сатурации
                BubblesWithIcon()
            }

            Spacer(modifier = Modifier.height(48.dp)) // Добавляем пространство перед кнопками

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                GradientButton(
                    text = "Настройка профиля",
                    icon = R.drawable.ic_profile,
                    onClick = { context.startActivity(Intent(context, ProfileActivity::class.java)) }
                )

                GradientButton(
                    text = "Начать тренировку",
                    icon = R.drawable.ic_workout,
                    onClick = {
                        if (viewModel.user == null) {
                            Toast.makeText(context, "Заполните профиль перед началом тренировки", Toast.LENGTH_LONG).show()
                        } else {
                            context.startActivity(Intent(context, WorkoutActivity::class.java))
                        }
                    }
                )
            }

            // Бегущий пёс-капец и мужик внизу экрана
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                RunningAnimation(animation = LottieCompositionSpec.RawRes(R.raw.running_1), modifier = Modifier.weight(1f))
                RunningAnimation(animation = LottieCompositionSpec.RawRes(R.raw.running_2), modifier = Modifier.weight(1f))
            }
        }
    }
}
