package com.capstone.bookshelf.feature.readbook.presentation

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capstone.bookshelf.core.domain.ChapterContentEntity
import com.capstone.bookshelf.feature.readbook.presentation.component.bottomBar.BottomBarDefault
import com.capstone.bookshelf.feature.readbook.presentation.component.content.HeaderText
import com.capstone.bookshelf.feature.readbook.presentation.component.content.ImageComponent
import com.capstone.bookshelf.feature.readbook.presentation.component.content.ParagraphText
import com.capstone.bookshelf.feature.readbook.presentation.component.content.ZoomableImage
import com.capstone.bookshelf.feature.readbook.presentation.component.drawer.NavigationDrawer
import com.capstone.bookshelf.feature.readbook.presentation.component.textToolBar.CustomTextToolbar
import com.capstone.bookshelf.feature.readbook.presentation.component.topBar.TopBar
import com.capstone.bookshelf.feature.readbook.presentation.state.ContentUIState
import com.capstone.bookshelf.feature.readbook.presentation.state.TTSState
import kotlinx.coroutines.flow.distinctUntilChanged

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BookContent(
    bookContentViewModel: BookContentViewModel,
    onBackIconClick: (Int) -> Unit
){
    val book by bookContentViewModel.book
    val uiState by bookContentViewModel.contentUIState.collectAsState()
    val ttsUiState by bookContentViewModel.ttsUiState.collectAsState()

    var triggerLoadChapter by remember { mutableStateOf(false) }
    var callbackLoadChapter by remember { mutableStateOf(false) }

//    val tts = rememberTextToSpeech()
//    val textMeasurer = rememberTextMeasurer()
//    var currentPosition by remember { mutableIntStateOf(0) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val pagerState = rememberPagerState(
        initialPage = uiState.currentChapterIndex,
        pageCount = { uiState.totalChapter }
    )
    val lazyListStates = remember { mutableStateMapOf<Int, LazyListState>() }
    val headers = remember{ mutableStateMapOf<Int,String>()}
    var currentLazyColumnState by remember { mutableStateOf<LazyListState?>(null) }
    val drawerLazyColumnState = rememberLazyListState()

    val textStyle = TextStyle(
        textIndent = TextIndent(firstLine = 40.sp),
        textAlign = TextAlign.Justify,
        fontSize = 24.sp,
        background = Color(0x80e2e873),
        lineBreak = LineBreak.Paragraph,
    )
    LaunchedEffect(Unit){
        drawerLazyColumnState.scrollToItem(uiState.currentChapterIndex)
        pagerState.animateScrollToPage(uiState.currentChapterIndex)
    }
    NavigationDrawer(
        bookContentViewModel = bookContentViewModel,
        uiState = uiState,
        book = book,
        drawerState = drawerState,
        drawerLazyColumnState = drawerLazyColumnState,
        onDrawerItemClick = { chapterIndex ->
            bookContentViewModel.updateCurrentChapterIndex(chapterIndex)
            bookContentViewModel.updateDrawerState(false)
        }
    ){
        LaunchedEffect(ttsUiState.isSpeaking) {
            bookContentViewModel.updateIsFocused(!ttsUiState.isSpeaking && ttsUiState.isPaused || ttsUiState.isSpeaking && !ttsUiState.isPaused)
        }
        LaunchedEffect(ttsUiState.isPaused) {
            bookContentViewModel.updateIsFocused(!(!ttsUiState.isSpeaking && !ttsUiState.isPaused))
        }
        LaunchedEffect(drawerState.currentValue) {
            if(drawerState.currentValue == DrawerValue.Closed) {
                bookContentViewModel.updateDrawerState(false)
                bookContentViewModel.updateTopBarState(false)
                bookContentViewModel.updateBottomBarState(false)
                drawerLazyColumnState.scrollToItem(uiState.currentChapterIndex)
            }
        }
        LaunchedEffect(uiState.currentChapterIndex) {
            drawerLazyColumnState.scrollToItem(uiState.currentChapterIndex)
            pagerState.animateScrollToPage(uiState.currentChapterIndex)
        }
        LaunchedEffect(uiState.drawerState){
            if(uiState.drawerState){
                drawerState.open()
            }else{
                drawerState.close()
                drawerLazyColumnState.animateScrollToItem(uiState.currentChapterIndex)
                pagerState.animateScrollToPage(uiState.currentChapterIndex)
            }
        }
        Scaffold(
            topBar = {
                TopBar(
                    topBarState = uiState.topBarState,
                    onMenuIconClick ={
                        bookContentViewModel.updateDrawerState(true)
                    },
                    onBackIconClick = {
                        onBackIconClick(uiState.currentChapterIndex+1)
                    }
                )
            },
            bottomBar = {
                BottomBarDefault(
                    uiState = uiState
                )
            }
        ){
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (uiState.enableScaffoldBar)
                            Modifier.clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) {
                                bookContentViewModel.updateBottomBarState(!uiState.bottomBarState)
                                bookContentViewModel.updateTopBarState(!uiState.topBarState)
                            }
                        else
                            Modifier
                    ),
            ) {
                LaunchedEffect(pagerState.currentPage) {
                    val currentPage = pagerState.currentPage
                    lazyListStates.keys.filter { pageIndex ->
                        pageIndex < currentPage - 2 || pageIndex > currentPage + 2
                    }.forEach { pageIndex ->
                        lazyListStates.remove(pageIndex)
                    }
                }
                LaunchedEffect(pagerState) {
                    snapshotFlow { pagerState.settledPage }
                        .distinctUntilChanged()
                        .collect { _ ->
                            triggerLoadChapter = true
                        }
                }
                LaunchedEffect(callbackLoadChapter) {
                    if (callbackLoadChapter) {
                        triggerLoadChapter = false
                        callbackLoadChapter = false
                    }
                }
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize(),
                    beyondViewportPageCount = 1,
                ) { page ->
                    val lazyListState = lazyListStates.getOrPut(page) { LazyListState() }
                    LaunchedEffect(uiState.currentChapterIndex) {
                        currentLazyColumnState = lazyListStates[uiState.currentChapterIndex]
                        bookContentViewModel.updateCurrentChapterHeader(headers[uiState.currentChapterIndex])
                    }
                    Chapter(
                        bookContentViewModel = bookContentViewModel,
                        uiState = uiState,
                        ttsUiState = ttsUiState,
                        totalChapter = uiState.totalChapter,
                        triggerLoadChapter = triggerLoadChapter,
                        textStyle = textStyle,
                        page = page,
                        pagerState = pagerState,
                        currentLazyColumnState = currentLazyColumnState,
                        contentLazyColumnState = lazyListState,
                        headers = headers,
                        currentChapter = { chapterIndex, readingIndex ->
                            bookContentViewModel.updateCurrentChapterIndex(chapterIndex)
                            bookContentViewModel.updateCurrentReadingParagraph(readingIndex)
                        },
                        callbackLoadChapter = {
                            callbackLoadChapter = it
                        }
                    )
                }
            }
        }
    }
}
@Composable
fun Chapter(
    bookContentViewModel: BookContentViewModel,
    uiState: ContentUIState,
    ttsUiState: TTSState,
    totalChapter: Int,
    triggerLoadChapter: Boolean,
    textStyle: TextStyle,
    page: Int,
    pagerState: PagerState,
    currentLazyColumnState: LazyListState?,
    contentLazyColumnState: LazyListState,
    headers: MutableMap<Int,String>,
    currentChapter: (Int, Int) -> Unit,
    callbackLoadChapter: (Boolean) -> Unit
) {
    val chapterContent by bookContentViewModel.chapterContent
    var data by remember { mutableStateOf<ChapterContentEntity?>(null) }
    val contentList = remember { mutableStateOf(listOf<@Composable (Boolean, Boolean) -> Unit>())}
    var header by remember { mutableStateOf("") }

    LaunchedEffect(triggerLoadChapter) {
        if (triggerLoadChapter && data == null) {
            bookContentViewModel.getChapterContent(page)
            data = chapterContent
            contentList.value = parseListToComposableList(textStyle, data!!.content)
            header = data!!.chapterTitle
            headers.getOrPut(page){data!!.chapterTitle}
            callbackLoadChapter(true)
        }
    }
    LaunchedEffect(pagerState.targetPage) {
        bookContentViewModel.updateFlagTriggerAdjustScroll(false)
        currentChapter(pagerState.targetPage,0)
    }
    LaunchedEffect(currentLazyColumnState) {
        if (currentLazyColumnState != null) {
            snapshotFlow { currentLazyColumnState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .collect { index ->
                    if (index != null) {
                        bookContentViewModel.updateLastVisibleItemIndex(index)
                    }
                }
        }
    }

    LaunchedEffect(currentLazyColumnState) {
        if (currentLazyColumnState != null) {
            snapshotFlow { currentLazyColumnState.layoutInfo.visibleItemsInfo.firstOrNull()?.index }
                .collect { index ->
                    if (index != null) {
                        bookContentViewModel.updateFirstVisibleItemIndex(index)
                    }
                }
        }
    }
    LaunchedEffect(currentLazyColumnState){
        if (currentLazyColumnState != null) {
            snapshotFlow { currentLazyColumnState.isScrollInProgress && !pagerState.isScrollInProgress }.collect { scrolling ->
                if (scrolling && (ttsUiState.isSpeaking || ttsUiState.isPaused) && ttsUiState.currentReadingParagraph == ttsUiState.firstVisibleItemIndex) {
                    bookContentViewModel.updateFlagTriggerAdjustScroll(true)
                }
            }
        }
    }

    LaunchedEffect(ttsUiState.currentReadingParagraph) {
        if ((ttsUiState.currentReadingParagraph >= ttsUiState.lastVisibleItemIndex
                    || ttsUiState.currentReadingParagraph <= ttsUiState.firstVisibleItemIndex)
            && !ttsUiState.flagTriggerScrolling
        ) {
            if(ttsUiState.isSpeaking)
            {
                currentLazyColumnState?.animateScrollToItem(ttsUiState.currentReadingParagraph)
                bookContentViewModel.updateFlagTriggerAdjustScroll(false)
            }
        }
    }

    LaunchedEffect(ttsUiState.flagTriggerScrolling){
        if(ttsUiState.flagTriggerScrolling)
            bookContentViewModel.updateFlagStartScrolling(true)
    }

    LaunchedEffect(ttsUiState.flagStartScrolling){
        if(ttsUiState.flagStartScrolling){
            if(ttsUiState.currentReadingParagraph != ttsUiState.firstVisibleItemIndex){
                currentLazyColumnState?.animateScrollToItem(ttsUiState.currentReadingParagraph)
                bookContentViewModel.updateFlagTriggerAdjustScroll(false)
                bookContentViewModel.updateFlagScrollAdjusted(true)
            }else if(!ttsUiState.flagTriggerAdjustScroll){
                currentLazyColumnState?.animateScrollBy(value = uiState.screenHeight.toFloat())
                bookContentViewModel.updateFlagTriggerAdjustScroll(false)
                bookContentViewModel.updateFlagStartScrolling(false)
            }else{
                bookContentViewModel.updateFlagStartScrolling(true)
            }
        }
    }

    LaunchedEffect(ttsUiState.flagStartAdjustScroll){
        if (ttsUiState.flagStartAdjustScroll) {
            currentLazyColumnState?.animateScrollToItem(ttsUiState.currentReadingParagraph)
            bookContentViewModel.updateFlagTriggerAdjustScroll(false)
            bookContentViewModel.updateFlagStartAdjustScroll(false)
            bookContentViewModel.updateFlagScrollAdjusted(true)
        }
    }

    LaunchedEffect(ttsUiState.flagScrollAdjusted){
        if (ttsUiState.flagScrollAdjusted) {
            currentLazyColumnState?.animateScrollBy(value = uiState.screenHeight.toFloat() * ttsUiState.scrollTime)
            bookContentViewModel.updateFlagTriggerAdjustScroll(false)
            bookContentViewModel.updateFlagScrollAdjusted(false)
            bookContentViewModel.updateFlagStartScrolling(false)
        }
    }
    Column(
        modifier = Modifier.fillMaxSize()
            .onGloballyPositioned {coordinates->
                bookContentViewModel.updateScreenWidth(coordinates.size.width)
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ){
            Text(
                modifier = Modifier.width(with(LocalDensity.current){uiState.screenWidth.toDp() - 100.dp}),
                text = header,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                modifier = Modifier.width(100.dp),
                text = "${pagerState.currentPage+1} / $totalChapter",
                textAlign = TextAlign.End,
                overflow = TextOverflow.Ellipsis,
            )
        }
        ChapterContents(
            bookContentViewModel = bookContentViewModel,
            uiState = uiState,
            isFocused = ttsUiState.isFocused,
            currentReadingItemIndex = ttsUiState.currentReadingParagraph,
            contentLazyColumnState = contentLazyColumnState,
            contentList = contentList.value,
        )
    }
}
@Composable
fun ChapterContents(
    bookContentViewModel: BookContentViewModel,
    uiState: ContentUIState,
    isFocused: Boolean,
    currentReadingItemIndex: Int,
    contentLazyColumnState: LazyListState,
    contentList: List<@Composable (Boolean, Boolean) -> Unit>,
){
    val view = LocalView.current
    CompositionLocalProvider(
        value = LocalTextToolbar provides
            CustomTextToolbar(
                view = view,
                scrollingPager = uiState.enablePagerScroll,
                enableScaffoldBar = uiState.enableScaffoldBar,
                output = { test->
                    Log.d("test",test)
                },
                updateState = {pager,scaffold->
                    bookContentViewModel.updateEnableScaffoldBar(pager)
                    bookContentViewModel.updateEnablePagerScroll(scaffold)
                }
            )
    ) {
        SelectionContainer{
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { coordinates ->
                        bookContentViewModel.updateScreenHeight(coordinates.size.height)
                    },
                state = contentLazyColumnState,
            ) {
                itemsIndexed(contentList) { index, composable ->
                    composable(index == currentReadingItemIndex, isFocused)
                }
            }
        }
    }
}

