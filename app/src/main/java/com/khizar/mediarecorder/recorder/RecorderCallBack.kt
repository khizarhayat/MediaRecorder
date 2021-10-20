package com.khizar.mediarecorder.recorder

import com.khizar.mediarecorder.RECORDING_MODES
import java.io.File

interface RecorderCallBack {
    fun showRecordingViews(mode: RECORDING_MODES)
    fun onError(text: String)
    fun onSubmitClick(file: File?)
}