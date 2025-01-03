package com.capstone.bookshelf.presentation.bookcontent.bottomBar.component

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarState
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.component.dialog.VoiceMenuDialog
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TTSState

@Composable
fun BottomBarSetting(
    bottomBarState: BottomBarState,
    ttsState: TTSState,
    colorPaletteState: ColorPalette,
    textToSpeech: TextToSpeech,
    context: Context,
    onSwitchChange: (Boolean) -> Unit
) {
    if(bottomBarState.openTTSVoiceMenu){
        VoiceMenuDialog(
            bottomBarState = bottomBarState,
            ttsState = ttsState,
            colorPaletteState = colorPaletteState,
            textToSpeech = textToSpeech,
            onDismiss = {

            },
            testVoiceButtonClicked = {
                val tts = TextToSpeech(context, null)
                tts.speak("This is an example of a voice", TextToSpeech.STOPPED, null, "utteranceId")
                tts.stop()
                tts.shutdown()
            }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(colorPaletteState.backgroundColor),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            modifier = Modifier
                .padding(4.dp),
            text = "Setting",
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorPaletteState.textColor
            )
        )
        HorizontalDivider(thickness = 2.dp)
        Row(
            modifier = Modifier.fillMaxWidth().height(50.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Keep Screen On",
                style = TextStyle(
                    color = colorPaletteState.textColor
                )
            )
            Switch(
                checked = bottomBarState.screenShallBeKeptOn,
                onCheckedChange = {
//                    viewModel.updateKeepScreenOn(it)
//                    viewModel.updateBookSettingKeepScreenOn(it)
                    onSwitchChange(it)
                },
                colors = SwitchDefaults.colors(
//                    checkedThumbColor = colorPaletteState.onBackground,
//                    checkedTrackColor = colorPaletteState.onBackground,
//                    checkedBorderColor = colorPaletteState.onBackground,
//                    checkedIconColor = colorPaletteState.onBackground,
//                    uncheckedThumbColor = colorPaletteState.onBackground,
                    uncheckedTrackColor = colorPaletteState.textColor,
//                    uncheckedBorderColor = colorPaletteState.onBackground,
//                    uncheckedIconColor = colorPaletteState.onBackground,
//                    disabledCheckedThumbColor = colorPaletteState.onBackground,
//                    disabledCheckedTrackColor = colorPaletteState.onBackground,
//                    disabledCheckedBorderColor = colorPaletteState.onBackground,
//                    disabledCheckedIconColor = colorPaletteState.onBackground,
//                    disabledUncheckedThumbColor = colorPaletteState.onBackground,
//                    disabledUncheckedTrackColor = colorPaletteState.onBackground,
//                    disabledUncheckedBorderColor = colorPaletteState.onBackground,
//                    disabledUncheckedIconColor = colorPaletteState.onBackground,
                )
            )
        }
        HorizontalDivider(thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clickable {
//                    viewModel.changeMenuTriggerVoice(true)
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Text to Speech",
                style = TextStyle(
                    color = colorPaletteState.textColor
                )
            )
            Icon(
                modifier = Modifier.size(30.dp),
                painter = painterResource(id = R.drawable.ic_setting),
                tint = colorPaletteState.textColor,
                contentDescription = "text to speech"
            )
        }
        HorizontalDivider(thickness = 1.dp)
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .height(50.dp)
                .clickable{

                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Auto Scroll Up",
                style = TextStyle(
                    color = colorPaletteState.textColor
                )
            )
            Icon(
                modifier = Modifier.size(30.dp),
                painter = painterResource(id = R.drawable.ic_setting),
                tint = colorPaletteState.textColor,
                contentDescription = "auto scroll up"
            )
        }
    }
}