package com.skytexcoder.mobilecomputingcoursekotlinjetpackcomposesuperapplication

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
data class SerializableSpan(
    val type: SpanType,
    val start: Int,
    val end: Int,
    val fontSizeInSp: Float? = null
)

@Serializable
enum class SpanType {
    BOLD, ITALIC, UNDERLINE, FONT_SIZE
}

@Serializable
data class RichTextData(
    val text: String,
    val spans: List<SerializableSpan>
)

fun AnnotatedString.toRichTextData(): RichTextData {
    val serializableSpans = mutableListOf<SerializableSpan>()
    spanStyles.forEach { range ->
        when {
            range.item.fontWeight == FontWeight.Bold ->
                serializableSpans.add(SerializableSpan(SpanType.BOLD, range.start, range.end))
            range.item.fontStyle == FontStyle.Italic ->
                serializableSpans.add(SerializableSpan(SpanType.ITALIC, range.start, range.end))
            range.item.textDecoration == TextDecoration.Underline ->
                serializableSpans.add(SerializableSpan(SpanType.UNDERLINE, range.start, range.end))
        }
        if (range.item.fontSize != TextUnit.Unspecified) {
            serializableSpans.add(SerializableSpan(SpanType.FONT_SIZE, range.start, range.end))
        }
    }
    return RichTextData(text = this.text, spans = serializableSpans)
}

fun RichTextData.toAnnotatedString(): AnnotatedString {

    return buildAnnotatedString {
        append(text)
        spans.forEach { span ->
            val style = when (span.type) {
                SpanType.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
                SpanType.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
                SpanType.UNDERLINE -> SpanStyle(textDecoration = TextDecoration.Underline)
                SpanType.FONT_SIZE -> SpanStyle(fontSize = span.fontSizeInSp?.sp ?: 16.sp)
            }
            addStyle(style, span.start, span.end)
        }
    }
}