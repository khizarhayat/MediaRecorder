package com.khizar.mediarecorder.media_player

import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.khizar.mediarecorder.AUDIO_MODES

interface GetViews {
    fun getPlayButton(): AppCompatImageView
    fun getSeekBar(): SeekBar
    fun getTimerTv(): TextView
    fun getCurrentMode(): AUDIO_MODES
}