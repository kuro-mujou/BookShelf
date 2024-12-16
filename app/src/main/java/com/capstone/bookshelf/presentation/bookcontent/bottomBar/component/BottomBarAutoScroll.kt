package com.capstone.bookshelf.presentation.bookcontent.bottomBar.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarState
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollState
import com.capstone.bookshelf.presentation.bookcontent.component.dialog.AutoScrollMenuDialog

@Composable
fun BottomBarAutoScroll(
    bottomBarState: BottomBarState,
    autoScrollState: AutoScrollState,
    onPreviousChapterIconClick: () -> Unit,
    onPlayPauseIconClick: () -> Unit,
    onNextChapterIconClick: () -> Unit,
    onStopIconClick: () -> Unit,
    onSettingIconClick: () -> Unit,
    onDismissDialogRequest: () -> Unit,
){
    val iconList = listOf(
        R.drawable.ic_previous_chapter,
        R.drawable.ic_play,
        R.drawable.ic_pause,
        R.drawable.ic_next_chapter,
        R.drawable.ic_stop,
        //R.drawable.ic_setting
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.surfaceContainer),
        horizontalAlignment = Alignment.CenterHorizontally,

    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ){
            IconButton(
                modifier = Modifier.size(50.dp),
                onClick = {
                    onPreviousChapterIconClick()
                }
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(id = iconList[0]),
                    contentDescription = "previous chapter"
                )
            }
            IconButton(
                modifier = Modifier.size(50.dp),
                onClick = {
                    onPlayPauseIconClick()
                }
            ) {
                if(autoScrollState.isAutoScroll) {
                    Icon(
                        modifier = Modifier.size(30.dp),
                        painter = painterResource(id = iconList[1]),
                        contentDescription = "play/pause"
                    )
                }else{
                    Icon(
                        modifier = Modifier.size(30.dp),
                        painter = painterResource(id = iconList[2]),
                        contentDescription = "play/pause"
                    )
                }
            }
            IconButton(
                modifier = Modifier.size(50.dp),
                onClick = {
                    onNextChapterIconClick()
                }
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(id = iconList[3]),
                    contentDescription = "next chapter"
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth()
                .navigationBarsPadding()
                .wrapContentHeight()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ){
            IconButton(
                modifier = Modifier.size(50.dp),
                onClick = {
                    onStopIconClick()
                }
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(id = iconList[4]),
                    contentDescription = "stop"
                )
            }
            IconButton(
                modifier = Modifier.size(50.dp),
                onClick = {
                    onSettingIconClick()
                }
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(id = iconList[4]),
                    contentDescription = "setting"
                )
            }
        }

        if(bottomBarState.openAutoScrollMenu){
            AutoScrollMenuDialog(
                autoScrollState = autoScrollState,
                onDismissRequest = {
//                    viewModel.changeMenuTriggerAutoScroll(false)
                    onDismissDialogRequest()
                }
            )
        }
    }
}