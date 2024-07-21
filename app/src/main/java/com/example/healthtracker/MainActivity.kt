package com.example.healthtracker

import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.room.Room
import com.airbnb.lottie.compose.*
import com.example.healthtracker.database.AppDatabase
import com.example.healthtracker.ui.theme.HealthTrackerTheme

class MainActivity : ComponentActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Устанавливаем цвет статус бара
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.parseColor("#4A90E2")

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
                        if (db.userDao().getUser() == null) {
                            Toast.makeText(context, "Заполните профиль перед началом тренировки", Toast.LENGTH_LONG).show()
                        } else {
                            context.startActivity(Intent(context, WorkoutActivity::class.java))
                        }
                    }
                )
            }

            // Бегущие анимации в нижней части экрана
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


@Composable
fun AnimatedTitle(text: String) {
    val infiniteTransition = rememberInfiniteTransition()
    val color by infiniteTransition.animateColor(
        initialValue = Color.White,
        targetValue = Color(0xFF50E3C2),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Text(
        text = text,
        color = color,
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@Composable
fun GradientButton(text: String, icon: Int, iconSize: Dp = 32.dp, isIconColored: Boolean = false, onClick: () -> Unit) {
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 16.dp)
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                colorFilter = if (isIconColored) null else androidx.compose.ui.graphics.ColorFilter.tint(Color.White),
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier.weight(1f), // Это позволит тексту занимать оставшееся пространство
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun PulsingHeart() {
    val infiniteTransition = rememberInfiniteTransition()

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        // Гачи анимация через key фреймы, тк сердце бьется нелинейно
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1500
                1.0f at 0 with LinearEasing
                1.1f at 300 with LinearEasing
                1.0f at 600 with LinearEasing
                1.1f at 900 with LinearEasing
                1.0f at 1500 with LinearEasing
            },
            repeatMode = RepeatMode.Restart
        )
    )

    Image(
        painter = painterResource(id = R.drawable.ic_heart),
        contentDescription = "Pulsing Heart",
        modifier = Modifier
            .size(92.dp)
            .scale(scale)
    )
}

@Composable
fun BubblesWithIcon() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(64.dp)
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
            animation = tween(5000, easing = LinearEasing),
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
            .size(92.dp)
            .scale(scale)
            .background(color, shape = CircleShape)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_bubbles),
            contentDescription = "SpO2 Icon",
            modifier = Modifier.size(88.dp)
        )
        Text(
            text = "SpO2",
            color = Color.White,
            fontSize = 14.sp,
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
            .size(32.dp)
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
            // Увеличиваем размер анимации, пушто мелкая
            modifier = Modifier.size(150.dp)
        )
    }
}
