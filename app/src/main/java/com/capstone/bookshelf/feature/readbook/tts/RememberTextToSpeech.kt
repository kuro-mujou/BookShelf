package com.capstone.bookshelf.feature.readbook.tts

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import com.capstone.bookshelf.feature.readbook.presentation.state.ContentUIState
import com.capstone.bookshelf.feature.readbook.presentation.state.TTSState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale


@Composable
fun rememberTextToSpeech(
    context: Context,
    wakeLock: PowerManager.WakeLock,
    ttsState : TTSState
): TextToSpeech {
    var tts : TextToSpeech = remember { TextToSpeech(context, null) }
    DisposableEffect(context) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = ttsState.currentLanguage?: Locale.getDefault()
                tts.voice = ttsState.currentVoice?: tts.defaultVoice
                tts.setSpeechRate(ttsState.currentSpeed?: 1f)
                tts.setPitch(ttsState.currentPitch?: 1f)
            }
        }
        tts = textToSpeech
        wakeLock.acquire(1*60*1000L)
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
            wakeLock.release()
        }
    }
    return tts
}

fun readNextParagraph(
    wakeLock: PowerManager.WakeLock,
    tts: TextToSpeech,
    uiState: ContentUIState,
    ttsState: TTSState,
    chapterContents: SnapshotStateMap<Int, List<String>>,
    targetParagraphIndex: Int,
    currentChapterIndex: Int,
    currentPosition: Int,
    isReading: Boolean,
    maxWidth: Int,
    maxHeight: Int,
    textStyle: TextStyle,
    textMeasurer: TextMeasurer,
    shouldScroll: Boolean,
    nextParagraph: (Int, Int, Int, Boolean, Int, Boolean) -> Unit
) {
    wakeLock.acquire(60*500L)
    val currentChapterContent = chapterContents[currentChapterIndex]
    if (currentChapterContent != null) {
        if (targetParagraphIndex in currentChapterContent.indices) {
            val currentText = currentChapterContent[targetParagraphIndex]
            val textToSpeakNow = currentText.substring(currentPosition)
            val oldPos = currentText.length - textToSpeakNow.length
            var i = 0
            var sumLength: Int
            val flowTextLength = processTextLength(
                text = currentText,
                maxWidth = maxWidth,
                maxHeight = maxHeight,
                textStyle = textStyle,
                textMeasurer = textMeasurer
            )
            sumLength = flowTextLength[i]
            tts.speak(textToSpeakNow, TextToSpeech.QUEUE_FLUSH, null, "utteranceId-$targetParagraphIndex")
            tts.setOnUtteranceProgressListener(
                object : UtteranceProgressListener(){
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        if (targetParagraphIndex+1 < currentChapterContent.size) {
                            readNextParagraph(
                                wakeLock = wakeLock,
                                tts = tts,
                                uiState = uiState,
                                ttsState = ttsState,
                                chapterContents = chapterContents,
                                targetParagraphIndex = targetParagraphIndex+1,
                                currentChapterIndex = currentChapterIndex,
                                nextParagraph = nextParagraph,
                                currentPosition = 0,
                                isReading = isReading,
                                maxWidth = maxWidth,
                                maxHeight = maxHeight,
                                textStyle = textStyle,
                                textMeasurer = textMeasurer,
                                shouldScroll = shouldScroll
                            )
                        }else{
                            if(currentChapterIndex+1 < uiState.totalChapter) {
                                nextParagraph(
                                    0,
                                    currentChapterIndex + 1,
                                    currentPosition,
                                    shouldScroll,
                                    i,
                                    true
                                )
                                stopReading(tts)
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(1000)
                                    readNextParagraph(
                                        wakeLock = wakeLock,
                                        tts = tts,
                                        uiState = uiState,
                                        ttsState = ttsState,
                                        chapterContents = chapterContents,
                                        targetParagraphIndex = 0,
                                        currentChapterIndex = currentChapterIndex + 1,
                                        nextParagraph = nextParagraph,
                                        currentPosition = 0,
                                        isReading = isReading,
                                        maxWidth = maxWidth,
                                        maxHeight = maxHeight,
                                        textStyle = textStyle,
                                        textMeasurer = textMeasurer,
                                        shouldScroll = shouldScroll
                                    )
                                }
                            }else{
                                nextParagraph(
                                    targetParagraphIndex,
                                    currentChapterIndex,
                                    currentPosition,
                                    shouldScroll,
                                    i,
                                    false
                                )
                                wakeLock.release()
                                stopReading(tts)
                            }
                            wakeLock.release()
                        }
                    }

                    @Deprecated("Deprecated in Java", ReplaceWith("wakeLock.release()"))
                    override fun onError(utteranceId: String?) {
                        wakeLock.release()
                    }
                    override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                        super.onRangeStart(utteranceId, start, end, frame)
                        if(isReading) {
                            val currentPos = textToSpeakNow.substring(0, end).length
                            nextParagraph(
                                targetParagraphIndex,
                                currentChapterIndex,
                                oldPos + currentPos,
                                shouldScroll,
                                i,
                                true
                            )
                            if (flowTextLength.size > 1) {
                                if (oldPos + currentPos > sumLength) {
                                    nextParagraph(
                                        targetParagraphIndex,
                                        currentChapterIndex,
                                        oldPos + currentPos,
                                        !shouldScroll,
                                        i + 1,
                                        true
                                    )
                                    sumLength += flowTextLength[i++]
                                }
                            }
                        }
                    }
                }
            )
        }else{
            nextParagraph(
                0,
                currentChapterIndex,
                currentPosition,
                shouldScroll,
                0,
                false
            )
            wakeLock.release()
            stopReading(tts)
        }
    }
}

fun stopReading(
    tts: TextToSpeech
) {
    if (tts.isSpeaking) {
        tts.stop()
    }
}

fun processTextLength(
    text: String,
    maxWidth: Int,
    maxHeight: Int,
    textStyle: TextStyle,
    textMeasurer: TextMeasurer,
): SnapshotStateList<Int> {
    var longText = text
    val subStringLength = mutableStateListOf<Int>()
    while (longText.isNotEmpty()) {
        val measuredLayoutResult = textMeasurer.measure(
            text = longText,
            style = textStyle,
            overflow = TextOverflow.Ellipsis,
            constraints = Constraints(
                maxWidth = maxWidth,
                maxHeight = maxHeight
            ),
        )
        if(measuredLayoutResult.hasVisualOverflow){
            val lastVisibleCharacterIndex = measuredLayoutResult.getLineEnd(
                lineIndex = measuredLayoutResult.lineCount - 1,
                visibleEnd = true
            )
            val endIndex = minOf(lastVisibleCharacterIndex, longText.length)
            val endSubString = longText.substring(0, endIndex)
            subStringLength.add(endSubString.trim().length)
            longText = longText.replaceFirst(endSubString, "", true)
        }else{
            subStringLength.add(longText.trim().length)
            longText = ""
        }
    }
    return subStringLength
}