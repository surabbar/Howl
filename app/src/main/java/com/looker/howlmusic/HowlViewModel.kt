package com.looker.howlmusic

import android.content.Context
import androidx.compose.material.BackdropScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.RenderersFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.extractor.flac.FlacExtractor
import com.google.android.exoplayer2.extractor.mp3.Mp3Extractor
import com.google.android.exoplayer2.extractor.mp4.Mp4Extractor
import com.google.android.exoplayer2.extractor.ogg.OggExtractor
import com.google.android.exoplayer2.extractor.ts.Ac3Extractor
import com.google.android.exoplayer2.extractor.ts.AdtsExtractor
import com.google.android.exoplayer2.extractor.wav.WavExtractor
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector
import com.looker.domain_music.Song
import kotlinx.coroutines.launch
import java.time.Clock

class HowlViewModel : ViewModel() {

    private lateinit var exoPlayer: SimpleExoPlayer

    private val _playing = MutableLiveData<Boolean>()
    private val _shuffle = MutableLiveData<Boolean>()
    private val _progress = MutableLiveData<Float>()
    private val _playIcon = MutableLiveData<ImageVector>()
    private val _handleIcon = MutableLiveData<ImageVector>()
    private val _currentSong = MutableLiveData<Song>()
    private val _enableGesture = MutableLiveData<Boolean>()
    private val _clock = MutableLiveData<Long>()

    val playing: LiveData<Boolean> = _playing
    val shuffle: LiveData<Boolean> = _shuffle
    val progress: LiveData<Float> = _progress
    val handleIcon: LiveData<ImageVector> = _handleIcon
    val currentSong: LiveData<Song> = _currentSong
    val playIcon: LiveData<ImageVector> = _playIcon
    val enableGesture: LiveData<Boolean> = _enableGesture
    val clock: LiveData<Long> = _clock

    fun buildExoPlayer(context: Context) {
        val audioOnlyRenderersFactory =
            RenderersFactory { handler, _, audioListener, _, _ ->
                arrayOf(
                    MediaCodecAudioRenderer(
                        context, MediaCodecSelector.DEFAULT, handler, audioListener
                    )
                )
            }

        val audioOnlyExtractorFactory = ExtractorsFactory {
            arrayOf(
                Mp3Extractor(),
                WavExtractor(),
                AdtsExtractor(),
                OggExtractor(),
                Ac3Extractor(),
                Mp4Extractor(),
                FlacExtractor()
            )
        }

        exoPlayer = SimpleExoPlayer.Builder(
            context,
            audioOnlyRenderersFactory,
            audioOnlyExtractorFactory
        ).build()
    }

    fun gestureState(allowGesture: Boolean) {
        _enableGesture.value = allowGesture
    }

    fun updateTime() {
        viewModelScope.launch { _clock.value = Clock.systemDefaultZone().millis() }
    }

    private fun updatePlayIcon() {
        _playIcon.value =
            if (_playing.value == true) Icons.Rounded.Pause else Icons.Rounded.PlayArrow
    }

    fun onPlayPause() {
        if (_playing.value == true) exoPlayer.pause() else exoPlayer.play()
        _playing.value = exoPlayer.isPlaying
        updatePlayIcon()
    }

    fun updateProgress() {
        _progress.value = exoPlayer.contentPosition.toFloat() / exoPlayer.contentDuration
    }

    fun onToggle(playerVisible: Boolean) {
        if (playerVisible) exoPlayer.shuffleModeEnabled = _shuffle.value ?: false
        else onPlayPause()
    }

    fun setHandleIcon(playerVisible: Boolean) {
        _handleIcon.value = if (playerVisible) Icons.Rounded.ArrowDropUp
        else Icons.Rounded.ArrowDropDown
    }

    fun onSongClicked(song: Song) {
        _playing.value = true
        _currentSong.value = song
        exoPlayer.apply {
            clearMediaItems()
            setMediaItem(MediaItem.fromUri(song.songUri))
            prepare()
            play()
        }
        updatePlayIcon()
    }

    fun onSeek(seekTo: Float) {
        _progress.value = seekTo
        exoPlayer.seekTo((exoPlayer.contentDuration * seekTo).toLong())
    }

    fun playNext() {
        exoPlayer.seekToNext()
    }

    fun playPrevious() {
        exoPlayer.seekToPrevious()
    }

    @ExperimentalMaterialApi
    fun playerVisible(state: BackdropScaffoldState): Boolean = state.isRevealed

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }
}
