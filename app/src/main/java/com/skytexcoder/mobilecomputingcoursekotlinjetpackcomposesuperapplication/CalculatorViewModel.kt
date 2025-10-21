package com.skytexcoder.mobilecomputingcoursekotlinjetpackcomposesuperapplication

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import java.math.BigDecimal
import java.math.BigInteger

class CalculatorViewModel : ViewModel() {

    private val _equationText = MutableLiveData("")
    val equationText : LiveData<String> = _equationText

    private val _resultText = MutableLiveData("0")
    val resultText: LiveData<String> = _resultText

    private val pressedButtonsHistory = mutableListOf<String>()

    private var lastButtonPressed: String? = null

    private val numberButtons = setOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")

    private val operators = setOf("+", "-", "*", "/")

    private val wasLastButtonANumber: Boolean
        get() = lastButtonPressed in numberButtons

    fun onCalculatorButtonClick(btn: String?) {
        pressedButtonsHistory.add(btn!!)
        Log.d("ButtonHistory", "History: $pressedButtonsHistory")
        Log.i("Clicked Button", btn!!)

        val currentEquation = _equationText.value ?: "0"
        val currentResultText = _resultText.value ?: "0"

        _equationText.value?.let {
            when (btn) {
                "AC" -> {
                    _equationText.value = "0"
                    _resultText.value = "0"
                    return
                }
                "BACKSPACE" -> {
                    if (currentEquation.isNotEmpty()) {
                        if (currentEquation.length == 1) {
                            _equationText.value = "0"
                            _resultText.value = "0"
                        } else {
                            _equationText.value = currentEquation.dropLast(1)
                        }
                        try {
                            _resultText.value = calculateResult(_equationText.value.toString())
                        } catch (_ : Exception) {

                        }
                        Log.i("Current Equation Text: ", _equationText.value.toString())
                        Log.i("Current Result Text: ", _resultText.value.toString())
                    }
                    return
                }
                "=" -> {
                    try {
                        _resultText.value = calculateResult(_equationText.value.toString())
                    } catch (_ : Exception) {

                    }
                    _equationText.value = _resultText.value
                    return
                }
                "." -> {
                    val lastCharacter = currentEquation.lastOrNull()?.toString()

                    if (lastCharacter in operators) {
                        _equationText.value += "0."
                        return
                    }

                    val lastOperatorIndex = currentEquation.findLast { it.toString() in operators }?.let {
                        currentEquation.lastIndexOf(it)
                    } ?: -1

                    val currentNumber = currentEquation.substring(lastOperatorIndex + 1)

                    if (!currentNumber.contains(".")) {
                        _equationText.value += "."
                        // return
                    }
                    try {
                        _resultText.value = calculateResult(_equationText.value.toString())
                    } catch (_ : Exception) {

                    }
                    return
                }
                "sin" -> {
                    if (_equationText.value == "0") {
                        _equationText.value = "sin("
                    } else {
                        _equationText.value += "sin("
                    }
                    try {
                        _resultText.value = calculateResult(_equationText.value.toString())
                    } catch (_: Exception) {

                    }
                    lastButtonPressed = btn
                    return
                }
                "cos" -> {
                    if (_equationText.value == "0") {
                        _equationText.value = "cos("
                    } else {
                        _equationText.value += "cos("
                    }
                    try {
                        _resultText.value = calculateResult(_equationText.value.toString())
                    } catch (_: Exception) {

                    }
                    lastButtonPressed = btn
                    return
                }
                "tan" -> {
                    if (currentEquation == "0") {
                        _equationText.value = "tan("
                    } else {
                        _equationText.value += "tan("
                    }
                    try {
                        _resultText.value = calculateResult(_equationText.value.toString())
                    } catch (_: Exception) {

                    }
                    lastButtonPressed = btn
                    return
                }
                "sin⁻¹" -> {
                    if (currentEquation == "0") {
                        _equationText.value = "sin⁻¹("
                    } else {
                        _equationText.value += "sin⁻¹("
                    }
                    try {
                        _resultText.value = calculateResult(_equationText.value.toString())
                    } catch (_: Exception) {

                    }
                    lastButtonPressed = btn
                    return
                }
                "cos⁻¹" -> {
                    if (currentEquation == "0") {
                        _equationText.value = "cos⁻¹("
                    } else {
                        _equationText.value += "cos⁻¹("
                    }
                    try {
                        _resultText.value = calculateResult(_equationText.value.toString())
                    } catch (_: Exception) {

                    }
                    lastButtonPressed = btn
                    return
                }
                "tan⁻¹" -> {
                    if (_equationText.value == "0") {
                        _equationText.value = "tan⁻¹("
                    } else {
                        _equationText.value += "tan⁻¹("
                    }
                    try {
                        _resultText.value = calculateResult(_equationText.value.toString())
                    } catch (_: Exception) {

                    }
                    lastButtonPressed = btn
                    return
                }
                "log" -> {
                    if (_equationText.value == "0") {
                        _equationText.value = "log("
                    } else {
                        _equationText.value += "log("
                    }
                    try {
                        _resultText.value = calculateResult(_equationText.value.toString())
                    } catch (_: Exception) {

                    }
                    lastButtonPressed = btn
                    return
                }
                "ln" -> {
                    if (_equationText.value == "0") {
                        _equationText.value = "ln("
                    } else {
                        _equationText.value += "ln("
                    }
                    try {
                        _resultText.value = calculateResult(_equationText.value.toString())
                    } catch (_ : Exception) {

                    }
                    lastButtonPressed = btn
                    return
                }
                "√x" -> {
                    if (currentEquation == "0") {
                        _equationText.value = "√"
                    } else {
                        _equationText.value += "√"
                    }
                    try {
                        _resultText.value = calculateResult(_equationText.value.toString())
                    } catch (_ : Exception) {

                    }
                    lastButtonPressed = btn
                    return
                }
                "x!" -> {
                    if (currentEquation != "0") {
                        _equationText.value += "!"
                    }
                    try {
                        _resultText.value = calculateResult(_equationText.value.toString())
                    } catch (_ : Exception) {

                    }
                    lastButtonPressed = btn
                    return
                }
                "xʸ" -> {
                    if (currentEquation != "0") {
                        _equationText.value += "^"
                    }
                    try {
                        _resultText.value = calculateResult(_equationText.value.toString())
                    } catch (_ : Exception) {

                    }
                    lastButtonPressed = btn
                    return
                }
                "1/x" -> {
                    if (currentEquation != "0") {
                        _equationText.value += "^(-1)"
                    }
                    try {
                        _resultText.value = calculateResult(_equationText.value.toString())
                    } catch (_ : Exception) {

                    }
                    lastButtonPressed = btn
                    return
                }
            }

            val characterToAppend = when (btn) {
                "×" -> "*"
                "÷" -> "/"
                else -> btn
            }

            /* if (currentEquation == "0") {
                if (characterToAppend !in operators) {
                    _equationText.value = characterToAppend
                } else {
                    _equationText.value += characterToAppend
                }
                Log.i("Current Equation Text: ", _equationText.value.toString())
                Log.i("Current Result Text: ", _resultText.value.toString())
                return
            } */

            if (currentEquation == "0" && lastButtonPressed != "0" && characterToAppend == "-") {
                _equationText.value = "-"
            } else {
                val lastCharacterDetected = currentEquation.lastOrNull()?.toString()
                val isLastCharacterOperator = lastCharacterDetected in operators
                val isCurrentButtonOperator = characterToAppend in operators
                if (isLastCharacterOperator && isCurrentButtonOperator) {
                    _equationText.value = currentEquation.dropLast(1) + characterToAppend
                } else if (currentEquation == "0" && !isCurrentButtonOperator) {
                    _equationText.value = characterToAppend
                } else {
                    _equationText.value += characterToAppend
                }
            }

            Log.i("[PRINT] EQT Before C.: ", _equationText.value.toString())

            try {
                _resultText.value = calculateResult(_equationText.value.toString())
            } catch (_ : Exception) {

            }

            Log.i("[PRINT] EQT After Calc.", _equationText.value.toString())
            Log.i("[PRINT] Result Text: ", _resultText.value.toString())

            lastButtonPressed = btn
        }
    }

