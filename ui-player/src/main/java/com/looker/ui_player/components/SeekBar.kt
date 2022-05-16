package com.looker.ui_player.components

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SeekBar(
	modifier: Modifier = Modifier,
	progress: Float,
	onValueChange: (Float) -> Unit,
	onValueChanged: () -> Unit
) {
	val sliderColors = SliderDefaults.colors(inactiveTrackColor = MaterialTheme.colors.surface)

	Slider(
		modifier = modifier,
		value = progress,
		onValueChange = onValueChange,
		colors = sliderColors,
		onValueChangeFinished = onValueChanged
	)
}