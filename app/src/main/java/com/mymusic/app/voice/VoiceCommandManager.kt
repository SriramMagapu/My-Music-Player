package com.mymusic.app.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.mymusic.app.data.MusicRepository
import com.mymusic.app.model.CustomVoiceCommand
import com.mymusic.app.model.VoiceAction
import com.mymusic.app.model.VoiceState
import com.mymusic.app.playback.MusicPlaybackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class VoiceCommandManager(
    private val context: Context,
    private val repository: MusicRepository
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _soundLevel = MutableStateFlow(0f)
    val soundLevel: StateFlow<Float> = _soundLevel.asStateFlow()

    init {
        checkAvailability()
    }

    private fun checkAvailability() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _voiceState.value = VoiceState.Unavailable("On-device voice recognition is not available.")
        }
    }

    fun startListening(isTrainingMode: Boolean = false, trainingAction: VoiceAction? = null) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _voiceState.value = VoiceState.Unavailable("Speech recognition not available on this device.")
            return
        }

        stopListening()

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _voiceState.value = VoiceState.Listening("Listening for your command...")
                    }

                    override fun onBeginningOfSpeech() {
                        _voiceState.value = VoiceState.Listening("Hearing you...")
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        _soundLevel.value = (rmsdB / 10f).coerceIn(0f, 1f)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _voiceState.value = VoiceState.Processing("Understanding your voice...")
                    }

                    override fun onError(error: Int) {
                        val message = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Please try again."
                            SpeechRecognizer.ERROR_CLIENT -> "Client error. Tap mic to retry."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                            SpeechRecognizer.ERROR_NETWORK -> "Network error (offline speech is supported)."
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Timeout. Tap mic and try again."
                            SpeechRecognizer.ERROR_NO_MATCH -> "Couldn't understand that. Speak clearly and try again."
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer is busy. Try again."
                            SpeechRecognizer.ERROR_SERVER -> "Server error. Try again."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech heard. Tap mic to start."
                            else -> "Could not recognize command. Please try again."
                        }
                        _voiceState.value = VoiceState.Error(message)
                        scope.launch {
                            delay(3000)
                            if (_voiceState.value is VoiceState.Error) {
                                _voiceState.value = VoiceState.Idle
                            }
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val phrase = matches?.firstOrNull()?.trim()

                        if (!phrase.isNullOrEmpty()) {
                            if (isTrainingMode && trainingAction != null) {
                                handleTrainingResult(phrase, trainingAction)
                            } else {
                                handleCommandExecution(phrase)
                            }
                        } else {
                            _voiceState.value = VoiceState.Error("No command heard. Try again.")
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partials = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = partials?.firstOrNull()
                        if (!text.isNullOrEmpty()) {
                            _voiceState.value = VoiceState.Listening("\"$text\"")
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _voiceState.value = VoiceState.Error("Voice activation failed: ${e.localizedMessage}")
        }
    }

    private fun handleTrainingResult(phrase: String, action: VoiceAction) {
        scope.launch {
            repository.saveVoiceCommand(phrase, action)
            _voiceState.value = VoiceState.Saved(phrase, action)
            delay(2500)
            _voiceState.value = VoiceState.Idle
        }
    }

    private fun handleCommandExecution(phrase: String) {
        val normalized = phrase.lowercase().trim()
        val customCommands = repository.voiceCommands.value
        val service = MusicPlaybackService.instance

        // 1. Check exact custom saved commands
        val matchedCustom = customCommands.find { it.normalizedPhrase == normalized || normalized.contains(it.normalizedPhrase) }
        if (matchedCustom != null) {
            executeAction(matchedCustom.action, matchedCustom.targetParam, phrase)
            return
        }

        // 2. Check standard built-in phrases
        for (action in VoiceAction.values()) {
            if (action.defaultPhrases.any { normalized.contains(it) }) {
                executeAction(action, null, phrase)
                return
            }
        }

        // 3. Check smart voice search ("play <song/artist>")
        if (normalized.startsWith("play ")) {
            val query = normalized.removePrefix("play ").trim()
            val allTracks = repository.allTracks.value
            val matchedTrack = allTracks.find {
                it.title.lowercase().contains(query) || it.artist.lowercase().contains(query)
            }
            if (matchedTrack != null && service != null) {
                service.playTrack(matchedTrack)
                _voiceState.value = VoiceState.Success("Playing \"${matchedTrack.title}\"")
                resetStateAfterDelay()
                return
            }
        }

        _voiceState.value = VoiceState.Error("Unrecognized: \"$phrase\". Tap to teach My Music.")
        resetStateAfterDelay()
    }

    private fun executeAction(action: VoiceAction, targetParam: String?, phrase: String) {
        val service = MusicPlaybackService.instance
        if (service == null) {
            _voiceState.value = VoiceState.Error("Player is not active.")
            resetStateAfterDelay()
            return
        }

        when (action) {
            VoiceAction.PLAY -> {
                service.play()
                _voiceState.value = VoiceState.Success("Resumed playback")
            }
            VoiceAction.PAUSE -> {
                service.pause()
                _voiceState.value = VoiceState.Success("Playback paused")
            }
            VoiceAction.NEXT -> {
                service.next()
                _voiceState.value = VoiceState.Success("Skipped to next track")
            }
            VoiceAction.PREVIOUS -> {
                service.previous()
                _voiceState.value = VoiceState.Success("Playing previous track")
            }
            VoiceAction.SHUFFLE -> {
                service.toggleShuffle()
                _voiceState.value = VoiceState.Success("Shuffle toggled")
            }
            VoiceAction.FAVORITE -> {
                val current = service.currentTrack.value
                if (current != null) {
                    scope.launch {
                        repository.toggleFavorite(current.id)
                    }
                    _voiceState.value = VoiceState.Success("Added to Favorites")
                } else {
                    _voiceState.value = VoiceState.Error("No track currently playing")
                }
            }
            VoiceAction.VOLUME_UP -> {
                val am = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
                am.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC, android.media.AudioManager.ADJUST_RAISE, android.media.AudioManager.FLAG_SHOW_UI)
                _voiceState.value = VoiceState.Success("Volume increased")
            }
            VoiceAction.VOLUME_DOWN -> {
                val am = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
                am.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC, android.media.AudioManager.ADJUST_LOWER, android.media.AudioManager.FLAG_SHOW_UI)
                _voiceState.value = VoiceState.Success("Volume decreased")
            }
            VoiceAction.SLEEP_TIMER_30 -> {
                service.setSleepTimer(30)
                _voiceState.value = VoiceState.Success("Sleep timer set for 30 minutes")
            }
            VoiceAction.PLAY_FAVORITES -> {
                val favs = repository.favorites.value
                if (favs.isNotEmpty()) {
                    service.playTracks(favs, 0)
                    _voiceState.value = VoiceState.Success("Playing your favorite tracks")
                } else {
                    _voiceState.value = VoiceState.Error("No favorites added yet")
                }
            }
        }
        resetStateAfterDelay()
    }

    private fun resetStateAfterDelay() {
        scope.launch {
            delay(2500)
            if (_voiceState.value !is VoiceState.Listening) {
                _voiceState.value = VoiceState.Idle
            }
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (_: Exception) {}
        _soundLevel.value = 0f
    }
}
