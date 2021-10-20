package com.khizar.mediarecorder.recorder

import android.view.View
import android.widget.TextView
import com.khizar.mediarecorder.RECORDING_MODES

interface RecorderGetViews {
    fun getParentView(): View
    fun getCurrentRecordingMode(): RECORDING_MODES
    fun getTimer(): TextView
}