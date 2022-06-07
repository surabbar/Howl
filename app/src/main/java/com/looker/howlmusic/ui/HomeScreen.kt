package com.looker.howlmusic.ui

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.BackdropValue.Concealed
import androidx.compose.material.BackdropValue.Revealed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.looker.components.*
import com.looker.components.ext.backgroundGradient
import com.looker.components.localComposers.LocalDurations
import com.looker.components.state.SheetsState
import com.looker.feature_player.ui.Controls
import com.looker.feature_player.ui.PlayerHeader
import com.looker.feature_player.ui.components.PlayPauseIcon
import com.looker.feature_player.ui.components.SeekBar
import com.looker.feature_player.utils.extension.toSong
import com.looker.howlmusic.navigation.TopLevelNavigation
import com.looker.howlmusic.ui.components.Backdrop
import com.looker.howlmusic.ui.components.BottomAppBar
import com.looker.howlmusic.ui.components.HomeNavGraph
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Home(
	navController: NavHostController,
	viewModel: HowlViewModel = viewModel()
) {
	val configuration = LocalConfiguration.current
	val state = rememberBackdropScaffoldState(Concealed)
	val currentSong by viewModel.nowPlaying.collectAsState()
	val isPlaying by viewModel.isPlaying.collectAsState()
	val dominantColorState = rememberDominantColorState()
	val expandedHeight = remember(configuration) { configuration.screenHeightDp.dp / 3 }

	Backdrop(
		state = state,
		// Best solution for now
		isPlaying = {
			animateDpAsState(
				targetValue = if (isPlaying) expandedHeight else BackdropScaffoldDefaults.PeekHeight,
				animationSpec = tween(LocalDurations.current.crossFade)
			).value
		},
		header = {
			PlayerHeader(
				modifier = Modifier
					.backgroundGradient(dominantColorState.color.overBackground())
					.statusBarsPadding(),
				songText = {
					AnimatedText(
						text = currentSong.toSong.name,
						maxLines = 2,
						style = MaterialTheme.typography.h4
					)
					AnimatedText(
						text = currentSong.toSong.artist,
						style = MaterialTheme.typography.subtitle1,
						fontWeight = FontWeight.SemiBold
					)
				},
				toggleIcon = {
					val toggle by viewModel.toggle.collectAsState()

					val toggleColor by animateColorAsState(
						targetValue =
						if (toggle) MaterialTheme.colors.secondaryVariant.overBackground()
						else MaterialTheme.colors.background,
						animationSpec = tween(LocalDurations.current.crossFade)
					)

					Button(
						modifier = Modifier
							.clip(MaterialTheme.shapes.medium)
							.align(Alignment.BottomEnd)
							.drawBehind { drawRect(toggleColor) },
						colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
						elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
						onClick = { viewModel.onToggleClick() },
					) {
						LaunchedEffect(state.currentValue) {
							viewModel.backdropValue.value = when (state.currentValue) {
								Concealed -> SheetsState.HIDDEN
								Revealed -> SheetsState.VISIBLE
							}
							viewModel.updateToggleIcon()
						}

						val toggleIcon by viewModel.toggleIcon.collectAsState()

						Icon(imageVector = toggleIcon, contentDescription = null)
					}
				}
			) {
				LaunchedEffect(currentSong.toSong.albumArt) {
					dominantColorState.updateColorsFromImageUrl(currentSong.toSong.albumArt)
				}
				val imageCorner by animateIntAsState(
					targetValue = if (isPlaying) 50 else 15,
					animationSpec = tween(LocalDurations.current.crossFade)
				)
				AsyncImage(
					modifier = Modifier
						.matchParentSize()
						.graphicsLayer {
							clip = true
							shape = RoundedCornerShape(imageCorner)
						},
					model = currentSong.toSong.albumArt,
					contentScale = ContentScale.Crop,
					contentDescription = "Album Art"
				)
			}
		},
		frontLayerContent = {
			val scope = rememberCoroutineScope()

			FrontLayer(
				navController = navController,
				openPlayer = {
					scope.launch {
						state.animateTo(Revealed, TweenSpec(400))
					}
				}
			)
		},
		backLayerContent = {
			Controls(
				skipNextClick = { viewModel.playNext() },
				skipPrevClick = { viewModel.playPrevious() },
				playButton = {
					val buttonShape by animateIntAsState(
						targetValue = if (isPlaying) 50 else 15,
						animationSpec = tween(LocalDurations.current.crossFade)
					)
					OpaqueIconButton(
						modifier = Modifier
							.height(60.dp)
							.weight(3f)
							.graphicsLayer {
								clip = true
								shape = RoundedCornerShape(buttonShape)
							},
						onClick = { viewModel.playMedia(currentSong.toSong) },
						backgroundColor = MaterialTheme.colors.primaryVariant.overBackground(0.9f),
						contentColor = MaterialTheme.colors.onPrimary
					) {
						val playIcon by viewModel.playIcon.collectAsState()
						PlayPauseIcon(playIcon)
					}
				}
			) {
				val progress by viewModel.progress.collectAsState()
				SeekBar(
					modifier = Modifier.height(60.dp),
					progress = progress,
					onValueChange = { viewModel.onSeek(it) },
					onValueChanged = { viewModel.onSeeked() }
				)
			}
		}
	)
}

@ExperimentalMaterialApi
@Composable
fun FrontLayer(
	navController: NavHostController,
	openPlayer: () -> Unit
) {
	val topLevelNavigation = remember(navController) {
		TopLevelNavigation(navController)
	}
	Scaffold(
		bottomBar = {
			val navBackStackEntry by navController.currentBackStackEntryAsState()
			val currentDestination = navBackStackEntry?.destination
			BottomAppBar(
				modifier = Modifier.windowInsetsBottomHeight(
					WindowInsets.navigationBars.add(WindowInsets(bottom = 56.dp))
				),
				currentDestination = currentDestination,
				onNavigate = topLevelNavigation::navigateUp
			)
		},
		floatingActionButton = {
			OpaqueIconButton(
				backgroundColor = MaterialTheme.colors.primaryVariant.overBackground(),
				contentPadding = PaddingValues(vertical = 16.dp),
				onClick = openPlayer,
				shape = MaterialTheme.shapes.small,
				icon = Icons.Rounded.KeyboardArrowDown
			)
		}
	) { bottomNavigationPadding ->
		Column(Modifier.padding(bottomNavigationPadding)) {
			HandleIcon { openPlayer() }
			HomeNavGraph(navController = navController)
		}
	}
}