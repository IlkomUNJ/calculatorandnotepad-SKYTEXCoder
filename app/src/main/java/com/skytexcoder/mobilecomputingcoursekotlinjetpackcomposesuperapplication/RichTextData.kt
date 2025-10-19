package com.skytexcoder.mobilecomputingcoursekotlinjetpackcomposesuperapplication

import android.util.Log
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlinx.serialization.Serializable

@Serializable
data class RichTextStyle(
    val start: Int,
    val end: Int,
    val isTextCurrentlyBold: Boolean = false,
    val isTextCurrentlyItalicized: Boolean = false,
    val isTextCurrentlyUnderlined: Boolean = false,
    val textFontSize: Float? = null,
)

@Serializable
data class RichTextData(
    val text: String,
    val textStyles: List<RichTextStyle>
)

fun RichTextData.toAnnotatedString(): AnnotatedString {
    return buildAnnotatedString {
        append(text)
        textStyles.forEach {
            textStyle ->
            addStyle(
                style = SpanStyle(
                    fontWeight = if (textStyle.isTextCurrentlyBold) FontWeight.Bold else null,
                    fontStyle = if (textStyle.isTextCurrentlyItalicized) FontStyle.Italic else null,
                    textDecoration = if (textStyle.isTextCurrentlyUnderlined) TextDecoration.Underline else null,
                    fontSize = textStyle.textFontSize?.sp ?: TextUnit.Unspecified,
                ),
                start = textStyle.start,
                end = textStyle.end,
            )
        }
    }
}

fun AnnotatedString.toRichTextData(): RichTextData {
    val groupedStyles = mutableMapOf<Pair<Int, Int>, MutableList<SpanStyle>>()

    this.spanStyles.forEach {
        range ->
        val key = Pair(range.start, range.end)
        groupedStyles.getOrPut(key) { mutableListOf() }.add(range.item)
    }

    val mergedTextStyles = groupedStyles.map { (range, styles) ->
        RichTextStyle(
            start = range.first,
            end = range.second,
            isTextCurrentlyBold = styles.any {
                it.fontWeight == FontWeight.Bold
            },
            isTextCurrentlyItalicized = styles.any {
                it.fontStyle == FontStyle.Italic
            },
            isTextCurrentlyUnderlined = styles.any {
                it.textDecoration == TextDecoration.Underline
            },
            textFontSize = styles.firstNotNullOfOrNull {
                it.fontSize?.value
            }?.takeIf { !it.isNaN() }
        )
    }

    Log.i("RichTextData", "toRichTextData: $mergedTextStyles")

    return RichTextData(
        text = this.text,
        textStyles = mergedTextStyles,
    )
}