package com.capstone.bookshelf.presentation.bookcontent.drawer.component.toc

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerState

@Composable
fun TableOfContents(
    drawerContainerState : DrawerContainerState,
    contentState : ContentState,
    drawerLazyColumnState: LazyListState,
    onDrawerItemClick: (Int) -> Unit,
) {
    var searchInput by remember { mutableStateOf("") }
    var targetDatabaseIndex by remember { mutableIntStateOf(-1) }
    var flag by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(flag) {
        if(flag){
            drawerLazyColumnState.scrollToItem(targetDatabaseIndex)
            flag = false
        }
    }
    LaunchedEffect(drawerContainerState.drawerState) {
        if (!drawerContainerState.drawerState) {
            searchInput = ""
            targetDatabaseIndex = -1
            flag = false
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }
    ModalDrawerSheet(
        drawerShape = RectangleShape,
        drawerContainerColor = MaterialTheme.colorScheme.background,
    ) {
        OutlinedTextField(
            value = searchInput,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) {
                    searchInput = newValue
                }
            },
            label = { Text("Enter a chapter number") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    val chapterIndex = searchInput.toIntOrNull()
                    if (chapterIndex != null) {
                        targetDatabaseIndex = if(chapterIndex < drawerContainerState.tableOfContents.size)
                            chapterIndex
                        else
                            drawerContainerState.tableOfContents.size-1
                        flag = true
                    }
                }
            ),
            modifier = Modifier.fillMaxWidth()
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = drawerLazyColumnState,
        ) {
            items(
                items = drawerContainerState.tableOfContents
            ) { tocItem ->
                NavigationDrawerItem(
                    label = {
                        Text(
                            text = tocItem.title,
                            style =
                                if (drawerContainerState.tableOfContents.indexOf(tocItem) == targetDatabaseIndex) {
                                    TextStyle(
                                        color = Color.Green,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else if(drawerContainerState.tableOfContents.indexOf(tocItem) == contentState.currentChapterIndex){
                                    TextStyle(
                                        fontStyle = FontStyle.Italic,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else{
                                    TextStyle(

                                    )
                                }
                        )
                    },
                    selected = drawerContainerState.tableOfContents.indexOf(tocItem) == contentState.currentChapterIndex,
                    onClick = {
                        onDrawerItemClick(drawerContainerState.tableOfContents.indexOf(tocItem))
                    },
                    modifier = Modifier.wrapContentHeight(),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                        unselectedContainerColor = Color.Transparent,
                        selectedTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        unselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    )
                )
            }
        }
    }
}