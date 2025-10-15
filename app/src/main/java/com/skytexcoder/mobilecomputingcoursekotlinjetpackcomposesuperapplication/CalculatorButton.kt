package com.skytexcoder.mobilecomputingcoursekotlinjetpackcomposesuperapplication

import android.view.SoundEffectConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skytexcoder.mobilecomputingcoursekotlinjetpackcomposesuperapplication.ui.theme.Cyan
import com.skytexcoder.mobilecomputingcoursekotlinjetpackcomposesuperapplication.ui.theme.Red
import androidx.compose.runtime.getValue
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay

data class CalculatorButton(
    val text: String? = null,
    val type: CalculatorButtonType,
    val icon: ImageVector? = null,
)

enum class CalculatorButtonType {
    NORMAL, ACTION, RESET, CLEAR, SCIENTIFIC_TRIGONOMETRY, SCIENTIFIC_ACTION
}

@Composable
fun CalculatorClickableButton(button: CalculatorButton, onClick: () -> Unit, modifier: Modifier = Modifier) {

    val view = LocalView.current

    val interactionSource = remember {
        MutableInteractionSource()
    }

    val isPressed by interactionSource.collectIsPressedAsState()

    if (button.type == CalculatorButtonType.CLEAR) {
        LaunchedEffect(isPressed) {
            if (isPressed) {
                val initialDelay = 500L
                val repeatDelay = 100L

                delay(initialDelay)

                while (true) {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onClick()
                    delay(repeatDelay)
                }
            }
        }
    }

    Box(modifier = Modifier
        .clip(RoundedCornerShape(16.dp))
        .background(MaterialTheme.colorScheme.secondary)
        .fillMaxHeight()
        .aspectRatio(1f)
        .then(
            if (button.type == CalculatorButtonType.CLEAR) {
                Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            onClick()
                            val press = PressInteraction.Press(it)
                            interactionSource.emit(press)
                            tryAwaitRelease()
                            interactionSource.emit(PressInteraction.Release(press))
                        }
                    )
                }
            } else {
                Modifier.clickable {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onClick()
                }
            }
        ),
        contentAlignment = Alignment.Center
    ) {
        val contentColor = when (button.type) {
            CalculatorButtonType.NORMAL -> MaterialTheme.colorScheme.onSecondary
            CalculatorButtonType.ACTION -> Cyan
            CalculatorButtonType.RESET -> Red
            CalculatorButtonType.CLEAR -> Red
            CalculatorButtonType.SCIENTIFIC_TRIGONOMETRY -> Color(0xFFFFA500)
            CalculatorButtonType.SCIENTIFIC_ACTION -> Color(0xFF00FF00)
        }
        if (button.icon != null) {
            Icon(modifier = Modifier.size(32.dp), imageVector = button.icon, contentDescription = null, tint = contentColor)
        } else {
            Text(
                text = button.text!!,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = when (button.type) {
                    CalculatorButtonType.ACTION -> 25.sp
                    CalculatorButtonType.SCIENTIFIC_ACTION -> 20.sp
                    CalculatorButtonType.SCIENTIFIC_TRIGONOMETRY -> 20.sp
                    else -> 20.sp
                }
            )
        }
    }
}



