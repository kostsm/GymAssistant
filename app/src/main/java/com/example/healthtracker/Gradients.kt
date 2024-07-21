package com.example.healthtracker

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
