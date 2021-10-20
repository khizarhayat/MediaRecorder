package com.khizar.mediarecorder.media_player

import android.media.MediaPlayer
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatImageView
import com.khizar.mediarecorder.AUDIO_MODES
import com.khizar.mediarecorder.R
import com.khizar.mediarecorder.getFormattedTimer
import com.khizar.mediarecorder.launchPeriodicAsync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.io.IOException

class HKHMediaPlayer {
    private val LOG_TAG = "HKHMediaPlayer"
    private var player: MediaPlayer? = null
    private var jobRunnable: Deferred<Any>? = null
    private var views: GetViews
    private var callBack: MPCallBack
    private var audioUrl: String? = null

    constructor(
        getViews: GetViews,
        callBack: MPCallBack, file: File? = null,
        url: String? = "",
    ) {
        this.views = getViews
        this.callBack = callBack
        audioUrl = url//?.trim()?.replace(" ","")
        setSeekBarListener()
    }

    fun setListeners() {
        getPlayButton().setOnClickListener {
            if (isAudioPlayInProgress()) {
                onPauseClick()
            } else {
                onPlayClick()
            }
        }
    }

    private fun onPlayClick() {
        getPlayButton().setImageResource(R.drawable.ic_pause)
        playAudioFile()
        callBack.startPlaying()
    }

    private fun playAudioFile() {
        onPlay(true)
    }

    private fun startPlaying() {
        if (audioUrl.isNullOrEmpty()) {
            return
        }
        if (player == null) {
            player = MediaPlayer().apply {
                try {
                    setDataSource(audioUrl!!)
                    prepare()
                    start()
                    setOnCompletionListener {
                        Log.e(LOG_TAG, "setOnCompletionListener")
                        stopPlaying()
                    }
                    // Get the current audio stats
                } catch (e: IOException) {
                    Log.e(LOG_TAG, "prepare() failed")
                }
            }
        } else {
            player?.start()
        }
        initializeSeekBar()
    }

    private fun stopPlaying() {
        player?.release()
        player = null
        removeCallBack()
        showPlayAssets()
    }

    private fun onPlay(start: Boolean) = if (start) {
        startPlaying()
    } else {
        pausePlaying()
    }

    private fun pausePlaying() {
        removeCallBack()
        player?.pause()
    }

    private fun removeCallBack() {
        jobRunnable?.cancel()
        Log.e(LOG_TAG, "removeCallBack")
    }

    fun handleCurrentState() {
        if (isAudioPlayInProgress()) {
            onPlayClick()
        } else {
            onPauseClick()
        }
    }

    private fun showPlayAssets() {
        getPlayButton().setImageResource(R.drawable.ic_play_icon)
        callBack.stopPlaying()
    }

    protected fun initializeSeekBar() {
        getSeekBar().max = (player?.duration ?: 1) / 1000
        removeCallBack()
        jobRunnable = CoroutineScope(Dispatchers.IO).launchPeriodicAsync(1000) {
            val mCurrentPosition: Int = (player!!.currentPosition / 1000) + 1 // In milliseconds
            getSeekBar().progress = mCurrentPosition
            Log.e(LOG_TAG, "mCurrentPosition $mCurrentPosition")
            Log.e(LOG_TAG, "currentAudioMode ${getCurrentMode()}")
            if (!isAudioPlayInProgress()) {
                onPauseClick()
            }
        }
    }

    private fun onPauseClick() {
        pauseAudioFile()
    }

    private fun pauseAudioFile() {
        showPlayAssets()
        onPlay(false)
    }

    private fun isAudioPlayInProgress(): Boolean {
        return getCurrentMode() == AUDIO_MODES.PLAY
    }

    private fun getCurrentMode(): AUDIO_MODES {
        return views.getCurrentMode()
    }

    private fun getSeekBar(): SeekBar {
        return views.getSeekBar()
    }

    private fun getPlayButton(): AppCompatImageView {
        return views.getPlayButton()
    }

    private fun setSeekBarListener() {
        getSeekBar().setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                player?.let {
                    Log.e(LOG_TAG, "progress $progress")

                    val timerToShow = progress.getFormattedTimer()
                    views.getTimerTv().text = timerToShow
                    if (fromUser) {
                        it.seekTo(progress * 1000)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                onPauseClick()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                onPlayClick()
            }
        })
    }
}