    fun calculateResult(equationText: String): String {
        var sanitizedEquation = equationText

        Log.i("[PRINT] SNTZDEQ0", "Original: '$sanitizedEquation'")

        sanitizedEquation = sanitizedEquation
            .replace("×", "*")
            .replace("÷", "/")
            // .replace("√", "Math.sqrt(")
            .replace("sin", "Math.sin")
            .replace("cos", "Math.cos")
            .replace("tan", "Math.tan")
            .replace("sin⁻¹", "asin")
            .replace("cos⁻¹", "acos")
            .replace("tan⁻¹", "atan")
            .replace("log", "Math.log10")
            .replace("ln", "Math.log")
            .replace("^", "**")

        Log.i("[PRINT] InitialSNTZDEq.", sanitizedEquation)

        sanitizedEquation = handleFactorial(sanitizedEquation)
        sanitizedEquation = handleSquareRoot(sanitizedEquation)
        sanitizedEquation = convertTrigonometryToDegrees(sanitizedEquation)

        Log.i("[PRINT] SNTZD Equation", "After Functions: '$sanitizedEquation'")

        val sanitizedEquationText = StringBuilder()
        for (i in sanitizedEquation.indices) {
            val currentCharacter = sanitizedEquation[i]
            if (i > 0) {
                val previousCharacter = sanitizedEquation[i - 1]
                if (previousCharacter.isDigit() && currentCharacter == '(') {
                    val substringBeforeCurrentIndex = sanitizedEquation.substring(0, i)
                    val isPartOfFunction = substringBeforeCurrentIndex.endsWith("Math.log10") ||
                            substringBeforeCurrentIndex.endsWith("Math.sqrt") ||
                            substringBeforeCurrentIndex.endsWith("Math.sin") ||
                            substringBeforeCurrentIndex.endsWith("Math.cos") ||
                            substringBeforeCurrentIndex.endsWith("Math.tan") ||
                            substringBeforeCurrentIndex.endsWith("Math.log") ||
                            substringBeforeCurrentIndex.endsWith("asin") ||
                            substringBeforeCurrentIndex.endsWith("acos") ||
                            substringBeforeCurrentIndex.endsWith("atan")
                    if (!isPartOfFunction) {
                        sanitizedEquationText.append('*')
                    }
                } else if (previousCharacter == ')' && currentCharacter.isDigit()) {
                    sanitizedEquationText.append('*')
                } else if (previousCharacter == ')' && currentCharacter == '(') {
                    sanitizedEquationText.append('*')
                }
            }
            sanitizedEquationText.append(currentCharacter)
        }
        val finalEquationTextToBeEvaluated = sanitizedEquationText.toString()
        Log.i("[PRINT] SNTZD Equation", "Original: '$equationText', Processed: '$finalEquationTextToBeEvaluated'")
        val context: Context = Context.enter()
        context.optimizationLevel = -1
        val scriptable: Scriptable = context.initStandardObjects()
        try {
            val resultFromRhino = context.evaluateString(scriptable, finalEquationTextToBeEvaluated, "Javascript", 1, null)
            // Log.i("[PRINT] RawRhinoOutput", resultFromRhino)
            val rawResultString = resultFromRhino.toString()
            Log.i("[PRINT] RawRhinoResult", rawResultString)
            try {
                val resultAsBigDecimal = BigDecimal(rawResultString)
                var plainStringResult = resultAsBigDecimal.toPlainString()
                Log.i("[PRINT]BigDecimalResult", plainStringResult)
                if (plainStringResult.endsWith(".0")) {
                    plainStringResult = plainStringResult.removeSuffix(".0")
                }
                return plainStringResult
            } catch (e: NumberFormatException) {
                Log.i("[PRINT] Exception: ", e.toString())
                return rawResultString
            }
        } finally {
            Context.exit()
        }
    }

