package com.capstone.bookshelf.feature.readbook.presentation.component.content

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

@Immutable
data class ImageContent(
    val content: String,
){
    var zoom = mutableFloatStateOf(1f)
    var offset = mutableStateOf(Offset.Zero)
}

@Immutable
data class HeaderContent(
    val content: String,
)

@Immutable
data class ParagraphContent(
    val content: String,
){
    val text = mutableStateOf(convertToAnnotatedStrings(content))
}
private fun convertToAnnotatedStrings(paragraph: String): AnnotatedString {
    return buildAnnotatedString {
        val stack = mutableListOf<String>()
        var currentIndex = 0

        while (currentIndex < paragraph.length) {
            when {
                paragraph.startsWith("<b>", currentIndex) -> {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    stack.add("b")
                    currentIndex += 3
                }
                paragraph.startsWith("</b>", currentIndex) -> {
                    if (stack.lastOrNull() == "b") {
                        pop()
                        stack.removeAt(stack.lastIndex)
                    }
                    currentIndex += 4
                }
                paragraph.startsWith("<i>", currentIndex) -> {
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    stack.add("i")
                    currentIndex += 3
                }
                paragraph.startsWith("</i>", currentIndex) -> {
                    if (stack.lastOrNull() == "i") {
                        pop()
                        stack.removeAt(stack.lastIndex)
                    }
                    currentIndex += 4
                }
                paragraph.startsWith("<u>", currentIndex) -> {
                    pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                    stack.add("u")
                    currentIndex += 3
                }
                paragraph.startsWith("</u>", currentIndex) -> {
                    if (stack.lastOrNull() == "u") {
                        pop()
                        stack.removeAt(stack.lastIndex)
                    }
                    currentIndex += 4
                }
                else -> {
                    append(paragraph[currentIndex])
                    currentIndex++
                }
            }
        }
    }
}
