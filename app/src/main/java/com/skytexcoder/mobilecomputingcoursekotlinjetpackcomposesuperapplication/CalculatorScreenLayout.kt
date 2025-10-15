package com.skytexcoder.mobilecomputingcoursekotlinjetpackcomposesuperapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

// CalculatorButtonType enum and CalculatorButton data class (as defined above or in their own file)
// enum class CalculatorButtonType { NORMAL, ACTION, RESET, CLEAR }
// data class CalculatorButton(val text: String, val type: CalculatorButtonType, val icon: ImageVector? = null)

val buttonList = listOf(
    "1/x", "sin", "cos", "tan",
    "x!", "sin⁻¹", "cos⁻¹", "tan⁻¹",
    "xʸ", "√x", "log", "ln",
    "BACKSPACE", "(", ")", "÷",
    "7", "8", "9", "×",
    "4", "5", "6", "+",
    "1", "2", "3", "-",
    "AC", "0", ".", "="
)

val buttonTypeMap: HashMap<String, CalculatorButtonType> = hashMapOf(
    "AC" to CalculatorButtonType.RESET,
    "BACKSPACE" to CalculatorButtonType.CLEAR,
    "0" to CalculatorButtonType.NORMAL,
    "1" to CalculatorButtonType.NORMAL,
    "2" to CalculatorButtonType.NORMAL,
    "3" to CalculatorButtonType.NORMAL,
    "4" to CalculatorButtonType.NORMAL,
    "5" to CalculatorButtonType.NORMAL,
    "6" to CalculatorButtonType.NORMAL,
    "7" to CalculatorButtonType.NORMAL,
    "8" to CalculatorButtonType.NORMAL,
    "9" to CalculatorButtonType.NORMAL,
    "." to CalculatorButtonType.NORMAL,
    "(" to CalculatorButtonType.NORMAL,
    ")" to CalculatorButtonType.NORMAL,
    "+" to CalculatorButtonType.ACTION,
    "-" to CalculatorButtonType.ACTION,
    "×" to CalculatorButtonType.ACTION,
    "÷" to CalculatorButtonType.ACTION,
    "=" to CalculatorButtonType.ACTION,
    "sin" to CalculatorButtonType.SCIENTIFIC_TRIGONOMETRY,
    "cos" to CalculatorButtonType.SCIENTIFIC_TRIGONOMETRY,
    "tan" to CalculatorButtonType.SCIENTIFIC_TRIGONOMETRY,
    "sin⁻¹" to CalculatorButtonType.SCIENTIFIC_TRIGONOMETRY,
    "cos⁻¹" to CalculatorButtonType.SCIENTIFIC_TRIGONOMETRY,
    "tan⁻¹" to CalculatorButtonType.SCIENTIFIC_TRIGONOMETRY,
    "log" to CalculatorButtonType.SCIENTIFIC_ACTION,
    "ln" to CalculatorButtonType.SCIENTIFIC_ACTION,
    "xʸ" to CalculatorButtonType.SCIENTIFIC_ACTION,
    "x!" to CalculatorButtonType.SCIENTIFIC_ACTION,
    "√x" to CalculatorButtonType.SCIENTIFIC_ACTION,
    "1/x" to CalculatorButtonType.SCIENTIFIC_ACTION,
)

val buttonIconsMap: HashMap<String, ImageVector> = hashMapOf(
    "BACKSPACE" to Icons.AutoMirrored.Filled.Backspace,
)

