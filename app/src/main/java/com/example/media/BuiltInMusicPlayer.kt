package com.example.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.example.model.DemoCatalog
import com.example.model.DemoTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class BuiltInMusicPlayer(
    private val context: Context,
    private val onStateUpdated: (DemoTrack, Boolean, Long, Bitmap?) -> Unit
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mainHandler = Handler(Looper.getMainLooper())

    private var currentIndex = 0
    private var isPlaying = false
    private var positionMs = 42_000L

    private var synthJob: Job? = null
    private var tickerJob: Job? = null
    private var mediaSession: MediaSession? = null

    private val artCache = mutableMapOf<Int, Bitmap?>()

    fun initializeSession() {
        if (mediaSession != null) return
        mainHandler.post {
            val session = MediaSession(context, "ToneFormSynthSession")
            session.setCallback(object : MediaSession.Callback() {
                override fun onPlay() {
                    play()
                }

                override fun onPause() {
                    pause()
                }

                override fun onSkipToNext() {
                    skipNext()
                }

                override fun onSkipToPrevious() {
                    skipPrevious()
                }

                override fun onSeekTo(pos: Long) {
                    seekTo(pos)
                }
            }, mainHandler)
            session.isActive = true
            mediaSession = session
            publishMediaSessionState()
        }
    }

    fun getCurrentTrack(): DemoTrack = DemoCatalog.tracks[currentIndex]

    fun getCurrentIndex(): Int = currentIndex

    fun isPlaying(): Boolean = isPlaying

    fun getPositionMs(): Long = positionMs

    fun getArtBitmap(track: DemoTrack): Bitmap? {
        return artCache.getOrPut(track.id) {
            val opts = BitmapFactory.Options().apply {
                inSampleSize = 2
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeResource(context.resources, track.artRes, opts)
        }
    }

    fun selectTrack(index: Int, autoPlay: Boolean = true) {
        val safeIdx = ((index % DemoCatalog.tracks.size) + DemoCatalog.tracks.size) % DemoCatalog.tracks.size
        currentIndex = safeIdx
        positionMs = 0L
        if (autoPlay) {
            play()
        } else {
            notifyAndPublish()
        }
    }

    fun togglePlayPause() {
        if (isPlaying) pause() else play()
    }

    fun play() {
        initializeSession()
        if (isPlaying) return
        isPlaying = true
        startSynthAudio()
        startTicker()
        notifyAndPublish()
    }

    fun pause() {
        if (!isPlaying) return
        isPlaying = false
        stopSynthAudio()
        stopTicker()
        notifyAndPublish()
    }

    fun skipNext() {
        selectTrack(currentIndex + 1, autoPlay = true)
    }

    fun skipPrevious() {
        if (positionMs > 4_000L) {
            positionMs = 0L
            notifyAndPublish()
        } else {
            selectTrack(currentIndex - 1, autoPlay = true)
        }
    }

    fun seekTo(posMs: Long) {
        val duration = getCurrentTrack().durationMs
        positionMs = posMs.coerceIn(0L, duration)
        notifyAndPublish()
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive && isPlaying) {
                delay(1000L)
                val track = getCurrentTrack()
                positionMs += 1000L
                if (positionMs >= track.durationMs) {
                    positionMs = 0L
                    currentIndex = (currentIndex + 1) % DemoCatalog.tracks.size
                }
                notifyAndPublish()
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private fun startSynthAudio() {
        synthJob?.cancel()
        synthJob = scope.launch(Dispatchers.IO) {
            val sampleRate = 22050
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(2048)

            var audioTrack: AudioTrack? = null
            try {
                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBuf * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack.play()
                val buffer = ShortArray(1024)
                var sampleIndex = 0L
                val chordRatios = doubleArrayOf(1.0, 1.25, 1.5, 1.875) // Warm Maj7 chord

                while (isActive && isPlaying) {
                    val track = getCurrentTrack()
                    val base = track.baseFreqHz
                    val stepSamples = (sampleRate * 60.0 / track.tempoBpm).toLong().coerceAtLeast(4000L)

                    for (i in buffer.indices) {
                        val t = sampleIndex.toDouble() / sampleRate
                        val chordStep = ((sampleIndex / (stepSamples * 2)) % 3).toInt()
                        val rootShift = when (chordStep) {
                            1 -> 1.12246 // +2 semitones
                            2 -> 0.89090 // -2 semitones
                            else -> 1.0
                        }
                        val noteEnv = 0.55 + 0.45 * sin(2.0 * PI * t * (track.tempoBpm / 120.0))
                        var mix = 0.0
                        for (r in chordRatios) {
                            mix += sin(2.0 * PI * (base * rootShift * r) * t)
                        }
                        val sample = (mix / chordRatios.size) * noteEnv * 0.14
                        buffer[i] = (sample * Short.MAX_VALUE).toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                            .toShort()
                        sampleIndex++
                    }
                    audioTrack.write(buffer, 0, buffer.size)
                }
            } catch (_: Throwable) {
                // Audio device unavailable in headless test environment; playback state still advances
            } finally {
                try {
                    audioTrack?.stop()
                    audioTrack?.release()
                } catch (_: Throwable) {
                }
            }
        }
    }

    private fun stopSynthAudio() {
        synthJob?.cancel()
        synthJob = null
    }

    private fun notifyAndPublish() {
        val track = getCurrentTrack()
        val art = getArtBitmap(track)
        publishMediaSessionState()
        onStateUpdated(track, isPlaying, positionMs, art)
    }

    private fun publishMediaSessionState() {
        val session = mediaSession ?: return
        val track = getCurrentTrack()
        val art = getArtBitmap(track)
        mainHandler.post {
            val metadata = MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, track.title)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, track.artist)
                .putString(MediaMetadata.METADATA_KEY_ALBUM, track.album)
                .putLong(MediaMetadata.METADATA_KEY_DURATION, track.durationMs)
                .apply {
                    if (art != null) {
                        putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, art)
                    }
                }
                .build()
            session.setMetadata(metadata)

            val stateCode = if (isPlaying) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED
            val actions = PlaybackState.ACTION_PLAY or
                PlaybackState.ACTION_PAUSE or
                PlaybackState.ACTION_PLAY_PAUSE or
                PlaybackState.ACTION_SKIP_TO_NEXT or
                PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                PlaybackState.ACTION_SEEK_TO

            val state = PlaybackState.Builder()
                .setActions(actions)
                .setState(stateCode, positionMs, if (isPlaying) 1.0f else 0.0f, SystemClock.elapsedRealtime())
                .build()
            session.setPlaybackState(state)
        }
    }

    fun release() {
        stopSynthAudio()
        stopTicker()
        mainHandler.post {
            mediaSession?.release()
            mediaSession = null
        }
    }
}