@SuppressLint("SdCardPath")
private fun parseListToComposableList(
    textStyle: TextStyle,
    paragraphs: List<String>
): List<@Composable (Boolean, Boolean) -> Unit> {
    val composable = mutableListOf<@Composable (Boolean,Boolean) -> Unit>()
    convertToAnnotatedStrings(paragraphs).forEach {
        val linkPattern = Regex("""/data/user/0/com\.capstone\.bookshelf/files/[^ ]*""")
        val headerPatten = Regex("""<h([1-6])[^>]*>(.*?)</h([1-6])>""")
        if(linkPattern.containsMatchIn(it)) {
            composable.add{ _, _ ->
                ImageComponent(ZoomableImage(it.text))
            }
        }else if(headerPatten.containsMatchIn(it)) {
            composable.add { isHighlighted, isSpeaking ->
                HeaderText(it.toString(), textStyle, isHighlighted, isSpeaking)
            }
        } else{
            composable.add {isHighlighted,isSpeaking ->
                ParagraphText(it,textStyle,isHighlighted,isSpeaking)
            }
        }
    }
    return composable
}

@SuppressLint("SdCardPath")
private fun cleanString(input: String): String {
    val htmlTagPattern = Regex(pattern = """<[^>]+>""")
    val linkPattern = Regex("""/data/user/0/com\.capstone\.bookshelf/files/[^ ]*""")
    var result = htmlTagPattern.replace(input, replacement = "")
    result = linkPattern.replace(result, replacement = " ")
    return result
}
private fun removeHtmlTagsFromList(list: List<String>): List<String> {
    return list.map { cleanString(it) }
}
private fun convertToAnnotatedStrings(paragraphs: List<String>): List<AnnotatedString> {
    return paragraphs.map { paragraph ->
        buildAnnotatedString {
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
}