package com.example.healthtracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.room.Room
import com.example.healthtracker.database.AppDatabase
import com.example.healthtracker.database.User
import com.example.healthtracker.ui.theme.HealthTrackerTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData

class WorkoutActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            HealthTrackerTheme {
                val db = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java, "health-tracker-db"
                ).allowMainThreadQueries().build()

                WorkoutScreen(db)
            }
        }
    }
}

@Composable
fun WorkoutScreen(db: AppDatabase) {
    val context = LocalContext.current
    val systemUiController = rememberSystemUiController()

    var user by remember { mutableStateOf<User?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var heartRate by remember { mutableStateOf(0) }
    var saturation by remember { mutableStateOf(0) }
    var isActive by remember { mutableStateOf(false) }
    var heartRateChart by remember { mutableStateOf<LineChart?>(null) }
    var saturationChart by remember { mutableStateOf<LineChart?>(null) }

    val fitnessDataReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    val heartRateValue = it.getIntExtra("heartRate", 0)
                    val saturationValue = it.getIntExtra("saturation", 0)
                    val isActiveValue = it.getBooleanExtra("isActive", false)
                    val data = FitnessData(heartRateValue, saturationValue, isActiveValue)

                    user?.let { u ->
                        heartRate = data.heartRate
                        saturation = data.saturation
                        isActive = data.isActive
                        evaluateHealth(data, u, mediaPlayer!!)
                        updateCharts(data, heartRateChart, saturationChart)
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        context.registerReceiver(fitnessDataReceiver, IntentFilter("FitnessDataUpdate"))
        context.startService(Intent(context, FitnessDataService::class.java))

        user = db.userDao().getUser()
        user?.let { u ->
            MockFitnessTracker.setUserParams(u.age, u.trainingLevel, u.smoking, u.drinking)
        }
        mediaPlayer = MediaPlayer.create(context, R.raw.warning_sound)
    }

    DisposableEffect(Unit) {
        onDispose {
            context.unregisterReceiver(fitnessDataReceiver)
            context.stopService(Intent(context, FitnessDataService::class.java))
            mediaPlayer?.release()
        }
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (isActive) Color(0xFF50E3C2) else Color(0xFF4A90E2)
    )

    SideEffect {
        systemUiController.setStatusBarColor(
            color = backgroundColor
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(backgroundColor, Color(0xFF50E3C2))
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
            Spacer(modifier = Modifier.height(48.dp))

            AnimatedTitle(text = "Health Tracker")

            WorkoutInfo(heartRate, saturation, isActive)

            Spacer(modifier = Modifier.height(16.dp))

            WorkoutCharts(
                onHeartRateChartCreated = { heartRateChart = it },
                onSaturationChartCreated = { saturationChart = it }
            )
        }
    }
}

@Composable
fun WorkoutInfo(heartRate: Int, saturation: Int, isActive: Boolean) {
    val heartRateColor by animateColorAsState(if (heartRate > 150) Color.Red else Color.White)
    val saturationColor by animateColorAsState(if (saturation < 93) Color.Red else Color.White)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "ЧСС",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$heartRate",
                    style = MaterialTheme.typography.displayLarge,
                    color = heartRateColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "уд/мин",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Сатурация",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$saturation",
                    style = MaterialTheme.typography.displayLarge,
                    color = saturationColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "%",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = if (isActive) listOf(Color.Green, Color.LightGray) else listOf(
                            Color.Red,
                            Color.DarkGray
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isActive) "Активен" else "Неактивен",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun WorkoutCharts(
    onHeartRateChartCreated: (LineChart) -> Unit,
    onSaturationChartCreated: (LineChart) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        AndroidView(
            factory = { ctx ->
                LineChart(ctx).apply {
                    data = LineData()
                    initializeCharts(this, "Критическая ЧСС", 180f, true)
                    onHeartRateChartCreated(this)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        AndroidView(
            factory = { ctx ->
                LineChart(ctx).apply {
                    data = LineData()
                    initializeCharts(this, "Критическая Сатурация", 90f, false)
                    onSaturationChartCreated(this)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
}
