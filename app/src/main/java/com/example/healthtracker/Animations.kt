package com.example.healthtracker

import android.icu.number.Scale
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*

@Composable
fun AnimatedTitle(text: String, scale: Float = 1f) {
    val infiniteTransition = rememberInfiniteTransition()
    val color by infiniteTransition.animateColor(
        initialValue = Color.White,
        targetValue = Color(0xFF50E3C2),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Text(
        text = text,
        color = color,
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(vertical = 16.dp)
            .scale(scale)
    )
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
        ), label = ""
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
        ), label = ""
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 5000
                1.0f at 0 with LinearEasing
                1.3f at 2500 with LinearEasing
                1.0f at 5000 with LinearEasing
            },
            repeatMode = RepeatMode.Restart
        ), label = ""
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
        ), label = ""
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing, delayMillis = delay),
            repeatMode = RepeatMode.Restart
        ), label = ""
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
