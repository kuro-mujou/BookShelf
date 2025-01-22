package com.capstone.bookshelf.presentation.bookcontent.content

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.capstone.bookshelf.domain.wrapper.Chapter
import com.capstone.bookshelf.presentation.bookcontent.BookContentRootState
import com.capstone.bookshelf.presentation.bookcontent.KeepScreenOn
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarState
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollAction
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollState
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.component.font.FontState
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TTSAction
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TTSState
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TTSViewModel
import com.capstone.bookshelf.presentation.bookcontent.content.content_component.HeaderContent
import com.capstone.bookshelf.presentation.bookcontent.content.content_component.HeaderText
import com.capstone.bookshelf.presentation.bookcontent.content.content_component.ImageComponent
import com.capstone.bookshelf.presentation.bookcontent.content.content_component.ImageContent
import com.capstone.bookshelf.presentation.bookcontent.content.content_component.ParagraphContent
import com.capstone.bookshelf.presentation.bookcontent.content.content_component.ParagraphText
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerState
import com.capstone.bookshelf.util.DataStoreManager
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContentScreen(
    ttsViewModel: TTSViewModel,
    contentViewModel: ContentViewModel,
    autoScrollViewModel: AutoScrollViewModel,
    bottomBarState: BottomBarState,
    hazeState: HazeState,
    pagerState : PagerState,
    bookContentRootState : BookContentRootState,
    drawerContainerState: DrawerContainerState,
    contentState : ContentState,
    ttsState : TTSState,
    colorPaletteState: ColorPalette,
    fontState: FontState,
    autoScrollState: AutoScrollState,
    dataStoreManager: DataStoreManager,
    updateSystemBar: () -> Unit,
    currentChapter : (Int,Int,Boolean) -> Unit,
    launchAlertDialog : (Boolean) -> Unit
){
    val lazyListStates = remember { mutableStateMapOf<Int, LazyListState>() }
    val chapterContents = remember { mutableStateMapOf<Int, List<String>>() }
    var triggerLoadChapter by remember { mutableStateOf(false) }
    var callbackLoadChapter by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var lazyListState by remember { mutableStateOf<LazyListState?>(LazyListState()) }
//    val currentReadingItemIndex by rememberUpdatedState(newValue = contentState.firstVisibleItemIndex)
//    val isFocused by rememberUpdatedState(newValue = ttsState.isFocused)
    if(autoScrollState.isStart)
        KeepScreenOn(true)
    else
        KeepScreenOn(false)
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    Modifier.haze(hazeState)
                else
                    Modifier
            ),
        color = colorPaletteState.backgroundColor
    ) {
        LaunchedEffect(pagerState.currentPage) {
            val currentPage = pagerState.currentPage
            lazyListStates.keys.filter { pageIndex ->
                pageIndex < currentPage - 2 || pageIndex > currentPage + 2
            }.forEach { pageIndex ->
                lazyListStates.remove(pageIndex)
            }
            chapterContents.keys.filter { pageIndex ->
                pageIndex < currentPage - 2 || pageIndex > currentPage + 2
            }.forEach { pageIndex ->
                chapterContents.remove(pageIndex)
            }
        }
        LaunchedEffect(callbackLoadChapter, contentState.currentChapterIndex) {
            if (callbackLoadChapter) {
                triggerLoadChapter = false
                callbackLoadChapter = false
            }
            lazyListState = lazyListStates[contentState.currentChapterIndex]
            ttsViewModel.onAction(dataStoreManager,TTSAction.UpdateCurrentChapterContent(chapterContents[contentState.currentChapterIndex]))
            if(autoScrollState.isStart && autoScrollState.isPaused){
                delay(1000)
                autoScrollViewModel.onAction(AutoScrollAction.UpdateIsPaused(false))
            }
        }
        val beyondBoundsPageCount = 1
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize(),
            beyondViewportPageCount = beyondBoundsPageCount,
            userScrollEnabled = bookContentRootState.enablePagerScroll,
            key = { page -> page }
        ) { page ->
            val newPage by rememberUpdatedState(newValue = page)
            val chapterContent by contentViewModel.chapterContent
            var data by remember { mutableStateOf<Chapter?>(null) }
            val listState = lazyListStates.getOrPut(newPage){ LazyListState() }
            val contentList = remember { mutableStateOf(listOf<@Composable (Boolean, Boolean,ColorPalette,FontState) -> Unit>())}
            val density = LocalDensity.current
            var hasPrintedAtEnd by remember { mutableStateOf(false) }
            var isAnimationRunning by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                snapshotFlow {
                    Pair(
                        pagerState.isScrollInProgress,
                        abs(pagerState.settledPage - newPage)
                    )
                }.collectLatest { (scrollInProgress, diff) ->
                    if (!scrollInProgress && (diff in 0..beyondBoundsPageCount)) {
                        if (diff > 0) delay(1000)
                        triggerLoadChapter = true
                        isAnimationRunning = false
                        cancel()
                    }
                }
            }
            LaunchedEffect(triggerLoadChapter) {
                if (triggerLoadChapter && data == null) {
                    try{
                        contentViewModel.getChapter((page))
                        data = chapterContent
                        parseListToUsableLists(data!!.content).also{
                            contentList.value = it.first
                            chapterContents[page] = it.second
                        }
                        callbackLoadChapter = true
                    }catch (e: Exception){
                        e.printStackTrace()
                        launchAlertDialog(true)
                    }
                }
            }
            LaunchedEffect(pagerState.targetPage) {
                contentViewModel.onAction(ContentAction.UpdateFlagTriggerAdjustScroll(false))
                currentChapter(pagerState.targetPage,0,autoScrollState.isStart)
            }
            LaunchedEffect(lazyListState) {
                lazyListState?.let {
                    snapshotFlow { it.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                        .collect { index ->
                            if (index != null) {
                                contentViewModel.onAction(ContentAction.UpdateLastVisibleItemIndex(index))
                            }
                        }
                }
            }

            LaunchedEffect(lazyListState) {
                lazyListState?.let {
                    snapshotFlow { it.layoutInfo.visibleItemsInfo.firstOrNull()?.index }
                        .collect { index ->
                            if (index != null) {
                                contentViewModel.onAction(ContentAction.UpdateFirstVisibleItemIndex(index))
                            }
                        }
                }

            }
            LaunchedEffect(lazyListState){
                lazyListState?.let {
                    snapshotFlow { it.isScrollInProgress && !pagerState.isScrollInProgress }.collect { scrolling ->
                        if (scrolling && (ttsState.isSpeaking || ttsState.isPaused) && ttsState.currentReadingParagraph == contentState.firstVisibleItemIndex) {
                            contentViewModel.onAction(
                                ContentAction.UpdateFlagTriggerAdjustScroll(
                                    true
                                )
                            )
                        }
                    }
                }
            }

            LaunchedEffect(ttsState.currentReadingParagraph) {
                lazyListState?.let {
                    if ((ttsState.currentReadingParagraph >= contentState.lastVisibleItemIndex
                                || ttsState.currentReadingParagraph <= contentState.firstVisibleItemIndex)
                        && !contentState.flagTriggerScrolling
                    ) {
                        if (ttsState.isSpeaking) {
                            it.animateScrollToItem(ttsState.currentReadingParagraph)
                            contentViewModel.onAction(
                                ContentAction.UpdateFlagTriggerAdjustScroll(
                                    false
                                )
                            )
                        }
                    }
                }
            }

            LaunchedEffect(contentState.flagTriggerScrolling){
                if(contentState.flagTriggerScrolling)
                    contentViewModel.onAction(ContentAction.UpdateFlagStartScrolling(true))
            }

            LaunchedEffect(contentState.flagStartScrolling){
                lazyListState?.let {
                    if (contentState.flagStartScrolling) {
                        if (ttsState.currentReadingParagraph != contentState.firstVisibleItemIndex) {
                            it.animateScrollToItem(ttsState.currentReadingParagraph)
                            contentViewModel.onAction(
                                ContentAction.UpdateFlagTriggerAdjustScroll(
                                    false
                                )
                            )
                            contentViewModel.onAction(ContentAction.UpdateFlagScrollAdjusted(true))

                        } else if (!contentState.flagTriggerAdjustScroll) {
                            it.animateScrollBy(value = contentState.screenHeight.toFloat())
                            contentViewModel.onAction(
                                ContentAction.UpdateFlagTriggerAdjustScroll(
                                    false
                                )
                            )
                            contentViewModel.onAction(ContentAction.UpdateFlagStartScrolling(false))
                        } else {
                            contentViewModel.onAction(ContentAction.UpdateFlagStartScrolling(true))
                        }
                    }
                }
            }

            LaunchedEffect(contentState.flagStartAdjustScroll){
                lazyListState?.let {
                    if (contentState.flagStartAdjustScroll) {
                        it.animateScrollToItem(ttsState.currentReadingParagraph)
                        contentViewModel.onAction(ContentAction.UpdateFlagTriggerAdjustScroll(false))
                        contentViewModel.onAction(ContentAction.UpdateFlagStartAdjustScroll(false))
                        contentViewModel.onAction(ContentAction.UpdateFlagScrollAdjusted(true))
                    }
                }
            }

            LaunchedEffect(contentState.flagScrollAdjusted){
                lazyListState?.let {
                    if (contentState.flagScrollAdjusted) {
                        it.animateScrollBy(value = contentState.screenHeight.toFloat() * ttsState.scrollTime)
                        contentViewModel.onAction(ContentAction.UpdateFlagTriggerAdjustScroll(false))
                        contentViewModel.onAction(ContentAction.UpdateFlagScrollAdjusted(false))
                        contentViewModel.onAction(ContentAction.UpdateFlagStartScrolling(false))
                    }
                }
            }
            LaunchedEffect(pagerState.currentPage,autoScrollState.isPaused,autoScrollState.isStart) {
                lazyListState?.let {
                    if (!autoScrollState.isPaused && autoScrollState.isStart) {
                        while (true) {
                            isAnimationRunning = true
                            coroutineScope.launch {
                                it.animateScrollBy(
                                    value = contentState.screenHeight.toFloat(),
                                    animationSpec = tween(
                                        durationMillis = autoScrollState.currentSpeed,
                                        delayMillis = 0,
                                        easing = LinearEasing
                                    )
                                )
                            }.invokeOnCompletion {
                                isAnimationRunning = false
                            }
                            delay(autoScrollState.currentSpeed.toLong())
                        }
                    }
                }
            }
            LaunchedEffect(lazyListState?.isScrollInProgress) {
                lazyListState?.let {
                    if (!it.isScrollInProgress && autoScrollState.isStart && !autoScrollState.isPaused) {
                        autoScrollViewModel.onAction(AutoScrollAction.UpdateIsPaused(true))
                    }
                }
            }
            LaunchedEffect(lazyListState?.isScrollInProgress){
                if(autoScrollState.isStart && !autoScrollState.isPaused) {
                    lazyListState?.let {
                        snapshotFlow { it.layoutInfo }
                            .collect { layoutInfo ->
                                if (layoutInfo.visibleItemsInfo.isNotEmpty()) {
                                    val lastVisibleItem = layoutInfo.visibleItemsInfo.last()
                                    if (lastVisibleItem.index == layoutInfo.totalItemsCount - 1 &&
                                        lastVisibleItem.offset + lastVisibleItem.size <= layoutInfo.viewportEndOffset + 1
                                    ) {
                                        if (!isAnimationRunning && !hasPrintedAtEnd && contentState.previousChapterIndex <= contentState.currentChapterIndex) {
                                            delay(1000)
                                            currentChapter(contentState.currentChapterIndex + 1,0,true)
                                            hasPrintedAtEnd = true
                                        }
                                    } else {
                                        hasPrintedAtEnd = false
                                    }
                                }
                            }
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(color = colorPaletteState.containerColor),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    drawerContainerState.currentTOC?.title?.let {
                        Text(
                            modifier = Modifier
                                .weight(1f)
                                .statusBarsPadding(),
                            text = it,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            style = TextStyle(
                                color = colorPaletteState.textColor,
                            )
                        )
                    }
                    Text(
                        modifier = Modifier
                            .statusBarsPadding()
                            .wrapContentWidth(),
                        text = "${pagerState.currentPage + 1} / ${contentState.totalChapter}",
                        style = TextStyle(
                            color = colorPaletteState.textColor,
                            textAlign = TextAlign.Right
                        ),
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .then(
                            if(!autoScrollState.isStart){
                                Modifier.clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = {
                                        updateSystemBar()
                                    },
                                )
                            }else{
                                Modifier.combinedClickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = {
                                        updateSystemBar()
                                    },
                                    onDoubleClick = {
                                        autoScrollViewModel.onAction(AutoScrollAction.UpdateIsPaused(!autoScrollState.isPaused))
                                    }
                                )
                            }
                        )
                        .onGloballyPositioned { coordinates ->
                            contentViewModel.onAction(ContentAction.UpdateScreenWidth(coordinates.size.width - (with(density) { 32.dp.toPx() }.toInt())))
                            contentViewModel.onAction(ContentAction.UpdateScreenHeight(coordinates.size.height))
                        },
                    state = listState,
                ) {
                    itemsIndexed(
                        items = contentList.value,
                        key = { index, _ -> index }
                    ) { index, composable ->
                        composable(
                            index == ttsState.currentReadingParagraph,
                            ttsState.isFocused,
                            colorPaletteState,
                            fontState
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(color = colorPaletteState.containerColor),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(start = 4.dp),
                        text = "${contentState.lastVisibleItemIndex + 1} / ${contentList.value.size}",
                        style = TextStyle(
                            color = colorPaletteState.textColor,
                            textAlign = TextAlign.Right
                        ),
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
@SuppressLint("SdCardPath")
private fun parseListToUsableLists(
    paragraphs: List<String>,
): Pair<List<@Composable (Boolean, Boolean, ColorPalette, FontState) -> Unit>,List<String>> {
    val composable = mutableListOf<@Composable (Boolean,Boolean,ColorPalette,FontState) -> Unit>()
    val ttsParagraph = mutableListOf<String>()
    paragraphs.forEach {
        val linkPattern = Regex("""/data/user/0/com\.capstone\.bookshelf/files/[^ ]*""")
        val headerPatten = Regex("""<h([1-6])[^>]*>(.*?)</h([1-6])>""")
        val headerLevel = Regex("""<h([1-6])>.*?</h\1>""")
        val htmlTagPattern = Regex(pattern = """<[^>]+>""")
        if(it.isNotEmpty()){
            if(linkPattern.containsMatchIn(it)) {
                composable.add{ _, _,_, _->
                    ImageComponent(
                        content = ImageContent(
                            content = it
                        )
                    )
                }
                ttsParagraph.add(linkPattern.replace(it, replacement = " "))
            }else if(headerPatten.containsMatchIn(it)) {
                if(htmlTagPattern.replace(it, replacement = "").isNotEmpty()){
                    composable.add {isHighlighted, isSpeaking, colorPaletteState, fontState ->
                        HeaderText(
                            colorPaletteState = colorPaletteState,
                            fontState = fontState,
                            content = HeaderContent(
                                content = htmlTagPattern.replace(it, replacement = ""),
                                fontState = fontState,
                                level = headerLevel.find(it)!!.groupValues[1].toInt(),
                            ),
                            isHighlighted = isHighlighted,
                            isSpeaking = isSpeaking
                        )
                    }
                    ttsParagraph.add(htmlTagPattern.replace(it, replacement = ""))
                }
            } else{
                if(htmlTagPattern.replace(it, replacement = "").isNotEmpty()){
                    composable.add { isHighlighted,isSpeaking, colorPaletteState, fontState->
                        ParagraphText(
                            colorPaletteState = colorPaletteState,
                            fontState = fontState,
                            content = ParagraphContent(
                                content = it,
                                fontState = fontState,
                            ),
                            isHighlighted = isHighlighted,
                            isSpeaking = isSpeaking
                        )
                    }
                    ttsParagraph.add(htmlTagPattern.replace(it, replacement = ""))
                }
            }
        }
    }
    return Pair(composable,ttsParagraph)
}