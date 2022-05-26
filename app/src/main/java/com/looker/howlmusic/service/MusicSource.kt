package com.looker.howlmusic.service

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_URI
import androidx.core.net.toUri
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.looker.data_music.data.SongsRepository
import com.looker.howlmusic.utils.extension.toMediaMetadataCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class MusicSource @Inject constructor(private val songsRepository: SongsRepository) {

	var songs = emptyList<MediaMetadataCompat>()

	private var state: State = State.STATE_CREATED
		set(value) {
			if (value == State.STATE_INITIALIZED || value == State.STATE_ERROR) {
				synchronized(onReadyListeners) {
					field = value
					onReadyListeners.forEach { listener ->
						listener(state == State.STATE_INITIALIZED)
					}
				}
			} else {
				field = value
			}
		}

	init {
		state = State.STATE_INITIALIZING
	}

	suspend fun load() {
		fetchMediaData()?.let { songList ->
			songs = songList
			state = State.STATE_INITIALIZED
		} ?: run {
			songs = emptyList()
			state = State.STATE_ERROR
		}
	}

	private suspend fun fetchMediaData() = withContext(Dispatchers.IO) {
		try {
			songsRepository.getAllSongs().map { it.toMediaMetadataCompat }
		} catch (ioException: IOException) {
			null
		}
	}

	fun asMediaSource(dataSourceFactory: DefaultDataSource.Factory): ConcatenatingMediaSource {
		val concatenatingMediaSource = ConcatenatingMediaSource()
		songs.forEach { song ->
			val mediaItem = MediaItem.fromUri(song.getString(METADATA_KEY_MEDIA_URI) ?: "null")
			val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
				.createMediaSource(mediaItem)
			concatenatingMediaSource.addMediaSource(mediaSource)
		}
		return concatenatingMediaSource
	}

	fun asMediaItem() = songs.map { song ->
		val mediaUri = song.getString(METADATA_KEY_MEDIA_URI) ?: "null"
		val description = MediaDescriptionCompat.Builder()
			.setMediaUri(mediaUri.toUri())
			.setTitle(song.description.title)
			.setSubtitle(song.description.subtitle)
			.setMediaId(song.description.mediaId ?: "null")
			.setIconUri(song.description.iconUri)
			.build()
		MediaBrowserCompat.MediaItem(description, FLAG_PLAYABLE)
	}.toMutableList()

	private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

	fun whenReady(performAction: (Boolean) -> Unit): Boolean =
		when (state) {
			State.STATE_CREATED, State.STATE_INITIALIZING -> {
				onReadyListeners += performAction
				false
			}
			else -> {
				performAction(state != State.STATE_ERROR)
				true
			}
		}
}

enum class State {
	STATE_CREATED,
	STATE_INITIALIZING,
	STATE_INITIALIZED,
	STATE_ERROR
}