@Composable
fun ScientificCalculatorApplicationScreenLayout(modifier: Modifier = Modifier, viewModel: CalculatorViewModel, navController: NavController) {

    // set the second text color hex code to 0xFF212121 for when you finally figure out how to add a dark mode / light mode switch into the application.
    val textColor = MaterialTheme.colorScheme.onSecondary

    val equationTextState = viewModel.equationText.observeAsState()
    val resultTextState = viewModel.resultText.observeAsState()

    // val displayedEquationText = equationTextState.value?.replace("*", "×")?.replace("/", "÷") ?: ""

    /* val displayedEquationText = equationTextState.value?.let { rawEquation ->
        val containsOperators = rawEquation.any { it in setOf('+', '-', '*', '/') }
        if (containsOperators) {
            rawEquation.replace("*", "×").replace("/", "÷")
        } else {
            formatResultTextForDisplay(rawEquation)
        }
    } ?: "0" */

    val formattedEquationText = formatEquationTextForDisplay(equationTextState.value)

    val formattedResultText = formatResultTextForDisplay(resultTextState.value)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.secondary
    ) {

        val calculatorButtons = remember {
            val buttons = buttonList.map {
                buttonText ->
                    val type = buttonTypeMap[buttonText] ?: CalculatorButtonType.NORMAL
                    val icon = buttonIconsMap[buttonText]
                CalculatorButton(
                    text = buttonText,
                    type = type,
                    icon = icon
                )
            }
            mutableStateListOf<CalculatorButton>().apply { addAll(buttons) }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomStart
        ) {
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxSize()) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth(),
                ) {
                    Spacer(modifier = Modifier.padding(20.dp))
                    Text(
                        text = formattedEquationText,
                        style = TextStyle(
                            fontSize = 30.sp,
                            textAlign = TextAlign.End,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                        ),
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = resultTextState.value?.let {
                            if (it == "NaN" || it == "undefined") {
                                it
                            } else if (it == "Infinity" || it == "-Infinity") {
                                "∞"
                            } else if (it.matches("""^[a-zA-Z_][\w.]*@[0-9a-fA-F]+$""".toRegex())) {
                                "Domain Error"
                            } else {
                                formattedResultText
                            }
                        } ?: "0",
                        style = TextStyle(
                            fontSize = 48.sp,
                            textAlign = TextAlign.End,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                        ),
                        maxLines = 5,
                        // overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2.0f)
                ) {
                    LazyVerticalGrid(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(8.dp),
                        columns = GridCells.Fixed(4),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(calculatorButtons) {
                            CalculatorClickableButton(
                                button = it,
                                onClick = {
                                    viewModel.onCalculatorButtonClick(it.text)
                                }
                            )
                        }
                    }
                }
            }
        }
        /* Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clip(
                    RoundedCornerShape(8.dp)
                )) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(id = R.drawable.ic_lightmode),
                    contentDescription = null,
                    tint = Color.White
                )
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(id = R.drawable.ic_darkmode_outlined),
                    contentDescription = null,
                    tint = Color.White
                )
            }
        } */
    }
}

/**
 * Formats a raw equation string for display.
 * - Replaces "*" with "×" and "/" with "÷".
 * - Formats numbers within the equation using `formatResultTextForDisplay`.
 * @param rawEquation The raw equation string from the calculation engine (e.g., "200000*3.5").
 * @return A formatted equation string for display (e.g., "200,000 × 3.5").
 */
fun formatEquationTextForDisplay(rawEquation: String?): String {
    if (rawEquation.isNullOrEmpty()) {
        return "0"
    }

    val regular_expression = "(?<=[+\\-*/()])|(?=[+\\-*/()])".toRegex()
    val tokens = rawEquation.split(regular_expression)

    return tokens.joinToString("") { token ->
        if (token.toDoubleOrNull() != null) {
            formatResultTextForDisplay(token)
        } else {
            when (token) {
                "*" -> "×"
                "/" -> "÷"
                else -> token
            }
        }
    }

}

/**
 * Formats a raw number string for display.
 * - Adds thousands separators.
 * - Trims trailing ".0" from whole numbers.
 * - Limits decimal places to a reasonable number.
 * @param numberString The raw string from the calculation engine (e.g., "200000.0").
 * @return A formatted string for display (e.g., "200,000").
 */
fun formatResultTextForDisplay(numberString: String?): String {
    if (numberString.isNullOrEmpty()) {
        return "0"
    }

    val numberAsBigDecimal = try {
        BigDecimal(numberString)
    } catch (e: NumberFormatException) {
        return numberString
    }

    val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
    formatter.maximumIntegerDigits = 500

    val isWholeNumber = numberAsBigDecimal.stripTrailingZeros().scale() <= 0

    return if (isWholeNumber) {
        formatter.maximumFractionDigits = 0
        formatter.format(numberAsBigDecimal)
    } else {
        formatter.maximumFractionDigits = 50
        formatter.format(numberAsBigDecimal)
    }

    /* if (numberString.isNullOrEmpty()) {
        return "0"
    }

    val number = numberString.toDoubleOrNull() ?: return numberString

    val formatter = NumberFormat.getNumberInstance(Locale.getDefault())

    formatter.maximumIntegerDigits = 50

    val isWholeNumber = number % 1 == 0.0

    return if (isWholeNumber) {
        formatter.maximumFractionDigits = 0
        formatter.format(number.toLong())
    } else {
        formatter.maximumFractionDigits = 20
        formatter.format(number)
    } */
}
