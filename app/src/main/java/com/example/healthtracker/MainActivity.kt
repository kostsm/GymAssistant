package com.example.healthtracker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.airbnb.lottie.compose.*
import com.example.healthtracker.database.AppDatabase
import com.example.healthtracker.ui.theme.HealthTrackerTheme
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HealthTrackerTheme {
                db = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java, "health-tracker-db"
                ).allowMainThreadQueries().build()

                MainScreen(db)
            }
        }
    }
}

@Composable
fun MainScreen(db: AppDatabase) {
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
            modifier = Modifier.fillMaxSize(),
            alpha = 0.1f
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Добавим пульсирующее сердце
                PulsingHeart()

                // Добавим пузырьки воздуха с иконкой сатурации
                BubblesWithIcon()
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                GradientButton(
                    text = "Настройка профиля",
                    onClick = { context.startActivity(Intent(context, ProfileActivity::class.java)) }
                )

                GradientButton(
                    text = "Начать тренировку",
                    onClick = {
                        if (db.userDao().getUser() == null) {
                            Toast.makeText(context, "Заполните профиль перед началом тренировки", Toast.LENGTH_LONG).show()
                        } else {
                            context.startActivity(Intent(context, WorkoutActivity::class.java))
                        }
                    }
                )
            }

            // Добавим анимации активности в нижнюю часть экрана
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp), // Увеличиваем высоту для большей анимации
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                RunningAnimation(animation = LottieCompositionSpec.RawRes(R.raw.running_1), modifier = Modifier.weight(1f))
                RunningAnimation(animation = LottieCompositionSpec.RawRes(R.raw.running_2), modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun GradientButton(text: String, onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val buttonColor by animateColorAsState(targetValue = if (isPressed) Color(0xFF00BCD4) else Color(0xFF4A90E2))
    val shadowSize by animateDpAsState(targetValue = if (isPressed) 8.dp else 16.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(elevation = shadowSize, shape = RoundedCornerShape(12.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF4A90E2), Color(0xFF50E3C2))
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable {
                isPressed = !isPressed
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}

@Composable
fun PulsingHeart() {
    val infiniteTransition = rememberInfiniteTransition()

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1500
                1.0f at 0 with LinearEasing // Стартовая точка
                1.1f at 300 with LinearEasing // Пик пульсации
                1.0f at 600 with LinearEasing // Обратно к норме
                1.1f at 900 with LinearEasing // Второй пик пульсации
                1.0f at 1500 with LinearEasing // Обратно к норме
            },
            repeatMode = RepeatMode.Restart
        )
    )

    Image(
        painter = painterResource(id = R.drawable.ic_heart),
        contentDescription = "Pulsing Heart",
        modifier = Modifier
            .size(128.dp)
            .scale(scale)
    )
}

@Composable
fun BubblesWithIcon() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(100.dp)
    ) {
        PulsingIconWithText()

        for (i in 1..5) {
            AnimatedBubble(delay = i * 300)
        }
    }
}

@Composable
fun PulsingIconWithText() {
    val infiniteTransition = rememberInfiniteTransition()

    val color by infiniteTransition.animateColor(
        initialValue = Color.Cyan,
        targetValue = Color.Blue,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
                1.0f at 0 with LinearEasing
                1.3f at 1000 with LinearEasing
                1.0f at 2000 with LinearEasing
            },
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
            .background(color, shape = CircleShape)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_bubbles),
            contentDescription = "SPo2 Icon",
            modifier = Modifier.size(80.dp)
        )
        Text(
            text = "SPo2",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun AnimatedBubble(delay: Int) {
    val infiniteTransition = rememberInfiniteTransition()

    val yOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -100f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing, delayMillis = delay),
            repeatMode = RepeatMode.Restart
        )
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing, delayMillis = delay),
            repeatMode = RepeatMode.Restart
        )
    )

    Image(
        painter = painterResource(id = R.drawable.ic_bubbles),
        contentDescription = "Bubble",
        modifier = Modifier
            .size(24.dp)
            .offset(y = yOffset.dp)
            .graphicsLayer(alpha = alpha)
    )
}

@Composable
fun RunningAnimation(animation: LottieCompositionSpec, modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(animation)
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        LottieAnimation(
            composition = composition,
            progress = progress,
            modifier = Modifier.size(200.dp)
        )
    }
}