    private fun handleFactorial(equation: String): String {
        var result = equation
        val factorialRegularExpression = "(\\d+(?:\\.\\d+)?)!".toRegex()

        Log.i("[PRINT] FactHandlingEq.", result)

        while (factorialRegularExpression.containsMatchIn(result)) {
            Log.i("[PRINT] FactHandledEq.", "While Loop START Result: $result")
            result = factorialRegularExpression.replace(result) { matchResult ->
                val number = matchResult.groupValues[1].toDoubleOrNull()
                if (number != null && number >= 0 && number == number.toInt().toDouble()) {
                    calculateFactorial(number.toInt()).toString()
                } else {
                    matchResult.value
                }
            }
            Log.i("[PRINT] FactHandledEq.", "While Loop END Result: $result")
        }

        Log.i("[PRINT] FactHandledEq.", "Final Result: $result")
        return result
    }

    private fun calculateFactorial(n: Int): BigInteger {
        Log.i("[PRINT] FactorialCalc:", "Calculating factorial of $n")
        if (n < 0) return 0.toBigInteger()
        if (n == 0 || n == 1) return 1.toBigInteger()
        var result = BigInteger.ONE
        for (i in 2..n) {
            result = result.multiply(i.toBigInteger())
        }
        return result
    }

    private fun handleSquareRoot(equation: String): String {
        var result = equation
        val squareRootPattern = "√".toRegex()
        val matches = squareRootPattern.findAll(result).toList().reversed()
        for (match in matches) {
            val squareRootIndex = match.range.first
            val afterSquareRootIndex = squareRootIndex + 1
            if (afterSquareRootIndex >= result.length) {
                result = result.substring(0, squareRootIndex) + "Math.sqrt()"
                continue
            }
            val characterAfterSquareRoot = result[afterSquareRootIndex]
            if (characterAfterSquareRoot == '(') {
                val closingIndex = findMatchingClosingParentheses(result, afterSquareRootIndex + 1)
                if (closingIndex != -1) {
                    val content = result.substring(afterSquareRootIndex + 1, closingIndex)
                    val replacement = "Math.sqrt($content)"
                    result = result.substring(0, squareRootIndex) + replacement + result.substring(closingIndex + 1)
                } else {
                    val content = result.substring(afterSquareRootIndex + 1)
                    val replacement = "Math.sqrt($content)"
                    result = result.substring(0, squareRootIndex) + replacement
                }
            } else {
                var endIndex = afterSquareRootIndex
                while (endIndex < result.length) {
                    val character = result[endIndex]
                    if (character.isDigit() || character == '.') {
                        endIndex++
                    } else {
                        break
                    }
                }
                val content = result.substring(afterSquareRootIndex, endIndex)
                val replacement = "Math.sqrt($content)"
                result = result.substring(0, squareRootIndex) + replacement + result.substring(endIndex)
            }
        }
        Log.i("[PRINT] SquareRoot", "Result: $result")
        return result
    }

