package com.capstone.bookshelf.presentation.bookcontent.component.colorpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ColorPicker() {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .padding(8.dp)
    ) {

        val Blue400 = Color(0xff42A5F5)
        var hue by remember { mutableFloatStateOf(0f) }
        var saturation by remember { mutableFloatStateOf(0.5f) }
        var lightness by remember { mutableFloatStateOf(0.5f) }

        val color = Color.hsl(hue = hue, saturation = saturation, lightness = lightness)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Color",
                color = Blue400,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp)
            )

            // Initial and Current Colors
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 50.dp, vertical = 20.dp)
            ) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .background(
                            Color.Black,
                            shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                        )
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .background(
                            color,
                            shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                        )
                )
            }

            // ColorWheel for hue selection
            // SaturationRhombus for saturation and lightness selections
            Box(
                modifier = Modifier.padding(8.dp),
                contentAlignment = Alignment.Center
            ) {

                ColorPickerWheel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    selectionRadius = 8.dp
                ) { hueChange ->
                    hue = hueChange.toFloat()
                }

                SaturationRhombus(
                    modifier = Modifier.size(200.dp),
                    hue = hue,
                    saturation = saturation,
                    lightness = lightness,
                    selectionRadius = 8.dp
                ) { s, l ->
                    println("CHANGING sat: $s, lightness: $l")
                    saturation = s
                    lightness = l
                }
            }

            // Sliders
            ColorSlider(
                modifier = Modifier
                    .padding(start = 12.dp, end = 12.dp)
                    .fillMaxWidth(),
                title = "Hue",
                titleColor = Color.Red,
                rgb = hue,
                onColorChanged = {
                    hue = it
                },
                valueRange = 0f..360f
            )
            Spacer(modifier = Modifier.height(4.dp))
            ColorSlider(
                modifier = Modifier
                    .padding(start = 12.dp, end = 12.dp)
                    .fillMaxWidth(),
                title = "Saturation",
                titleColor = Color.Green,
                rgb = saturation * 100f,
                onColorChanged = {
                    saturation = it / 100f
                },
                valueRange = 0f..100f
            )
            Spacer(modifier = Modifier.height(4.dp))

            ColorSlider(
                modifier = Modifier
                    .padding(start = 12.dp, end = 12.dp)
                    .fillMaxWidth(),
                title = "Lightness",
                titleColor = Color.Blue,
                rgb = lightness * 100f,
                onColorChanged = {
                    lightness = it / 100f
                },
                valueRange = 0f..100f
            )

        }

    }
}