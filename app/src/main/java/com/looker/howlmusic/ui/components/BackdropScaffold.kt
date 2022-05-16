package com.looker.howlmusic.ui.components

import androidx.compose.material.BackdropScaffold
import androidx.compose.material.BackdropScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@Composable
fun Backdrop(
	modifier: Modifier = Modifier,
	state: BackdropScaffoldState,
	header: @Composable () -> Unit,
	backLayerContent: @Composable () -> Unit,
	frontLayerContent: @Composable () -> Unit
) {
	BackdropScaffold(
		modifier = modifier,
		scaffoldState = state,
		appBar = header,
		backLayerContent = backLayerContent,
		frontLayerContent = frontLayerContent,
		backLayerBackgroundColor = MaterialTheme.colors.background,
		peekHeight = 50.dp,
		frontLayerShape = MaterialTheme.shapes.large,
		frontLayerBackgroundColor = MaterialTheme.colors.background,
	)
}