    private fun convertTrigonometryToDegrees(equation: String): String {
        var result = equation

        result = processTrigonometryFunction(result, "Math.sin")
        result = processTrigonometryFunction(result, "Math.cos")
        result = processTrigonometryFunction(result, "Math.tan")

        result = processInverseTrigonometryFunction(result, "Math.asin")
        result = processInverseTrigonometryFunction(result, "Math.acos")
        result = processInverseTrigonometryFunction(result, "Math.atan")

        return result
    }

    private fun processTrigonometryFunction(equation: String, functionName: String): String {
        var result = equation
        val pattern = "$functionName\\(".toRegex()

        val matches = pattern.findAll(result).toList().reversed()

        for (match in matches) {
            val startIndex = match.range.last + 1
            val closingIndex = findMatchingClosingParentheses(result, startIndex)

            if (closingIndex != -1) {
                val content = result.substring(startIndex, closingIndex)

                val replacement = "$functionName((Math.PI/180)*($content))"

                result = result.substring(0, match.range.first) + replacement + result.substring(closingIndex + 1)
            }
        }
        return result
    }

    private fun processInverseTrigonometryFunction(equation: String, functionName: String): String {
        var result = equation
        val pattern = "$functionName\\(".toRegex()

        val matches = pattern.findAll(result).toList().reversed()

        for (match in matches) {
            val startIndex = match.range.last + 1
            val closingIndex = findMatchingClosingParentheses(result, startIndex)

            if (closingIndex != -1) {
                val content = result.substring(startIndex, closingIndex)
                val replacement = "((180/Math.PI)*$functionName($content))"

                result = result.substring(0, match.range.first) + replacement + result.substring(closingIndex + 1)
            }
        }
        return result
    }

    private fun findMatchingClosingParentheses(text: String, startIndex: Int): Int {
        var depth = 1
        var index = startIndex

        while (index < text.length && depth > 0) {
            when (text[index]) {
                '(' -> depth++
                ')' -> depth--
            }

            if (depth == 0) {
                return index
            }
            index++
        }
        return -1
    }
}